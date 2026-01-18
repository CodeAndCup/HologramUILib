package fr.perrier.hologramuilib.api;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Represents a UI element in a HologramMenu.
 * Supports various types: button, text, slider, image, etc.
 */
public class MenuElement {

    private final String type;
    private final String id;
    private String content;

    private int width;
    private int height;
    private double minValue;
    private double maxValue;
    private double value;

    private BiConsumer<org.bukkit.entity.Player, ButtonClickEvent> clickCallback;
    private BiConsumer<org.bukkit.entity.Player, SliderChangeEvent> changeCallback;
    private Map<String, MenuElement> children;

    public MenuElement(String type, String id, String content) {
        this.type = type;
        this.id = id;
        this.content = content;
        this.children = new LinkedHashMap<>();
    }

    // Getters and Setters

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWidth() {
        return width;
    }

    public MenuElement setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public MenuElement setHeight(int height) {
        this.height = height;
        return this;
    }

    public double getMinValue() {
        return minValue;
    }

    public MenuElement setMinValue(double minValue) {
        this.minValue = minValue;
        return this;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public MenuElement setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public double getValue() {
        return value;
    }

    public MenuElement setValue(double value) {
        this.value = value;
        return this;
    }

    public BiConsumer<org.bukkit.entity.Player, ButtonClickEvent> getClickCallback() {
        return clickCallback;
    }

    public MenuElement setClickCallback(BiConsumer<org.bukkit.entity.Player, ButtonClickEvent> clickCallback) {
        this.clickCallback = clickCallback;
        return this;
    }

    public BiConsumer<org.bukkit.entity.Player, SliderChangeEvent> getChangeCallback() {
        return changeCallback;
    }

    public MenuElement setChangeCallback(BiConsumer<org.bukkit.entity.Player, SliderChangeEvent> changeCallback) {
        this.changeCallback = changeCallback;
        return this;
    }

    public Map<String, MenuElement> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    public MenuElement setChildren(Map<String, MenuElement> children) {
        this.children = new LinkedHashMap<>(children);
        return this;
    }

    /**
     * Converts this element to a JSON representation for sending to the mod.
     */
    public String toJson() {
        com.google.gson.JsonObject json = new com.google.gson.JsonObject();
        json.addProperty("id", id);
        json.addProperty("type", type);

        if (content != null && !content.isEmpty()) {
            json.addProperty("content", content);
        }

        if (width > 0) json.addProperty("width", width);
        if (height > 0) json.addProperty("height", height);
        if (minValue > 0) json.addProperty("minValue", minValue);
        if (maxValue > 0) json.addProperty("maxValue", maxValue);
        if (value > 0) json.addProperty("value", value);

        if (!children.isEmpty()) {
            com.google.gson.JsonArray childrenArray = new com.google.gson.JsonArray();
            for (MenuElement child : children.values()) {
                childrenArray.add(com.google.gson.JsonParser.parseString(child.toJson()));
            }
            json.add("children", childrenArray);
        }

        return json.toString();
    }
}
