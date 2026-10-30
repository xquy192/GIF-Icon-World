package dev.quy.worldgificons.gif;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.screens.FaviconTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Client-side playback/cache for world-local icon.gif files. */
public final class WorldGifIconManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("GIF Icon World");
    private static final long FILE_CHECK_INTERVAL_MS = 1000L;
    private static final long FAILURE_RETRY_MS = 5000L;

    private static final Map<Path, AnimationEntry> ANIMATIONS = new HashMap<>();
    private static final Map<Path, SettingsEntry> SETTINGS = new HashMap<>();
    private static final WeakHashMap<FaviconTexture, Playback> PLAYBACKS = new WeakHashMap<>();
    private static boolean loggedUnexpectedFailure;

    private WorldGifIconManager() {
    }

    public static void update(Path gifPath, Path staticIconPath, FaviconTexture vanillaIcon) {
        long now = System.nanoTime() / 1_000_000L;
        Path key = gifPath.toAbsolutePath().normalize();
        AnimationEntry entry = getAnimation(key, now);
        GifAnimation animation = entry == null ? null : entry.animation;
        Playback playback = PLAYBACKS.get(vanillaIcon);

        if (animation == null) {
            if (playback != null && playback.frameWasUploaded) {
                PLAYBACKS.remove(vanillaIcon);
                restoreStaticIcon(staticIconPath, vanillaIcon);
            }
            return;
        }

        GifIconSettings settings = getSettings(key.getParent(), animation.frameCount(), now);
        String settingsFingerprint = settings.fingerprint();
        if (playback == null || !key.equals(playback.gifPath) || playback.animation != animation
                || !settingsFingerprint.equals(playback.settingsFingerprint)) {
            playback = new Playback(key, animation, settingsFingerprint, now);
            PLAYBACKS.put(vanillaIcon, playback);
        }

        int frameIndex = animation.frameAt(Math.max(0L, now - playback.startedAtMs), settings);
        if (frameIndex == playback.lastFrame) return;

        NativeImage nativeFrame = convert(animation.renderFrame(frameIndex, settings));
        try {
            vanillaIcon.upload(nativeFrame);
            playback.lastFrame = frameIndex;
            playback.frameWasUploaded = true;
        } catch (RuntimeException exception) {
            nativeFrame.close();
            throw exception;
        }
    }

    public static void release(FaviconTexture icon) {
        PLAYBACKS.remove(icon);
    }

    public static void invalidate(Path worldDirectory) {
        Path world = worldDirectory.toAbsolutePath().normalize();
        ANIMATIONS.remove(world.resolve("icon.gif"));
        SETTINGS.remove(world.resolve(GifIconSettings.CONFIG_FILE));
    }

    public static void logUnexpectedFailure(String worldId, RuntimeException exception) {
        if (!loggedUnexpectedFailure) {
            loggedUnexpectedFailure = true;
            LOGGER.warn("Could not update a world GIF icon; leaving vanilla icons enabled. First affected world: {}", worldId, exception);
        }
    }

    private static AnimationEntry getAnimation(Path path, long now) {
        AnimationEntry cached = ANIMATIONS.get(path);
        if (cached != null && now < cached.nextCheckAtMs) return cached;

        try {
            if (!Files.isRegularFile(path)) {
                AnimationEntry missing = new AnimationEntry(null, -1L, -1L, now + FILE_CHECK_INTERVAL_MS);
                ANIMATIONS.put(path, missing);
                return missing;
            }

            long modified = Files.getLastModifiedTime(path).toMillis();
            long size = Files.size(path);
            if (cached != null && cached.animation != null
                    && cached.lastModified == modified && cached.size == size) {
                cached.nextCheckAtMs = now + FILE_CHECK_INTERVAL_MS;
                return cached;
            }

            GifAnimation animation = GifAnimation.read(path);
            AnimationEntry loaded = new AnimationEntry(animation, modified, size, now + FILE_CHECK_INTERVAL_MS);
            ANIMATIONS.put(path, loaded);
            LOGGER.info("Loaded animated world icon {} ({} frames, {}x{})",
                    path.getFileName(), animation.frameCount(), animation.originalWidth(), animation.originalHeight());
            return loaded;
        } catch (IOException | RuntimeException exception) {
            long modified = -1L;
            long size = -1L;
            try {
                if (Files.exists(path)) {
                    modified = Files.getLastModifiedTime(path).toMillis();
                    size = Files.size(path);
                }
            } catch (IOException ignored) {
            }
            ANIMATIONS.put(path, new AnimationEntry(null, modified, size, now + FAILURE_RETRY_MS));
            LOGGER.warn("Could not decode world icon GIF at {}. Vanilla icon.png will be used instead.", path, exception);
            return null;
        }
    }

    private static GifIconSettings getSettings(Path worldDirectory, int frameCount, long now) {
        Path file = worldDirectory.resolve(GifIconSettings.CONFIG_FILE).toAbsolutePath().normalize();
        SettingsEntry cached = SETTINGS.get(file);
        if (cached != null && now < cached.nextCheckAtMs) return cached.settings;

        try {
            long modified = Files.isRegularFile(file) ? Files.getLastModifiedTime(file).toMillis() : -1L;
            long size = Files.isRegularFile(file) ? Files.size(file) : -1L;
            if (cached != null && cached.lastModified == modified && cached.size == size) {
                cached.nextCheckAtMs = now + FILE_CHECK_INTERVAL_MS;
                cached.settings.sanitize(frameCount);
                return cached.settings;
            }

            GifIconSettings settings = GifIconSettings.load(worldDirectory);
            settings.sanitize(frameCount);
            SETTINGS.put(file, new SettingsEntry(settings, modified, size, now + FILE_CHECK_INTERVAL_MS));
            return settings;
        } catch (IOException exception) {
            GifIconSettings settings = new GifIconSettings();
            settings.sanitize(frameCount);
            SETTINGS.put(file, new SettingsEntry(settings, -1L, -1L, now + FAILURE_RETRY_MS));
            return settings;
        }
    }

    private static NativeImage convert(BufferedImage source) {
        NativeImage result = new NativeImage(GifAnimation.ICON_SIZE, GifAnimation.ICON_SIZE, true);
        for (int y = 0; y < GifAnimation.ICON_SIZE; y++) {
            for (int x = 0; x < GifAnimation.ICON_SIZE; x++) {
                result.setPixel(x, y, source.getRGB(x, y));
            }
        }
        return result;
    }

    private static void restoreStaticIcon(Path staticIconPath, FaviconTexture vanillaIcon) {
        if (staticIconPath != null && Files.isRegularFile(staticIconPath)) {
            NativeImage image = null;
            try (InputStream stream = Files.newInputStream(staticIconPath)) {
                image = NativeImage.read(stream);
                vanillaIcon.upload(image);
                image = null;
                return;
            } catch (IOException | RuntimeException exception) {
                LOGGER.debug("Could not restore static world icon at {}", staticIconPath, exception);
            } finally {
                if (image != null) image.close();
            }
        }
        vanillaIcon.clear();
    }

    private static final class AnimationEntry {
        private final GifAnimation animation;
        private final long lastModified;
        private final long size;
        private long nextCheckAtMs;

        private AnimationEntry(GifAnimation animation, long lastModified, long size, long nextCheckAtMs) {
            this.animation = animation;
            this.lastModified = lastModified;
            this.size = size;
            this.nextCheckAtMs = nextCheckAtMs;
        }
    }

    private static final class SettingsEntry {
        private final GifIconSettings settings;
        private final long lastModified;
        private final long size;
        private long nextCheckAtMs;

        private SettingsEntry(GifIconSettings settings, long lastModified, long size, long nextCheckAtMs) {
            this.settings = settings;
            this.lastModified = lastModified;
            this.size = size;
            this.nextCheckAtMs = nextCheckAtMs;
        }
    }

    private static final class Playback {
        private final Path gifPath;
        private final GifAnimation animation;
        private final String settingsFingerprint;
        private final long startedAtMs;
        private int lastFrame = -1;
        private boolean frameWasUploaded;

        private Playback(Path gifPath, GifAnimation animation, String settingsFingerprint, long startedAtMs) {
            this.gifPath = gifPath;
            this.animation = animation;
            this.settingsFingerprint = settingsFingerprint;
            this.startedAtMs = startedAtMs;
        }
    }
}
