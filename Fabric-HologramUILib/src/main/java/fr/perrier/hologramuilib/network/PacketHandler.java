package fr.perrier.hologramuilib.network;

import fr.perrier.hologramuilib.common.network.MenuInteractionPacket;
import fr.perrier.hologramuilib.network.packets.MenuClickPacket;
import fr.perrier.hologramuilib.network.packets.MenuClosePacket;
import fr.perrier.hologramuilib.network.packets.MenuDataPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles network packet registration and sending for menu communication (server-side).
 */
public class PacketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/Network");

    /**
     * Registers all custom packets for server-side networking.
     * Should be called during server initialization.
     */
    public static void registerServerPackets() {
        // Register packet types
        PayloadTypeRegistry.playC2S().register(MenuClickPacket.ID, MenuClickPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MenuDataPacket.ID, MenuDataPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MenuClosePacket.ID, MenuClosePacket.CODEC);

        // Register MenuInteractionPacket (bidirectional)
        PayloadTypeRegistry.playC2S().register(MenuInteractionPacket.ID, MenuInteractionPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MenuInteractionPacket.ID, MenuInteractionPacket.CODEC);

        // Register packet receivers
        ServerPlayNetworking.registerGlobalReceiver(MenuClickPacket.ID, (payload, context) ->
            context.server().execute(() -> handleMenuClickPacket(payload, context.player()))
        );


        LOGGER.info("Server packets registered");
    }

    /**
     * Sends a menu data packet from server to client.
     */
    public static void sendMenuData(ServerPlayerEntity player, MenuDataPacket packet) {
        if (!ServerPlayNetworking.canSend(player, MenuDataPacket.ID)) {
            LOGGER.warn("Cannot send MenuDataPacket to player {} - not connected or packet not registered",
                       player.getName().getString());
            return;
        }

        ServerPlayNetworking.send(player, packet);
        LOGGER.debug("Sent menu data to player {}: menu={}",
                    player.getName().getString(), packet.menuId());
    }

    /**
     * Sends a menu close packet from server to client.
     */
    public static void sendMenuClose(ServerPlayerEntity player, String menuId, boolean playSound) {
        if (!ServerPlayNetworking.canSend(player, MenuClosePacket.ID)) {
            LOGGER.warn("Cannot send MenuClosePacket to player {} - not connected or packet not registered",
                       player.getName().getString());
            return;
        }

        MenuClosePacket packet = new MenuClosePacket(menuId, playSound);
        ServerPlayNetworking.send(player, packet);
        LOGGER.debug("Sent menu close to player {}: menu={}",
                    player.getName().getString(), menuId);
    }

    /**
     * Handles incoming menu click packets on the server.
     */
    private static void handleMenuClickPacket(MenuClickPacket packet, ServerPlayerEntity player) {
        try {
            LOGGER.info("Received menu click from {}: menu={}, element={}, button={}",
                       player.getName().getString(), packet.menuId(), packet.elementId(), packet.button());

            // TODO: Validate and process the click action
            // This will be implemented when we add server-side menu logic

        } catch (Exception e) {
            LOGGER.error("Error handling menu click packet from " + player.getName().getString(), e);
        }
    }
}
