package fr.perrier.hologramuilib.api;

import org.bukkit.entity.Player;

import java.util.*;

/**
 * Main API entry point for HologramUILib on Spigot servers.
 * Provides fluent API to create and manage holographic menus.
 *
 * Example:
 * <pre>
 * HologramMenuAPI.builder("my_menu")
 *     .forPlayers(player)
 *     .at(location)
 *     .withTitle("§6My Shop")
 *     .addButton("buy", "Buy Item", (p, btn) -> {
 *         // Handle purchase
 *     })
 *     .show();
 * </pre>
 */
public class HologramMenuAPI {

    private static final Map<String, HologramMenu> activeMenus = new HashMap<>();
    private static final Map<UUID, Map<String, Long>> playerMenuTimestamps = new HashMap<>();

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
     * @param player The player viewing the menu
     * @param menuId The menu ID
     * @return The menu, or null if not found
     */
    public static HologramMenu getMenu(Player player, String menuId) {
        return activeMenus.get(menuId);
    }

    /**
     * Checks if a menu exists and is active.
     *
     * @param menuId The menu ID
     * @return true if the menu is active
     */
    public static boolean hasMenu(String menuId) {
        return activeMenus.containsKey(menuId);
    }

    /**
     * Closes (destroys) a menu for a specific player.
     *
     * @param player The player who's closing the menu
     * @param menuId The menu ID to close
     */
    public static void closeMenu(Player player, String menuId) {
        HologramMenu menu = activeMenus.get(menuId);
        if (menu != null) {
            menu.close(player);
            if (menu.getVisiblePlayers().isEmpty()) {
                activeMenus.remove(menuId);
            }
        }
    }

    /**
     * Closes all menus for a specific player.
     *
     * @param player The player who's closing menus
     */
    public static void closeAllMenus(Player player) {
        List<String> menusToClose = new ArrayList<>(activeMenus.keySet());
        for (String menuId : menusToClose) {
            closeMenu(player, menuId);
        }
    }

    /**
     * Gets all active menus.
     *
     * @return An unmodifiable collection of active menus
     */
    public static Collection<HologramMenu> getActiveMenus() {
        return Collections.unmodifiableCollection(activeMenus.values());
    }

    /**
     * Registers a menu as active.
     * Internal use only.
     */
    protected static void registerMenu(String menuId, HologramMenu menu) {
        activeMenus.put(menuId, menu);
    }

    /**
     * Unregisters a menu.
     * Internal use only.
     */
    protected static void unregisterMenu(String menuId) {
        activeMenus.remove(menuId);
    }

    /**
     * Records a menu interaction timestamp.
     * Used to prevent spam and track user activity.
     *
     * @param player The player
     * @param menuId The menu ID
     */
    public static void recordMenuInteraction(Player player, String menuId) {
        playerMenuTimestamps
            .computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
            .put(menuId, System.currentTimeMillis());
    }

    /**
     * Gets the last interaction timestamp for a menu.
     *
     * @param player The player
     * @param menuId The menu ID
     * @return The timestamp, or -1 if never interacted
     */
    public static long getLastMenuInteraction(Player player, String menuId) {
        Map<String, Long> timestamps = playerMenuTimestamps.get(player.getUniqueId());
        return timestamps != null ? timestamps.getOrDefault(menuId, -1L) : -1L;
    }

    /**
     * Clears all menu data for a player (on logout).
     *
     * @param player The player
     */
    public static void clearPlayerData(Player player) {
        playerMenuTimestamps.remove(player.getUniqueId());
    }
}
