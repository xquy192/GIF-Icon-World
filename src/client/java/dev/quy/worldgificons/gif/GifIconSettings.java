package dev.quy.worldgificons.gif;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Settings that belong to one world save. */
public final class GifIconSettings {
    public static final String CONFIG_FILE = "gif_icon_world.json";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public float speed = 1.0f;
    public int startFrame;
    public int endFrame = -1;
    public int offsetX;
    public int offsetY;
    public float zoom = 1.0f;
    public String fitMode = FitMode.CONTAIN.name();
    public String playbackMode = PlaybackMode.LOOP.name();

    public enum FitMode { CONTAIN, COVER, STRETCH }
    public enum PlaybackMode { LOOP, PING_PONG, ONCE }

    public static GifIconSettings load(Path worldDirectory) {
        Path file = worldDirectory.resolve(CONFIG_FILE);
        if (!Files.isRegularFile(file)) return new GifIconSettings();

        try (Reader reader = Files.newBufferedReader(file)) {
            GifIconSettings settings = GSON.fromJson(reader, GifIconSettings.class);
            if (settings == null) settings = new GifIconSettings();
            settings.sanitize(300);
            return settings;
        } catch (IOException | RuntimeException exception) {
            return new GifIconSettings();
        }
    }

    public void save(Path worldDirectory, int frameCount) throws IOException {
        sanitize(frameCount);
        Files.createDirectories(worldDirectory);
        Path target = worldDirectory.resolve(CONFIG_FILE);
        Path temporary = worldDirectory.resolve(CONFIG_FILE + ".tmp");

        try (Writer writer = Files.newBufferedWriter(temporary)) {
            GSON.toJson(this, writer);
        }

        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public void sanitize(int frameCount) {
        int lastFrame = Math.max(1, frameCount) - 1;
        speed = clamp(Float.isFinite(speed) ? speed : 1.0f, 0.1f, 4.0f);
        zoom = clamp(Float.isFinite(zoom) ? zoom : 1.0f, 0.5f, 2.0f);
        startFrame = clamp(startFrame, 0, lastFrame);
        if (endFrame < 0) endFrame = lastFrame;
        endFrame = clamp(endFrame, startFrame, lastFrame);
        offsetX = clamp(offsetX, -32, 32);
        offsetY = clamp(offsetY, -32, 32);
        fitMode = validEnum(fitMode, FitMode.CONTAIN);
        playbackMode = validEnum(playbackMode, PlaybackMode.LOOP);
    }

    public FitMode getFitMode() {
        try { return FitMode.valueOf(fitMode); }
        catch (Exception ignored) { return FitMode.CONTAIN; }
    }

    public PlaybackMode getPlaybackMode() {
        try { return PlaybackMode.valueOf(playbackMode); }
        catch (Exception ignored) { return PlaybackMode.LOOP; }
    }

    public String fingerprint() {
        return speed + "|" + startFrame + "|" + endFrame + "|" + offsetX + "|" + offsetY
                + "|" + zoom + "|" + fitMode + "|" + playbackMode;
    }

    private static String validEnum(String value, String fallback, Enum<?>[] values) {
        if (value != null) {
            for (Enum<?> candidate : values) {
                if (candidate.name().equalsIgnoreCase(value)) return candidate.name();
            }
        }
        return fallback;
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
