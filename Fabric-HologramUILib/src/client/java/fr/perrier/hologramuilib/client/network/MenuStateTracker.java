package fr.perrier.hologramuilib.client.network;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which players are currently interacting with hologram menus.
 * This allows us to render animations/overlays for players in menus.
 */
public class MenuStateTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/MenuState");
    private static MenuStateTracker instance;

    private final Map<UUID, PlayerMenuState> playerStates = new ConcurrentHashMap<>();

    private MenuStateTracker() {}

    public static MenuStateTracker getInstance() {
        if (instance == null) {
            instance = new MenuStateTracker();
        }
        return instance;
    }

    /**
     * Marks a player as being in a menu.
     */
    public void setPlayerInMenu(UUID playerId, boolean inMenu) {
        if (inMenu) {
            playerStates.put(playerId, new PlayerMenuState(System.currentTimeMillis()));
            LOGGER.debug("Player {} is now in a menu", playerId);
        } else {
            playerStates.remove(playerId);
            LOGGER.debug("Player {} left menu", playerId);
        }
    }

    /**
     * Checks if a player is currently in a menu.
     */
    public boolean isPlayerInMenu(UUID playerId) {
        return playerStates.containsKey(playerId);
    }

    /**
     * Checks if a player entity is in a menu.
     */
    public boolean isPlayerInMenu(AbstractClientPlayerEntity player) {
        return isPlayerInMenu(player.getUuid());
    }

    /**
     * Gets the menu state for a player.
     */
    public PlayerMenuState getMenuState(UUID playerId) {
        return playerStates.get(playerId);
    }

    /**
     * Gets how long a player has been in the menu (in milliseconds).
     */
    public long getTimeInMenu(UUID playerId) {
        PlayerMenuState state = playerStates.get(playerId);
        if (state == null) return 0;
        return System.currentTimeMillis() - state.startTime;
    }

    /**
     * Clears all tracked states (useful for world changes).
     */
    public void clearAll() {
        playerStates.clear();
    }

    /**
     * Represents the state of a player in a menu.
     */
    public static class PlayerMenuState {
        private final long startTime;

        public PlayerMenuState(long startTime) {
            this.startTime = startTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getDuration() {
            return System.currentTimeMillis() - startTime;
        }
    }
}
