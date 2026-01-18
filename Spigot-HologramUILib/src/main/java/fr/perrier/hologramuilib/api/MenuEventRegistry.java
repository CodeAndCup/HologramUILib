package fr.perrier.hologramuilib.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

/**
 * Registry for menu events.
 * Handles dispatching of all menu-related events.
 */
public class MenuEventRegistry {

    private static final PluginManager pluginManager = Bukkit.getPluginManager();

    /**
     * Fires a MenuOpenEvent.
     */
    public static void fireMenuOpen(MenuOpenEvent event) {
        pluginManager.callEvent(event);
    }

    /**
     * Fires a MenuCloseEvent.
     */
    public static void fireMenuClose(MenuCloseEvent event) {
        pluginManager.callEvent(event);
    }

    /**
     * Fires a ButtonClickEvent.
     */
    public static void fireButtonClick(ButtonClickEvent event) {
        pluginManager.callEvent(event);
    }

    /**
     * Fires a SliderChangeEvent.
     */
    public static void fireSliderChange(SliderChangeEvent event) {
        pluginManager.callEvent(event);
    }

    /**
     * Fires a MenuElementClickEvent (generic).
     */
    public static void fireMenuElementClick(MenuElementClickEvent event) {
        pluginManager.callEvent(event);
    }

    /**
     * Fires a MenuElementChangeEvent (generic).
     */
    public static void fireMenuElementChange(MenuElementChangeEvent event) {
        pluginManager.callEvent(event);
    }
}
