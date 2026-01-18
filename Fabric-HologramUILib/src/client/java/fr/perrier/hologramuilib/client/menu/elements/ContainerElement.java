package fr.perrier.hologramuilib.client.menu.elements;

import fr.perrier.hologramuilib.client.config.ItemConfig;
import fr.perrier.hologramuilib.client.config.LayoutConfig;
import fr.perrier.hologramuilib.client.menu.MenuElement;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Container element that can hold and layout multiple child elements.
 * Supports vertical and horizontal layouts.
 */
public class ContainerElement extends AbstractMenuElement {

    private final List<MenuElement> children;
    private LayoutConfig.LayoutType layoutType;
    private float padding;
    private float spacing;
    private LayoutConfig.Alignment alignment;
    private int backgroundColor;
    private boolean hasBackground;
    private int borderColor;
    private boolean hasBorder;
    private float borderWidth;

    public ContainerElement(String id) {
        super(id);
        this.children = new ArrayList<>();
        this.layoutType = LayoutConfig.LayoutType.VERTICAL;
        this.padding = 4;
        this.spacing = 2;
        this.alignment = LayoutConfig.Alignment.LEFT;
        this.backgroundColor = 0x80000000;
        this.hasBackground = false;
        this.borderColor = 0xFF888888;
        this.hasBorder = false;
        this.borderWidth = 1;
        this.width = 100;
        this.height = 50;
    }

    /**
     * Creates a container element from configuration.
     */
    public static ContainerElement fromConfig(ItemConfig config) {
        ContainerElement container = new ContainerElement(config.getId());

        ItemConfig.ItemStyleConfig style = config.getStyle();
        if (style != null && style.getHeight() > 0) {
            container.height = style.getHeight();
        }

        return container;
    }

    @Override
    public void render(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers,
                       float x, float y, boolean hovered, float tickDelta) {
        this.bounds = new Bounds(x, y, width, height);

        matrices.push();
        matrices.translate(x, y, 0);

        // Render background if enabled
        if (hasBackground) {
            renderBackground(matrices, vertexConsumers, backgroundColor);
        }

        // Render border if enabled
        if (hasBorder) {
            renderBorder(matrices, vertexConsumers, borderColor, borderWidth);
        }

        // Render children
        matrices.push();
        matrices.translate(padding, padding, 0.01f);
        renderChildren(matrices, textRenderer, vertexConsumers, tickDelta);
        matrices.pop();

        matrices.pop();
    }

    /**
     * Renders all child elements with proper layout.
     */
    private void renderChildren(MatrixStack matrices, TextRenderer textRenderer,
                               VertexConsumerProvider vertexConsumers, float tickDelta) {
        if (children.isEmpty()) {
            return;
        }

        float currentX = 0;
        float currentY = 0;

        for (MenuElement child : children) {
            // Calculate position based on layout type and alignment
            float elementX = currentX;
            float elementY = currentY;

            if (layoutType == LayoutConfig.LayoutType.VERTICAL) {
                elementX = calculateAlignmentX(child);
            } else if (layoutType == LayoutConfig.LayoutType.HORIZONTAL) {
                // For horizontal layout, elements flow left to right
                // TODO: Implement horizontal alignment (top, center, bottom)
            }

            // Render the child element
            // Note: Container children are never individually hovered (container handles that)
            child.render(matrices, textRenderer, vertexConsumers, elementX, elementY, false, tickDelta);

            // Update position for next element
            if (layoutType == LayoutConfig.LayoutType.VERTICAL) {
                currentY += child.getHeight() + spacing;
            } else if (layoutType == LayoutConfig.LayoutType.HORIZONTAL) {
                currentX += child.getWidth() + spacing;
            }
        }
    }

    /**
     * Calculates the X position for an element based on alignment.
     */
    private float calculateAlignmentX(MenuElement element) {
        float availableWidth = width - (padding * 2);

        return switch (alignment) {
            case CENTER -> (availableWidth - element.getWidth()) / 2;
            case RIGHT -> availableWidth - element.getWidth();
            default -> 0; // LEFT
        };
    }

    /**
     * Renders the background.
     */
    private void renderBackground(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());

        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, height, 0).color(r, g, b, a);
        buffer.vertex(matrix, width, height, 0).color(r, g, b, a);

        buffer.vertex(matrix, width, height, 0).color(r, g, b, a);
        buffer.vertex(matrix, width, 0, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a);
    }

    /**
     * Renders the border.
     */
    private void renderBorder(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                             int color, float lineWidth) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(lineWidth));

        buffer.vertex(matrix, 0, 0, 0.01f).color(r, g, b, a);
        buffer.vertex(matrix, width, 0, 0.01f).color(r, g, b, a);
        buffer.vertex(matrix, width, height, 0.01f).color(r, g, b, a);
        buffer.vertex(matrix, 0, height, 0.01f).color(r, g, b, a);
        buffer.vertex(matrix, 0, 0, 0.01f).color(r, g, b, a);
    }

    /**
     * Adds a child element to this container.
     */
    public void addChild(MenuElement element) {
        children.add(element);
        recalculateSize();
    }

    /**
     * Removes a child element from this container.
     */
    public void removeChild(MenuElement element) {
        children.remove(element);
        recalculateSize();
    }

    /**
     * Clears all child elements.
     */
    public void clearChildren() {
        children.clear();
    }

    /**
     * Gets all child elements.
     */
    public List<MenuElement> getChildren() {
        return children;
    }

    /**
     * Recalculates container size based on children.
     */
    public void recalculateSize() {
        if (children.isEmpty()) {
            return;
        }

        if (layoutType == LayoutConfig.LayoutType.VERTICAL) {
            // Calculate total height
            float totalHeight = padding * 2;
            float maxWidth = 0;

            for (int i = 0; i < children.size(); i++) {
                MenuElement child = children.get(i);
                totalHeight += child.getHeight();
                maxWidth = Math.max(maxWidth, child.getWidth());

                if (i < children.size() - 1) {
                    totalHeight += spacing;
                }
            }

            this.height = totalHeight;
            // Optionally adjust width too
            // this.width = maxWidth + padding * 2;

        } else if (layoutType == LayoutConfig.LayoutType.HORIZONTAL) {
            // Calculate total width
            float totalWidth = padding * 2;
            float maxHeight = 0;

            for (int i = 0; i < children.size(); i++) {
                MenuElement child = children.get(i);
                totalWidth += child.getWidth();
                maxHeight = Math.max(maxHeight, child.getHeight());

                if (i < children.size() - 1) {
                    totalWidth += spacing;
                }
            }

            this.width = totalWidth;
            // Optionally adjust height too
            // this.height = maxHeight + padding * 2;
        }
    }

    // Getters and setters

    public LayoutConfig.LayoutType getLayoutType() {
        return layoutType;
    }

    public void setLayoutType(LayoutConfig.LayoutType layoutType) {
        this.layoutType = layoutType;
        recalculateSize();
    }

    public float getPadding() {
        return padding;
    }

    public void setPadding(float padding) {
        this.padding = padding;
        recalculateSize();
    }

    public float getSpacing() {
        return spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
        recalculateSize();
    }

    public LayoutConfig.Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(LayoutConfig.Alignment alignment) {
        this.alignment = alignment;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isHasBackground() {
        return hasBackground;
    }

    public void setHasBackground(boolean hasBackground) {
        this.hasBackground = hasBackground;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public boolean isHasBorder() {
        return hasBorder;
    }

    public void setHasBorder(boolean hasBorder) {
        this.hasBorder = hasBorder;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }
}

