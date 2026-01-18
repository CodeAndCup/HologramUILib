package fr.perrier.hologramuilib.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.entity.Player;

/**
 * Fired when a player closes a hologram menu.
 */
public class MenuCloseEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final String menuId;

    public MenuCloseEvent(Player player, String menuId) {
        this.player = player;
        this.menuId = menuId;
    }

    public Player getPlayer() {
        return player;
    }

    public String getMenuId() {
        return menuId;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
