package fr.perrier.hologramuilib.client.menu;

import fr.perrier.hologramuilib.client.animation.AnimationManager;
import fr.perrier.hologramuilib.client.interaction.MenuInteractionTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active hologram menus.
 * Handles creation, retrieval, and lifecycle management of menus.
 *
 * Features:
 * - Menu creation and tracking
 * - Template caching for performance
 * - Display conditions (distance, line of sight, etc.)
 * - Lifecycle management (create, update, destroy)
 * - Multi-menu support
 */
public class MenuManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/MenuManager");
    private static MenuManager INSTANCE;

    private final Map<String, HologramMenu> menus;
    private final Map<String, MenuConditions> menuConditions;
    private final AnimationManager animationManager;
    private final MenuCache menuCache;

    // Statistics
    private long menusCreated = 0;
    private long menusDestroyed = 0;

    private MenuManager() {
        this.menus = new ConcurrentHashMap<>();
        this.menuConditions = new ConcurrentHashMap<>();
        this.animationManager = new AnimationManager();
        this.menuCache = new MenuCache();
    }

    public static MenuManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MenuManager();
        }
        return INSTANCE;
    }

    /**
     * Gets the animation manager for this menu manager.
     */
    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    /**
     * Gets the menu cache.
     */
    public MenuCache getMenuCache() {
        return menuCache;
    }

    /**
     * Ticks the menu manager, updating animations and cleaning up expired cache.
     */
    public void tick() {
        animationManager.tick();

        // Periodically cleanup expired cache entries (every 5 seconds)
        if (System.currentTimeMillis() % 5000 < 50) {
            menuCache.cleanupExpired();
        }
    }

    /**
     * Updates menu visibility based on conditions.
     * Should be called each frame for distance-based culling.
     */
    public void updateMenuVisibility(Vec3d playerPos) {
        menus.forEach((id, menu) -> {
            MenuConditions conditions = menuConditions.get(id);
            if (conditions != null) {
                boolean shouldBeVisible = conditions.checkConditions(playerPos, menu.getPosition());
                menu.setVisible(shouldBeVisible);
            }
        });
    }

    /**
     * Creates a new menu with the given ID.
     *
     * @param id Unique identifier for the menu
     * @return The created menu
     */
    public HologramMenu createMenu(String id) {
        HologramMenu menu = new HologramMenu(id);
        menu.setAnimationManager(animationManager);
        menus.put(id, menu);
        menusCreated++;
        LOGGER.debug("Created menu: {}", id);
        return menu;
    }

    /**
     * Creates a new menu at the specified position.
     *
     * @param id Unique identifier for the menu
     * @param position The position in world space
     * @return The created menu
     */
    public HologramMenu createMenu(String id, Vec3d position) {
        HologramMenu menu = createMenu(id);
        menu.setPosition(position);

        // IMPORTANT: Démarrer le tracking IMMÉDIATEMENT pour éviter les clics rapides
        // Si on attend le prochain tick, le joueur peut casser des blocs en cliquant rapidement
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Vec3d playerPos = client.player.getPos();
            Vec3d menuPos = position;
            double distance = playerPos.distanceTo(menuPos);

            // Si le menu est proche et devant le joueur, démarrer le tracking immédiatement
            if (distance < 10.0) {
                MenuInteractionTracker.getInstance().startInteraction();
                LOGGER.debug("Started interaction tracking immediately for menu: {}", id);
            }
        }

        return menu;
    }

    /**
     * Creates a new menu with display conditions.
     */
    public HologramMenu createMenu(String id, Vec3d position, MenuConditions conditions) {
        HologramMenu menu = createMenu(id, position);
        menuConditions.put(id, conditions);
        return menu;
    }

    /**
     * Creates a menu from a cached template.
     */
    public HologramMenu createMenuFromTemplate(String menuId, String templateId, Vec3d position) {
        return menuCache.getTemplate(templateId)
            .map(template -> {
                HologramMenu menu = createMenu(menuId, position);
                // TODO: Apply template configuration
                LOGGER.debug("Created menu from template: {} -> {}", templateId, menuId);
                return menu;
            })
            .orElseGet(() -> {
                LOGGER.warn("Template not found: {}", templateId);
                return createMenu(menuId, position);
            });
    }

    /**
     * Registers an existing menu.
     *
     * @param menu The menu to register
     */
    public void registerMenu(HologramMenu menu) {
        menu.setAnimationManager(animationManager);
        menus.put(menu.getId(), menu);
        menusCreated++;
        LOGGER.debug("Registered menu: {}", menu.getId());
    }

    /**
     * Gets a menu by its ID.
     *
     * @param id The menu ID
     * @return The menu, or null if not found
     */
    public HologramMenu getMenu(String id) {
        return menus.get(id);
    }

    /**
     * Checks if a menu with the given ID exists.
     *
     * @param id The menu ID
     * @return true if the menu exists
     */
    public boolean hasMenu(String id) {
        return menus.containsKey(id);
    }

    /**
     * Removes a menu by its ID.
     *
     * @param id The menu ID
     * @return The removed menu, or null if not found
     */
    public HologramMenu removeMenu(String id) {
        MenuConditions conditions = menuConditions.remove(id);
        HologramMenu menu = menus.remove(id);
        if (menu != null) {
            menusDestroyed++;
            LOGGER.debug("Removed menu: {}", id);
        }
        return menu;
    }

    /**
     * Destroys a menu and cleans up its resources.
     */
    public void destroyMenu(String id) {
        HologramMenu menu = removeMenu(id);
        if (menu != null) {
            // Cancel all animations for this menu's elements
            menu.getElements().forEach(element -> {
                if (element.getId() != null) {
                    animationManager.cancelAll(element.getId());
                }
            });
            menu.clearElements();
        }
    }

    /**
     * Gets all active menus.
     *
     * @return Collection of all active menus
     */
    public Collection<HologramMenu> getActiveMenus() {
        return menus.values();
    }

    /**
     * Gets only visible menus (based on conditions).
     */
    public Collection<HologramMenu> getVisibleMenus() {
        return menus.values().stream()
            .filter(HologramMenu::isVisible)
            .toList();
    }

    /**
     * Gets the number of active menus.
     *
     * @return The menu count
     */
    public int getMenuCount() {
        return menus.size();
    }

    /**
     * Removes all menus and clears all animations.
     */
    public void clearAllMenus() {
        menus.keySet().forEach(this::destroyMenu);
        menus.clear();
        menuConditions.clear();
        animationManager.clear();
        LOGGER.info("Cleared all menus");
    }

    /**
     * Gets all menu IDs.
     *
     * @return Collection of all menu IDs
     */
    public Collection<String> getMenuIds() {
        return menus.keySet();
    }

    /**
     * Sets display conditions for a menu.
     */
    public void setMenuConditions(String menuId, MenuConditions conditions) {
        menuConditions.put(menuId, conditions);
    }

    /**
     * Gets display conditions for a menu.
     */
    public MenuConditions getMenuConditions(String menuId) {
        return menuConditions.get(menuId);
    }

    /**
     * Gets manager statistics.
     */
    public String getStatistics() {
        return String.format(
            "MenuManager Stats: Active=%d, Created=%d, Destroyed=%d, Cached=%d",
            menus.size(), menusCreated, menusDestroyed, menuCache.size()
        );
    }

    /**
     * Logs current statistics.
     */
    public void logStatistics() {
        LOGGER.info(getStatistics());
    }
}

