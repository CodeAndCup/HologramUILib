package fr.perrier.hologramuilib.client.menu;

import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

/**
 * Configuration for menu display conditions.
 * Determines when a menu should be visible and active.
 */
public class MenuConditions {

    private double maxDistance = 10.0;
    private double minDistance = 0.0;
    private boolean requiresLineOfSight = false;
    private Predicate<Vec3d> customCondition = null;

    public MenuConditions() {
    }

    /**
     * Sets the maximum distance at which the menu is visible.
     */
    public MenuConditions maxDistance(double distance) {
        this.maxDistance = distance;
        return this;
    }

    /**
     * Sets the minimum distance at which the menu is visible.
     */
    public MenuConditions minDistance(double distance) {
        this.minDistance = distance;
        return this;
    }

    /**
     * Sets whether line of sight is required to see the menu.
     */
    public MenuConditions requireLineOfSight(boolean requires) {
        this.requiresLineOfSight = requires;
        return this;
    }

    /**
     * Sets a custom condition predicate that must be satisfied.
     * The predicate receives the player's position.
     */
    public MenuConditions customCondition(Predicate<Vec3d> condition) {
        this.customCondition = condition;
        return this;
    }

    /**
     * Checks if all conditions are met for the given player position and menu position.
     */
    public boolean checkConditions(Vec3d playerPos, Vec3d menuPos) {
        // Check distance
        double distance = playerPos.distanceTo(menuPos);
        if (distance > maxDistance || distance < minDistance) {
            return false;
        }

        // Check line of sight (if required)
        if (requiresLineOfSight) {
            // TODO: Implement raycast check for line of sight
            // This would require world context
        }

        // Check custom condition
        if (customCondition != null && !customCondition.test(playerPos)) {
            return false;
        }

        return true;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public boolean requiresLineOfSight() {
        return requiresLineOfSight;
    }
}
