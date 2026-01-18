package fr.perrier.hologramuilib.network;

import fr.perrier.hologramuilib.api.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * Manages network communication between Spigot server and Fabric mod client.
 * Uses Minecraft's plugin messaging channel system.
 */
public class NetworkManager implements PluginMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/Network");
    private static final String CHANNEL = "hologramuilib:main";

    private final JavaPlugin plugin;
    private final Map<UUID, PlayerNetworkHandler> playerHandlers;

    public NetworkManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.playerHandlers = new HashMap<>();
    }

    /**
     * Initialize the network manager.
     */
    public void initialize() {
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        LOGGER.info("Network manager initialized on channel: {}", CHANNEL);
    }

    /**
     * Shutdown the network manager.
     */
    public void shutdown() {
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        playerHandlers.clear();
        LOGGER.info("Network manager shutdown");
    }

    /**
     * Sends a menu to players via plugin messaging.
     */
    public void sendMenu(HologramMenu menu, Collection<Player> players) {
        try {
            JsonObject menuJson = serializeMenu(menu);
            byte[] data = menuJson.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

            for (Player player : players) {
                if (player.isOnline()) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);

                    dos.writeUTF("menu_data");
                    dos.writeUTF(menuJson.toString());

                    player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
                    LOGGER.debug("Menu sent to {}: {}", player.getName(), menu.getMenuId());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error sending menu", e);
        }
    }

    /**
     * Sends a menu close packet to players.
     */
    public void closeMenu(String menuId, Collection<Player> players) {
        try {
            JsonObject closeJson = new JsonObject();
            closeJson.addProperty("action", "close");
            closeJson.addProperty("menuId", menuId);

            for (Player player : players) {
                if (player.isOnline()) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);

                    dos.writeUTF("menu_close");
                    dos.writeUTF(closeJson.toString());

                    player.sendPluginMessage(plugin, CHANNEL, out.toByteArray());
                    LOGGER.debug("Menu close sent to {}: {}", player.getName(), menuId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error closing menu", e);
        }
    }

    /**
     * Handles incoming plugin messages from the client mod.
     */
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(CHANNEL)) {
            return;
        }

        try {
            ByteArrayInputStream in = new ByteArrayInputStream(message);
            DataInputStream dis = new DataInputStream(in);

            String action = dis.readUTF();
            String data = dis.readUTF();

            handleClientMessage(player, action, data);
        } catch (Exception e) {
            LOGGER.error("Error handling plugin message from {}", player.getName(), e);
        }
    }

    /**
     * Handles messages from the client mod.
     */
    private void handleClientMessage(Player player, String action, String data) {
        LOGGER.debug("Received from {}: {} - {}", player.getName(), action, data);

        try {
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();

            switch (action) {
                case "menu_click":
                    handleMenuClick(player, json);
                    break;

                case "menu_interaction":
                    handleMenuInteraction(player, json);
                    break;

                case "slider_change":
                    handleSliderChange(player, json);
                    break;

                case "hello":
                    LOGGER.info("Player {} has HologramUILib mod installed", player.getName());
                    break;

                default:
                    LOGGER.warn("Unknown action from {}: {}", player.getName(), action);
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing client message from {}: {}", player.getName(), data, e);
        }
    }

    /**
     * Handles menu click events from client.
     */
    private void handleMenuClick(Player player, JsonObject json) {
        String menuId = json.get("menuId").getAsString();
        String elementId = json.get("elementId").getAsString();
        int button = json.get("button").getAsInt();

        HologramMenu menu = HologramMenuAPI.getMenu(player, menuId);
        if (menu == null) {
            LOGGER.warn("Player {} clicked non-existent menu: {}", player.getName(), menuId);
            return;
        }

        MenuElement element = menu.getElement(elementId);
        if (element == null) {
            LOGGER.warn("Player {} clicked non-existent element: {}", player.getName(), elementId);
            return;
        }

        // Fire button click event
        if ("button".equals(element.getType())) {
            ButtonClickEvent event = new ButtonClickEvent(player, menuId, elementId, element.getContent(), button);
            MenuEventRegistry.fireButtonClick(event);

            // Call direct callback if set
            if (element.getClickCallback() != null) {
                element.getClickCallback().accept(player, event);
            }
        }

        // Fire generic element click event
        MenuElementClickEvent clickEvent = new MenuElementClickEvent(player, menuId, elementId, element.getType());
        MenuEventRegistry.fireMenuElementClick(clickEvent);

        LOGGER.debug("Menu click: {} - {} - {}", player.getName(), menuId, elementId);
    }

    /**
     * Handles menu interaction events from client.
     */
    private void handleMenuInteraction(Player player, JsonObject json) {
        String menuId = json.get("menuId").getAsString();
        boolean inMenu = json.get("inMenu").getAsBoolean();

        LOGGER.debug("Menu interaction: {} - {} - inMenu={}", player.getName(), menuId, inMenu);
    }

    /**
     * Handles slider change events from client.
     */
    private void handleSliderChange(Player player, JsonObject json) {
        String menuId = json.get("menuId").getAsString();
        String elementId = json.get("elementId").getAsString();
        double oldValue = json.get("oldValue").getAsDouble();
        double newValue = json.get("newValue").getAsDouble();

        HologramMenu menu = HologramMenuAPI.getMenu(player, menuId);
        if (menu == null) {
            return;
        }

        MenuElement element = menu.getElement(elementId);
        if (element == null || !"slider".equals(element.getType())) {
            return;
        }

        // Fire slider change event
        SliderChangeEvent event = new SliderChangeEvent(player, menuId, elementId, oldValue, newValue);
        MenuEventRegistry.fireSliderChange(event);

        // Call direct callback if set
        if (element.getChangeCallback() != null) {
            element.getChangeCallback().accept(player, event);
        }

        // Fire generic element change event
        MenuElementChangeEvent changeEvent = new MenuElementChangeEvent(player, menuId, elementId, "slider", oldValue, newValue);
        MenuEventRegistry.fireMenuElementChange(changeEvent);

        LOGGER.debug("Slider change: {} - {} - {} -> {}", player.getName(), menuId, elementId, newValue);
    }

    /**
     * Serializes a menu to JSON for sending to the client.
     */
    private JsonObject serializeMenu(HologramMenu menu) {
        JsonObject json = new JsonObject();
        json.addProperty("action", "show_menu");
        json.addProperty("menuId", menu.getMenuId());

        if (menu.getPosition() != null) {
            JsonObject pos = new JsonObject();
            pos.addProperty("x", menu.getPosition().getX());
            pos.addProperty("y", menu.getPosition().getY());
            pos.addProperty("z", menu.getPosition().getZ());
            json.add("position", pos);
        }

        if (menu.getTitle() != null) {
            json.addProperty("title", menu.getTitle());
        }

        json.addProperty("width", menu.getWidth());
        json.addProperty("height", menu.getHeight());
        json.addProperty("scale", menu.getScale());
        json.addProperty("maxDistance", menu.getMaxRenderDistance());
        json.addProperty("backgroundColor", String.format("#%08X", menu.getBackgroundColor()));
        json.addProperty("borderColor", String.format("#%08X", menu.getBorderColor()));
        json.addProperty("padding", menu.getPadding());
        json.addProperty("spacing", menu.getSpacing());
        json.addProperty("backgroundEnabled", menu.isBackgroundEnabled());

        // Serialize elements
        JsonArray elementsArray = new JsonArray();
        for (MenuElement element : menu.getElements().values()) {
            elementsArray.add(JsonParser.parseString(element.toJson()));
        }
        json.add("elements", elementsArray);

        return json;
    }
}
