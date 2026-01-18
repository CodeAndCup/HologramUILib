package fr.perrier.hologramuilib.client.animation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages animations for menu elements.
 * Supports multiple concurrent animations per element.
 */
public class AnimationManager {

    // Map of element ID -> property name -> animation
    private final Map<String, Map<String, Animation>> activeAnimations = new ConcurrentHashMap<>();

    /**
     * Adds an animation for an element.
     *
     * @param elementId The ID of the element to animate
     * @param animation The animation to add
     */
    public void addAnimation(String elementId, Animation animation) {
        Map<String, Animation> elementAnimations = activeAnimations.computeIfAbsent(
            elementId,
            k -> new ConcurrentHashMap<>()
        );

        // Cancel existing animation for the same property
        Animation existing = elementAnimations.get(animation.getPropertyName());
        if (existing != null) {
            existing.cancel();
        }

        elementAnimations.put(animation.getPropertyName(), animation);
        animation.start();
    }

    /**
     * Gets the current animated value for a property.
     *
     * @param elementId The element ID
     * @param propertyName The property name
     * @param defaultValue The default value if no animation is active
     * @return The current animated value
     */
    public float getValue(String elementId, String propertyName, float defaultValue) {
        Map<String, Animation> elementAnimations = activeAnimations.get(elementId);
        if (elementAnimations == null) {
            return defaultValue;
        }

        Animation animation = elementAnimations.get(propertyName);
        if (animation == null) {
            return defaultValue;
        }

        Float value = animation.update();
        if (value == null || animation.isCompleted() || animation.isCancelled()) {
            elementAnimations.remove(propertyName);
            if (elementAnimations.isEmpty()) {
                activeAnimations.remove(elementId);
            }
            return animation.isCompleted() ? animation.getEndValue() : defaultValue;
        }

        return value;
    }

    /**
     * Checks if an element has an active animation for a property.
     */
    public boolean hasAnimation(String elementId, String propertyName) {
        Map<String, Animation> elementAnimations = activeAnimations.get(elementId);
        if (elementAnimations == null) {
            return false;
        }

        Animation animation = elementAnimations.get(propertyName);
        return animation != null && !animation.isCompleted() && !animation.isCancelled();
    }

    /**
     * Cancels all animations for an element.
     */
    public void cancelAll(String elementId) {
        Map<String, Animation> elementAnimations = activeAnimations.remove(elementId);
        if (elementAnimations != null) {
            elementAnimations.values().forEach(Animation::cancel);
        }
    }

    /**
     * Cancels a specific animation.
     */
    public void cancel(String elementId, String propertyName) {
        Map<String, Animation> elementAnimations = activeAnimations.get(elementId);
        if (elementAnimations != null) {
            Animation animation = elementAnimations.remove(propertyName);
            if (animation != null) {
                animation.cancel();
            }
            if (elementAnimations.isEmpty()) {
                activeAnimations.remove(elementId);
            }
        }
    }

    /**
     * Updates all animations and removes completed ones.
     */
    public void tick() {
        activeAnimations.forEach((elementId, animations) -> {
            animations.values().removeIf(animation -> {
                animation.update();
                return animation.isCompleted() || animation.isCancelled();
            });
        });

        // Remove empty element entries
        activeAnimations.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    /**
     * Clears all animations.
     */
    public void clear() {
        activeAnimations.values().forEach(animations ->
            animations.values().forEach(Animation::cancel)
        );
        activeAnimations.clear();
    }

    /**
     * Gets the number of active animations.
     */
    public int getActiveAnimationCount() {
        return activeAnimations.values().stream()
            .mapToInt(Map::size)
            .sum();
    }
}

