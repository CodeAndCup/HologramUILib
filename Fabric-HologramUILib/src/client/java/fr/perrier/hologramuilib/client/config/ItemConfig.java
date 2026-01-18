package fr.perrier.hologramuilib.client.config;

import com.google.gson.JsonObject;
import fr.perrier.hologramuilib.util.ColorHelper;

/**
 * Configuration for a single menu element/item.
 * Supports various element types like buttons, text, separators, etc.
 */
public class ItemConfig {

    private String type;
    private String id;
    private String text;
    private String content;
    private IconConfig icon;
    private ItemStyleConfig style;
    private String action;
    private float height = -1; // -1 means auto
    private String color;
    private boolean centered = false;

    // Raw JSON for extensibility
    private JsonObject rawJson;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public IconConfig getIcon() {
        return icon;
    }

    public void setIcon(IconConfig icon) {
        this.icon = icon;
    }

    public ItemStyleConfig getStyle() {
        return style;
    }

    public void setStyle(ItemStyleConfig style) {
        this.style = style;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getColor() {
        return color;
    }

    public int getColorInt() {
        return color != null ? ColorHelper.parseColor(color) : 0xFFFFFFFF;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isCentered() {
        return centered;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    public JsonObject getRawJson() {
        return rawJson;
    }

    public void setRawJson(JsonObject rawJson) {
        this.rawJson = rawJson;
    }

    /**
     * Icon configuration for buttons and other elements.
     */
    public static class IconConfig {
        private String type; // "item" or "texture"
        private String item; // minecraft:ender_pearl
        private String texture; // custom texture path

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getItem() {
            return item;
        }

        public void setItem(String item) {
            this.item = item;
        }

        public String getTexture() {
            return texture;
        }

        public void setTexture(String texture) {
            this.texture = texture;
        }
    }

    /**
     * Style configuration for individual items.
     */
    public static class ItemStyleConfig {
        private float height = 20;
        private String textColor = "#FFFFFF";
        private String hoverColor = "#40FFFFFF";
        private String clickSound = "ui.button.click";

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public String getTextColor() {
            return textColor;
        }

        public int getTextColorInt() {
            return ColorHelper.parseColor(textColor);
        }

        public void setTextColor(String textColor) {
            this.textColor = textColor;
        }

        public String getHoverColor() {
            return hoverColor;
        }

        public int getHoverColorInt() {
            return ColorHelper.parseColor(hoverColor);
        }

        public void setHoverColor(String hoverColor) {
            this.hoverColor = hoverColor;
        }

        public String getClickSound() {
            return clickSound;
        }

        public void setClickSound(String clickSound) {
            this.clickSound = clickSound;
        }
    }
}

