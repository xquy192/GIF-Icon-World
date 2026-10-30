package dev.quy.worldgificons.screen;

import dev.quy.worldgificons.gif.GifAnimation;
import dev.quy.worldgificons.gif.GifIconSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.EventQueue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class GifIconImportScreen extends Screen {
    private static final long MAX_GIF_BYTES = 64L * 1024L * 1024L;

    private final Screen parent;
    private final Path worldDirectory;
    private Component status = Component.translatable("gif_icon_world.import.status.initial");
    private boolean statusIsError;
    private int cardLeft;
    private int cardWidth;
    private int dropTop;
    private int dropBottom;

    public GifIconImportScreen(Screen parent, Path worldDirectory) {
        super(Component.translatable("gif_icon_world.title"));
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
                Button.builder(Component.translatable("gif_icon_world.import.button.choose"), button -> chooseFile())
                        .bounds(center - 205, buttonsY, 200, 20)
                        .build()
        );
        this.addRenderableWidget(
                Button.builder(Component.translatable("gif_icon_world.import.button.open_folder"), button -> openWorldFolder())
                        .bounds(center + 5, buttonsY, 200, 20)
                        .build()
        );

        Path currentGif = worldDirectory.resolve("icon.gif");
        if (Files.isRegularFile(currentGif)) {
            this.addRenderableWidget(
                    Button.builder(Component.translatable("gif_icon_world.import.button.edit_current"), button -> openEditor(currentGif))
                            .bounds(center - 205, buttonsY + 26, 410, 20)
                            .build()
            );
        }
        this.addRenderableWidget(
                Button.builder(Component.translatable("gif_icon_world.import.button.back"), button -> this.minecraft.gui.setScreen(parent))
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
        graphics.centeredText(this.font, Component.translatable("gif_icon_world.title"), this.width / 2, 18, 0xFFFFFFFF);
        graphics.centeredText(
                this.font,
                Component.translatable("gif_icon_world.import.subtitle"),
                this.width / 2,
                31,
                0xFFBFBFBF
        );

        int dropCenter = (dropTop + dropBottom) / 2;
        graphics.centeredText(
                this.font,
                Component.translatable("gif_icon_world.import.drop"),
                this.width / 2,
                dropCenter - 17,
                0xFFFFFFFF
        );
        graphics.centeredText(
                this.font,
                Component.translatable("gif_icon_world.import.or_select"),
                this.width / 2,
                dropCenter,
                0xFFD0D0D0
        );
        graphics.centeredText(
                this.font,
                Component.translatable("gif_icon_world.import.detect"),
                this.width / 2,
                dropCenter + 17,
                0xFFB0B0B0
        );

        Component current = Files.isRegularFile(worldDirectory.resolve("icon.gif"))
                ? Component.translatable("gif_icon_world.import.current_exists")
                : Component.translatable("gif_icon_world.import.current_missing");
        graphics.text(this.font, current, cardLeft + 14, this.height - 91, 0xFFE0E0E0);

        String pathText = worldDirectory.toString();
        if (pathText.length() > 44) {
            pathText = "…" + pathText.substring(pathText.length() - 43);
        }
        graphics.text(this.font, Component.translatable("gif_icon_world.import.world_path", pathText), cardLeft + 14, this.height - 77, 0xFFB0B0B0);

        int statusColor = statusIsError ? 0xFFFF6B6B : 0xFFFFD866;
        graphics.text(this.font, status, cardLeft + 14, this.height - 60, statusColor);
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
        setStatus(Component.translatable("gif_icon_world.import.status.no_gif_dropped"), true);
    }

    private void chooseFile() {
        EventQueue.invokeLater(() -> {
            Path selected;
            try {
                JFileChooser chooser = new JFileChooser(
                        Files.isDirectory(worldDirectory) ? worldDirectory.toFile() : null
                );
                chooser.setDialogTitle(Component.translatable("gif_icon_world.import.dialog.title").getString());
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setMultiSelectionEnabled(false);
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileFilter(new FileNameExtensionFilter(Component.translatable("gif_icon_world.import.dialog.filter").getString(), "gif"));

                if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION
                        && chooser.getSelectedFile() != null) {
                    selected = chooser.getSelectedFile().toPath();
                } else {
                    selected = null;
                }
            } catch (RuntimeException exception) {
                String message = safeMessage(exception);
                Minecraft.getInstance().execute(() ->
                        setStatus(Component.translatable("gif_icon_world.import.status.dialog_error", message), true)
                );
                return;
            }

            Path result = selected;
            Minecraft.getInstance().execute(() -> {
                if (result != null) {
                    openEditor(result);
                } else {
                    setStatus(Component.translatable("gif_icon_world.import.status.not_selected"), false);
                }
            });
        });
    }

    private void openEditor(Path source) {
        Path normalized = source.toAbsolutePath().normalize();
        try {
            if (!Files.isRegularFile(normalized)) {
                setStatus(Component.translatable("gif_icon_world.import.error.file_missing"), true);
                return;
            }
            if (!isGifName(normalized)) {
                setStatus(Component.translatable("gif_icon_world.import.error.file_extension"), true);
                return;
            }

            long size = Files.size(normalized);
            if (size < 1 || size > MAX_GIF_BYTES) {
                setStatus(Component.translatable("gif_icon_world.import.error.file_size"), true);
                return;
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
            setStatus(Component.translatable("gif_icon_world.import.status.read_error", safeMessage(exception)), true);
        }
    }

    private void openWorldFolder() {
        try {
            if (!Files.isDirectory(worldDirectory)) {
                setStatus(Component.translatable("gif_icon_world.import.error.world_folder_missing"), true);
                return;
            }

            String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            ProcessBuilder opener;
            if (osName.contains("win")) {
                opener = new ProcessBuilder("explorer.exe", worldDirectory.toString());
            } else if (osName.contains("mac")) {
                opener = new ProcessBuilder("open", worldDirectory.toString());
            } else {
                opener = new ProcessBuilder("xdg-open", worldDirectory.toString());
            }
            opener.start();
            setStatus(Component.translatable("gif_icon_world.import.status.folder_opening"), false);
        } catch (IOException | RuntimeException exception) {
            setStatus(Component.translatable("gif_icon_world.import.status.folder_open_error", safeMessage(exception)), true);
        }
    }

    private void setStatus(Component value, boolean error) {
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
