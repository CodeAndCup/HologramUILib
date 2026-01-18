package fr.perrier.hologramuilib.client.config;

/**
 * Layout configuration for hologram menus.
 * Defines how menu elements are arranged.
 */
public class LayoutConfig {

    private LayoutType type = LayoutType.VERTICAL;
    private float padding = 8;
    private float spacing = 4;
    private Alignment alignment = Alignment.CENTER;

    public LayoutType getType() {
        return type;
    }

    public void setType(LayoutType type) {
        this.type = type;
    }

    public float getPadding() {
        return padding;
    }

    public void setPadding(float padding) {
        this.padding = padding;
    }

    public float getSpacing() {
        return spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    /**
     * Layout types for arranging menu elements.
     */
    public enum LayoutType {
        VERTICAL,
        HORIZONTAL,
        GRID
    }

    /**
     * Alignment options for menu elements.
     */
    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }
}

