package fr.perrier.hologramuilib.client.menu.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.perrier.hologramuilib.client.web.AnimatedMediaLoader;
import fr.perrier.hologramuilib.client.web.URLResourceLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Menu element that displays an image/animated GIF loaded from a URL.
 *
 * Features:
 * - Async loading
 * - Animated GIF support with frame-by-frame animation
 * - Static image support (PNG, JPG)
 * - Loading placeholder
 * - Error handling
 * - Automatic caching
 *
 * Example usage:
 * <pre>
 * // Static image
 * ImageURLElement img = new ImageURLElement("avatar", "https://example.com/avatar.png");
 * img.setSize(64, 64);
 * menu.addElement(img);
 *
 * // Animated GIF
 * ImageURLElement gif = new ImageURLElement("nyan", "https://example.com/nyan.gif");
 * gif.setSize(200, 133);
 * menu.addElement(gif);
 * </pre>
 */
public class ImageURLElement extends AbstractMenuElement {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/ImageURL");

    private final String url;
    private final List<Identifier> textureIds = new ArrayList<>();
    private final List<Integer> frameDelays = new ArrayList<>();

    private LoadState state = LoadState.LOADING;
    private boolean isAnimated = false;
    private int currentFrame = 0;
    private long lastFrameTime = 0;

    public ImageURLElement(String id, String url) {
        super(id);
        this.url = url;
        this.width = 64;
        this.height = 64;

        // Start loading immediately
        startLoading();
    }

    /**
     * Starts loading the image/GIF from the URL.
     */
    private void startLoading() {
        state = LoadState.LOADING;

        // Check if it's an animated format (GIF)
        if (AnimatedMediaLoader.isAnimatedFormat(url)) {
            // Load as animated GIF
            CompletableFuture<AnimatedMediaLoader.AnimatedMedia> loadFuture =
                    URLResourceLoader.getInstance().loadAnimatedMedia(url);

            loadFuture.thenAccept(media -> {
                try {
                    // Convert all frames to textures
                    textureIds.clear();
                    frameDelays.clear();

                    for (AnimatedMediaLoader.Frame frame : media.getFrames()) {
                        Identifier frameTexture = createTextureFromBufferedImage(frame.getImage());
                        textureIds.add(frameTexture);
                        frameDelays.add(frame.getDelayMs());
                    }

                    isAnimated = media.isAnimated();
                    currentFrame = 0;
                    lastFrameTime = System.currentTimeMillis();

                    state = LoadState.LOADED;
                    LOGGER.info("Animated media loaded: {} ({} frames)", url, textureIds.size());
                } catch (Exception e) {
                    LOGGER.error("Failed to create textures from animated media", e);
                    state = LoadState.ERROR;
                }
            }).exceptionally(ex -> {
                LOGGER.error("Failed to load animated media from URL: " + url, ex);
                state = LoadState.ERROR;
                return null;
            });
        } else {
            // Load as static image
            CompletableFuture<BufferedImage> loadFuture =
                    URLResourceLoader.getInstance().loadImage(url);

            loadFuture.thenAccept(image -> {
                try {
                    // Convert BufferedImage to Minecraft texture
                    Identifier texture = createTextureFromBufferedImage(image);
                    textureIds.clear();
                    textureIds.add(texture);
                    frameDelays.add(0); // 0 = static
                    isAnimated = false;

                    state = LoadState.LOADED;
                    LOGGER.info("Image loaded: {}", url);
                } catch (Exception e) {
                    LOGGER.error("Failed to create texture from image", e);
                    state = LoadState.ERROR;
                }
            }).exceptionally(ex -> {
                LOGGER.error("Failed to load image from URL: " + url, ex);
                state = LoadState.ERROR;
                return null;
            });
        }
    }

    /**
     * Converts a BufferedImage to a Minecraft texture identifier.
     */
    private Identifier createTextureFromBufferedImage(BufferedImage bufferedImage) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Convert BufferedImage to NativeImage
        int imgWidth = bufferedImage.getWidth();
        int imgHeight = bufferedImage.getHeight();
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, imgWidth, imgHeight, false);

        // Copy pixels directly - setColorArgb expects ARGB format
        for (int y = 0; y < imgHeight; y++) {
            for (int x = 0; x < imgWidth; x++) {
                int argb = bufferedImage.getRGB(x, y);

                // setColorArgb already handles the conversion internally
                // Just pass the ARGB value directly
                nativeImage.setColorArgb(x, y, argb);
            }
        }

        // Create texture with unique ID for each frame
        NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
        Identifier id = Identifier.of("hologramuilib", "url_image_" + this.id + "_f" + textureIds.size());
        client.getTextureManager().registerTexture(id, texture);

        return id;
    }

    @Override
    public void render(MatrixStack matrices, TextRenderer textRenderer,
                       VertexConsumerProvider vertexConsumers,
                       float x, float y, boolean hovered, float tickDelta) {
        this.bounds = new Bounds(x, y, width, height);

        // Update animation frame if needed
        if (state == LoadState.LOADED && isAnimated && !textureIds.isEmpty()) {
            updateAnimation();
        }

        matrices.push();
        matrices.translate(x, y, 0);

        // Render based on state
        switch (state) {
            case LOADING:
                renderLoadingState(matrices, textRenderer, vertexConsumers);
                break;
            case LOADED:
                renderLoadedState(matrices, textRenderer, vertexConsumers);
                break;
            case ERROR:
                renderErrorState(matrices, textRenderer, vertexConsumers);
                break;
        }

        matrices.pop();
    }

    /**
     * Updates the current animation frame based on time.
     */
    private void updateAnimation() {
        if (!isAnimated || textureIds.isEmpty() || frameDelays.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        int currentDelay = frameDelays.get(currentFrame);

        // Check if it's time to advance to next frame
        if (currentTime - lastFrameTime >= currentDelay) {
            currentFrame = (currentFrame + 1) % textureIds.size();
            lastFrameTime = currentTime;
        }
    }

    private void renderLoadingState(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers) {
        // Draw loading text
        String loadingText = "Loading...";
        float textX = (width - textRenderer.getWidth(loadingText)) / 2f;
        float textY = (height - 8) / 2f;

        textRenderer.draw(
                loadingText,
                textX,
                textY,
                0xFFFFFF,
                false,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0,
                15728880
        );
    }

    private void renderLoadedState(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers) {
        if (textureIds.isEmpty()) {
            return;
        }

        // Get current frame texture
        Identifier currentTexture = textureIds.get(currentFrame);

        // Render the texture
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Bind the texture
        RenderSystem.setShaderTexture(0, currentTexture);

        // Use GUI texture layer for proper rendering
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getText(currentTexture));

        // UV coordinates (0,0 to 1,1 for full texture)
        float u0 = 0.0f, v0 = 0.0f;
        float u1 = 1.0f, v1 = 1.0f;

        // Draw textured quad
        // Top left
        buffer.vertex(matrix, 0, 0, 0.01f)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(u0, v0)
                .light(15728880);

        // Bottom left
        buffer.vertex(matrix, 0, height, 0.01f)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(u0, v1)
                .light(15728880);

        // Bottom right
        buffer.vertex(matrix, width, height, 0.01f)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(u1, v1)
                .light(15728880);

        // Top right
        buffer.vertex(matrix, width, 0, 0.01f)
                .color(1.0f, 1.0f, 1.0f, 1.0f)
                .texture(u1, v0)
                .light(15728880);
    }

    private void renderErrorState(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers) {
        // Draw error text
        String errorText = "Failed to load";
        float textX = (width - textRenderer.getWidth(errorText)) / 2f;
        float textY = (height - 8) / 2f;

        textRenderer.draw(
                errorText,
                textX,
                textY,
                0xFF0000,
                false,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0,
                15728880
        );
    }

    @Override
    public void onClick(int button) {
        // Could open URL in browser or perform other action
    }

    // Getters and setters

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public LoadState getState() {
        return state;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public int getFrameCount() {
        return textureIds.size();
    }

    public enum LoadState {
        LOADING,
        LOADED,
        ERROR
    }
}
