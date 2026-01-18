package fr.perrier.hologramuilib.client.menu.elements;

import fr.perrier.hologramuilib.client.config.ItemConfig;
import fr.perrier.hologramuilib.util.ColorHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Text element for displaying static text in hologram menus.
 * Supports Minecraft color codes (§) and centering.
 */
public class TextElement extends AbstractMenuElement {

    private String content;
    private int textColor;
    private boolean centered;

    public TextElement(String id, String content) {
        super(id);
        this.content = content != null ? content : "";
        this.textColor = 0xFFFFFFFF;
        this.centered = false;
        this.height = 10; // Default text height
        this.width = 180;
    }

    /**
     * Creates a text element from configuration.
     */
    public static TextElement fromConfig(ItemConfig config) {
        TextElement text = new TextElement(config.getId(), config.getContent());
        text.centered = config.isCentered();

        if (config.getColor() != null) {
            text.textColor = config.getColorInt();
        }

        ItemConfig.ItemStyleConfig style = config.getStyle();
        if (style != null) {
            text.textColor = style.getTextColorInt();
        }

        return text;
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

        if (animationManager != null) {
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

        // Calculate text position
        float textWidth = textRenderer.getWidth(content);
        float textX = centered ? (width - textWidth) / 2 : 4; // 4px padding if not centered

        // Translate to element position with animation offset
        matrices.translate(x + textX + translateX, y + translateY, 0.01f);

        // Apply scale animation (centered on text)
        if (scaleX != 1.0f || scaleY != 1.0f) {
            matrices.push();
            matrices.translate(textWidth / 2, height / 2, 0);
            matrices.scale(scaleX, scaleY, 1);
            matrices.translate(-textWidth / 2, -height / 2, 0);
        }

        // Apply rotation animation (centered on text)
        if (rotation != 0) {
            matrices.push();
            matrices.translate(textWidth / 2, height / 2, 0);
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
            matrices.translate(-textWidth / 2, -height / 2, 0);
        }

        // Apply alpha to text color
        int renderColor = textColor;
        if (finalAlpha < 1.0f) {
            int alpha = (int)(finalAlpha * 255);
            renderColor = (textColor & 0x00FFFFFF) | (alpha << 24);
        }

        // Render only if visible
        if (finalAlpha > 0.01f) {
            // No flip needed - the menu's negative Y scale handles the orientation
            // Text renders normally

            textRenderer.draw(
                content,
                0,
                0,
                renderColor,
                false,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                15728880
            );
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

    // Getters and setters

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public boolean isCentered() {
        return centered;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    public void setWidth(float width) {
        this.width = width;
    }
}

