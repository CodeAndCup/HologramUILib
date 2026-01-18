package fr.perrier.hologramuilib.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

/**
 * Fired when a player clicks on a button element in a menu.
 */
public class ButtonClickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String menuId;
    private final String elementId;
    private final String label;
    private int button; // 0 = left, 1 = right, 2 = middle

    public ButtonClickEvent(Player player, String menuId, String elementId, String label, int button) {
        this.player = player;
        this.menuId = menuId;
        this.elementId = elementId;
        this.label = label;
        this.button = button;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMenuId() {
        return menuId;
    }

    public String getElementId() {
        return elementId;
    }

    public String getLabel() {
        return label;
    }

    public int getButton() {
        return button;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
