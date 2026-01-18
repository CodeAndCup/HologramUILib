package fr.perrier.hologramuilib.network.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Packet sent from server to client to close a specific menu.
 */
public record MenuClosePacket(
    String menuId,
    boolean playSound
) implements CustomPayload {

    public static final Id<MenuClosePacket> ID = new Id<>(
        Identifier.of("hologramuilib", "menu_close")
    );

    public static final PacketCodec<PacketByteBuf, MenuClosePacket> CODEC = PacketCodec.of(
        MenuClosePacket::write,
        MenuClosePacket::read
    );

    /**
     * Writes the packet data to the buffer.
     */
    public void write(PacketByteBuf buf) {
        buf.writeString(menuId);
        buf.writeBoolean(playSound);
    }

    /**
     * Reads the packet data from the buffer.
     */
    public static MenuClosePacket read(PacketByteBuf buf) {
        String menuId = buf.readString();
        boolean playSound = buf.readBoolean();
        return new MenuClosePacket(menuId, playSound);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
