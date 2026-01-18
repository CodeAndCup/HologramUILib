package fr.perrier.hologramuilib.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

/**
 * Fired when a player changes the value of a slider element.
 */
public class SliderChangeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String menuId;
    private final String elementId;
    private final double oldValue;
    private final double newValue;

    public SliderChangeEvent(Player player, String menuId, String elementId, double oldValue, double newValue) {
        this.player = player;
        this.menuId = menuId;
        this.elementId = elementId;
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

    public double getOldValue() {
        return oldValue;
    }

    public double getNewValue() {
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
