package fr.perrier.hologramuilib.client.menu.elements;

import fr.perrier.hologramuilib.client.animation.AnimationPresets;
import fr.perrier.hologramuilib.client.animation.AnimationProperties;
import fr.perrier.hologramuilib.client.config.ItemConfig;
import fr.perrier.hologramuilib.client.interaction.ActionRegistry;
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
 * Button element for hologram menus.
 * Supports text, optional icon, hover effects, and click actions.
 *
 * <p>Supports 3 ways to handle clicks:
 * <ul>
 *   <li>Simple callback: {@code .onClick(() -> {...})}</li>
 *   <li>Element callback: {@code .onClickCallback(element -> {...})}</li>
 *   <li>Action registry: {@code new ButtonElement("id", "text", "action_id")}</li>
 * </ul>
 */
public class ButtonElement extends AbstractMenuElement {

    private String text;
    private String action;
    private int textColor;
    private int hoverColor;
    private String clickSound;

    // Callback support
    private Consumer<ButtonElement> clickCallback;
    private Runnable simpleCallback;

    private boolean hovered = false;

    public ButtonElement(String id, String text, String action) {
        super(id);
        this.text = text != null ? text : "";
        this.action = action;
        this.textColor = 0xFFFFFFFF;
        this.hoverColor = 0x40FFFFFF;
        this.clickSound = "ui.button.click";
        this.height = 20;
        this.width = 180; // Default width
    }

    /**
     * Creates a button without an action (for use with callbacks).
     */
    public ButtonElement(String id, String text) {
        this(id, text, null);
    }

    /**
     * Creates a button element from configuration.
     */
    public static ButtonElement fromConfig(ItemConfig config) {
        ButtonElement button = new ButtonElement(config.getId(), config.getText(), config.getAction());

        ItemConfig.ItemStyleConfig style = config.getStyle();
        if (style != null) {
            button.textColor = style.getTextColorInt();
            button.hoverColor = style.getHoverColorInt();
            button.clickSound = style.getClickSound();
            if (style.getHeight() > 0) {
                button.height = style.getHeight();
            }
        }

        return button;
    }

    @Override
    public void render(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers,
                       float x, float y, boolean hovered, float tickDelta) {
        this.hovered = hovered;
        this.bounds = new Bounds(x, y, width, height);

        matrices.push();

        // Apply translation animations
        float translateX = getAnimatedValue(AnimationProperties.TRANSLATE_X, 0);
        float translateY = getAnimatedValue(AnimationProperties.TRANSLATE_Y, 0);
        matrices.translate(x + translateX, y + translateY, 0);

        // Get animated scale (check both SCALE and HOVER_SCALE)
        float scaleX = getAnimatedValue(AnimationProperties.SCALE_X,
                       getAnimatedValue(AnimationProperties.SCALE, 1.0f));
        float scaleY = getAnimatedValue(AnimationProperties.SCALE_Y,
                       getAnimatedValue(AnimationProperties.SCALE, 1.0f));
        float hoverScale = getAnimatedValue(AnimationProperties.HOVER_SCALE, 1.0f);

        // Combine scales
        float finalScaleX = scaleX * hoverScale;
        float finalScaleY = scaleY * hoverScale;

        // Apply scale animation (centered)
        if (finalScaleX != 1.0f || finalScaleY != 1.0f) {
            matrices.push();
            matrices.translate(width / 2, height / 2, 0);
            matrices.scale(finalScaleX, finalScaleY, 1);
            matrices.translate(-width / 2, -height / 2, 0);
        }

        // Apply rotation animation (centered)
        float rotation = getAnimatedValue(AnimationProperties.ROTATION, 0);
        if (rotation != 0) {
            matrices.push();
            matrices.translate(width / 2, height / 2, 0);
            matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
            matrices.translate(-width / 2, -height / 2, 0);
        }

        // Get opacity animation
        float opacity = getAnimatedValue(AnimationProperties.OPACITY, 1.0f);
        float colorAlpha = getAnimatedValue(AnimationProperties.COLOR_ALPHA, 1.0f);
        float finalAlpha = opacity * colorAlpha;

        // Draw background if hovered
        if (hovered && finalAlpha > 0.01f) {
            renderBackground(matrices, vertexConsumers, finalAlpha);
        }

        // Calculate text position
        float textWidth = textRenderer.getWidth(text);
        float textY = (height - 8) / 2; // 8 is approximate text height
        float textX = 4; // 4px padding from left
        int color = hovered ? brightenColor(textColor) : textColor;

        // Apply alpha to text color
        if (finalAlpha < 1.0f) {
            int alpha = (int)(finalAlpha * 255);
            color = (color & 0x00FFFFFF) | (alpha << 24);
        }

        // Use immediate mode text rendering
        if (finalAlpha > 0.01f) {
            matrices.push();
            matrices.translate(textX, textY, 0.01f); // Small Z offset to render above background

            // No flip needed - the menu's negative Y scale handles the orientation
            // Text renders normally

            textRenderer.draw(
                text,
                0,
                0,
                color,
                false, // shadow
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0, // backgroundColor
                15728880 // light
            );

            matrices.pop();
        }

        // Pop rotation transform if applied
        if (rotation != 0) {
            matrices.pop();
        }

        // Pop scale transform if applied
        if (finalScaleX != 1.0f || finalScaleY != 1.0f) {
            matrices.pop();
        }

        matrices.pop();
    }

    private void renderBackground(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float alphaMultiplier) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float a = (ColorHelper.getAlpha(hoverColor) / 255f) * alphaMultiplier;
        float r = ColorHelper.getRed(hoverColor) / 255f;
        float g = ColorHelper.getGreen(hoverColor) / 255f;
        float b = ColorHelper.getBlue(hoverColor) / 255f;

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getGui());

        buffer.vertex(matrix, 0, height, 0).color(r, g, b, a);
        buffer.vertex(matrix, width, height, 0).color(r, g, b, a);
        buffer.vertex(matrix, width, 0, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a);
    }

    private int brightenColor(int color) {
        int r = Math.min(255, ColorHelper.getRed(color) + 30);
        int g = Math.min(255, ColorHelper.getGreen(color) + 30);
        int b = Math.min(255, ColorHelper.getBlue(color) + 30);
        return ColorHelper.fromRGBA(r, g, b, ColorHelper.getAlpha(color));
    }

    @Override
    public void onClick(int button) {
        if (button == 0) { // Left click
            // Play click sound
            playClickSound();

            // Priority 1: Simple callback (Runnable)
            if (simpleCallback != null) {
                try {
                    simpleCallback.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            // Priority 2: Element callback (Consumer<ButtonElement>)
            if (clickCallback != null) {
                try {
                    clickCallback.accept(this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            // Priority 3: Action registry (String action ID)
            if (action != null) {
                ActionRegistry.getInstance().executeAction(action, this);
            }
        }
    }

    /**
     * Plays the click sound.
     */
    private void playClickSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
        }
    }

    @Override
    public void onHoverStart() {
        this.hovered = true;
        // Start hover grow animation
        if (animationManager != null) {
            animate(AnimationPresets.hoverGrow(id));
        }
    }

    @Override
    public void onHoverEnd() {
        this.hovered = false;
        // Start hover shrink animation
        if (animationManager != null) {
            animate(AnimationPresets.hoverShrink(id));
        }
    }

    // Callback methods (Fluent API)

    /**
     * Sets a simple callback that runs when the button is clicked.
     * This takes priority over element callbacks and action registry.
     *
     * @param callback The callback to run on click
     * @return This button for method chaining
     */
    public ButtonElement onClick(Runnable callback) {
        this.simpleCallback = callback;
        return this;
    }

    /**
     * Sets a callback with access to the button element.
     * This takes priority over action registry but not simple callbacks.
     *
     * @param callback The callback to run on click
     * @return This button for method chaining
     */
    public ButtonElement onClickCallback(Consumer<ButtonElement> callback) {
        this.clickCallback = callback;
        return this;
    }

    /**
     * Clears all callbacks (simple, element, and action).
     *
     * @return This button for method chaining
     */
    public ButtonElement clearCallbacks() {
        this.simpleCallback = null;
        this.clickCallback = null;
        this.action = null;
        return this;
    }

    // Getters and setters

    public String getText() {
        return text;
    }

    public ButtonElement setText(String text) {
        this.text = text;
        return this;
    }

    public String getAction() {
        return action;
    }

    public ButtonElement setAction(String action) {
        this.action = action;
        return this;
    }

    public int getTextColor() {
        return textColor;
    }

    public ButtonElement setTextColor(int textColor) {
        this.textColor = textColor;
        return this;
    }

    public int getHoverColor() {
        return hoverColor;
    }

    public ButtonElement setHoverColor(int hoverColor) {
        this.hoverColor = hoverColor;
        return this;
    }

    public ButtonElement setWidth(float width) {
        this.width = width;
        return this;
    }

    public ButtonElement setHeight(float height) {
        this.height = height;
        return this;
    }
}

