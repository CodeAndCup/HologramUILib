package fr.perrier.hologramuilib.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Configuration for menu interaction behavior.
 */
public class InteractionConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/Config");
    private static final String CONFIG_FILE = "config/hologramuilib/interaction.json";
    private static InteractionConfig INSTANCE;

    // Configuration options
    private boolean suppressWorldInteractions = true; // Activé par défaut
    private boolean suppressBlockBreaking = true;
    private boolean suppressEntityAttacking = true;
    private boolean suppressBlockUsage = false;
    private boolean suppressEntityInteraction = false;
    private int interactionCooldownMs = 300; // Augmenté à 300ms pour être plus sûr

    public static InteractionConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    /**
     * Loads the configuration from file, or creates a default one if it doesn't exist.
     */
    public static InteractionConfig load() {
        File configFile = new File(CONFIG_FILE);

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Gson gson = new Gson();
                InteractionConfig config = gson.fromJson(reader, InteractionConfig.class);
                LOGGER.info("Loaded interaction config from: {}", CONFIG_FILE);
                return config;
            } catch (IOException e) {
                LOGGER.error("Failed to load interaction config", e);
            }
        }

        // Create default config
        InteractionConfig defaultConfig = new InteractionConfig();
        defaultConfig.save();
        return defaultConfig;
    }

    /**
     * Saves the configuration to file.
     */
    public void save() {
        File configFile = new File(CONFIG_FILE);
        configFile.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(configFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(this, writer);
            LOGGER.info("Saved interaction config to: {}", CONFIG_FILE);
        } catch (IOException e) {
            LOGGER.error("Failed to save interaction config", e);
        }
    }

    // Getters

    public boolean isSuppressWorldInteractions() {
        return suppressWorldInteractions;
    }

    public boolean isSuppressBlockBreaking() {
        return suppressBlockBreaking;
    }

    public boolean isSuppressEntityAttacking() {
        return suppressEntityAttacking;
    }

    public boolean isSuppressBlockUsage() {
        return suppressBlockUsage;
    }

    public boolean isSuppressEntityInteraction() {
        return suppressEntityInteraction;
    }

    public int getInteractionCooldownMs() {
        return interactionCooldownMs;
    }

    // Setters

    public void setSuppressWorldInteractions(boolean suppressWorldInteractions) {
        this.suppressWorldInteractions = suppressWorldInteractions;
    }

    public void setSuppressBlockBreaking(boolean suppressBlockBreaking) {
        this.suppressBlockBreaking = suppressBlockBreaking;
    }

    public void setSuppressEntityAttacking(boolean suppressEntityAttacking) {
        this.suppressEntityAttacking = suppressEntityAttacking;
    }

    public void setSuppressBlockUsage(boolean suppressBlockUsage) {
        this.suppressBlockUsage = suppressBlockUsage;
    }

    public void setSuppressEntityInteraction(boolean suppressEntityInteraction) {
        this.suppressEntityInteraction = suppressEntityInteraction;
    }

    public void setInteractionCooldownMs(int interactionCooldownMs) {
        this.interactionCooldownMs = interactionCooldownMs;
    }
}
