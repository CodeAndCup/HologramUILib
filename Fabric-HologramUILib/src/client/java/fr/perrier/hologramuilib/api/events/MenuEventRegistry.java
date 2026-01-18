package fr.perrier.hologramuilib.api.events;

import fr.perrier.hologramuilib.client.menu.HologramMenu;
import fr.perrier.hologramuilib.client.menu.MenuElement;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Event system for hologram menu interactions.
 * Allows mods to register callbacks for various menu events.
 */
public class MenuEventRegistry {

    private static MenuEventRegistry INSTANCE;

    private final Map<String, List<Consumer<MenuEvent>>> menuOpenListeners = new HashMap<>();
    private final Map<String, List<Consumer<MenuEvent>>> menuCloseListeners = new HashMap<>();
    private final Map<String, List<BiConsumer<MenuElement, PlayerEntity>>> elementClickListeners = new HashMap<>();

    private MenuEventRegistry() {
    }

    public static MenuEventRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MenuEventRegistry();
        }
        return INSTANCE;
    }

    /**
     * Registers a listener for when a menu is opened.
     *
     * @param menuId The menu ID to listen for (or "*" for all menus)
     * @param listener The listener callback
     */
    public void onMenuOpen(String menuId, Consumer<MenuEvent> listener) {
        menuOpenListeners.computeIfAbsent(menuId, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Registers a listener for when a menu is closed.
     *
     * @param menuId The menu ID to listen for (or "*" for all menus)
     * @param listener The listener callback
     */
    public void onMenuClose(String menuId, Consumer<MenuEvent> listener) {
        menuCloseListeners.computeIfAbsent(menuId, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Registers a listener for when a menu element is clicked.
     *
     * @param elementId The element ID to listen for (format: "menuId:elementId" or "*" for all)
     * @param listener The listener callback
     */
    public void onElementClick(String elementId, BiConsumer<MenuElement, PlayerEntity> listener) {
        elementClickListeners.computeIfAbsent(elementId, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Triggers menu open event.
     */
    public void triggerMenuOpen(HologramMenu menu) {
        MenuEvent event = new MenuEvent(menu);

        // Trigger specific menu listeners
        List<Consumer<MenuEvent>> listeners = menuOpenListeners.get(menu.getId());
        if (listeners != null) {
            listeners.forEach(listener -> listener.accept(event));
        }

        // Trigger wildcard listeners
        List<Consumer<MenuEvent>> wildcardListeners = menuOpenListeners.get("*");
        if (wildcardListeners != null) {
            wildcardListeners.forEach(listener -> listener.accept(event));
        }
    }

    /**
     * Triggers menu close event.
     */
    public void triggerMenuClose(HologramMenu menu) {
        MenuEvent event = new MenuEvent(menu);

        // Trigger specific menu listeners
        List<Consumer<MenuEvent>> listeners = menuCloseListeners.get(menu.getId());
        if (listeners != null) {
            listeners.forEach(listener -> listener.accept(event));
        }

        // Trigger wildcard listeners
        List<Consumer<MenuEvent>> wildcardListeners = menuCloseListeners.get("*");
        if (wildcardListeners != null) {
            wildcardListeners.forEach(listener -> listener.accept(event));
        }
    }

    /**
     * Triggers element click event.
     */
    public void triggerElementClick(MenuElement element, PlayerEntity player) {
        String fullId = element.getId();

        // Trigger specific element listeners
        List<BiConsumer<MenuElement, PlayerEntity>> listeners = elementClickListeners.get(fullId);
        if (listeners != null) {
            listeners.forEach(listener -> listener.accept(element, player));
        }

        // Trigger wildcard listeners
        List<BiConsumer<MenuElement, PlayerEntity>> wildcardListeners = elementClickListeners.get("*");
        if (wildcardListeners != null) {
            wildcardListeners.forEach(listener -> listener.accept(element, player));
        }
    }

    /**
     * Clears all listeners.
     */
    public void clearAllListeners() {
        menuOpenListeners.clear();
        menuCloseListeners.clear();
        elementClickListeners.clear();
    }

    /**
     * Event object for menu events.
     */
    public static class MenuEvent {
        private final HologramMenu menu;
        private boolean cancelled = false;

        public MenuEvent(HologramMenu menu) {
            this.menu = menu;
        }

        public HologramMenu getMenu() {
            return menu;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
