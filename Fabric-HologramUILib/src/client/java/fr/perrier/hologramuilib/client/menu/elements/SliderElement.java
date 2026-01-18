package fr.perrier.hologramuilib.client.menu.elements;

import fr.perrier.hologramuilib.client.animation.AnimationManager;
import fr.perrier.hologramuilib.client.animation.AnimationProperties;
import fr.perrier.hologramuilib.client.config.ItemConfig;
import fr.perrier.hologramuilib.util.ColorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvents;
import org.joml.Matrix4f;

import java.util.function.Consumer;

/**
 * Slider element for hologram menus.
 * Allows users to select a value within a range by dragging a handle.
 */
public class SliderElement extends AbstractMenuElement {

    private float value; // Current value (0.0 to 1.0)
    private float minValue;
    private float maxValue;
    private int trackColor;
    private int fillColor;
    private int handleColor;
    private int borderColor;
    private boolean showValue;
    private String label;
    private String unit;
    private int decimals;

    private boolean dragging = false;
    private Consumer<Float> valueChangeCallback;
    private AnimationManager animationManager;

    public SliderElement(String id) {
        super(id);
        this.value = 0.5f; // Default middle
        this.minValue = 0.0f;
        this.maxValue = 100.0f;
        this.trackColor = 0xFF333333;
        this.fillColor = 0xFF00FF00; // Green
        this.handleColor = 0xFFFFFFFF;
        this.borderColor = 0xFF888888;
        this.showValue = true;
        this.label = "";
        this.unit = "";
        this.decimals = 0;
        this.width = 150;
        this.height = 16;
    }

    /**
     * Creates a slider element from configuration.
     */
    public static SliderElement fromConfig(ItemConfig config) {
        SliderElement slider = new SliderElement(config.getId());

        ItemConfig.ItemStyleConfig style = config.getStyle();
        if (style != null) {
            if (style.getHeight() > 0) {
                slider.height = style.getHeight();
            }
            slider.fillColor = style.getTextColorInt();
        }

        return slider;
    }

    @Override
    public void render(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers,
                       float x, float y, boolean hovered, float tickDelta) {
        this.bounds = new Bounds(x, y, width, height);

        matrices.push();

        // Apply translation animations
        float translateX = 0;
        float translateY = 0;
        float scale = 1.0f;
        float scaleX = 1.0f;
        float scaleY = 1.0f;
        float rotation = 0;
        float opacity = 1.0f;
        float colorAlpha = 1.0f;

        // Get animated value if available
        float animatedValue = value;
        if (animationManager != null) {
            animatedValue = animationManager.getValue(id, AnimationProperties.SLIDER_VALUE, value);
            translateX = animationManager.getValue(id, "translateX", 0);
            translateY = animationManager.getValue(id, "translateY", 0);
            scale = animationManager.getValue(id, "scale", 1.0f);
            scaleX = animationManager.getValue(id, "scaleX", scale);
            scaleY = animationManager.getValue(id, "scaleY", scale);
            rotation = animationManager.getValue(id, "rotation", 0);
            opacity = animationManager.getValue(id, "opacity", 1.0f);
            colorAlpha = animationManager.getValue(id, "colorA", 1.0f);
        }

        float finalAlpha = opacity * colorAlpha;

        matrices.translate(x + translateX, y + translateY, 0);

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

        float borderThickness = 1.0f;
        float handleWidth = 8.0f;
        float handleHeight = height + 4.0f;
        float trackHeight = height * 0.4f;
        float trackY = (height - trackHeight) / 2;

        // Render only if visible
        if (finalAlpha > 0.01f) {
            // Render border around track
            renderBorder(matrices, vertexConsumers, -borderThickness, trackY - borderThickness,
                        width + borderThickness * 2, trackHeight + borderThickness * 2,
                        borderColor, borderThickness, 0.05f, finalAlpha);

            // Render track background
            renderFilledRect(matrices, vertexConsumers, 0, trackY, width, trackHeight, trackColor, 0.06f, finalAlpha);

            // Render filled portion (from left to handle position)
            float handleX = width * animatedValue;
            int fillColorToUse = hovered ? brightenColor(fillColor) : fillColor;
            if (handleX > 0) {
                renderFilledRect(matrices, vertexConsumers, 0, trackY, handleX, trackHeight, fillColorToUse, 0.07f, finalAlpha);
            }

            // Render handle
            float handleRenderX = handleX - handleWidth / 2;
            float handleRenderY = (height - handleHeight) / 2;
            int handleColorToUse = (hovered || dragging) ? brightenColor(handleColor) : handleColor;
            renderFilledRect(matrices, vertexConsumers, handleRenderX, handleRenderY, handleWidth, handleHeight, handleColorToUse, 0.08f, finalAlpha);

            // Render handle border
            renderBorder(matrices, vertexConsumers, handleRenderX, handleRenderY, handleWidth, handleHeight,
                        borderColor, 1.0f, 0.09f, finalAlpha);

            // Render value text if enabled
            if (showValue) {
                float actualValue = minValue + (maxValue - minValue) * animatedValue;
                String valueText;

                if (decimals > 0) {
                    String format = "%." + decimals + "f";
                    valueText = String.format(format, actualValue);
                } else {
                    valueText = String.format("%.0f", actualValue);
                }

                if (!unit.isEmpty()) {
                    valueText += unit;
                }

                String displayText = label.isEmpty() ? valueText : label + ": " + valueText;
                float textWidth = textRenderer.getWidth(displayText);
                float textX = (width - textWidth) / 2;
                float textY = -10; // Above the slider

                matrices.push();
                matrices.translate(textX, textY, 0.1f);

                // Apply alpha to text
                int textAlpha = (int)(finalAlpha * 255);
                int textColor = 0x00FFFFFF | (textAlpha << 24);

                textRenderer.draw(
                    displayText,
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

    private void renderFilledRect(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                                   float x, float y, float width, float height, int color, float z, float alphaMultiplier) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float a = (ColorHelper.getAlpha(color) / 255f) * alphaMultiplier;
        float r = ColorHelper.getRed(color) / 255f;
        float g = ColorHelper.getGreen(color) / 255f;
        float b = ColorHelper.getBlue(color) / 255f;

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getGui());

        buffer.vertex(matrix, x, y + height, z).color(r, g, b, a);
        buffer.vertex(matrix, x + width, y + height, z).color(r, g, b, a);
        buffer.vertex(matrix, x + width, y, z).color(r, g, b, a);
        buffer.vertex(matrix, x, y, z).color(r, g, b, a);
    }

    private void renderBorder(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                              float x, float y, float width, float height, int color, float thickness, float z, float alphaMultiplier) {
        // Top
        renderFilledRect(matrices, vertexConsumers, x, y, width, thickness, color, z, alphaMultiplier);
        // Bottom
        renderFilledRect(matrices, vertexConsumers, x, y + height - thickness, width, thickness, color, z, alphaMultiplier);
        // Left
        renderFilledRect(matrices, vertexConsumers, x, y, thickness, height, color, z, alphaMultiplier);
        // Right
        renderFilledRect(matrices, vertexConsumers, x + width - thickness, y, thickness, height, color, z, alphaMultiplier);
    }

    private int brightenColor(int color) {
        int r = Math.min(255, ColorHelper.getRed(color) + 30);
        int g = Math.min(255, ColorHelper.getGreen(color) + 30);
        int b = Math.min(255, ColorHelper.getBlue(color) + 30);
        return ColorHelper.fromRGBA(r, g, b, ColorHelper.getAlpha(color));
    }

    @Override
    public void onClick(int button) {
        if (button == 0) { // Left click to start dragging
            dragging = true;
            playSound();
        }
    }

    /**
     * Called when the mouse is released (from interaction handler).
     */
    public void onRelease() {
        if (dragging) {
            dragging = false;
        }
    }

    /**
     * Updates the slider value based on mouse position.
     * Should be called from the interaction handler when dragging.
     *
     * @param mouseX Mouse X in element-local coordinates
     */
    public void updateValueFromMouse(float mouseX) {
        if (!dragging) {
            return;
        }

        // Clamp mouse position to slider bounds
        float clampedX = Math.max(0, Math.min(width, mouseX));
        float newValue = clampedX / width;


        if (Math.abs(newValue - value) > 0.001f) {
            setValue(newValue);

            if (valueChangeCallback != null) {
                float actualValue = minValue + (maxValue - minValue) * value;
                valueChangeCallback.accept(actualValue);
            }
        }
    }

    private void playSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
        }
    }

    // === GETTERS AND SETTERS ===

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = Math.max(0, Math.min(1, value));
    }

    public float getActualValue() {
        return minValue + (maxValue - minValue) * value;
    }

    public void setActualValue(float actualValue) {
        this.value = (actualValue - minValue) / (maxValue - minValue);
        this.value = Math.max(0, Math.min(1, this.value));
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public void setRange(float min, float max) {
        this.minValue = min;
        this.maxValue = max;
    }

    public int getTrackColor() {
        return trackColor;
    }

    public void setTrackColor(int trackColor) {
        this.trackColor = trackColor;
    }

    public int getFillColor() {
        return fillColor;
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
    }

    public int getHandleColor() {
        return handleColor;
    }

    public void setHandleColor(int handleColor) {
        this.handleColor = handleColor;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public boolean isShowValue() {
        return showValue;
    }

    public void setShowValue(boolean showValue) {
        this.showValue = showValue;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label != null ? label : "";
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit != null ? unit : "";
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = Math.max(0, decimals);
    }

    public boolean isDragging() {
        return dragging;
    }

    public SliderElement onValueChange(Consumer<Float> callback) {
        this.valueChangeCallback = callback;
        return this;
    }

    public SliderElement setWidth(float width) {
        this.width = width;
        return this;
    }

    public void setAnimationManager(AnimationManager animationManager) {
        this.animationManager = animationManager;
    }
}

