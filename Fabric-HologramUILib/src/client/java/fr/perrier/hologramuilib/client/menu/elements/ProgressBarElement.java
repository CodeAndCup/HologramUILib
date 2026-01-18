package fr.perrier.hologramuilib.client.menu.elements;

import fr.perrier.hologramuilib.client.config.ItemConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

/**
 * Progress bar element for displaying progress/percentage in hologram menus.
 */
public class ProgressBarElement extends AbstractMenuElement {

    private float progress; // 0.0 to 1.0
    private int backgroundColor;
    private int foregroundColor;
    private int borderColor;
    private boolean showPercentage;
    private String label;

    public ProgressBarElement(String id) {
        super(id);
        this.progress = 0.5f; // Default 50%
        this.backgroundColor = 0xFF333333;
        this.foregroundColor = 0xFF00FF00; // Green
        this.borderColor = 0xFF888888;
        this.showPercentage = true;
        this.label = "";
        this.width = 100;
        this.height = 12;
    }

    /**
     * Creates a progress bar element from configuration.
     */
    public static ProgressBarElement fromConfig(ItemConfig config) {
        ProgressBarElement progressBar = new ProgressBarElement(config.getId());

        ItemConfig.ItemStyleConfig style = config.getStyle();
        if (style != null) {
            if (style.getHeight() > 0) {
                progressBar.height = style.getHeight();
            }
            progressBar.foregroundColor = style.getTextColorInt();
        }

        return progressBar;
    }

    @Override
    public void render(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers,
                       float x, float y, boolean hovered, float tickDelta) {
        this.bounds = new Bounds(x, y, width, height);

        matrices.push();

        // Apply translation animations
        float translateX = 0;
        float translateY = 0;
        if (animationManager != null) {
            translateX = animationManager.getValue(id, "translateX", 0);
            translateY = animationManager.getValue(id, "translateY", 0);
        }
        matrices.translate(x + translateX, y + translateY, 0);

        // Get animated scale
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        float scale = 1.0f;
        float rotation = 0;
        float opacity = 1.0f;
        float colorAlpha = 1.0f;

        if (animationManager != null) {
            scale = animationManager.getValue(id, "scale", 1.0f);
            scaleX = animationManager.getValue(id, "scaleX", scale);
            scaleY = animationManager.getValue(id, "scaleY", scale);
            rotation = animationManager.getValue(id, "rotation", 0);
            opacity = animationManager.getValue(id, "opacity", 1.0f);
            colorAlpha = animationManager.getValue(id, "colorA", 1.0f);
        }

        float finalAlpha = opacity * colorAlpha;

        // Apply scale animation (centered)
        if (scaleX != 1.0f || scaleY != 1.0f) {
            matrices.push();
            matrices.translate(width / 2, height / 2, 0);
            matrices.scale(scaleX, scaleY, 1);
            matrices.translate(-width / 2, -height / 2, 0);
        }

        // Apply rotation animation (centered)
        if (rotation != 0) {
            matrices.push();
            matrices.translate(width / 2, height / 2, 0);
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
            matrices.translate(-width / 2, -height / 2, 0);
        }

        // Get animated progress value if available
        float currentProgress = progress;
        if (animationManager != null) {
            currentProgress = animationManager.getValue(id, "progress", progress);
        }

        float borderThickness = 2.0f;

        // Render only if visible
        if (finalAlpha > 0.01f) {
            // Use larger Z offsets to prevent z-fighting (we're already at 0.1 from HologramMenu)
            // Render border AROUND the entire progress bar (extends outside)
            renderThickBorder(matrices, vertexConsumers, -borderThickness, -borderThickness,
                             width + borderThickness * 2, height + borderThickness * 2,
                             borderColor, borderThickness, 0.05f, finalAlpha);

            // Render background (full size)
            renderFilledRect(matrices, vertexConsumers, 0, 0, width, height, backgroundColor, 0.06f, finalAlpha);

            // Render progress bar (full height, partial width based on current animated progress)
            float progressWidth = width * Math.max(0, Math.min(1, currentProgress));
            if (progressWidth > 0) {
                int color = hovered ? brightenColor(foregroundColor) : foregroundColor;
                renderFilledRect(matrices, vertexConsumers, 0, 0, progressWidth, height, color, 0.07f, finalAlpha);
            }

            // Render percentage text if enabled (using animated progress)
            if (showPercentage) {
                String text = (label.isEmpty() ? "" : label + " ") + String.format("%.0f%%", currentProgress * 100);
                float textWidth = textRenderer.getWidth(text);
                float textX = (width - textWidth) / 2;
                float textY = (height - 8) / 2;

                matrices.push();
                matrices.translate(textX, textY, 0.08f);

                // Apply alpha to text
                int textAlpha = (int)(finalAlpha * 255);
                int textColor = (0xFFFFFFFF & 0x00FFFFFF) | (textAlpha << 24);

                textRenderer.draw(
                    text,
                    0,
                    0,
                    textColor,
                    true, // shadow
                    matrices.peek().getPositionMatrix(),
                    vertexConsumers,
                    TextRenderer.TextLayerType.SEE_THROUGH,
                    0,
                    15728880
                );

                matrices.pop();
            }
        }

        // Pop rotation transform if applied
        if (rotation != 0) {
            matrices.pop();
        }

        // Pop scale transform if applied
        if (scaleX != 1.0f || scaleY != 1.0f) {
            matrices.pop();
        }

        matrices.pop();
    }

    /**
     * Renders a filled rectangle as a simple quad (4 vertices).
     */
    private void renderFilledRect(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                  float x, float y, float w, float h, int color, float z, float alphaMultiplier) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float a = (((color >> 24) & 0xFF) / 255f) * alphaMultiplier;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getGui());

        // Draw filled quad (single rectangle, 4 vertices in order)
        buffer.vertex(matrix, x, y + h, z).color(r, g, b, a);      // Bottom-left
        buffer.vertex(matrix, x + w, y + h, z).color(r, g, b, a);  // Bottom-right
        buffer.vertex(matrix, x + w, y, z).color(r, g, b, a);      // Top-right
        buffer.vertex(matrix, x, y, z).color(r, g, b, a);          // Top-left
    }

    /**
     * Renders a thick border around a rectangle.
     */
    private void renderThickBorder(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                   float x, float y, float w, float h, int color, float thickness, float z, float alphaMultiplier) {

        // Top border
        renderFilledRect(matrices, vertexConsumers, x, y, w, thickness, color, z, alphaMultiplier);

        // Bottom border
        renderFilledRect(matrices, vertexConsumers, x, y + h - thickness, w, thickness, color, z, alphaMultiplier);

        // Left border
        renderFilledRect(matrices, vertexConsumers, x, y + thickness, thickness, h - thickness * 2, color, z, alphaMultiplier);

        // Right border
        renderFilledRect(matrices, vertexConsumers, x + w - thickness, y + thickness, thickness, h - thickness * 2, color, z, alphaMultiplier);
    }

    /**
     * Brightens a color for hover effect.
     */
    private int brightenColor(int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Brighten by 20%
        r = Math.min(1.0f, r * 1.2f);
        g = Math.min(1.0f, g * 1.2f);
        b = Math.min(1.0f, b * 1.2f);

        return ((int)(a * 255) << 24) |
               ((int)(r * 255) << 16) |
               ((int)(g * 255) << 8) |
               (int)(b * 255);
    }

    // Getters and setters

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0, Math.min(1, progress));
    }

    /**
     * Alias for setProgress for consistency with other elements.
     */
    public void setValue(float value) {
        setProgress(value);
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(int foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public boolean isShowPercentage() {
        return showPercentage;
    }

    public void setShowPercentage(boolean showPercentage) {
        this.showPercentage = showPercentage;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label != null ? label : "";
    }

    public void setWidth(float width) {
        this.width = width;
    }
}

