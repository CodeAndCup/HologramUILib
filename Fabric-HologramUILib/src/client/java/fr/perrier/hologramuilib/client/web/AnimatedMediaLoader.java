package fr.perrier.hologramuilib.client.web;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Handles animated media (GIF, APNG) by extracting frames.
 * For MP4/video support, this would need a proper video decoding library (JAVE, Xuggler, etc.)
 * which are too heavy for this demo. GIF support is included as it uses standard ImageIO.
 */
public class AnimatedMediaLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/AnimatedMedia");

    /**
     * Represents an animated media with multiple frames.
     */
    public static class AnimatedMedia {
        private final List<Frame> frames;
        private final boolean isAnimated;

        public AnimatedMedia(List<Frame> frames, boolean isAnimated) {
            this.frames = frames;
            this.isAnimated = isAnimated;
        }

        public List<Frame> getFrames() {
            return frames;
        }

        public boolean isAnimated() {
            return isAnimated;
        }

        public int getFrameCount() {
            return frames.size();
        }
    }

    /**
     * Represents a single frame in an animation.
     */
    public static class Frame {
        private final BufferedImage image;
        private final int delayMs;

        public Frame(BufferedImage image, int delayMs) {
            this.image = image;
            this.delayMs = delayMs;
        }

        public BufferedImage getImage() {
            return image;
        }

        public int getDelayMs() {
            return delayMs;
        }
    }

    /**
     * Loads an animated GIF from bytes.
     * Returns an AnimatedMedia with all frames properly composed.
     */
    public static AnimatedMedia loadAnimatedGif(byte[] data) {
        try {
            List<Frame> frames = new ArrayList<>();

            ImageInputStream stream = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

            if (!readers.hasNext()) {
                throw new Exception("No image reader found");
            }

            ImageReader reader = readers.next();
            reader.setInput(stream);

            int frameCount = reader.getNumImages(true);
            LOGGER.info("Loading animated GIF with {} frames", frameCount);

            // Get the first frame to determine dimensions
            BufferedImage firstFrame = reader.read(0);
            int width = firstFrame.getWidth();
            int height = firstFrame.getHeight();

            // Canvas to compose frames on (for proper GIF rendering with disposal methods)
            BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D graphics = canvas.createGraphics();

            for (int i = 0; i < frameCount; i++) {
                BufferedImage frameImage = reader.read(i);

                // Try to get frame metadata (delay and disposal method)
                int delay = 100; // Default 100ms per frame
                String disposalMethod = "none";

                try {
                    IIOMetadata metadata = reader.getImageMetadata(i);
                    String metaFormat = metadata.getNativeMetadataFormatName();
                    if (metaFormat != null) {
                        org.w3c.dom.Node tree = metadata.getAsTree(metaFormat);
                        org.w3c.dom.NodeList children = tree.getChildNodes();
                        for (int j = 0; j < children.getLength(); j++) {
                            org.w3c.dom.Node child = children.item(j);
                            if ("GraphicControlExtension".equals(child.getNodeName())) {
                                org.w3c.dom.NamedNodeMap attributes = child.getAttributes();

                                // Get delay time
                                org.w3c.dom.Node delayNode = attributes.getNamedItem("delayTime");
                                if (delayNode != null) {
                                    delay = Integer.parseInt(delayNode.getNodeValue()) * 10;
                                    if (delay == 0) delay = 100;
                                }

                                // Get disposal method
                                org.w3c.dom.Node disposalNode = attributes.getNamedItem("disposalMethod");
                                if (disposalNode != null) {
                                    disposalMethod = disposalNode.getNodeValue();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.debug("Could not read frame metadata, using defaults: {}", e.getMessage());
                }

                // Clear canvas based on disposal method from previous frame
                if (i > 0) {
                    if ("restoreToBackgroundColor".equals(disposalMethod) || "restoreToPrevious".equals(disposalMethod)) {
                        graphics.setComposite(java.awt.AlphaComposite.Clear);
                        graphics.fillRect(0, 0, width, height);
                        graphics.setComposite(java.awt.AlphaComposite.SrcOver);
                    }
                }

                // Draw current frame on canvas
                graphics.drawImage(frameImage, 0, 0, null);

                // Create a copy of the current canvas state for this frame
                BufferedImage composedFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D frameGraphics = composedFrame.createGraphics();
                frameGraphics.drawImage(canvas, 0, 0, null);
                frameGraphics.dispose();

                frames.add(new Frame(composedFrame, delay));
            }

            graphics.dispose();
            reader.dispose();
            stream.close();

            boolean animated = frameCount > 1;
            LOGGER.info("Loaded {} frames, animated: {}", frameCount, animated);

            return new AnimatedMedia(frames, animated);

        } catch (Exception e) {
            LOGGER.error("Failed to load animated GIF", e);
            return null;
        }
    }

    /**
     * Loads a static image (PNG, JPG) as a single-frame AnimatedMedia.
     */
    public static AnimatedMedia loadStaticImage(BufferedImage image) {
        List<Frame> frames = new ArrayList<>();
        frames.add(new Frame(image, 0)); // 0 delay = static
        return new AnimatedMedia(frames, false);
    }

    /**
     * Detects if a URL points to an animated format.
     */
    public static boolean isAnimatedFormat(String url) {
        String lower = url.toLowerCase();
        return lower.endsWith(".gif") || lower.contains(".gif?");
    }

    /**
     * Detects if a URL points to a video format.
     * Note: MP4/video support would require additional libraries (JAVE, Xuggler, JavaCV)
     * which are too heavy for this implementation. This is just detection.
     */
    public static boolean isVideoFormat(String url) {
        String lower = url.toLowerCase();
        return lower.endsWith(".mp4") ||
               lower.endsWith(".webm") ||
               lower.endsWith(".mov") ||
               lower.contains(".mp4?") ||
               lower.contains(".webm?");
    }
}
