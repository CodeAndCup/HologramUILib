package fr.perrier.hologramuilib.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Fluent builder for creating HologramMenu instances.
 */
public class MenuBuilder {

    private final String menuId;
    private final HologramMenu menu;
    private final Set<Player> targetPlayers;
    private Location location;

    protected MenuBuilder(String menuId) {
        this.menuId = menuId;
        this.menu = new HologramMenu(menuId);
        this.targetPlayers = new HashSet<>();

        // Default configuration (mirrors Fabric API)
        this.menu.setWidth(220);
        this.menu.setHeight(-1); // Auto height
        this.menu.setScale(0.02f);
        this.menu.setMaxRenderDistance(20.0);
        this.menu.setBackgroundColor(0xDD000000);
        this.menu.setBorderColor(0xFFFFD700);
        this.menu.setPadding(10);
        this.menu.setSpacing(5);
    }

    /**
     * Sets the position of the menu in world coordinates.
     *
     * @param location The location
     * @return This builder
     */
    public MenuBuilder at(Location location) {
        this.location = location;
        this.menu.setPosition(location);
        return this;
    }

    /**
     * Sets the position relative to the player.
     *
     * @param player The player
     * @param offsetX X offset from player
     * @param offsetY Y offset from player
     * @param offsetZ Z offset from player
     * @return This builder
     */
    public MenuBuilder atPlayer(Player player, double offsetX, double offsetY, double offsetZ) {
        Location playerLoc = player.getLocation();
        Location menuLoc = playerLoc.add(offsetX, offsetY, offsetZ);
        this.location = menuLoc;
        this.menu.setPosition(menuLoc);
        return this;
    }

    /**
     * Sets the position in front of the player.
     *
     * @param player The player
     * @param distance Distance in front of player
     * @return This builder
     */
    public MenuBuilder inFrontOfPlayer(Player player, double distance) {
        Location playerLoc = player.getLocation();
        org.bukkit.util.Vector direction = playerLoc.getDirection().normalize();

        double x = playerLoc.getX() + direction.getX() * distance;
        double y = playerLoc.getY() + direction.getY() * distance;
        double z = playerLoc.getZ() + direction.getZ() * distance;

        Location menuLoc = new Location(player.getWorld(), x, y + 1.5, z, playerLoc.getYaw(), playerLoc.getPitch());
        this.location = menuLoc;
        this.menu.setPosition(menuLoc);
        return this;
    }

    /**
     * Sets which players can see this menu.
     *
     * @param players The players who can see the menu
     * @return This builder
     */
    public MenuBuilder forPlayers(Player... players) {
        this.targetPlayers.addAll(Arrays.asList(players));
        return this;
    }

    /**
     * Sets which players can see this menu.
     *
     * @param players The players who can see the menu
     * @return This builder
     */
    public MenuBuilder forPlayers(Collection<Player> players) {
        this.targetPlayers.addAll(players);
        return this;
    }

    /**
     * Sets the menu title.
     *
     * @param title The title (supports Minecraft color codes)
     * @return This builder
     */
    public MenuBuilder withTitle(String title) {
        this.menu.setTitle(title);
        return this;
    }

    /**
     * Sets the menu width in pixels.
     *
     * @param width The width
     * @return This builder
     */
    public MenuBuilder withWidth(int width) {
        this.menu.setWidth(width);
        return this;
    }

    /**
     * Sets the menu height in pixels.
     * Use -1 for automatic height based on content.
     *
     * @param height The height
     * @return This builder
     */
    public MenuBuilder withHeight(int height) {
        this.menu.setHeight(height);
        return this;
    }

    /**
     * Sets the menu scale.
     *
     * @param scale The scale (0.01 - 0.1)
     * @return This builder
     */
    public MenuBuilder withScale(float scale) {
        this.menu.setScale(scale);
        return this;
    }

    /**
     * Sets the background color.
     *
     * @param hexColor Color in hex format (e.g., "#DD000000")
     * @return This builder
     */
    public MenuBuilder withBackgroundColor(String hexColor) {
        try {
            this.menu.setBackgroundColor(parseHexColor(hexColor));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex color: " + hexColor);
        }
        return this;
    }

    /**
     * Sets whether the menu has a background.
     *
     * @param enabled true to show background
     * @return This builder
     */
    public MenuBuilder withBackground(boolean enabled) {
        this.menu.setBackgroundEnabled(enabled);
        return this;
    }

    /**
     * Sets the border color.
     *
     * @param hexColor Color in hex format (e.g., "#FFFF00")
     * @return This builder
     */
    public MenuBuilder withBorderColor(String hexColor) {
        try {
            this.menu.setBorderColor(parseHexColor(hexColor));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex color: " + hexColor);
        }
        return this;
    }

    /**
     * Sets the maximum render distance in blocks.
     *
     * @param distance The distance
     * @return This builder
     */
    public MenuBuilder withMaxDistance(double distance) {
        this.menu.setMaxRenderDistance(distance);
        return this;
    }

    /**
     * Sets a condition that determines if the menu should be visible.
     * The predicate receives the player and should return true if menu is visible.
     *
     * @param condition The visibility condition
     * @return This builder
     */
    public MenuBuilder withCondition(Predicate<Player> condition) {
        this.menu.setVisibilityCondition(condition);
        return this;
    }

    /**
     * Adds a text element.
     *
     * @param id Unique element ID
     * @param content The text content
     * @return This builder
     */
    public MenuBuilder addText(String id, String content) {
        this.menu.addElement(id, new MenuElement("text", id, content));
        return this;
    }

    /**
     * Adds a button element.
     *
     * @param id Unique element ID
     * @param label The button label
     * @param onClick Callback when clicked
     * @return This builder
     */
    public MenuBuilder addButton(String id, String label, BiConsumer<Player, ButtonClickEvent> onClick) {
        MenuElement button = new MenuElement("button", id, label);
        button.setClickCallback(onClick);
        this.menu.addElement(id, button);
        return this;
    }

    /**
     * Adds a slider element.
     *
     * @param id Unique element ID
     * @param min Minimum value
     * @param max Maximum value
     * @param value Current value
     * @param onChange Callback when value changes
     * @return This builder
     */
    public MenuBuilder addSlider(String id, double min, double max, double value,
                                 BiConsumer<Player, SliderChangeEvent> onChange) {
        MenuElement slider = new MenuElement("slider", id, "");
        slider.setMinValue(min);
        slider.setMaxValue(max);
        slider.setValue(value);
        slider.setChangeCallback(onChange);
        this.menu.addElement(id, slider);
        return this;
    }

    /**
     * Adds a progress bar element.
     *
     * @param id Unique element ID
     * @param value Current value
     * @param max Maximum value
     * @return This builder
     */
    public MenuBuilder addProgressBar(String id, double value, double max) {
        MenuElement progressBar = new MenuElement("progress_bar", id, "");
        progressBar.setValue(value);
        progressBar.setMaxValue(max);
        this.menu.addElement(id, progressBar);
        return this;
    }

    /**
     * Adds an image element from resources.
     *
     * @param id Unique element ID
     * @param resourcePath Resource path
     * @param width Image width
     * @param height Image height
     * @return This builder
     */
    public MenuBuilder addImage(String id, String resourcePath, int width, int height) {
        MenuElement image = new MenuElement("image", id, resourcePath);
        image.setWidth(width);
        image.setHeight(height);
        this.menu.addElement(id, image);
        return this;
    }

    /**
     * Adds an image element from URL.
     *
     * @param id Unique element ID
     * @param url Image URL
     * @param width Image width
     * @param height Image height
     * @return This builder
     */
    public MenuBuilder addImageURL(String id, String url, int width, int height) {
        MenuElement image = new MenuElement("image_url", id, url);
        image.setWidth(width);
        image.setHeight(height);
        this.menu.addElement(id, image);
        return this;
    }

    /**
     * Adds a separator element.
     *
     * @return This builder
     */
    public MenuBuilder addSeparator() {
        this.menu.addElement("separator_" + UUID.randomUUID(), 
                           new MenuElement("separator", UUID.randomUUID().toString(), ""));
        return this;
    }

    /**
     * Adds a separator element with custom ID.
     *
     * @param id Unique element ID
     * @return This builder
     */
    public MenuBuilder addSeparator(String id) {
        this.menu.addElement(id, new MenuElement("separator", id, ""));
        return this;
    }

    /**
     * Adds vertical spacing.
     *
     * @param height Height in pixels
     * @return This builder
     */
    public MenuBuilder addSpacing(int height) {
        this.menu.addElement("spacing_" + UUID.randomUUID(),
                           new MenuElement("spacing", UUID.randomUUID().toString(), "")
                               .setHeight(height));
        return this;
    }

    /**
     * Adds a container element with nested elements.
     *
     * @param id Unique element ID
     * @param elements Consumer to build container elements
     * @return This builder
     */
    public MenuBuilder addContainer(String id, Consumer<ContainerBuilder> elements) {
        ContainerBuilder containerBuilder = new ContainerBuilder();
        elements.accept(containerBuilder);
        MenuElement container = new MenuElement("container", id, "");
        container.setChildren(containerBuilder.getElements());
        this.menu.addElement(id, container);
        return this;
    }

    /**
     * Shows the menu to all target players.
     */
    public void show() {
        if (this.targetPlayers.isEmpty()) {
            throw new IllegalStateException("No players specified! Use forPlayers() to set target players.");
        }
        this.menu.show(this.targetPlayers);
        HologramMenuAPI.registerMenu(this.menuId, this.menu);
    }

    /**
     * Shows the menu to specific players.
     *
     * @param players The players to show the menu to
     */
    public void show(Player... players) {
        this.menu.show(Arrays.asList(players));
        HologramMenuAPI.registerMenu(this.menuId, this.menu);
    }

    /**
     * Parses a hex color string to an integer.
     * Supports formats: #RRGGBB and #RRGGBBAA
     */
    private static int parseHexColor(String hexColor) {
        String hex = hexColor.replace("#", "");
        if (hex.length() == 6) {
            hex = "FF" + hex; // Add full alpha
        }
        return (int) Long.parseLong(hex, 16);
    }

    /**
     * Builder for container elements.
     */
    public static class ContainerBuilder {
        private final Map<String, MenuElement> elements = new LinkedHashMap<>();

        public ContainerBuilder addText(String id, String content) {
            elements.put(id, new MenuElement("text", id, content));
            return this;
        }

        public ContainerBuilder addButton(String id, String label, BiConsumer<Player, ButtonClickEvent> onClick) {
            MenuElement button = new MenuElement("button", id, label);
            button.setClickCallback(onClick);
            elements.put(id, button);
            return this;
        }

        public ContainerBuilder addSeparator() {
            elements.put("sep_" + UUID.randomUUID(), new MenuElement("separator", UUID.randomUUID().toString(), ""));
            return this;
        }

        protected Map<String, MenuElement> getElements() {
            return elements;
        }
    }
}
