package fr.perrier.hologramuilib.network.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Packet sent from client to server when a menu element is clicked.
 */
public record MenuClickPacket(
    String menuId,
    String elementId,
    int button,
    long timestamp
) implements CustomPayload {

    public static final Id<MenuClickPacket> ID = new Id<>(
        Identifier.of("hologramuilib", "menu_click")
    );

    public static final PacketCodec<PacketByteBuf, MenuClickPacket> CODEC = PacketCodec.of(
        MenuClickPacket::write,
        MenuClickPacket::read
    );

    /**
     * Writes the packet data to the buffer.
     */
    public void write(PacketByteBuf buf) {
        buf.writeString(menuId);
        buf.writeString(elementId);
        buf.writeInt(button);
        buf.writeLong(timestamp);
    }

    /**
     * Reads the packet data from the buffer.
     */
    public static MenuClickPacket read(PacketByteBuf buf) {
        String menuId = buf.readString();
        String elementId = buf.readString();
        int button = buf.readInt();
        long timestamp = buf.readLong();
        return new MenuClickPacket(menuId, elementId, button, timestamp);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
