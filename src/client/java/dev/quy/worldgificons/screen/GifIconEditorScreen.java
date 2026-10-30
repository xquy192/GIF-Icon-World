package dev.quy.worldgificons.screen;

import com.mojang.blaze3d.platform.NativeImage;
import dev.quy.worldgificons.gif.GifAnimation;
import dev.quy.worldgificons.gif.GifIconSettings;
import dev.quy.worldgificons.gif.WorldGifIconManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public final class GifIconEditorScreen extends Screen {
    private final Screen parent;
    private final Path worldDirectory;
    private final Path sourceGif;
    private final GifAnimation animation;
    private final GifIconSettings settings;
    private final Identifier previewId;

    private DynamicTexture previewTexture;
    private Button fitModeButton;
    private Button playbackModeButton;
    private Button previewToggleButton;
    private int lastPreviewFrame = -1;
    private String lastPreviewSettings = "";
    private long previewStartMs = System.currentTimeMillis();
    private int manualFrame;
    private boolean previewPaused;
    private String status = "Chỉnh sửa chỉ áp dụng sau khi nhấn Lưu.";
    private boolean statusIsError;

    // All positions use Minecraft's scaled GUI coordinates rather than desktop pixels.
    private int panelLeft;
    private int panelWidth;
    private int controlsX;
    private int rowStartY;
    private int rowGap;
    private int previewX;
    private int previewY;
    private int previewSize;

    public GifIconEditorScreen(
            Screen parent,
            Path worldDirectory,
            Path sourceGif,
            GifAnimation animation,
            GifIconSettings settings
    ) {
        super(Component.literal("GIF Icon World - Chỉnh sửa"));
        this.parent = parent;
        this.worldDirectory = worldDirectory.toAbsolutePath().normalize();
        this.sourceGif = sourceGif.toAbsolutePath().normalize();
        this.animation = animation;
        this.settings = settings;
        this.settings.sanitize(animation.frameCount());
        this.manualFrame = this.settings.startFrame;
        this.previewId = Identifier.fromNamespaceAndPath(
                "gif_icon_world",
                "preview/" + Integer.toUnsignedString(System.identityHashCode(this), 36)
        );
    }

    @Override
    protected void init() {
        this.addRenderableOnly((graphics, mouseX, mouseY, partialTick) -> drawPanel(graphics));
        if (this.previewTexture == null) {
            this.previewTexture = new DynamicTexture(
                    () -> "GIF Icon World preview",
                    GifAnimation.ICON_SIZE,
                    GifAnimation.ICON_SIZE,
                    true
            );
            this.minecraft.getTextureManager().register(this.previewId, this.previewTexture);
        }

        this.panelWidth = Math.min(620, this.width - 24);
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.controlsX = this.panelLeft + 180;
        this.rowGap = this.height < 350 ? 18 : 22;
        this.rowStartY = Math.max(
                38,
                Math.min(this.height < 350 ? 54 : 72, this.height - 116 - this.rowGap * 8)
        );

        int leftColumnWidth = 154;
        this.previewSize = Math.max(72, Math.min(96, this.height / 4));
        this.previewX = this.panelLeft + Math.max(18, (leftColumnWidth - this.previewSize) / 2 + 6);
        this.previewY = Math.max(78, Math.min(104, this.height / 3 - 20));

        addStepRow(rowStartY, () -> changeSpeed(-0.1f), () -> changeSpeed(0.1f));
        addStepRow(rowStartY + rowGap, () -> changeStart(-1), () -> changeStart(1));
        addStepRow(rowStartY + rowGap * 2, () -> changeEnd(-1), () -> changeEnd(1));
        addStepRow(rowStartY + rowGap * 3, () -> changeOffsetX(-2), () -> changeOffsetX(2));
        addStepRow(rowStartY + rowGap * 4, () -> changeOffsetY(-2), () -> changeOffsetY(2));
        addStepRow(rowStartY + rowGap * 5, () -> changeZoom(-0.1f), () -> changeZoom(0.1f));

        int modeY = rowStartY + rowGap * 6;
        this.fitModeButton = this.addRenderableWidget(
                Button.builder(Component.literal(fitModeLabel(settings.getFitMode())), button -> cycleFitMode())
                        .bounds(controlsX + 128, modeY, 130, 20)
                        .build()
        );

        int playbackY = rowStartY + rowGap * 7;
        this.playbackModeButton = this.addRenderableWidget(
                Button.builder(Component.literal(playbackLabel(settings.getPlaybackMode())), button -> cyclePlaybackMode())
                        .bounds(controlsX + 128, playbackY, 130, 20)
                        .build()
        );

        int previewYRow = rowStartY + rowGap * 8;
        this.addRenderableWidget(Button.builder(Component.literal("◀"), button -> stepPreview(-1))
                .bounds(controlsX + 128, previewYRow, 26, 20).build());
        this.previewToggleButton = this.addRenderableWidget(
                Button.builder(Component.literal(previewPaused ? "Tiếp tục" : "Tạm dừng"), button -> togglePreview())
                        .bounds(controlsX + 160, previewYRow, 90, 20)
                        .build()
        );
        this.addRenderableWidget(Button.builder(Component.literal("▶"), button -> stepPreview(1))
                .bounds(controlsX + 256, previewYRow, 26, 20).build());

        int footerY = this.height - 29;
        int footerButtonWidth = Math.min(180, (this.width - 36) / 2);
        int footerLeft = (this.width - (footerButtonWidth * 2 + 8)) / 2;
        int autoFitY = Math.min(this.height - 72, this.previewY + this.previewSize + 76);
        this.addRenderableWidget(Button.builder(Component.literal("Tự căn chỉnh"), button -> autoFit())
                .bounds(this.panelLeft + 12, autoFitY, 130, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Lưu"), button -> save())
                .bounds(footerLeft, footerY, footerButtonWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Hủy"), button -> this.minecraft.gui.setScreen(parent))
                .bounds(footerLeft + footerButtonWidth + 8, footerY, footerButtonWidth, 20).build());

        updatePreview(true);
        refreshModeButtonMessages();
    }

    private void addStepRow(int y, Runnable minus, Runnable plus) {
        int minusX = this.controlsX + 126;
        int plusX = this.controlsX + 204;
        this.addRenderableWidget(Button.builder(Component.literal("−"), button -> minus.run())
                .bounds(minusX, y, 26, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), button -> plus.run())
                .bounds(plusX, y, 26, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.previewPaused) {
            updatePreview(false);
        }
    }

    private void drawPanel(GuiGraphicsExtractor graphics) {
        int top = 44;
        int bottom = this.height - 35;
        graphics.fill(panelLeft, top, panelLeft + panelWidth, bottom, 0x92000000);
        graphics.fill(panelLeft, top, panelLeft + 1, bottom, 0xFF454545);
        graphics.fill(panelLeft + panelWidth - 1, top, panelLeft + panelWidth, bottom, 0xFF454545);
        graphics.fill(panelLeft, top, panelLeft + panelWidth, top + 1, 0xFF454545);
        graphics.fill(panelLeft, bottom - 1, panelLeft + panelWidth, bottom, 0xFF454545);

        graphics.fill(panelLeft + 14, 62, panelLeft + 160, bottom - 42, 0x65000000);
        graphics.fill(panelLeft + 14, 62, panelLeft + 160, 63, 0xFF393939);
        graphics.fill(panelLeft + 14, bottom - 43, panelLeft + 160, bottom - 42, 0xFF393939);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, Component.literal("GIF Icon World"), this.width / 2, 18, 0xFFFFFF);
        graphics.centeredText(
                this.font,
                Component.literal("Chỉnh sửa ảnh động của thế giới"),
                this.width / 2,
                31,
                0xFFBFBFBF
        );

        int cell = Math.max(9, this.previewSize / 8);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int color = ((x + y) & 1) == 0 ? 0xFFCBD2D9 : 0xFF8995A1;
                graphics.fill(
                        previewX + x * cell,
                        previewY + y * cell,
                        previewX + (x + 1) * cell,
                        previewY + (y + 1) * cell,
                        color
                );
            }
        }
        if (this.previewTexture != null) {
            graphics.blit(
                    this.previewId,
                    previewX,
                    previewY,
                    previewX + this.previewSize,
                    previewY + this.previewSize,
                    0.0f,
                    1.0f,
                    0.0f,
                    1.0f
            );
        }

        int right = previewX + this.previewSize;
        int bottom = previewY + this.previewSize;
        graphics.fill(previewX - 1, previewY - 1, right + 1, previewY, 0xFFBDBDBD);
        graphics.fill(previewX - 1, bottom, right + 1, bottom + 1, 0xFFBDBDBD);
        graphics.fill(previewX - 1, previewY, previewX, bottom, 0xFFBDBDBD);
        graphics.fill(right, previewY, right + 1, bottom, 0xFFBDBDBD);

        graphics.centeredText(this.font, Component.literal("Xem trước"), panelLeft + 87, previewY - 14, 0xFFFFFFFF);
        int infoX = panelLeft + 20;
        int infoY = previewY + this.previewSize + 12;
        graphics.text(
                this.font,
                Component.literal(animation.originalWidth() + " × " + animation.originalHeight()),
                infoX,
                infoY,
                0xFFE0E0E0
        );
        graphics.text(
                this.font,
                Component.literal(animation.frameCount() + " khung hình"),
                infoX,
                infoY + 13,
                0xFFBDBDBD
        );

        long bytes = 0;
        try {
            bytes = Files.size(sourceGif);
        } catch (IOException ignored) {
        }
        graphics.text(this.font, Component.literal(formatBytes(bytes)), infoX, infoY + 26, 0xFFBDBDBD);
        graphics.text(
                this.font,
                Component.literal(shortName(sourceGif.getFileName().toString())),
                infoX,
                infoY + 39,
                0xFFBDBDBD
        );
        graphics.text(
                this.font,
                Component.literal("Frame: " + (currentPreviewFrame() + 1) + "/" + animation.frameCount()),
                infoX,
                infoY + 52,
                0xFFE0E0E0
        );

        int valueCenter = controlsX + 176;
        drawValue(graphics, rowStartY, "Tốc độ", String.format(Locale.ROOT, "%.1fx", settings.speed), valueCenter);
        drawValue(graphics, rowStartY + rowGap, "Frame bắt đầu", Integer.toString(settings.startFrame), valueCenter);
        drawValue(graphics, rowStartY + rowGap * 2, "Frame kết thúc", Integer.toString(settings.endFrame), valueCenter);
        drawValue(graphics, rowStartY + rowGap * 3, "Vị trí X", settings.offsetX + " px", valueCenter);
        drawValue(graphics, rowStartY + rowGap * 4, "Vị trí Y", settings.offsetY + " px", valueCenter);
        drawValue(graphics, rowStartY + rowGap * 5, "Zoom", String.format(Locale.ROOT, "%.1fx", settings.zoom), valueCenter);
        graphics.text(this.font, Component.literal("Căn ảnh"), controlsX, rowStartY + rowGap * 6 + 6, 0xFFFFFFFF);
        graphics.text(this.font, Component.literal("Chế độ phát"), controlsX, rowStartY + rowGap * 7 + 6, 0xFFFFFFFF);
        graphics.text(this.font, Component.literal("Xem trước"), controlsX, rowStartY + rowGap * 8 + 6, 0xFFFFFFFF);

        graphics.text(
                this.font,
                Component.literal("Dùng − / + để chỉnh. Lưu để áp dụng vào thế giới."),
                panelLeft + 14,
                this.height - 62,
                0xFFBDBDBD
        );
        graphics.text(
                this.font,
                Component.literal(status),
                panelLeft + 14,
                this.height - 48,
                statusIsError ? 0xFFFF6B6B : 0xFFFFD866
        );
    }

    private void drawValue(GuiGraphicsExtractor graphics, int y, String label, String value, int valueCenter) {
        graphics.text(this.font, Component.literal(label), controlsX, y + 6, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.literal(value), valueCenter, y + 6, 0xFFE0E0E0);
    }

    private int currentPreviewFrame() {
        if (this.previewPaused) {
            return clamp(manualFrame, settings.startFrame, settings.endFrame);
        }
        return animation.frameAt(Math.max(0L, System.currentTimeMillis() - previewStartMs), settings);
    }

    private void updatePreview(boolean force) {
        settings.sanitize(animation.frameCount());
        int frame = currentPreviewFrame();
        String fingerprint = settings.fingerprint() + "|pause=" + previewPaused + "|manual=" + manualFrame;
        if (!force && frame == lastPreviewFrame && fingerprint.equals(lastPreviewSettings)) {
            return;
        }
        if (previewTexture == null) {
            return;
        }

        BufferedImage rendered = animation.renderFrame(frame, settings);
        NativeImage pixels = previewTexture.getPixels();
        for (int y = 0; y < GifAnimation.ICON_SIZE; y++) {
            for (int x = 0; x < GifAnimation.ICON_SIZE; x++) {
                pixels.setPixel(x, y, rendered.getRGB(x, y));
            }
        }
        previewTexture.upload();
        lastPreviewFrame = frame;
        lastPreviewSettings = fingerprint;
    }

    private void changeSpeed(float amount) {
        settings.speed = Math.round((settings.speed + amount) * 10.0f) / 10.0f;
        settings.sanitize(animation.frameCount());
        updatePreview(true);
    }

    private void changeStart(int amount) {
        settings.startFrame = clamp(settings.startFrame + amount, 0, Math.max(0, settings.endFrame));
        manualFrame = settings.startFrame;
        settings.sanitize(animation.frameCount());
        updatePreview(true);
    }

    private void changeEnd(int amount) {
        settings.endFrame = clamp(settings.endFrame + amount, settings.startFrame, animation.frameCount() - 1);
        manualFrame = settings.endFrame;
        settings.sanitize(animation.frameCount());
        updatePreview(true);
    }

    private void changeOffsetX(int amount) {
        settings.offsetX = clamp(settings.offsetX + amount, -32, 32);
        updatePreview(true);
    }

    private void changeOffsetY(int amount) {
        settings.offsetY = clamp(settings.offsetY + amount, -32, 32);
        updatePreview(true);
    }

    private void changeZoom(float amount) {
        settings.zoom = Math.round((settings.zoom + amount) * 10.0f) / 10.0f;
        settings.sanitize(animation.frameCount());
        updatePreview(true);
    }

    private void cycleFitMode() {
        GifIconSettings.FitMode[] modes = GifIconSettings.FitMode.values();
        settings.fitMode = modes[(settings.getFitMode().ordinal() + 1) % modes.length].name();
        refreshModeButtonMessages();
        updatePreview(true);
    }

    private void cyclePlaybackMode() {
        GifIconSettings.PlaybackMode[] modes = GifIconSettings.PlaybackMode.values();
        settings.playbackMode = modes[(settings.getPlaybackMode().ordinal() + 1) % modes.length].name();
        previewStartMs = System.currentTimeMillis();
        refreshModeButtonMessages();
        updatePreview(true);
    }

    private void refreshModeButtonMessages() {
        if (this.fitModeButton != null) {
            this.fitModeButton.setMessage(Component.literal(fitModeLabel(settings.getFitMode())));
        }
        if (this.playbackModeButton != null) {
            this.playbackModeButton.setMessage(Component.literal(playbackLabel(settings.getPlaybackMode())));
        }
        if (this.previewToggleButton != null) {
            this.previewToggleButton.setMessage(Component.literal(previewPaused ? "Tiếp tục" : "Tạm dừng"));
        }
    }

    private void stepPreview(int amount) {
        int shownFrame = currentPreviewFrame();
        previewPaused = true;
        manualFrame = clamp(shownFrame + amount, settings.startFrame, settings.endFrame);
        refreshModeButtonMessages();
        updatePreview(true);
    }

    private void togglePreview() {
        int shownFrame = currentPreviewFrame();
        previewPaused = !previewPaused;
        if (!previewPaused) {
            previewStartMs = System.currentTimeMillis();
        } else {
            manualFrame = shownFrame;
        }
        refreshModeButtonMessages();
        updatePreview(true);
    }

    private void autoFit() {
        animation.fitToIcon(settings);
        status = "Đã căn ảnh theo vùng nội dung GIF.";
        statusIsError = false;
        refreshModeButtonMessages();
        updatePreview(true);
    }

    private void save() {
        Path target = worldDirectory.resolve("icon.gif").toAbsolutePath().normalize();
        Path temporary = worldDirectory.resolve("icon.gif.importing");
        try {
            Files.createDirectories(worldDirectory);
            if (!sourceGif.equals(target)) {
                Files.copy(sourceGif, temporary, StandardCopyOption.REPLACE_EXISTING);
                try {
                    Files.move(
                            temporary,
                            target,
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE
                    );
                } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            settings.sanitize(animation.frameCount());
            settings.save(worldDirectory, animation.frameCount());
            WorldGifIconManager.invalidate(worldDirectory);
            this.minecraft.gui.setScreen(parent);
        } catch (IOException | RuntimeException exception) {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException ignored) {
            }
            this.status = "Không thể lưu: " + safeMessage(exception);
            this.statusIsError = true;
        }
    }

    @Override
    public void removed() {
        super.removed();
        if (this.previewTexture != null) {
            this.minecraft.getTextureManager().release(this.previewId);
            this.previewTexture = null;
        }
    }

    private static String fitModeLabel(GifIconSettings.FitMode mode) {
        return switch (mode) {
            case CONTAIN -> "Vừa khung";
            case COVER -> "Phủ khung";
            case STRETCH -> "Kéo giãn";
        };
    }

    private static String playbackLabel(GifIconSettings.PlaybackMode mode) {
        return switch (mode) {
            case LOOP -> "Lặp vô hạn";
            case PING_PONG -> "Tiến / lùi";
            case ONCE -> "Chạy một lần";
        };
    }

    private static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f KiB", bytes / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1f MiB", bytes / (1024.0 * 1024.0));
    }

    private static String shortName(String name) {
        return name.length() > 17 ? "…" + name.substring(name.length() - 16) : name;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.length() > 100 ? message.substring(0, 97) + "..." : message;
    }
}
