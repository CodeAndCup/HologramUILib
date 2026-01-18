package fr.perrier.hologramuilib.client.interaction;

import fr.perrier.hologramuilib.client.config.InteractionConfig;
import fr.perrier.hologramuilib.common.network.MenuInteractionPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks players who are currently interacting with hologram menus.
 * When a player is interacting with a menu, their world interactions
 * (breaking blocks, attacking entities) are suppressed to prevent accidents.
 */
public class MenuInteractionTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/InteractionTracker");
    private static MenuInteractionTracker INSTANCE;

    private final Set<UUID> playersInMenu = new HashSet<>();
    private long lastInteractionTime = 0;

    private MenuInteractionTracker() {
    }

    public static MenuInteractionTracker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MenuInteractionTracker();
        }
        return INSTANCE;
    }

    /**
     * Marks the client player as interacting with a menu.
     */
    public void startInteraction() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            playersInMenu.add(client.player.getUuid());
            lastInteractionTime = System.currentTimeMillis();
            LOGGER.info("🔒 Player {} started menu interaction - world interactions SUPPRESSED", client.player.getName().getString());

            // Send packet to server to notify other clients
            if (ClientPlayNetworking.canSend(MenuInteractionPacket.ID)) {
                ClientPlayNetworking.send(new MenuInteractionPacket(client.player.getUuid(), true));
            }
        }
    }

    /**
     * Marks the client player as no longer interacting with a menu.
     */
    public void endInteraction() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            playersInMenu.remove(client.player.getUuid());
            LOGGER.info("🔓 Player {} ended menu interaction - world interactions RESTORED", client.player.getName().getString());

            // Send packet to server to notify other clients
            if (ClientPlayNetworking.canSend(MenuInteractionPacket.ID)) {
                ClientPlayNetworking.send(new MenuInteractionPacket(client.player.getUuid(), false));
            }
        }
    }

    /**
     * Checks if the client player is currently interacting with a menu.
     */
    public boolean isInteracting() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return false;
        }
        return playersInMenu.contains(client.player.getUuid());
    }

    /**
     * Checks if world interactions should be suppressed.
     * This includes breaking blocks and attacking entities.
     */
    public boolean shouldSuppressWorldInteraction() {
        InteractionConfig config = InteractionConfig.getInstance();
        if (!config.isSuppressWorldInteractions()) {
            return false;
        }
        return isInteracting();
    }

    /**
     * Checks if a specific type of interaction should be suppressed.
     */
    public boolean shouldSuppressInteraction(InteractionType type) {
        InteractionConfig config = InteractionConfig.getInstance();

        if (!config.isSuppressWorldInteractions()) {
            return false;
        }

        if (!isInteracting()) {
            return false;
        }

        // PAS DE COOLDOWN - bloquer tant qu'on regarde un menu
        // Le cooldown causait des problèmes car 100-300ms c'est trop court

        return switch (type) {
            case ATTACK_ENTITY -> config.isSuppressEntityAttacking();
            case BREAK_BLOCK -> config.isSuppressBlockBreaking();
            case USE_ITEM -> false; // Allow using items (like food)
            case USE_BLOCK -> config.isSuppressBlockUsage();
        };
    }

    /**
     * Updates the last interaction time.
     * Should be called when the player clicks on a menu element.
     */
    public void updateInteractionTime() {
        this.lastInteractionTime = System.currentTimeMillis();
    }

    /**
     * Clears all tracked interactions.
     * Should be called when disconnecting from a server.
     */
    public void clear() {
        playersInMenu.clear();
        lastInteractionTime = 0;
        LOGGER.debug("Cleared all menu interactions");
    }

    /**
     * Types of interactions that can be suppressed.
     */
    public enum InteractionType {
        ATTACK_ENTITY,
        BREAK_BLOCK,
        USE_ITEM,
        USE_BLOCK
    }
}
