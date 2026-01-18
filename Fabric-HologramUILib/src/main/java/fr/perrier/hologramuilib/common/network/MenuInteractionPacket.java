package fr.perrier.hologramuilib.common.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Packet sent from client to server to notify that the player is interacting with a hologram menu.
 * This allows other clients to see visual feedback (overlay, particles, etc.) when a player is in a menu.
 */
public record MenuInteractionPacket(java.util.UUID playerUuid, boolean inMenu) implements CustomPayload {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/Network");
    public static final CustomPayload.Id<MenuInteractionPacket> ID = new CustomPayload.Id<>(Identifier.of("hologramuilib", "menu_interaction"));
    public static final PacketCodec<PacketByteBuf, MenuInteractionPacket> CODEC = PacketCodec.of(
        MenuInteractionPacket::write,
        MenuInteractionPacket::read
    );

    private static MenuInteractionPacket read(PacketByteBuf buf) {
        return new MenuInteractionPacket(buf.readUuid(), buf.readBoolean());
    }

    private void write(PacketByteBuf buf) {
        buf.writeUuid(playerUuid);
        buf.writeBoolean(inMenu);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Registers the packet handler on the server side.
     */
    public static void registerServerHandler() {
        ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            boolean inMenu = payload.inMenu();

            context.server().execute(() -> {
                // Broadcast to all nearby players with the player UUID
                broadcastToNearbyPlayers(player, inMenu);
            });
        });

        LOGGER.info("Menu interaction packet handler registered");
    }

    /**
     * Broadcasts the menu interaction state to all nearby players.
     */
    private static void broadcastToNearbyPlayers(ServerPlayerEntity player, boolean inMenu) {
        // Create packet with player UUID
        MenuInteractionPacket packet = new MenuInteractionPacket(player.getUuid(), inMenu);

        // Get all players in the same world within render distance
        player.getServerWorld().getPlayers().forEach(otherPlayer -> {
            if (otherPlayer != player && otherPlayer.squaredDistanceTo(player) < 4096) { // 64 blocks
                // Send packet to other player
                ServerPlayNetworking.send(otherPlayer, packet);
            }
        });

        LOGGER.debug("Player {} menu state: {} - broadcasted to nearby players",
            player.getName().getString(), inMenu);
    }
}
