package fr.perrier.hologramuilib.client;

import fr.perrier.hologramuilib.client.config.ConfigLoader;
import fr.perrier.hologramuilib.client.config.InteractionConfig;
import fr.perrier.hologramuilib.client.interaction.ActionRegistry;
import fr.perrier.hologramuilib.client.interaction.InteractionHandler;
import fr.perrier.hologramuilib.client.interaction.MenuInteractionTracker;
import fr.perrier.hologramuilib.client.menu.HologramMenu;
import fr.perrier.hologramuilib.client.menu.MenuManager;
import fr.perrier.hologramuilib.client.menu.TestMenus;
import fr.perrier.hologramuilib.client.menu.elements.ButtonElement;
import fr.perrier.hologramuilib.client.network.ClientPacketHandler;
import fr.perrier.hologramuilib.client.network.MenuInteractionClientHandler;
import fr.perrier.hologramuilib.client.network.SpigotPluginChannelHandler;
import fr.perrier.hologramuilib.client.render.HologramRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side entry point for HologramUILib.
 * Initializes the hologram menu system.
 */
public class MainClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib");
    public static final String MOD_ID = "hologramuilib";

    // Keybindings
    private static KeyBinding debugKeyBinding;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing HologramUILib client...");

        // Register network packets
        ClientPacketHandler.registerClientPackets();

        // Register menu interaction packet handler
        MenuInteractionClientHandler.register();

        // Register Spigot plugin channel handler for server communication
        SpigotPluginChannelHandler.register();

        // Register the hologram renderer with Fabric's world render events
        HologramRenderer.getInstance().register();

        // Initialize the config loader
        ConfigLoader.getInstance().initialize();
        ConfigLoader.getInstance().createExampleConfig();

        // Load interaction configuration
        InteractionConfig.getInstance();

        // Initialize action registry
        ActionRegistry.getInstance();

        // Initialize and register interaction handler
        InteractionHandler.getInstance().register();

        // Register keybindings (including overlay debug)
        registerKeybindings();

        // Register commands
        registerCommands();

        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);

        LOGGER.info("HologramUILib client initialized!");
    }

    /**
     * Registers keybindings for the mod.
     */
    private void registerKeybindings() {
        debugKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.hologramuilib.debug",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            "category.hologramuilib"
        ));
    }

    /**
     * Registers client-side commands.
     */
    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("hologram")
                .then(ClientCommandManager.literal("clear")
                    .executes(context -> {
                        MenuManager.getInstance().clearAllMenus();
                        context.getSource().sendFeedback(Text.literal("§e[HologramUI] All menus cleared!"));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("debug")
                    .executes(context -> {
                        HologramRenderer.getInstance().toggleDebugMode();
                        boolean debugMode = HologramRenderer.getInstance().isDebugMode();
                        context.getSource().sendFeedback(Text.literal("§e[HologramUI] Debug mode: " + (debugMode ? "§aON" : "§cOFF")));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("interaction")
                    .then(ClientCommandManager.literal("status")
                        .executes(context -> {
                            InteractionConfig config = InteractionConfig.getInstance();
                            MenuInteractionTracker tracker = MenuInteractionTracker.getInstance();

                            context.getSource().sendFeedback(Text.literal("§6=== Interaction Suppression Status ==="));
                            context.getSource().sendFeedback(Text.literal("§7Enabled: " + (config.isSuppressWorldInteractions() ? "§a✔ YES" : "§c✘ NO")));
                            context.getSource().sendFeedback(Text.literal("§7Currently in menu: " + (tracker.isInteracting() ? "§a✔ YES" : "§c✘ NO")));
                            context.getSource().sendFeedback(Text.literal("§7Block breaking: " + (config.isSuppressBlockBreaking() ? "§a✔ Suppressed" : "§c✘ Allowed")));
                            context.getSource().sendFeedback(Text.literal("§7Entity attacking: " + (config.isSuppressEntityAttacking() ? "§a✔ Suppressed" : "§c✘ Allowed")));
                            context.getSource().sendFeedback(Text.literal("§7Cooldown: §e" + config.getInteractionCooldownMs() + "ms"));

                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("toggle")
                        .executes(context -> {
                            InteractionConfig config = InteractionConfig.getInstance();
                            boolean newState = !config.isSuppressWorldInteractions();
                            config.setSuppressWorldInteractions(newState);
                            config.save();

                            context.getSource().sendFeedback(Text.literal(
                                newState ?
                                "§a[HologramUI] Interaction suppression ENABLED - blocks won't break when using menus!" :
                                "§c[HologramUI] Interaction suppression DISABLED - blocks can break when using menus!"
                            ));

                            return 1;
                        })
                    )
                )
            );
        });
    }

    /**
     * Client tick event handler.
     */
    private void onClientTick(MinecraftClient client) {
        // Tick the menu manager to update animations
        MenuManager.getInstance().tick();


        // Update activity tracker (Phase 10.1 - Player Activity Monitoring)
        if (client.world != null && client.player != null) {
            fr.perrier.hologramuilib.client.activity.ActivityTracker.getInstance().update();
        }

        // Handle debug keybind
        while (debugKeyBinding.wasPressed()) {
            HologramRenderer.getInstance().toggleDebugMode();
            boolean debugMode = HologramRenderer.getInstance().isDebugMode();
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§e[HologramUI] Debug mode: " + (debugMode ? "§aON" : "§cOFF")),
                    true
                );
            }
            LOGGER.info("Debug mode toggled: {}", debugMode);
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}


