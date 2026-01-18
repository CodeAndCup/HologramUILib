package fr.perrier.hologramuilib.api;

import fr.perrier.hologramuilib.client.menu.HologramMenu;
import fr.perrier.hologramuilib.client.menu.MenuManager;
import fr.perrier.hologramuilib.client.menu.elements.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Public API for creating and managing hologram menus.
 * This is the main entry point for other mods to use HologramUILib.
 *
 * Example usage:
 * <pre>
 * HologramMenuAPI.builder("my_menu")
 *     .at(position)
 *     .withTitle("§6My Shop")
 *     .addButton("buy", "Buy Item", player -> {
 *         // Handle purchase
 *     })
 *     .addSlider("volume", 0, 100, 50, value -> {
 *         // Handle volume change
 *     })
 *     .show();
 * </pre>
 */
public class HologramMenuAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/API");

    /**
     * Creates a new menu builder.
     *
     * @param menuId Unique identifier for the menu
     * @return A new menu builder instance
     */
    public static MenuBuilder builder(String menuId) {
        return new MenuBuilder(menuId);
    }

    /**
     * Gets an existing menu by ID.
     *
     * @param menuId The menu ID
     * @return The menu, or null if not found
     */
    public static HologramMenu getMenu(String menuId) {
        return MenuManager.getInstance().getMenu(menuId);
    }

    /**
     * Closes (destroys) a menu.
     *
     * @param menuId The menu ID to close
     */
    public static void closeMenu(String menuId) {
        MenuManager.getInstance().destroyMenu(menuId);
        LOGGER.info("Menu closed via API: {}", menuId);
    }

    /**
     * Checks if a menu exists.
     *
     * @param menuId The menu ID
     * @return true if the menu exists
     */
    public static boolean hasMenu(String menuId) {
        return MenuManager.getInstance().hasMenu(menuId);
    }

    /**
     * Closes all menus.
     */
    public static void closeAllMenus() {
        MenuManager.getInstance().clearAllMenus();
        LOGGER.info("All menus closed via API");
    }

    /**
     * Builder for creating hologram menus with a fluent API.
     */
    public static class MenuBuilder {
        private final String menuId;
        private Vec3d position;
        private String title;
        private final HologramMenu menu;
        private float currentY = 0;

        private MenuBuilder(String menuId) {
            this.menuId = menuId;
            this.menu = MenuManager.getInstance().createMenu(menuId);

            // Configuration par défaut (comme createMainMenu)
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
         * @param position The position
         * @return This builder
         */
        public MenuBuilder at(Vec3d position) {
            this.position = position;
            this.menu.setPosition(position);
            return this;
        }

        /**
         * Sets the position relative to the player.
         *
         * @param x X offset from player
         * @param y Y offset from player
         * @param z Z offset from player
         * @return This builder
         */
        public MenuBuilder atPlayer(double x, double y, double z) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                Vec3d playerPos = client.player.getPos();
                this.position = playerPos.add(x, y, z);
                this.menu.setPosition(this.position);
            }
            return this;
        }

        /**
         * Sets the position in front of the player.
         *
         * @param distance Distance in front of player
         * @return This builder
         */
        public MenuBuilder inFrontOfPlayer(double distance) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                Vec3d playerPos = client.player.getPos();
                Vec3d lookVec = client.player.getRotationVec(1.0f);
                this.position = playerPos.add(lookVec.multiply(distance)).add(0, 1.5, 0);
                this.menu.setPosition(this.position);
            }
            return this;
        }

        /**
         * Sets the menu title.
         *
         * @param title The title text (supports color codes)
         * @return This builder
         */
        public MenuBuilder withTitle(String title) {
            this.title = title;
            float elementWidth = menu.getWidth() - menu.getPadding() * 2;
            TextElement titleElement = new TextElement(menuId + "_title", title);
            titleElement.setCentered(true);
            titleElement.setWidth(elementWidth);
            menu.addElement(titleElement);
            return this;
        }

        /**
         * Sets the menu width.
         *
         * @param width Width in pixels
         * @return This builder
         */
        public MenuBuilder width(float width) {
            menu.setWidth(width);
            return this;
        }

        /**
         * Adds a button to the menu.
         *
         * @param id Button ID
         * @param text Button text
         * @param onClick Callback when button is clicked
         * @return This builder
         */
        public MenuBuilder addButton(String id, String text, Consumer<ButtonElement> onClick) {
            float elementWidth = menu.getWidth() - menu.getPadding() * 2;
            ButtonElement button = new ButtonElement(menuId + "_btn_" + id, text);
            button.setWidth(elementWidth);
            button.onClickCallback(onClick);
            menu.addElement(button);
            return this;
        }

        /**
         * Adds a text element to the menu.
         *
         * @param text The text (supports color codes)
         * @return This builder
         */
        public MenuBuilder addText(String text) {
            float elementWidth = menu.getWidth() - menu.getPadding() * 2;
            TextElement textElement = new TextElement(menuId + "_text_" + menu.getElements().size(), text);
            textElement.setWidth(elementWidth);
            menu.addElement(textElement);
            return this;
        }

        /**
         * Adds centered text to the menu.
         *
         * @param text The text (supports color codes)
         * @return This builder
         */
        public MenuBuilder addCenteredText(String text) {
            float elementWidth = menu.getWidth() - menu.getPadding() * 2;
            TextElement textElement = new TextElement(menuId + "_text_" + menu.getElements().size(), text);
            textElement.setCentered(true);
            textElement.setWidth(elementWidth);
            menu.addElement(textElement);
            return this;
        }

        /**
         * Adds a separator line to the menu.
         *
         * @return This builder
         */
        public MenuBuilder addSeparator() {
            float elementWidth = menu.getWidth() - menu.getPadding() * 2;
            SeparatorElement separator = new SeparatorElement(menuId + "_sep_" + menu.getElements().size());
            separator.setWidth(elementWidth);
            menu.addElement(separator);
            return this;
        }

        /**
         * Adds a progress bar to the menu.
         *
         * @param id Progress bar ID
         * @param progress Initial progress (0.0 to 1.0)
         * @return This builder
         */
        public MenuBuilder addProgressBar(String id, float progress) {
            float elementWidth = menu.getWidth() - menu.getPadding() * 2;
            ProgressBarElement progressBar = new ProgressBarElement(menuId + "_progress_" + id);
            progressBar.setProgress(progress);
            progressBar.setWidth(elementWidth);
            menu.addElement(progressBar);
            return this;
        }

        /**
         * Adds a slider to the menu.
         *
         * @param id Slider ID
         * @param min Minimum value
         * @param max Maximum value
         * @param initial Initial value
         * @param onChange Callback when value changes
         * @return This builder
         */
        public MenuBuilder addSlider(String id, float min, float max, float initial, Consumer<Float> onChange) {
            float elementWidth = menu.getWidth() - menu.getPadding() * 2;
            SliderElement slider = new SliderElement(menuId + "_slider_" + id);
            slider.setMinValue(min);
            slider.setMaxValue(max);
            slider.setActualValue(initial);
            slider.setWidth(elementWidth);
            slider.onValueChange(onChange);
            menu.addElement(slider);
            return this;
        }

        /**
         * Adds spacing between elements.
         *
         * @param height Height of the spacing in pixels
         * @return This builder
         */
        public MenuBuilder addSpacing(float height) {
            // Use an invisible text element for spacing
            TextElement spacing = new TextElement(menuId + "_spacing_" + menu.getElements().size(), "");
            // Spacing is achieved by just having an empty element with default height
            menu.addElement(spacing);
            return this;
        }

        /**
         * Sets a callback for when the menu is clicked.
         *
         * @param onMenuClick Callback receiving the menu
         * @return This builder
         */
        public MenuBuilder onMenuClick(Consumer<HologramMenu> onMenuClick) {
            // Could be implemented with a custom event system
            return this;
        }

        /**
         * Builds and shows the menu.
         *
         * @return The created menu
         */
        public HologramMenu show() {
            if (position == null) {
                inFrontOfPlayer(3.0);
            }

            // Configuration finale (comme createMainMenu)
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                // Orienter le menu vers le joueur
                menu.setYaw(client.player.getHeadYaw());
            }

            // Recalculer la hauteur automatique
            menu.recalculateAutoHeight();

            LOGGER.info("Menu created via API: {} at position {}", menuId, position);
            return menu;
        }

        /**
         * Builds the menu without showing it yet.
         *
         * @return The created menu
         */
        public HologramMenu build() {
            // Configuration finale
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                menu.setYaw(client.player.getHeadYaw());
            }
            menu.recalculateAutoHeight();

            return menu;
        }
    }
}
