package fr.perrier.hologramuilib;

import fr.perrier.hologramuilib.api.HologramMenuAPI;
import fr.perrier.hologramuilib.commands.HologramCommand;
import fr.perrier.hologramuilib.examples.APIExamples;
import fr.perrier.hologramuilib.network.NetworkManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Main plugin class for Spigot-HologramUILib.
 * This is the entry point for the plugin system.
 */
public class HologramUILibPlugin extends JavaPlugin implements Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/Spigot");
    private static HologramUILibPlugin instance;

    private NetworkManager networkManager;

    @Override
    public void onEnable() {
        instance = this;

        LOGGER.info("║════════════════════════════════════════════════════════════════════║");
        LOGGER.info("║          HologramUILib Spigot API - v{}", this.getDescription().getVersion());
        LOGGER.info("║════════════════════════════════════════════════════════════════════║");

        // Create default config
        this.saveDefaultConfig();

        // Initialize network manager for packet communication
        this.networkManager = new NetworkManager(this);
        this.networkManager.initialize();

        // Register events
        this.getServer().getPluginManager().registerEvents(this, this);

        LOGGER.info("✓ HologramUILib Spigot API enabled successfully");
        LOGGER.info("✓ Ready for menu creation and management");

        // Load commands
        Objects.requireNonNull(this.getCommand("hologramenu")).setExecutor(new HologramCommand());
        LOGGER.info("✓ Commands registered: /hologramenu");

    }

    @Override
    public void onDisable() {
        LOGGER.info("║════════════════════════════════════════════════════════════════════║");
        LOGGER.info("║          HologramUILib Spigot API disabled");
        LOGGER.info("║════════════════════════════════════════════════════════════════════║");

        // Close all menus
        for (Player player : Bukkit.getOnlinePlayers()) {
            HologramMenuAPI.closeAllMenus(player);
        }

        // Cleanup
        if (this.networkManager != null) {
            this.networkManager.shutdown();
        }

        LOGGER.info("✓ HologramUILib Spigot API disabled");
    }

    /**
     * Handle player logout - cleanup menu data.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        HologramMenuAPI.clearPlayerData(event.getPlayer());
    }

    /**
     * Gets the plugin instance.
     */
    public static HologramUILibPlugin getInstance() {
        return instance;
    }

    /**
     * Gets the network manager.
     */
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    /**
     * Gets the logger for the plugin.
     */
    public static Logger getPluginLogger() {
        return LOGGER;
    }
}
