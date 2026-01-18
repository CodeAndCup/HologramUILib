package fr.perrier.hologramuilib.client.activity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;

/**
 * Tracks and detects player activities in real-time.
 * Can detect when players are in menus, crafting, trading, etc.
 */
public class ActivityTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib/Activity");
    private static ActivityTracker INSTANCE;

    private final Map<UUID, PlayerActivity> playerActivities = new HashMap<>();
    private final List<ActivityDetector> customDetectors = new ArrayList<>();
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 500; // Update every 500ms

    private ActivityTracker() {
    }

    public static ActivityTracker getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ActivityTracker();
        }
        return INSTANCE;
    }

    /**
     * Updates activity tracking for all players.
     * Should be called regularly (e.g., every tick or every 500ms).
     */
    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < UPDATE_INTERVAL_MS) {
            return;
        }
        lastUpdateTime = now;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return;
        }

        // Update activity for all players in range
        for (PlayerEntity player : client.world.getPlayers()) {
            updatePlayerActivity(player);
        }
    }

    /**
     * Updates the activity for a specific player.
     */
    private void updatePlayerActivity(PlayerEntity player) {
        UUID playerId = player.getUuid();
        ActivityType detectedActivity = detectActivity(player);

        PlayerActivity currentActivity = playerActivities.get(playerId);

        if (currentActivity == null || currentActivity.getType() != detectedActivity) {
            // Activity changed
            PlayerActivity newActivity = new PlayerActivity(player, detectedActivity);
            playerActivities.put(playerId, newActivity);

            LOGGER.debug("Player {} activity changed to: {}",
                        player.getName().getString(), detectedActivity.name());
        } else {
            // Update duration
            currentActivity.updateDuration();
        }
    }

    /**
     * Detects the current activity of a player.
     */
    private ActivityType detectActivity(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if player is in a screen/menu
        if (client.player != null && client.player.getUuid().equals(player.getUuid())) {
            if (client.currentScreen != null) {
                String screenClass = client.currentScreen.getClass().getSimpleName();

                if (screenClass.contains("Inventory")) {
                    return ActivityType.INVENTORY;
                } else if (screenClass.contains("Crafting")) {
                    return ActivityType.CRAFTING;
                } else if (screenClass.contains("Merchant") || screenClass.contains("Trading")) {
                    return ActivityType.TRADING;
                } else if (screenClass.contains("Chest") || screenClass.contains("Container")) {
                    return ActivityType.CHEST;
                } else if (screenClass.contains("Enchant")) {
                    return ActivityType.ENCHANTING;
                } else if (screenClass.contains("Brewing")) {
                    return ActivityType.BREWING;
                } else if (screenClass.contains("Book")) {
                    return ActivityType.READING;
                } else {
                    return ActivityType.IN_MENU;
                }
            }
        }

        // Check custom detectors
        for (ActivityDetector detector : customDetectors) {
            if (detector.test(player)) {
                return detector.getActivityType();
            }
        }

        // Check physical activities
        if (player.isSwimming()) {
            return ActivityType.SWIMMING;
        }
        // Check if flying (creative/spectator)
        if (player.getAbilities().flying) {
            return ActivityType.FLYING;
        }
        if (player.hasVehicle()) {
            return ActivityType.RIDING;
        }
        if (player.handSwinging && player.getAttacking() != null) {
            return ActivityType.COMBAT;
        }
        if (player.isUsingItem()) {
            // Check if eating (simple check based on item use)
            return ActivityType.EATING;
        }

        // Check if AFK (no movement for a while)
        PlayerActivity currentActivity = playerActivities.get(player.getUuid());
        if (currentActivity != null && currentActivity.getDurationSeconds() > 60) {
            if (player.getVelocity().lengthSquared() < 0.001) {
                return ActivityType.AFK;
            }
        }

        return ActivityType.IDLE;
    }

    /**
     * Gets the current activity for a player.
     */
    public PlayerActivity getActivity(UUID playerId) {
        return playerActivities.get(playerId);
    }

    /**
     * Gets the current activity for a player.
     */
    public PlayerActivity getActivity(PlayerEntity player) {
        return getActivity(player.getUuid());
    }

    /**
     * Gets all players with a specific activity.
     */
    public List<PlayerActivity> getPlayersByActivity(ActivityType type) {
        return playerActivities.values().stream()
            .filter(activity -> activity.getType() == type)
            .toList();
    }

    /**
     * Gets all tracked player activities.
     */
    public Collection<PlayerActivity> getAllActivities() {
        return playerActivities.values();
    }

    /**
     * Registers a custom activity detector.
     */
    public void registerCustomDetector(ActivityDetector detector) {
        customDetectors.add(detector);
        LOGGER.info("Registered custom activity detector: {}", detector.getActivityType().name());
    }

    /**
     * Clears all tracked activities.
     */
    public void clear() {
        playerActivities.clear();
    }

    /**
     * Custom activity detector interface.
     */
    public interface ActivityDetector extends Predicate<PlayerEntity> {
        ActivityType getActivityType();
    }

    /**
     * Represents a player's current activity.
     */
    public static class PlayerActivity {
        private final PlayerEntity player;
        private final ActivityType type;
        private final long startTime;
        private long lastUpdateTime;

        public PlayerActivity(PlayerEntity player, ActivityType type) {
            this.player = player;
            this.type = type;
            this.startTime = System.currentTimeMillis();
            this.lastUpdateTime = startTime;
        }

        public PlayerEntity getPlayer() {
            return player;
        }

        public ActivityType getType() {
            return type;
        }

        public long getDurationMs() {
            return lastUpdateTime - startTime;
        }

        public long getDurationSeconds() {
            return getDurationMs() / 1000;
        }

        public void updateDuration() {
            this.lastUpdateTime = System.currentTimeMillis();
        }

        public String getFormattedDuration() {
            long seconds = getDurationSeconds();
            if (seconds < 60) {
                return seconds + "s";
            } else if (seconds < 3600) {
                return (seconds / 60) + "m " + (seconds % 60) + "s";
            } else {
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                return hours + "h " + minutes + "m";
            }
        }
    }
}
