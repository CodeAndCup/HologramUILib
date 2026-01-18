package fr.perrier.hologramuilib.client.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.perrier.hologramuilib.client.menu.HologramMenu;
import fr.perrier.hologramuilib.client.menu.MenuManager;
import fr.perrier.hologramuilib.client.menu.elements.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestionnaire pour les messages du plugin Spigot via le canal hologramuilib:main.
 */
public class SpigotPluginChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/PluginChannel");
    private static final Identifier CHANNEL_ID = Identifier.of("hologramuilib", "main");

    /**
     * Payload pour les messages bruts du plugin Spigot.
     *
     * IMPORTANT : Ce record utilise un codec personnalisé pour gérer correctement
     * les paquets CustomPayload envoyés depuis des serveurs Spigot/Paper.
     *
     * Pourquoi un codec personnalisé ?
     * - Les serveurs Spigot/Paper peuvent ajouter des métadonnées supplémentaires au paquet
     * - PacketCodec.tuple() est trop strict et ne consomme que les données spécifiées
     * - Si le buffer n'est pas entièrement consommé, Minecraft lève une DecoderException
     *
     * Solution :
     * - Utiliser PacketCodec.of() avec des méthodes read/write personnalisées
     * - Après avoir lu les données nécessaires, consommer le reste du buffer avec skipBytes()
     * - Ceci évite l'erreur "Packet was larger than I expected, found X bytes extra"
     *
     * @see <a href="file:///NETWORK_PROTOCOL_FIX.md">NETWORK_PROTOCOL_FIX.md</a>
     * @see <a href="file:///NETWORK_BEST_PRACTICES.md">NETWORK_BEST_PRACTICES.md</a>
     */
    public record SpigotPluginMessage(String action, String data) implements CustomPayload {
        public static final CustomPayload.Id<SpigotPluginMessage> ID = new CustomPayload.Id<>(CHANNEL_ID);

        public static final PacketCodec<PacketByteBuf, SpigotPluginMessage> CODEC = PacketCodec.of(
            SpigotPluginMessage::write,
            SpigotPluginMessage::read
        );

        public void write(PacketByteBuf buf) {
            // IMPORTANT : Écrire au format DataOutputStream.writeUTF()
            // Format : short (2 bytes) pour la longueur, puis les bytes UTF-8

            // Écrire action
            byte[] actionBytes = action.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf.writeShort(actionBytes.length);
            buf.writeBytes(actionBytes);

            // Écrire data
            byte[] dataBytes = data.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            buf.writeShort(dataBytes.length);
            buf.writeBytes(dataBytes);
        }

        public static SpigotPluginMessage read(PacketByteBuf buf) {
            int totalBytes = buf.readableBytes();
            LOGGER.debug("Reading SpigotPluginMessage from buffer: {} bytes available", totalBytes);

            // Sauvegarder la position pour analyse en cas d'erreur
            int readerIndex = buf.readerIndex();

            try {
                // IMPORTANT : Le serveur Spigot utilise DataOutputStream.writeUTF()
                // qui écrit la longueur en 2 bytes (short) AVANT chaque string
                // On doit utiliser readShort() + readBytes() au lieu de readString()

                // Lire la première string (action)
                short actionLength = buf.readShort();
                byte[] actionBytes = new byte[actionLength];
                buf.readBytes(actionBytes);
                String action = new String(actionBytes, java.nio.charset.StandardCharsets.UTF_8);

                // Lire la deuxième string (data)
                short dataLength = buf.readShort();
                byte[] dataBytes = new byte[dataLength];
                buf.readBytes(dataBytes);
                String data = new String(dataBytes, java.nio.charset.StandardCharsets.UTF_8);

                LOGGER.debug("Read action='{}', data length={} chars", action, data.length());
                LOGGER.debug("Action: '{}'", action);
                LOGGER.debug("Data preview: '{}'", data.length() > 200 ? data.substring(0, 200) + "..." : data);

                // Consommer tout le buffer restant pour éviter l'erreur "bytes extra"
                if (buf.isReadable()) {
                    int remaining = buf.readableBytes();
                    LOGGER.debug("Consuming {} remaining bytes from buffer", remaining);
                    buf.skipBytes(remaining);
                }

                return new SpigotPluginMessage(action, data);

            } catch (Exception e) {
                LOGGER.error("Error reading SpigotPluginMessage", e);

                // Réinitialiser le buffer et lire en raw pour analyse
                buf.readerIndex(readerIndex);
                byte[] rawData = new byte[Math.min(100, buf.readableBytes())];
                buf.readBytes(rawData);

                LOGGER.error("Raw buffer data (first {} bytes): {}", rawData.length, bytesToHex(rawData));

                // Consommer le reste pour éviter l'erreur de décodage
                if (buf.isReadable()) {
                    buf.skipBytes(buf.readableBytes());
                }

                // Retourner un message vide pour ne pas crasher
                return new SpigotPluginMessage("", "");
            }
        }

        private static String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X ", b));
            }
            return sb.toString();
        }

        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() {
            return ID;
        }
    }

    /**
     * Enregistre le gestionnaire de messages du plugin.
     */
    public static void register() {
        // Enregistrer le payload côté serveur vers client (S2C)
        PayloadTypeRegistry.playS2C().register(SpigotPluginMessage.ID, SpigotPluginMessage.CODEC);

        // Enregistrer le payload côté client vers serveur (C2S)
        PayloadTypeRegistry.playC2S().register(SpigotPluginMessage.ID, SpigotPluginMessage.CODEC);

        // Enregistrer le récepteur pour les messages du serveur
        ClientPlayNetworking.registerGlobalReceiver(SpigotPluginMessage.ID, (payload, context) ->
            context.client().execute(() -> handlePluginMessage(context.client(), payload))
        );

        LOGGER.info("Spigot plugin channel handler registered on {}", CHANNEL_ID);
    }

    /**
     * Traite les messages reçus du plugin Spigot.
     */
    private static void handlePluginMessage(MinecraftClient client, SpigotPluginMessage payload) {
        try {
            String action = payload.action();
            String data = payload.data();

            LOGGER.debug("Handling plugin message: action='{}', data length={}", action, data != null ? data.length() : 0);

            switch (action) {
                case "menu_data":
                    handleMenuData(client, data);
                    break;
                case "menu_close":
                    handleMenuClose(client, data);
                    break;
                default:
                    LOGGER.warn("Unknown action received: '{}'", action);
            }
        } catch (Exception e) {
            LOGGER.error("Error processing plugin message: action='" + payload.action() + "'", e);
        }
    }

    /**
     * Traite les données d'un menu reçu du serveur.
     */
    private static void handleMenuData(MinecraftClient client, String jsonData) {
        try {
            LOGGER.debug("handleMenuData called with {} chars", jsonData.length());

            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
            String menuId = json.get("menuId").getAsString();

            LOGGER.info("Creating menu '{}' from Spigot server", menuId);

            // Position du menu
            Vec3d position;
            if (json.has("position")) {
                JsonObject pos = json.getAsJsonObject("position");
                position = new Vec3d(
                    pos.get("x").getAsDouble(),
                    pos.get("y").getAsDouble(),
                    pos.get("z").getAsDouble()
                );
            } else if (client.player != null) {
                position = client.player.getPos().add(0, 2, 0);
            } else {
                LOGGER.warn("Cannot determine menu position");
                return;
            }

            // Créer le menu
            MenuManager manager = MenuManager.getInstance();
            HologramMenu menu = manager.createMenu(menuId, position);

            // Configuration du menu (sauf height qui sera appliqué après les éléments)
            if (json.has("title")) {
                TextElement titleElement = new TextElement("__title__", parseMinecraftColors(json.get("title").getAsString()));
                menu.addElement(titleElement);
            }

            if (json.has("width")) {
                menu.setWidth(json.get("width").getAsInt());
            }

            if (json.has("scale")) {
                menu.setScale(json.get("scale").getAsFloat());
            }

            if (json.has("maxDistance")) {
                menu.setMaxRenderDistance(json.get("maxDistance").getAsDouble());
            }

            if (json.has("backgroundColor")) {
                menu.setBackgroundColor(parseColor(json.get("backgroundColor").getAsString()));
            }

            if (json.has("backgroundEnabled")) {
                menu.setHasBackground(json.get("backgroundEnabled").getAsBoolean());
            }

            // Ajouter les éléments
            if (json.has("elements")) {
                JsonArray elements = json.getAsJsonArray("elements");
                for (JsonElement elementJson : elements) {
                    addElementToMenu(menu, elementJson.getAsJsonObject());
                }
            }

            // IMPORTANT : Appliquer height APRÈS avoir ajouté les éléments
            // Si height = -1, cela active le mode auto-height qui calcule la hauteur
            // basée sur le contenu (éléments). Donc on doit le faire à la fin.
            if (json.has("height")) {
                menu.setHeight(json.get("height").getAsInt());
            }

            // IMPORTANT : Réappliquer la position APRÈS avoir défini la height finale
            // Cela permet au système anti-collision de recalculer correctement
            // la position pour éviter que le menu ne soit dans le sol
            menu.setPosition(position);

            LOGGER.info("Menu '{}' created successfully with {} elements", menuId, menu.getElements().size());

        } catch (Exception e) {
            LOGGER.error("Error creating menu from JSON data", e);
        }
    }

    /**
     * Ajoute un élément au menu depuis les données JSON.
     */
    private static void addElementToMenu(HologramMenu menu, JsonObject elementJson) {
        try {
            String type = elementJson.get("type").getAsString();
            String id = elementJson.get("id").getAsString();
            String content = elementJson.has("content") ? elementJson.get("content").getAsString() : "";

            switch (type) {
                case "text":
                    TextElement textElement = new TextElement(id, parseMinecraftColors(content));
                    if (elementJson.has("width")) {
                        textElement.setWidth(elementJson.get("width").getAsInt());
                    }
                    menu.addElement(textElement);
                    break;

                case "button":
                    ButtonElement button = new ButtonElement(id, parseMinecraftColors(content));
                    if (elementJson.has("width")) {
                        button.setWidth(elementJson.get("width").getAsInt());
                    }
                    if (elementJson.has("height")) {
                        button.setHeight(elementJson.get("height").getAsInt());
                    }
                    button.onClickCallback(e -> sendButtonClickToServer(menu.getId(), id));
                    menu.addElement(button);
                    break;

                case "slider":
                    double min = elementJson.has("min") ? elementJson.get("min").getAsDouble() : 0;
                    double max = elementJson.has("max") ? elementJson.get("max").getAsDouble() : 100;
                    double value = elementJson.has("value") ? elementJson.get("value").getAsDouble() : min;

                    SliderElement slider = new SliderElement(id);
                    slider.setMinValue((float) min);
                    slider.setMaxValue((float) max);
                    slider.setActualValue((float) value);
                    if (elementJson.has("width")) {
                        slider.setWidth((float) elementJson.get("width").getAsInt());
                    }
                    final double initialValue = value;
                    slider.onValueChange(newValue -> sendSliderChangeToServer(menu.getId(), id, initialValue, newValue));
                    menu.addElement(slider);
                    break;

                case "progress_bar":
                    double progressValue = elementJson.has("value") ? elementJson.get("value").getAsDouble() : 0;
                    double progressMax = elementJson.has("max") ? elementJson.get("max").getAsDouble() : 100;

                    ProgressBarElement progressBar = new ProgressBarElement(id);
                    progressBar.setProgress((float) (progressValue / progressMax));
                    if (elementJson.has("width")) {
                        progressBar.setWidth(elementJson.get("width").getAsInt());
                    }
                    menu.addElement(progressBar);
                    break;

                case "separator":
                    SeparatorElement separator = new SeparatorElement(id);
                    menu.addElement(separator);
                    break;

                case "spacing":
                    // Utiliser SeparatorElement avec couleur transparente comme espacement
                    int spacingHeight = elementJson.has("height") ? elementJson.get("height").getAsInt() : 10;
                    SeparatorElement spacing = new SeparatorElement(id);
                    spacing.setHeight(spacingHeight);
                    spacing.setColor(0x00000000); // Complètement transparent = invisible
                    menu.addElement(spacing);
                    break;

                default:
                    LOGGER.warn("Unknown element type: {}", type);
            }

        } catch (Exception e) {
            LOGGER.error("Error adding element to menu", e);
        }
    }

    /**
     * Traite la fermeture d'un menu.
     */
    private static void handleMenuClose(MinecraftClient client, String jsonData) {
        try {
            JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();
            String menuId = json.get("menuId").getAsString();

            LOGGER.info("Closing menu '{}' from server", menuId);

            MenuManager manager = MenuManager.getInstance();
            manager.destroyMenu(menuId);

        } catch (Exception e) {
            LOGGER.error("Error closing menu", e);
        }
    }

    /**
     * Envoie un clic de bouton au serveur Spigot.
     */
    private static void sendButtonClickToServer(String menuId, String elementId) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("menuId", menuId);
            json.addProperty("elementId", elementId);
            json.addProperty("button", 0);

            SpigotPluginMessage message = new SpigotPluginMessage("menu_click", json.toString());
            ClientPlayNetworking.send(message);

            LOGGER.debug("Sent button click to server: menu={}, element={}", menuId, elementId);

        } catch (Exception e) {
            LOGGER.error("Error sending button click", e);
        }
    }

    /**
     * Envoie un changement de slider au serveur Spigot.
     */
    private static void sendSliderChangeToServer(String menuId, String elementId, double oldValue, double newValue) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("menuId", menuId);
            json.addProperty("elementId", elementId);
            json.addProperty("oldValue", oldValue);
            json.addProperty("newValue", newValue);

            SpigotPluginMessage message = new SpigotPluginMessage("slider_change", json.toString());
            ClientPlayNetworking.send(message);

            LOGGER.debug("Sent slider change to server: menu={}, element={}, value={}", menuId, elementId, newValue);

        } catch (Exception e) {
            LOGGER.error("Error sending slider change", e);
        }
    }

    /**
     * Parse une couleur hexadécimale.
     */
    private static int parseColor(String hexColor) {
        try {
            String hex = hexColor.replace("#", "");
            if (hex.length() == 6) {
                hex = "FF" + hex;
            }
            return (int) Long.parseLong(hex, 16);
        } catch (Exception e) {
            LOGGER.warn("Cannot parse color: {}", hexColor);
            return 0xFFFFFFFF;
        }
    }

    /**
     * Conserve les codes couleur Minecraft (§).
     */
    private static String parseMinecraftColors(String text) {
        return text;
    }
}
