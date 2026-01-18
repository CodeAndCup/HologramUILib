package fr.perrier.hologramuilib;

import fr.perrier.hologramuilib.common.network.MenuInteractionPacket;
import fr.perrier.hologramuilib.network.PacketHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main implements ModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib");

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing HologramUILib...");

        // Register network packets
        PacketHandler.registerServerPackets();

        // Register menu interaction packet handler
        MenuInteractionPacket.registerServerHandler();

        LOGGER.info("HologramUILib initialized");
    }
}
