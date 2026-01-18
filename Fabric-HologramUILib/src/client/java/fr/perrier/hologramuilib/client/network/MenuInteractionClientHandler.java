package fr.perrier.hologramuilib.client.network;

import fr.perrier.hologramuilib.common.network.MenuInteractionPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Handles incoming menu interaction packets on the client side.
 */
public class MenuInteractionClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/ClientNetwork");

    /**
     * Registers the client-side packet receiver.
     */
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(MenuInteractionPacket.ID, (payload, context) -> {
            UUID playerUuid = payload.playerUuid();
            boolean inMenu = payload.inMenu();

            context.client().execute(() -> {
                // Update the menu state tracker
                MenuStateTracker.getInstance().setPlayerInMenu(playerUuid, inMenu);
                LOGGER.debug("Player {} menu state updated: {}", playerUuid, inMenu);
            });
        });

        LOGGER.info("Menu interaction client handler registered");
    }

    // Remove the old handleMenuInteraction method as it's no longer needed
}
