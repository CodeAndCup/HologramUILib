package fr.perrier.hologramuilib.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

/**
 * Generic event fired when any menu element value changes.
 */
public class MenuElementChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String menuId;
    private final String elementId;
    private final String elementType;
    private final Object oldValue;
    private final Object newValue;

    public MenuElementChangeEvent(Player player, String menuId, String elementId, String elementType,
                                   Object oldValue, Object newValue) {
        this.player = player;
        this.menuId = menuId;
        this.elementId = elementId;
        this.elementType = elementType;
        this.oldValue = oldValue;
        this.newValue = newValue;
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

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
