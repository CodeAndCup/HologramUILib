package fr.perrier.hologramuilib.client.config;

import fr.perrier.hologramuilib.util.ColorHelper;

/**
 * Style configuration for hologram menus.
 * Contains visual properties like dimensions, colors, background, and border.
 */
public class StyleConfig {

    private float width = 200;
    private Object height = 100f; // Can be float or "auto"
    private float scale = 0.025f;
    private double maxRenderDistance = 10.0;
    private BackgroundConfig background;
    private BorderConfig border;

    public StyleConfig() {
        this.background = new BackgroundConfig();
        this.border = new BorderConfig();
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        if (height instanceof Number) {
            return ((Number) height).floatValue();
        }
        // "auto" height - return -1 to indicate auto-calculation
        return -1;
    }

    public boolean isAutoHeight() {
        return height instanceof String && "auto".equalsIgnoreCase((String) height);
    }

    public void setHeight(Object height) {
        this.height = height;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public double getMaxRenderDistance() {
        return maxRenderDistance;
    }

    public void setMaxRenderDistance(double maxRenderDistance) {
        this.maxRenderDistance = maxRenderDistance;
    }

    public BackgroundConfig getBackground() {
        return background;
    }

    public void setBackground(BackgroundConfig background) {
        this.background = background;
    }

    public BorderConfig getBorder() {
        return border;
    }

    public void setBorder(BorderConfig border) {
        this.border = border;
    }

    /**
     * Background configuration for menus.
     */
    public static class BackgroundConfig {
        private boolean enabled = true;
        private String color = "#80000000";
        private int blur = 0;
        private int borderRadius = 4;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getColor() {
            return color;
        }

        public int getColorInt() {
            return ColorHelper.parseColor(color);
        }

        public void setColor(String color) {
            this.color = color;
        }

        public int getBlur() {
            return blur;
        }

        public void setBlur(int blur) {
            this.blur = blur;
        }

        public int getBorderRadius() {
            return borderRadius;
        }

        public void setBorderRadius(int borderRadius) {
            this.borderRadius = borderRadius;
        }
    }

    /**
     * Border configuration for menus.
     */
    public static class BorderConfig {
        private boolean enabled = true;
        private String color = "#FFFFFFFF";
        private float width = 2;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getColor() {
            return color;
        }

        public int getColorInt() {
            return ColorHelper.parseColor(color);
        }

        public void setColor(String color) {
            this.color = color;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }
    }
}

