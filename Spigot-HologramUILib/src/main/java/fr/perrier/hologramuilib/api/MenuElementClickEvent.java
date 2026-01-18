package fr.perrier.hologramuilib.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

/**
 * Generic event fired when any menu element is clicked.
 */
public class MenuElementClickEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String menuId;
    private final String elementId;
    private final String elementType;

    public MenuElementClickEvent(Player player, String menuId, String elementId, String elementType) {
        this.player = player;
        this.menuId = menuId;
        this.elementId = elementId;
        this.elementType = elementType;
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

    public String getElementType() {
        return elementType;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
