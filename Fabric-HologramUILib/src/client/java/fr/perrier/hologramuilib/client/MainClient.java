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


        // Register some example actions
        registerExampleActions();

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
                .then(ClientCommandManager.literal("spawn")
                    .then(ClientCommandManager.literal("main")
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                MenuManager.getInstance().clearAllMenus();
                                TestMenus.createMainMenu(client);
                                context.getSource().sendFeedback(Text.literal("§a[HologramUI] Main menu spawned!"));
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("test")
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                MenuManager.getInstance().clearAllMenus();
                                TestMenus.createSimpleTestMenu(client);
                                context.getSource().sendFeedback(Text.literal("§a[HologramUI] Test menu spawned!"));
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("animations")
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                MenuManager.getInstance().clearAllMenus();
                                TestMenus.createAnimationMenu(client);
                                context.getSource().sendFeedback(Text.literal("§a[HologramUI] Animation menu spawned!"));
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("sliders")
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                MenuManager.getInstance().clearAllMenus();
                                TestMenus.createSliderMenu(client);
                                context.getSource().sendFeedback(Text.literal("§a[HologramUI] Slider menu spawned!"));
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("api")
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                MenuManager.getInstance().clearAllMenus();
                                TestMenus.createAPIShowcaseMenu(client);
                                context.getSource().sendFeedback(Text.literal("§d[HologramUI] API Showcase menu spawned!"));
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("shop")
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                MenuManager.getInstance().clearAllMenus();
                                TestMenus.createAPIShopExample(client);
                                context.getSource().sendFeedback(Text.literal("§6[HologramUI] Shop menu spawned!"));
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("settings")
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                MenuManager.getInstance().clearAllMenus();
                                TestMenus.createAPISettingsExample(client);
                                context.getSource().sendFeedback(Text.literal("§b[HologramUI] Settings menu spawned!"));
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("stats")
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                MenuManager.getInstance().clearAllMenus();
                                TestMenus.createAPIStatsExample(client);
                                context.getSource().sendFeedback(Text.literal("§e[HologramUI] Stats menu spawned!"));
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("activity")
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                MenuManager.getInstance().clearAllMenus();
                                TestMenus.createActivityMonitorExample(client);
                                context.getSource().sendFeedback(Text.literal("§b[HologramUI] Activity Monitor spawned!"));
                            }
                            return 1;
                        })
                    )
                    .then(ClientCommandManager.literal("web")
                        .executes(context -> {
                            MinecraftClient client = MinecraftClient.getInstance();
                            if (client.player != null) {
                                MenuManager.getInstance().clearAllMenus();
                                TestMenus.createWebResourceExample(client);
                                context.getSource().sendFeedback(Text.literal("§a[HologramUI] Web Resource menu spawned!"));
                            }
                            return 1;
                        })
                    )
                )
                .then(ClientCommandManager.literal("spawn_test_menu")
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        if (client.player != null) {
                            MenuManager.getInstance().clearAllMenus();
                            TestMenus.createSimpleTestMenu(client);
                            context.getSource().sendFeedback(Text.literal("§a[HologramUI] Simple test menu spawned!"));
                        }
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
     * Registers example actions for testing.
     */
    private void registerExampleActions() {
        ActionRegistry registry = ActionRegistry.getInstance();

        registry.register("test_action", element -> {
            LOGGER.info("Test action executed! Element: {}", element.getId());
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("§aButton clicked: " + element.getId()),
                    true
                );
            }
        });

        // Action for callback demo button
        registry.register("callback_demo", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§b⚡ Callback button clicked!"), false);
            }
        });

        // Action for toggle button
        registry.register("toggle_action", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (element instanceof ButtonElement btnElement) {
                if (btnElement.getText().contains("OFF")) {
                    btnElement.setText("§aON");
                    btnElement.setTextColor(0xFF00FF00);
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§aToggled ON!"), false);
                    }
                } else {
                    btnElement.setText("§cOFF");
                    btnElement.setTextColor(0xFFFF0000);
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§cToggled OFF!"), false);
                    }
                }
            }
        });

        // Action for close button
        registry.register("close_menu", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();

            // Find the menu that contains this element
            for (HologramMenu menu : manager.getActiveMenus()) {
                if (menu.getElements().contains(element)) {
                    manager.removeMenu(menu.getId());
                    if (client.player != null) {
                        client.player.sendMessage(Text.literal("§eMenu closed!"), false);
                    }
                    return; // Exit after closing the menu
                }
            }
        });

        // Navigation actions - close all menus before opening new one
        registry.register("open_main_menu", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();

            // Close all existing menus
            manager.clearAllMenus();

            // Create the main menu
            TestMenus.createMainMenu(client);
        });

        registry.register("open_test_menu", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();

            // Close all existing menus
            manager.clearAllMenus();

            // Create the test menu
            TestMenus.createSimpleTestMenu(client);
        });

        registry.register("open_animation_menu", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();

            // Close all existing menus
            manager.clearAllMenus();

            // Create the animation menu
            TestMenus.createAnimationMenu(client);
        });

        registry.register("open_slider_menu", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();

            // Close all existing menus
            manager.clearAllMenus();

            // Create the slider menu
            TestMenus.createSliderMenu(client);
        });

        // Phase 9 & 10: New API menus
        registry.register("open_api_showcase", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();
            manager.clearAllMenus();
            TestMenus.createAPIShowcaseMenu(client);
        });

        registry.register("open_shop_demo", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();
            manager.clearAllMenus();
            TestMenus.createAPIShopExample(client);
        });

        registry.register("open_settings_demo", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();
            manager.clearAllMenus();
            TestMenus.createAPISettingsExample(client);
        });

        registry.register("open_stats_demo", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();
            manager.clearAllMenus();
            TestMenus.createAPIStatsExample(client);
        });

        registry.register("open_activity_monitor", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();
            manager.clearAllMenus();
            TestMenus.createActivityMonitorExample(client);
        });

        registry.register("open_web_resources", element -> {
            MinecraftClient client = MinecraftClient.getInstance();
            MenuManager manager = MenuManager.getInstance();
            manager.clearAllMenus();
            TestMenus.createWebResourceExample(client);
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


