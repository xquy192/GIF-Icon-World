package dev.quy.worldgificons.gif;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import org.w3c.dom.Node;

/** Reads GIF frames, applies disposal rules and renders the selected frame to a world icon. */
public final class GifAnimation {
    public static final int ICON_SIZE = 64;
    private static final int MAX_DIMENSION = 4096;
    private static final long MAX_CANVAS_PIXELS = 16_777_216L;
    private static final int MAX_FRAMES = 300;
    private static final int STORED_MAX_SIDE = 128;
    private static final long MAX_INPUT_BYTES = 64L * 1024L * 1024L;

    private final List<BufferedImage> frames;
    private final int[] delaysMs;
    private final int originalWidth;
    private final int originalHeight;

    private GifAnimation(List<BufferedImage> frames, int[] delaysMs, int originalWidth, int originalHeight) {
        this.frames = List.copyOf(frames);
        this.delaysMs = delaysMs;
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
    }

    public static GifAnimation read(Path path) throws IOException {
        if (!java.nio.file.Files.isRegularFile(path)) {
            throw new IOException("File does not exist: " + path);
        }
        long fileSize = java.nio.file.Files.size(path);
        if (fileSize <= 0 || fileSize > MAX_INPUT_BYTES) {
            throw new IOException("GIF file must be between 1 byte and 64 MiB");
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(path.toFile())) {
            if (input == null) {
                throw new IOException("Could not open GIF: " + path);
            }

            var readers = ImageIO.getImageReaders(input);
            ImageReader reader = null;
            while (readers.hasNext()) {
                ImageReader candidate = readers.next();
                if (candidate.getFormatName().equalsIgnoreCase("gif")) {
                    reader = candidate;
                    break;
                }
                candidate.dispose();
            }
            if (reader == null) {
                throw new IOException("This file is not a supported GIF image");
            }

            try {
                reader.setInput(input, false, false);
                int count = reader.getNumImages(true);
                if (count < 1) {
                    throw new IOException("GIF has no frames");
                }
                if (count > MAX_FRAMES) {
                    throw new IOException("GIF has " + count + " frames; maximum supported is " + MAX_FRAMES);
                }

                int canvasWidth = reader.getWidth(0);
                int canvasHeight = reader.getHeight(0);
                IIOMetadata streamMetadata = reader.getStreamMetadata();
                Node logicalScreen = findNode(
                        tree(streamMetadata, "javax_imageio_gif_stream_1.0"),
                        "LogicalScreenDescriptor"
                );
                if (logicalScreen != null) {
                    canvasWidth = integerAttribute(logicalScreen, "logicalScreenWidth", canvasWidth);
                    canvasHeight = integerAttribute(logicalScreen, "logicalScreenHeight", canvasHeight);
                }
                validateDimensions(canvasWidth, canvasHeight);

                BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
                List<BufferedImage> storedFrames = new ArrayList<>(count);
                int[] delays = new int[count];
                Rectangle previousRect = null;
                String previousDisposal = "none";
                BufferedImage savedCanvas = null;

                for (int index = 0; index < count; index++) {
                    if (previousRect != null) {
                        if ("restoreToBackgroundColor".equals(previousDisposal)) {
                            clearRectangle(canvas, previousRect);
                        } else if ("restoreToPrevious".equals(previousDisposal) && savedCanvas != null) {
                            canvas = copy(savedCanvas);
                        }
                    }

                    IIOMetadata frameMetadata = reader.getImageMetadata(index);
                    Node frameRoot = tree(frameMetadata, "javax_imageio_gif_image_1.0");
                    Node descriptor = findNode(frameRoot, "ImageDescriptor");
                    Node control = findNode(frameRoot, "GraphicControlExtension");
                    int left = integerAttribute(descriptor, "imageLeftPosition", 0);
                    int top = integerAttribute(descriptor, "imageTopPosition", 0);
                    int frameWidth = integerAttribute(descriptor, "imageWidth", reader.getWidth(index));
                    int frameHeight = integerAttribute(descriptor, "imageHeight", reader.getHeight(index));
                    validateDimensions(frameWidth, frameHeight);

                    String disposal = stringAttribute(control, "disposalMethod", "none");
                    savedCanvas = "restoreToPrevious".equals(disposal) ? copy(canvas) : null;

                    BufferedImage raw = reader.read(index);
                    Graphics2D graphics = canvas.createGraphics();
                    try {
                        graphics.setComposite(AlphaComposite.SrcOver);
                        int drawWidth = Math.min(raw.getWidth(), frameWidth);
                        int drawHeight = Math.min(raw.getHeight(), frameHeight);
                        graphics.drawImage(raw, left, top, left + drawWidth, top + drawHeight,
                                0, 0, drawWidth, drawHeight, null);
                    } finally {
                        graphics.dispose();
                    }

                    storedFrames.add(shrinkPreservingAspect(canvas));
                    int delayCentiseconds = integerAttribute(control, "delayTime", 10);
                    delays[index] = Math.max(20, delayCentiseconds * 10);
                    previousRect = new Rectangle(left, top, frameWidth, frameHeight);
                    previousDisposal = disposal;
                }

                return new GifAnimation(storedFrames, delays, canvasWidth, canvasHeight);
            } finally {
                reader.dispose();
            }
        }
    }

    public int frameCount() {
        return frames.size();
    }

    public int originalWidth() {
        return originalWidth;
    }

    public int originalHeight() {
        return originalHeight;
    }

    public int frameDelayMs(int index) {
        return delaysMs[Math.max(0, Math.min(delaysMs.length - 1, index))];
    }

    public BufferedImage sourceFrame(int index) {
        return frames.get(Math.max(0, Math.min(frames.size() - 1, index)));
    }

    public void fitToIcon(GifIconSettings settings) {
        settings.sanitize(frames.size());

        int start = settings.startFrame;
        int end = settings.endFrame;
        BufferedImage firstFrame = frames.get(start);
        int canvasWidth = firstFrame.getWidth();
        int canvasHeight = firstFrame.getHeight();

        int minX = canvasWidth;
        int minY = canvasHeight;
        int maxX = -1;
        int maxY = -1;

        for (int frameIndex = start; frameIndex <= end; frameIndex++) {
            BufferedImage frame = frames.get(frameIndex);
            for (int y = 0; y < frame.getHeight(); y++) {
                for (int x = 0; x < frame.getWidth(); x++) {
                    if ((frame.getRGB(x, y) >>> 24) != 0) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
        }

        settings.fitMode = GifIconSettings.FitMode.CONTAIN.name();
        if (maxX < minX || maxY < minY) {
            settings.zoom = 1.0f;
            settings.offsetX = 0;
            settings.offsetY = 0;
            return;
        }

        double baseScale = Math.min(
                (double) ICON_SIZE / canvasWidth,
                (double) ICON_SIZE / canvasHeight
        );
        int contentWidth = maxX - minX + 1;
        int contentHeight = maxY - minY + 1;
        double zoom = Math.min(
                (ICON_SIZE - 8.0) / (contentWidth * baseScale),
                (ICON_SIZE - 8.0) / (contentHeight * baseScale)
        );
        zoom = Math.max(0.5, Math.min(2.0, zoom));

        double scale = baseScale * zoom;
        double contentCenterX = (minX + maxX + 1) / 2.0;
        double contentCenterY = (minY + maxY + 1) / 2.0;
        settings.zoom = (float) zoom;
        settings.offsetX = Math.max(-32, Math.min(32,
                (int) Math.round((canvasWidth / 2.0 - contentCenterX) * scale)
        ));
        settings.offsetY = Math.max(-32, Math.min(32,
                (int) Math.round((canvasHeight / 2.0 - contentCenterY) * scale)
        ));
    }

    public int frameAt(long elapsedMs, GifIconSettings settings) {
        int start = Math.max(0, Math.min(frames.size() - 1, settings.startFrame));
        int end = Math.max(start, Math.min(
                frames.size() - 1,
                settings.endFrame < 0 ? frames.size() - 1 : settings.endFrame
        ));
        long elapsed = Math.max(0L, (long) (elapsedMs * settings.speed));
        long duration = 0;
        for (int i = start; i <= end; i++) {
            duration += delaysMs[i];
        }
        duration = Math.max(1L, duration);

        GifIconSettings.PlaybackMode mode = settings.getPlaybackMode();
        if (mode == GifIconSettings.PlaybackMode.ONCE) {
            elapsed = Math.min(elapsed, duration - 1);
        } else {
            long cycle = mode == GifIconSettings.PlaybackMode.PING_PONG && end > start
                    ? Math.max(1L, duration * 2 - delaysMs[start] - delaysMs[end])
                    : duration;
            elapsed = Math.floorMod(elapsed, cycle);
        }

        if (mode != GifIconSettings.PlaybackMode.PING_PONG || end == start) {
            for (int i = start; i <= end; i++) {
                if (elapsed < delaysMs[i]) {
                    return i;
                }
                elapsed -= delaysMs[i];
            }
            return end;
        }

        for (int i = start; i <= end; i++) {
            if (elapsed < delaysMs[i]) {
                return i;
            }
            elapsed -= delaysMs[i];
        }
        for (int i = end - 1; i > start; i--) {
            if (elapsed < delaysMs[i]) {
                return i;
            }
            elapsed -= delaysMs[i];
        }
        return start;
    }

        public BufferedImage renderFrame(int index, GifIconSettings settings) {
        BufferedImage source = sourceFrame(index);
        BufferedImage result = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.SrcOver);
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            double scaleX = (double) ICON_SIZE / source.getWidth();
            double scaleY = (double) ICON_SIZE / source.getHeight();
            double scale = switch (settings.getFitMode()) {
                case CONTAIN -> Math.min(scaleX, scaleY);
                case COVER -> Math.max(scaleX, scaleY);
                case STRETCH -> 1.0;
            };

            boolean stretch = settings.getFitMode() == GifIconSettings.FitMode.STRETCH;
            int width = Math.max(1, (int) Math.round(
                    source.getWidth() * (stretch ? scaleX : scale) * settings.zoom
            ));
            int height = Math.max(1, (int) Math.round(
                    source.getHeight() * (stretch ? scaleY : scale) * settings.zoom
            ));
            int x = (ICON_SIZE - width) / 2 + settings.offsetX;
            int y = (ICON_SIZE - height) / 2 + settings.offsetY;
            graphics.drawImage(source, x, y, width, height, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private static BufferedImage shrinkPreservingAspect(BufferedImage source) {
        double scale = Math.min(1.0, (double) STORED_MAX_SIDE / Math.max(source.getWidth(), source.getHeight()));
        int width = Math.max(1, (int) Math.round(source.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(source.getHeight() * scale));
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return scaled;
    }

    private static void clearRectangle(BufferedImage canvas, Rectangle rectangle) {
        Graphics2D graphics = canvas.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Clear);
            graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        } finally {
            graphics.dispose();
        }
    }

    private static BufferedImage copy(BufferedImage source) {
        BufferedImage result = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.setComposite(AlphaComposite.Src);
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private static void validateDimensions(int width, int height) throws IOException {
        if (width < 1 || height < 1 || width > MAX_DIMENSION || height > MAX_DIMENSION
                || (long) width * height > MAX_CANVAS_PIXELS) {
            throw new IOException(
                    "Unsupported GIF frame size: " + width + "x" + height
                            + " (maximum dimension " + MAX_DIMENSION + ")"
            );
        }
    }

    private static Node tree(IIOMetadata metadata, String format) throws IOException {
        if (metadata == null) {
            return null;
        }
        try {
            return metadata.getAsTree(format);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Unsupported GIF metadata tree " + format, exception);
        }
    }

    private static Node findNode(Node parent, String name) {
        if (parent == null) {
            return null;
        }
        if (name.equals(parent.getNodeName())) {
            return parent;
        }
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            Node found = findNode(child, name);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static String stringAttribute(Node node, String name, String fallback) {
        if (node == null || node.getAttributes() == null || node.getAttributes().getNamedItem(name) == null) {
            return fallback;
        }
        return node.getAttributes().getNamedItem(name).getNodeValue();
    }

    private static int integerAttribute(Node node, String name, int fallback) {
        try {
            return Integer.parseInt(stringAttribute(node, name, Integer.toString(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
