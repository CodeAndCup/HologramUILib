package fr.perrier.hologramuilib.client.activity;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Represents different types of player activities.
 */
public enum ActivityType {
    IDLE("§7Idle", "⏸"),
    IN_MENU("§eIn Menu", "📋"),
    CRAFTING("§bCrafting", "🔨"),
    TRADING("§2Trading", "💰"),
    INVENTORY("§6Inventory", "🎒"),
    CHEST("§eChest", "📦"),
    RIDING("§aRiding", "🐴"),
    SWIMMING("§9Swimming", "🏊"),
    FLYING("§dFlying", "✈"),
    COMBAT("§cCombat", "⚔"),
    MINING("§8Mining", "⛏"),
    FISHING("§3Fishing", "🎣"),
    EATING("§eEating", "🍖"),
    ENCHANTING("§5Enchanting", "✨"),
    BREWING("§dBrewing", "🧪"),
    READING("§7Reading", "📖"),
    AFK("§8AFK", "💤"),
    CUSTOM("§fCustom", "❓");

    private final String displayName;
    private final String icon;

    ActivityType(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getFullDisplay() {
        return icon + " " + displayName;
    }
}
