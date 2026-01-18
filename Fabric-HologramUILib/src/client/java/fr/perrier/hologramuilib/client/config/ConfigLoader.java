package fr.perrier.hologramuilib.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and manages menu configurations from JSON files.
 * Supports hot-reloading when files change.
 */
public class ConfigLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib");
    private static ConfigLoader INSTANCE;

    private final Gson gson;
    private final Path configDir;
    private final Map<String, MenuConfig> menuConfigs;
    private WatchService watchService;
    private Thread watcherThread;
    private boolean watching = false;

    private ConfigLoader() {
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
        this.configDir = Paths.get("config", "hologramuilib", "menus");
        this.menuConfigs = new HashMap<>();
    }

    public static ConfigLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigLoader();
        }
        return INSTANCE;
    }

    /**
     * Initializes the config loader and creates necessary directories.
     */
    public void initialize() {
        try {
            Files.createDirectories(configDir);
            loadAllConfigs();
            startWatching();
            LOGGER.info("ConfigLoader initialized. Watching: {}", configDir);
        } catch (IOException e) {
            LOGGER.error("Failed to initialize ConfigLoader", e);
        }
    }

    /**
     * Loads all JSON configuration files from the config directory.
     */
    public void loadAllConfigs() {
        menuConfigs.clear();

        try {
            if (!Files.exists(configDir)) {
                return;
            }

            Files.list(configDir)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(this::loadConfigFile);

            LOGGER.info("Loaded {} menu configurations", menuConfigs.size());
        } catch (IOException e) {
            LOGGER.error("Failed to load configs", e);
        }
    }

    /**
     * Loads a single configuration file.
     */
    private void loadConfigFile(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            if (root.has("menus")) {
                JsonObject menus = root.getAsJsonObject("menus");
                for (String menuId : menus.keySet()) {
                    JsonObject menuJson = menus.getAsJsonObject(menuId);
                    MenuConfig config = parseMenuConfig(menuJson);
                    menuConfigs.put(menuId, config);
                    LOGGER.debug("Loaded menu config: {}", menuId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load config file: {}", path, e);
        }
    }

    /**
     * Parses a menu configuration from JSON.
     */
    private MenuConfig parseMenuConfig(JsonObject json) {
        MenuConfig config = new MenuConfig();

        // Parse style
        if (json.has("style")) {
            StyleConfig style = gson.fromJson(json.get("style"), StyleConfig.class);
            config.setStyle(style);
        }

        // Parse layout
        if (json.has("layout")) {
            LayoutConfig layout = gson.fromJson(json.get("layout"), LayoutConfig.class);
            config.setLayout(layout);
        }

        // Parse items
        if (json.has("items")) {
            json.getAsJsonArray("items").forEach(itemElement -> {
                ItemConfig item = gson.fromJson(itemElement, ItemConfig.class);
                item.setRawJson(itemElement.getAsJsonObject());
                config.addItem(item);
            });
        }

        return config;
    }

    /**
     * Gets a menu configuration by ID.
     */
    public MenuConfig getMenuConfig(String id) {
        return menuConfigs.get(id);
    }

    /**
     * Gets all loaded menu configurations.
     */
    public Map<String, MenuConfig> getAllMenuConfigs() {
        return new HashMap<>(menuConfigs);
    }

    /**
     * Checks if a menu configuration exists.
     */
    public boolean hasMenuConfig(String id) {
        return menuConfigs.containsKey(id);
    }

    /**
     * Starts watching the config directory for changes.
     */
    public void startWatching() {
        if (watching) {
            return;
        }

        try {
            watchService = FileSystems.getDefault().newWatchService();
            configDir.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE);

            watching = true;
            watcherThread = new Thread(this::watchLoop, "HologramUILib-ConfigWatcher");
            watcherThread.setDaemon(true);
            watcherThread.start();

            LOGGER.info("Started config file watcher");
        } catch (IOException e) {
            LOGGER.error("Failed to start config watcher", e);
        }
    }

    /**
     * The main watch loop for hot-reloading.
     */
    private void watchLoop() {
        while (watching) {
            try {
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    if (fileName.toString().endsWith(".json")) {
                        LOGGER.info("Config file changed: {}", fileName);
                        // Reload all configs
                        loadAllConfigs();
                    }
                }

                key.reset();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Stops watching for config changes.
     */
    public void stopWatching() {
        watching = false;
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close watch service", e);
            }
        }
        if (watcherThread != null) {
            watcherThread.interrupt();
        }
    }

    /**
     * Gets the config directory path.
     */
    public Path getConfigDir() {
        return configDir;
    }

    /**
     * Creates a default example configuration file.
     */
    public void createExampleConfig() {
        Path examplePath = configDir.resolve("example_menu.json");

        if (Files.exists(examplePath)) {
            return;
        }

        String exampleJson = """
            {
              "menus": {
                "example_menu": {
                  "style": {
                    "width": 200,
                    "height": "auto",
                    "scale": 0.025,
                    "maxRenderDistance": 10.0,
                    "background": {
                      "enabled": true,
                      "color": "#80000000",
                      "blur": 0,
                      "borderRadius": 4
                    },
                    "border": {
                      "enabled": true,
                      "color": "#FFFFFFFF",
                      "width": 2
                    }
                  },
                  "layout": {
                    "type": "VERTICAL",
                    "padding": 8,
                    "spacing": 4,
                    "alignment": "CENTER"
                  },
                  "items": [
                    {
                      "type": "text",
                      "content": "§b§lExample Menu",
                      "centered": true
                    },
                    {
                      "type": "separator",
                      "height": 1,
                      "color": "#40FFFFFF"
                    },
                    {
                      "type": "button",
                      "id": "button_1",
                      "text": "§aClick Me!",
                      "style": {
                        "height": 20,
                        "textColor": "#00FF00",
                        "hoverColor": "#40FFFFFF"
                      },
                      "action": "example_action"
                    }
                  ]
                }
              }
            }
            """;

        try {
            Files.writeString(examplePath, exampleJson);
            LOGGER.info("Created example config: {}", examplePath);
        } catch (IOException e) {
            LOGGER.error("Failed to create example config", e);
        }
    }
}

