package fr.perrier.hologramuilib.api;

import fr.perrier.hologramuilib.HologramUILibPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Represents a hologram menu visible to players.
 */
public class HologramMenu {

    private final String menuId;
    private final Map<String, MenuElement> elements;
    private final Set<Player> visiblePlayers;

    private Location position;
    private String title;
    private int width;
    private int height;
    private float scale;
    private double maxRenderDistance;
    private int backgroundColor;
    private int borderColor;
    private int padding;
    private int spacing;
    private boolean backgroundEnabled;
    private Predicate<Player> visibilityCondition;

    public HologramMenu(String menuId) {
        this.menuId = menuId;
        this.elements = new LinkedHashMap<>();
        this.visiblePlayers = new HashSet<>();
        this.visibilityCondition = player -> true; // Always visible by default
    }

    /**
     * Adds an element to this menu.
     */
    protected void addElement(String id, MenuElement element) {
        this.elements.put(id, element);
    }

    /**
     * Gets an element by ID.
     */
    public MenuElement getElement(String id) {
        return this.elements.get(id);
    }

    /**
     * Gets all elements.
     */
    public Map<String, MenuElement> getElements() {
        return Collections.unmodifiableMap(this.elements);
    }

    /**
     * Shows this menu to players.
     */
    protected void show(Collection<Player> players) {
        List<Player> playersToShow = new ArrayList<>();

        for (Player player : players) {
            if (visibilityCondition.test(player)) {
                this.visiblePlayers.add(player);
                playersToShow.add(player);
                MenuEventRegistry.fireMenuOpen(new MenuOpenEvent(player, this.menuId));
            }
        }

        // Send menu to clients via NetworkManager
        if (!playersToShow.isEmpty()) {
            HologramUILibPlugin plugin = HologramUILibPlugin.getInstance();
            if (plugin != null && plugin.getNetworkManager() != null) {
                plugin.getNetworkManager().sendMenu(this, playersToShow);
            }
        }
    }

    /**
     * Closes this menu for a player.
     */
    protected void close(Player player) {
        if (this.visiblePlayers.remove(player)) {
            MenuEventRegistry.fireMenuClose(new MenuCloseEvent(player, this.menuId));

            // Send close signal to client
            HologramUILibPlugin plugin = HologramUILibPlugin.getInstance();
            if (plugin != null && plugin.getNetworkManager() != null) {
                plugin.getNetworkManager().closeMenu(this.menuId, Collections.singletonList(player));
            }
        }
    }

    /**
     * Closes this menu for all players.
     */
    public void closeAll() {
        List<Player> players = new ArrayList<>(this.visiblePlayers);
        for (Player player : players) {
            close(player);
        }
    }

    /**
     * Gets all players who can see this menu.
     */
    public Set<Player> getVisiblePlayers() {
        return Collections.unmodifiableSet(this.visiblePlayers);
    }

    /**
     * Checks if a player can see this menu.
     */
    public boolean canSee(Player player) {
        return this.visiblePlayers.contains(player) && visibilityCondition.test(player);
    }

    // Getters and Setters

    public String getMenuId() {
        return menuId;
    }

    public Location getPosition() {
        return position;
    }

    protected void setPosition(Location position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    public int getWidth() {
        return width;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public float getScale() {
        return scale;
    }

    protected void setScale(float scale) {
        this.scale = scale;
    }

    public double getMaxRenderDistance() {
        return maxRenderDistance;
    }

    protected void setMaxRenderDistance(double maxRenderDistance) {
        this.maxRenderDistance = maxRenderDistance;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    protected void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getBorderColor() {
        return borderColor;
    }

    protected void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public int getPadding() {
        return padding;
    }

    protected void setPadding(int padding) {
        this.padding = padding;
    }

    public int getSpacing() {
        return spacing;
    }

    protected void setSpacing(int spacing) {
        this.spacing = spacing;
    }

    public boolean isBackgroundEnabled() {
        return backgroundEnabled;
    }

    protected void setBackgroundEnabled(boolean backgroundEnabled) {
        this.backgroundEnabled = backgroundEnabled;
    }

    public Predicate<Player> getVisibilityCondition() {
        return visibilityCondition;
    }

    protected void setVisibilityCondition(Predicate<Player> visibilityCondition) {
        this.visibilityCondition = visibilityCondition;
    }
}
