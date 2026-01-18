package fr.perrier.hologramuilib.network;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles network operations for individual players.
 */
public class PlayerNetworkHandler {

    private final Player player;
    private final JavaPlugin plugin;
    private boolean hasModInstalled;

    public PlayerNetworkHandler(Player player, JavaPlugin plugin) {
        this.player = player;
        this.plugin = plugin;
        this.hasModInstalled = false;
    }

    /**
     * Checks if the player has the mod installed.
     */
    public boolean hasModInstalled() {
        return hasModInstalled;
    }

    /**
     * Sets whether the player has the mod installed.
     */
    public void setModInstalled(boolean installed) {
        this.hasModInstalled = installed;
    }

    public Player getPlayer() {
        return player;
    }
}
