package fr.perrier.hologramuilib.network.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

/**
 * Packet sent from server to client to create/update a menu.
 * Contains menu configuration and position data.
 */
public record MenuDataPacket(
    String menuId,
    Vec3d position,
    float yaw,
    String configJson,
    boolean replace
) implements CustomPayload {

    public static final Id<MenuDataPacket> ID = new Id<>(
        Identifier.of("hologramuilib", "menu_data")
    );

    public static final PacketCodec<PacketByteBuf, MenuDataPacket> CODEC = PacketCodec.of(
        MenuDataPacket::write,
        MenuDataPacket::read
    );

    /**
     * Writes the packet data to the buffer.
     */
    public void write(PacketByteBuf buf) {
        buf.writeString(menuId);
        buf.writeDouble(position.x);
        buf.writeDouble(position.y);
        buf.writeDouble(position.z);
        buf.writeFloat(yaw);
        buf.writeString(configJson);
        buf.writeBoolean(replace);
    }

    /**
     * Reads the packet data from the buffer.
     */
    public static MenuDataPacket read(PacketByteBuf buf) {
        String menuId = buf.readString();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        Vec3d position = new Vec3d(x, y, z);
        float yaw = buf.readFloat();
        String configJson = buf.readString();
        boolean replace = buf.readBoolean();
        return new MenuDataPacket(menuId, position, yaw, configJson, replace);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
