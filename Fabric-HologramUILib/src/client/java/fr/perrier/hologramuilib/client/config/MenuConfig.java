package fr.perrier.hologramuilib.client.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a single hologram menu.
 * Contains style, layout, and items definitions.
 */
public class MenuConfig {

    private StyleConfig style;
    private LayoutConfig layout;
    private List<ItemConfig> items;

    public MenuConfig() {
        this.style = new StyleConfig();
        this.layout = new LayoutConfig();
        this.items = new ArrayList<>();
    }

    public StyleConfig getStyle() {
        return style;
    }

    public void setStyle(StyleConfig style) {
        this.style = style;
    }

    public LayoutConfig getLayout() {
        return layout;
    }

    public void setLayout(LayoutConfig layout) {
        this.layout = layout;
    }

    public List<ItemConfig> getItems() {
        return items;
    }

    public void setItems(List<ItemConfig> items) {
        this.items = items;
    }

    public void addItem(ItemConfig item) {
        this.items.add(item);
    }
}

