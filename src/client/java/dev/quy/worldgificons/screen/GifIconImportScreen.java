package dev.quy.worldgificons.screen;

import dev.quy.worldgificons.gif.GifAnimation;
import dev.quy.worldgificons.gif.GifIconSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class GifIconImportScreen extends Screen {
    private static final long MAX_GIF_BYTES = 64L * 1024L * 1024L;

    private final Screen parent;
    private final Path worldDirectory;
    private Component status = Component.translatable("gif_icon_world.import.status.initial");
    private boolean statusIsError;

    private int scale = 1;
    private int cardLeft;
    private int cardWidth;
    private int panelTop;
    private int panelBottom;
    private int dropLeft;
    private int dropRight;
    private int dropTop;
    private int dropBottom;
    private int buttonsY;
    private int buttonWidth;
    private int buttonHeight;
    private int buttonGap;
    private int firstButtonX;
    private int secondButtonX;
    private int editButtonY;
    private int footerY;
    private int footerWidth;
    private int currentInfoY;
    private int pathInfoY;
    private int statusY;
    private int lineGap;
    private boolean buttonsSideBySide;
    private boolean hasCurrentGif;

    public GifIconImportScreen(Screen parent, Path worldDirectory) {
        super(Component.translatable("gif_icon_world.title"));
        this.parent = parent;
        this.worldDirectory = worldDirectory.toAbsolutePath().normalize();
    }

    @Override
    protected void init() {
        computeLayout();
        this.addRenderableOnly((graphics, mouseX, mouseY, partialTick) -> drawPanel(graphics));

        this.addRenderableWidget(
                Button.builder(Component.translatable("gif_icon_world.import.button.choose"), button -> chooseFile())
                        .bounds(firstButtonX, buttonsY, buttonWidth, buttonHeight)
                        .build()
        );

        int openFolderY = buttonsSideBySide ? buttonsY : buttonsY + buttonHeight + buttonGap;
        this.addRenderableWidget(
                Button.builder(Component.translatable("gif_icon_world.import.button.open_folder"), button -> openWorldFolder())
                        .bounds(secondButtonX, openFolderY, buttonWidth, buttonHeight)
                        .build()
        );

        if (hasCurrentGif) {
            this.addRenderableWidget(
                    Button.builder(Component.translatable("gif_icon_world.import.button.edit_current"), button -> openEditor(worldDirectory.resolve("icon.gif")))
                            .bounds(cardLeft + scaled(14), editButtonY, cardWidth - scaled(28), buttonHeight)
                            .build()
            );
        }

        this.addRenderableWidget(
                Button.builder(Component.translatable("gif_icon_world.import.button.back"), button -> this.minecraft.gui.setScreen(parent))
                        .bounds((this.width - footerWidth) / 2, footerY, footerWidth, buttonHeight)
                        .build()
        );
    }

    private void computeLayout() {
        float factor = Math.min(this.width / 640.0f, this.height / 360.0f);
        this.scale = Math.max(1, Math.round(Math.max(0.62f, Math.min(1.15f, factor)) * 100.0f));

        int horizontalMargin = scaled(18);
        this.cardWidth = Math.max(1, Math.min(this.width - horizontalMargin * 2, scaled(520)));
        this.cardLeft = (this.width - this.cardWidth) / 2;
        this.panelTop = scaled(44);
        this.panelBottom = Math.max(this.panelTop + scaled(100), this.height - scaled(39));

        int innerPadding = scaled(14);
        this.dropLeft = this.cardLeft + innerPadding;
        this.dropRight = this.cardLeft + this.cardWidth - innerPadding;
        this.dropTop = this.panelTop + scaled(36);

        this.buttonHeight = Math.max(16, scaled(20));
        this.buttonGap = scaled(6);
        this.buttonsSideBySide = this.cardWidth >= scaled(390);
        this.hasCurrentGif = Files.isRegularFile(worldDirectory.resolve("icon.gif"));
        this.lineGap = Math.max(10, scaled(13));

        this.statusY = this.panelBottom - scaled(12);
        this.pathInfoY = this.statusY - this.lineGap;
        this.currentInfoY = this.pathInfoY - this.lineGap;

        int rows = buttonsSideBySide ? (hasCurrentGif ? 2 : 1) : (hasCurrentGif ? 3 : 2);
        int buttonBlockHeight = rows * this.buttonHeight + Math.max(0, rows - 1) * this.buttonGap;
        this.buttonsY = this.currentInfoY - scaled(10) - buttonBlockHeight;

        int contentWidth = Math.max(1, this.cardWidth - innerPadding * 2);
        if (buttonsSideBySide) {
            this.buttonWidth = Math.max(1, (contentWidth - this.buttonGap) / 2);
            this.firstButtonX = this.cardLeft + innerPadding;
            this.secondButtonX = this.firstButtonX + this.buttonWidth + this.buttonGap;
            this.editButtonY = this.buttonsY + this.buttonHeight + this.buttonGap;
        } else {
            this.buttonWidth = contentWidth;
            this.firstButtonX = this.cardLeft + innerPadding;
            this.secondButtonX = this.firstButtonX;
            this.editButtonY = this.buttonsY + this.buttonHeight * 2 + this.buttonGap * 2;
        }

        this.dropBottom = Math.max(this.dropTop + scaled(28), this.buttonsY - scaled(9));
        int maxDropBottom = this.panelBottom - scaled(76);
        if (this.dropBottom > maxDropBottom) {
            this.dropBottom = Math.max(this.dropTop + scaled(24), maxDropBottom);
        }

        this.footerY = this.height - scaled(28);
        this.footerWidth = Math.min(scaled(200), Math.max(1, this.width - scaled(24)));
    }

    private int scaled(int value) {
        return Math.max(1, Math.round(value * this.scale / 100.0f));
    }

    private void drawPanel(GuiGraphicsExtractor graphics) {
        computeLayout();

        graphics.fill(cardLeft, panelTop, cardLeft + cardWidth, panelBottom, 0x92000000);
        graphics.fill(cardLeft, panelTop, cardLeft + 1, panelBottom, 0xFF454545);
        graphics.fill(cardLeft + cardWidth - 1, panelTop, cardLeft + cardWidth, panelBottom, 0xFF454545);
        graphics.fill(cardLeft, panelTop, cardLeft + cardWidth, panelTop + 1, 0xFF454545);
        graphics.fill(cardLeft, panelBottom - 1, cardLeft + cardWidth, panelBottom, 0xFF454545);

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
        graphics.centeredText(this.font, Component.translatable("gif_icon_world.title"), this.width / 2, scaled(18), 0xFFFFFFFF);
        graphics.centeredText(
                this.font,
                Component.translatable("gif_icon_world.import.subtitle"),
                this.width / 2,
                scaled(31),
                0xFFBFBFBF
        );

        int dropCenter = (dropTop + dropBottom) / 2;
        int textGap = scaled(14);
        graphics.centeredText(
                this.font,
                Component.translatable("gif_icon_world.import.drop"),
                this.width / 2,
                dropCenter - textGap,
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
                dropCenter + textGap,
                0xFFB0B0B0
        );

        Component current = hasCurrentGif
                ? Component.translatable("gif_icon_world.import.current_exists")
                : Component.translatable("gif_icon_world.import.current_missing");
        int textLeft = cardLeft + scaled(14);
        graphics.text(this.font, current, textLeft, currentInfoY, 0xFFE0E0E0);

        String pathText = worldDirectory.toString();
        int availablePathWidth = Math.max(20, cardWidth - scaled(28));
        Component pathComponent = Component.translatable("gif_icon_world.import.world_path", pathText);
        if (this.font.width(pathComponent) > availablePathWidth) {
            do {
                if (!pathText.isEmpty()) {
                    pathText = pathText.substring(1);
                }
                pathComponent = Component.translatable("gif_icon_world.import.world_path", "…" + pathText);
            } while (!pathText.isEmpty() && this.font.width(pathComponent) > availablePathWidth);
        }
        graphics.text(
                this.font,
                pathComponent,
                textLeft,
                pathInfoY,
                0xFFB0B0B0
        );

        int statusColor = statusIsError ? 0xFFFF6B6B : 0xFFFFD866;
        graphics.text(this.font, status, textLeft, statusY, statusColor);
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
        Thread pickerThread = new Thread(() -> {
            try {
                Path selected = chooseNativeGifFile();
                Minecraft.getInstance().execute(() -> {
                    if (selected != null) {
                        openEditor(selected);
                    } else {
                        setStatus(Component.translatable("gif_icon_world.import.status.not_selected"), false);
                    }
                });
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                Minecraft.getInstance().execute(() ->
                        setStatus(Component.translatable("gif_icon_world.import.status.not_selected"), false)
                );
            } catch (IOException | RuntimeException exception) {
                String message = safeMessage(exception);
                Minecraft.getInstance().execute(() ->
                        setStatus(Component.translatable("gif_icon_world.import.status.dialog_error", message), true)
                );
            }
        }, "GIF Icon World file picker");

        pickerThread.setDaemon(true);
        pickerThread.start();
    }

    private Path chooseNativeGifFile() throws IOException, InterruptedException {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            return chooseWindowsGifFile();
        }

        if (osName.contains("mac")) {
            String script = "POSIX path of (choose file with prompt \"Choose a GIF for this world\" of type {\"GIF\"})";
            return runFilePicker(List.of("osascript", "-e", script), true);
        }

        String initialFileName = worldDirectory.toString() + java.io.File.separator;
        try {
            return runFilePicker(List.of(
                    "zenity",
                    "--file-selection",
                    "--title=Choose a GIF for this world",
                    "--file-filter=GIF images | *.gif",
                    "--filename=" + initialFileName
            ), true);
        } catch (IOException zenityUnavailable) {
            try {
                return runFilePicker(List.of(
                        "kdialog",
                        "--getopenfilename",
                        worldDirectory.toString(),
                        "GIF images (*.gif)",
                        "--title",
                        "Choose a GIF for this world"
                ), true);
            } catch (IOException kdialogUnavailable) {
                throw new IOException("No supported native file picker was found (install zenity or kdialog).", kdialogUnavailable);
            }
        }
    }

    private Path chooseWindowsGifFile() throws IOException, InterruptedException {
        String script = String.join(" ",
                "$ErrorActionPreference = 'Stop';",
                "[Console]::OutputEncoding = New-Object System.Text.UTF8Encoding($false);",
                "Add-Type -AssemblyName System.Windows.Forms;",
                "$dialog = New-Object System.Windows.Forms.OpenFileDialog;",
                "$dialog.Title = [Environment]::GetEnvironmentVariable('GIF_ICON_WORLD_DIALOG_TITLE');",
                "$dialog.Filter = [Environment]::GetEnvironmentVariable('GIF_ICON_WORLD_DIALOG_FILTER');",
                "$dialog.FilterIndex = 1;",
                "$dialog.Multiselect = $false;",
                "$dialog.CheckFileExists = $true;",
                "$initialDirectory = [Environment]::GetEnvironmentVariable('GIF_ICON_WORLD_INITIAL_DIR');",
                "if ($initialDirectory -and [System.IO.Directory]::Exists($initialDirectory)) { $dialog.InitialDirectory = $initialDirectory; }",
                "if ($dialog.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK) { [Console]::WriteLine($dialog.FileName); }",
                "$dialog.Dispose();"
        );

        ProcessBuilder builder = new ProcessBuilder(
                "powershell.exe",
                "-NoLogo",
                "-NoProfile",
                "-STA",
                "-WindowStyle",
                "Hidden",
                "-ExecutionPolicy",
                "Bypass",
                "-Command",
                script
        );
        builder.redirectErrorStream(true);
        builder.environment().put(
                "GIF_ICON_WORLD_DIALOG_TITLE",
                Component.translatable("gif_icon_world.import.dialog.title").getString()
        );
        builder.environment().put(
                "GIF_ICON_WORLD_DIALOG_FILTER",
                Component.translatable("gif_icon_world.import.dialog.filter").getString() + "|*.gif"
        );
        builder.environment().put("GIF_ICON_WORLD_INITIAL_DIR", worldDirectory.toString());

        return runFilePicker(builder, true);
    }

    private Path runFilePicker(List<String> command, boolean cancelIsNormal)
            throws IOException, InterruptedException {
        return runFilePicker(new ProcessBuilder(command), cancelIsNormal);
    }

    private Path runFilePicker(ProcessBuilder builder, boolean cancelIsNormal)
            throws IOException, InterruptedException {
        builder.redirectErrorStream(true);
        Process process = builder.start();
        byte[] output;
        try (var input = process.getInputStream()) {
            output = input.readAllBytes();
        }

        int exitCode = process.waitFor();
        String result = new String(output, StandardCharsets.UTF_8).trim();
        if (exitCode != 0) {
            if (cancelIsNormal && (exitCode == 1 || exitCode == 255)) {
                return null;
            }
            String detail = result.isBlank() ? "Exit code " + exitCode : result;
            throw new IOException(detail.length() > 300 ? detail.substring(0, 297) + "..." : detail);
        }
        if (result.isBlank()) {
            return null;
        }

        Path selected = Path.of(result).toAbsolutePath().normalize();
        if (!Files.isRegularFile(selected)) {
            throw new IOException("The selected file could not be found.");
        }
        return selected;
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
