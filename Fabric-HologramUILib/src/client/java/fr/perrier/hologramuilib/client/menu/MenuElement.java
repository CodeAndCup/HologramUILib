package fr.perrier.hologramuilib.client.menu;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Interface for all menu elements (buttons, text, separators, etc.)
 * Each element must implement rendering, hit testing, and interaction handling.
 */
public interface MenuElement {

    /**
     * Renders this element.
     *
     * @param matrices The transformation matrix stack
     * @param textRenderer The text renderer for drawing text
     * @param vertexConsumers Vertex consumers for drawing
     * @param x The x position to render at
     * @param y The y position to render at
     * @param hovered Whether this element is currently hovered
     * @param tickDelta Partial tick for smooth animations
     */
    void render(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers,
                float x, float y, boolean hovered, float tickDelta);

    /**
     * Tests if a point is inside this element's bounds.
     *
     * @param x The x coordinate to test
     * @param y The y coordinate to test
     * @return true if the point is inside this element
     */
    boolean isPointInside(float x, float y);

    /**
     * Gets the bounds of this element.
     *
     * @return The element's bounds
     */
    Bounds getBounds();

    /**
     * Sets the bounds of this element.
     *
     * @param bounds The new bounds
     */
    void setBounds(Bounds bounds);

    /**
     * Called when this element is clicked.
     *
     * @param button The mouse button (0 = left, 1 = right, 2 = middle)
     */
    void onClick(int button);

    /**
     * Called when the mouse starts hovering over this element.
     */
    void onHoverStart();

    /**
     * Called when the mouse stops hovering over this element.
     */
    void onHoverEnd();

    /**
     * Gets the unique ID of this element.
     *
     * @return The element ID, or null if not set
     */
    String getId();

    /**
     * Gets the width of this element.
     *
     * @return The width in pixels
     */
    float getWidth();

    /**
     * Gets the height of this element.
     *
     * @return The height in pixels
     */
    float getHeight();

    /**
     * Record representing the bounds of a menu element.
     */
    record Bounds(float x, float y, float width, float height) {
        public boolean contains(float px, float py) {
            return px >= x && px <= x + width && py >= y && py <= y + height;
        }
    }
}

