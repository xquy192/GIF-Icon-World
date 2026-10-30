package dev.quy.worldgificons.screen;

import dev.quy.worldgificons.gif.GifAnimation;
import dev.quy.worldgificons.gif.GifIconSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/** Handles GIF selection and opens the per-world animation editor. */
public final class GifIconImportScreen extends Screen {
    private static final long MAX_GIF_BYTES = 64L * 1024L * 1024L;

    private final Screen parent;
    private final Path worldDirectory;
    private String status = "Thả file GIF vào khung hoặc chọn từ máy tính.";
    private boolean statusIsError;
    private int cardLeft;
    private int cardWidth;
    private int dropTop;
    private int dropBottom;

    public GifIconImportScreen(Screen parent, Path worldDirectory) {
        super(Component.literal("GIF Icon World"));
        this.parent = parent;
        this.worldDirectory = worldDirectory.toAbsolutePath().normalize();
    }

    @Override
    protected void init() {
        computeLayout();
        this.addRenderableOnly((graphics, mouseX, mouseY, partialTick) -> drawPanel(graphics));

        int center = this.width / 2;
        int buttonsY = Math.min(this.height - 110, this.dropBottom + 9);
        this.addRenderableWidget(
                Button.builder(Component.literal("Chọn file GIF..."), button -> chooseFile())
                        .bounds(center - 205, buttonsY, 200, 20)
                        .build()
        );
        this.addRenderableWidget(
                Button.builder(Component.literal("Mở thư mục thế giới"), button -> openWorldFolder())
                        .bounds(center + 5, buttonsY, 200, 20)
                        .build()
        );

        Path currentGif = worldDirectory.resolve("icon.gif");
        if (Files.isRegularFile(currentGif)) {
            this.addRenderableWidget(
                    Button.builder(Component.literal("Chỉnh sửa GIF đang dùng"), button -> openEditor(currentGif))
                            .bounds(center - 205, buttonsY + 26, 410, 20)
                            .build()
            );
        }
        this.addRenderableWidget(
                Button.builder(Component.literal("Quay lại"), button -> this.minecraft.gui.setScreen(parent))
                        .bounds(center - 100, this.height - 28, 200, 20)
                        .build()
        );
    }

    private void computeLayout() {
        this.cardWidth = Math.min(440, this.width - 32);
        this.cardLeft = (this.width - this.cardWidth) / 2;
        int panelTop = 50;
        int panelBottom = this.height - 43;
        this.dropTop = panelTop + 32;
        this.dropBottom = Math.min(panelBottom - 126, this.dropTop + 110);
    }

    private void drawPanel(GuiGraphicsExtractor graphics) {
        computeLayout();
        int panelTop = 50;
        int panelBottom = this.height - 43;

        graphics.fill(cardLeft, panelTop, cardLeft + cardWidth, panelBottom, 0x92000000);
        graphics.fill(cardLeft, panelTop, cardLeft + 1, panelBottom, 0xFF454545);
        graphics.fill(cardLeft + cardWidth - 1, panelTop, cardLeft + cardWidth, panelBottom, 0xFF454545);
        graphics.fill(cardLeft, panelTop, cardLeft + cardWidth, panelTop + 1, 0xFF454545);
        graphics.fill(cardLeft, panelBottom - 1, cardLeft + cardWidth, panelBottom, 0xFF454545);

        int dropLeft = cardLeft + 18;
        int dropRight = cardLeft + cardWidth - 18;
        graphics.fill(dropLeft, dropTop, dropRight, dropBottom, 0x66000000);
        graphics.fill(dropLeft, dropTop, dropRight, dropTop + 1, 0xFF777777);
        graphics.fill(dropLeft, dropBottom - 1, dropRight, dropBottom, 0xFF777777);
        graphics.fill(dropLeft, dropTop, dropLeft + 1, dropBottom, 0xFF777777);
        graphics.fill(dropRight - 1, dropTop, dropRight, dropBottom, 0xFF777777);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, Component.literal("GIF Icon World"), this.width / 2, 18, 0xFFFFFFFF);
        graphics.centeredText(
                this.font,
                Component.literal("Ảnh động cho thế giới"),
                this.width / 2,
                31,
                0xFFBFBFBF
        );

        int dropCenter = (dropTop + dropBottom) / 2;
        graphics.centeredText(
                this.font,
                Component.literal("Kéo thả file GIF vào đây"),
                this.width / 2,
                dropCenter - 17,
                0xFFFFFFFF
        );
        graphics.centeredText(
                this.font,
                Component.literal("hoặc nhấn Chọn file GIF..."),
                this.width / 2,
                dropCenter,
                0xFFD0D0D0
        );
        graphics.centeredText(
                this.font,
                Component.literal("Tự nhận diện độ phân giải, frame và thời gian"),
                this.width / 2,
                dropCenter + 17,
                0xFFB0B0B0
        );

        String current = Files.isRegularFile(worldDirectory.resolve("icon.gif"))
                ? "Đã có icon.gif — bạn có thể thay hoặc chỉnh sửa tiếp."
                : "Chưa có icon.gif — ảnh world hiện tại vẫn được giữ nguyên.";
        graphics.text(this.font, Component.literal(current), cardLeft + 14, this.height - 91, 0xFFE0E0E0);

        String pathText = worldDirectory.toString();
        if (pathText.length() > 44) {
            pathText = "…" + pathText.substring(pathText.length() - 43);
        }
        graphics.text(this.font, Component.literal("Thư mục: " + pathText), cardLeft + 14, this.height - 77, 0xFFB0B0B0);

        int statusColor = statusIsError ? 0xFFFF6B6B : 0xFFFFD866;
        graphics.text(this.font, Component.literal(status), cardLeft + 14, this.height - 60, statusColor);
    }

    @Override
    public void onFilesDrop(List<Path> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        for (Path file : files) {
            if (file != null && isGifName(file)) {
                openEditor(file);
                return;
            }
        }
        setStatus("Không tìm thấy file .gif trong những file vừa thả.", true);
    }

    private void chooseFile() {
        try {
            EventQueue.invokeLater(() -> {
                Path selected = null;
                try {
                    FileDialog dialog = new FileDialog((Frame) null, "Chọn GIF động cho world", FileDialog.LOAD);
                    dialog.setFilenameFilter(
                            (directory, name) -> name.toLowerCase(Locale.ROOT).endsWith(".gif")
                    );
                    dialog.setVisible(true);
                    if (dialog.getFile() != null && dialog.getDirectory() != null) {
                        selected = Path.of(dialog.getDirectory(), dialog.getFile());
                    }
                    dialog.dispose();
                } catch (HeadlessException ignored) {
                    // Drag-and-drop remains available if the native file dialog cannot open.
                }

                Path result = selected;
                Minecraft.getInstance().execute(() -> {
                    if (result != null) {
                        openEditor(result);
                    } else {
                        setStatus("Chưa chọn file. Bạn vẫn có thể kéo thả GIF vào khung.", false);
                    }
                });
            });
        } catch (RuntimeException exception) {
            setStatus("Không mở được hộp thoại file. Hãy kéo thả file GIF vào khung.", true);
        }
    }

    private void openEditor(Path source) {
        Path normalized = source.toAbsolutePath().normalize();
        try {
            if (!Files.isRegularFile(normalized)) {
                throw new IOException("Không tìm thấy file đã chọn.");
            }
            if (!isGifName(normalized)) {
                throw new IOException("Chỉ nhận file có đuôi .gif.");
            }

            long size = Files.size(normalized);
            if (size < 1 || size > MAX_GIF_BYTES) {
                throw new IOException("Dung lượng GIF phải từ 1 byte đến 64 MiB.");
            }

            GifAnimation animation = GifAnimation.read(normalized);
            boolean editingCurrent = normalized.equals(
                    worldDirectory.resolve("icon.gif").toAbsolutePath().normalize()
            );
            GifIconSettings settings = editingCurrent
                    ? GifIconSettings.load(worldDirectory)
                    : new GifIconSettings();
            settings.sanitize(animation.frameCount());

            this.minecraft.gui.setScreen(
                    new GifIconEditorScreen(parent, worldDirectory, normalized, animation, settings)
            );
        } catch (IOException | RuntimeException exception) {
            setStatus("Không đọc được GIF: " + safeMessage(exception), true);
        }
    }

    private void openWorldFolder() {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(worldDirectory.toFile());
            } else {
                setStatus("Không thể mở File Explorer ở môi trường này.", true);
            }
        } catch (Exception exception) {
            setStatus("Không mở được thư mục thế giới: " + safeMessage(exception), true);
        }
    }

    private void setStatus(String value, boolean error) {
        this.status = value;
        this.statusIsError = error;
    }

    private static boolean isGifName(Path path) {
        return path.getFileName() != null
                && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".gif");
    }

    private static String safeMessage(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return message.length() > 140 ? message.substring(0, 137) + "..." : message;
    }
}
