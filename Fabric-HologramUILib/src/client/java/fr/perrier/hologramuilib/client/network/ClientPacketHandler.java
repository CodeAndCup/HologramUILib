package fr.perrier.hologramuilib.client.network;

import fr.perrier.hologramuilib.client.menu.MenuManager;
import fr.perrier.hologramuilib.network.packets.MenuClickPacket;
import fr.perrier.hologramuilib.network.packets.MenuClosePacket;
import fr.perrier.hologramuilib.network.packets.MenuDataPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles network packet registration and sending for menu communication (client-side).
 */
public class ClientPacketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/Network/Client");

    /**
     * Registers all custom packets for client-side networking.
     * Should be called during client initialization.
     */
    public static void registerClientPackets() {
        // Register packet receivers (S2C - server to client)
        ClientPlayNetworking.registerGlobalReceiver(MenuDataPacket.ID, (payload, context) ->
            context.client().execute(() -> handleMenuDataPacket(payload, context.client()))
        );

        ClientPlayNetworking.registerGlobalReceiver(MenuClosePacket.ID, (payload, context) ->
            context.client().execute(() -> handleMenuClosePacket(payload, context.client()))
        );


        LOGGER.info("Client packets registered successfully");
    }

    /**
     * Sends a menu click packet from client to server.
     */
    public static void sendMenuClick(String menuId, String elementId, int button) {
        if (!ClientPlayNetworking.canSend(MenuClickPacket.ID)) {
            LOGGER.warn("Cannot send MenuClickPacket - not connected or packet not registered");
            return;
        }

        MenuClickPacket packet = new MenuClickPacket(
            menuId,
            elementId,
            button,
            System.currentTimeMillis()
        );

        ClientPlayNetworking.send(packet);
        LOGGER.debug("Sent menu click: menu={}, element={}, button={}", menuId, elementId, button);
    }

    /**
     * Handles incoming menu data packets on the client.
     */
    private static void handleMenuDataPacket(MenuDataPacket packet, MinecraftClient client) {
        try {
            LOGGER.info("Received menu data: menu={}, position={}",
                       packet.menuId(), packet.position());

            // TODO: Parse configJson and create/update menu
            // For now, just log the received data
            MenuManager manager = MenuManager.getInstance();

            // Create or update the menu
            if (manager.hasMenu(packet.menuId())) {
                // Update existing menu
                var menu = manager.getMenu(packet.menuId());
                if (menu != null) {
                    menu.setPosition(packet.position());
                    menu.setYaw(packet.yaw());
                }
            } else {
                // Create new menu
                var menu = manager.createMenu(packet.menuId(), packet.position());
                menu.setYaw(packet.yaw());
                // TODO: Parse and apply configJson when config system is ready
            }

        } catch (Exception e) {
            LOGGER.error("Error handling menu data packet", e);
        }
    }

    /**
     * Handles incoming menu close packets on the client.
     */
    private static void handleMenuClosePacket(MenuClosePacket packet, MinecraftClient client) {
        try {
            LOGGER.info("Received menu close: menu={}, playSound={}",
                       packet.menuId(), packet.playSound());

            MenuManager manager = MenuManager.getInstance();
            manager.destroyMenu(packet.menuId());

            // Play sound if requested
            if (packet.playSound() && client.player != null) {
                client.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1.0f);
            }

        } catch (Exception e) {
            LOGGER.error("Error handling menu close packet", e);
        }
    }
}
