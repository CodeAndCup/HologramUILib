package fr.perrier.hologramuilib.client.animation;

import fr.perrier.hologramuilib.client.menu.MenuElement;

/**
 * Trait/interface that menu elements can implement to support animations.
 */
public interface Animatable {

    /**
     * Gets the animation manager for this element.
     */
    AnimationManager getAnimationManager();

    /**
     * Sets the animation manager for this element.
     */
    void setAnimationManager(AnimationManager manager);

    /**
     * Convenience method to start an animation on this element.
     */
    default void animate(Animation animation) {
        AnimationManager manager = getAnimationManager();
        if (manager != null && this instanceof MenuElement element) {
            manager.addAnimation(element.getId(), animation);
        }
    }

    /**
     * Gets the current value of an animated property.
     */
    default float getAnimatedValue(String property, float defaultValue) {
        AnimationManager manager = getAnimationManager();
        if (manager != null && this instanceof MenuElement element) {
            return manager.getValue(element.getId(), property, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Checks if this element has an active animation for a property.
     */
    default boolean hasAnimation(String property) {
        AnimationManager manager = getAnimationManager();
        if (manager != null && this instanceof MenuElement element) {
            return manager.hasAnimation(element.getId(), property);
        }
        return false;
    }

    /**
     * Cancels all animations on this element.
     */
    default void cancelAllAnimations() {
        AnimationManager manager = getAnimationManager();
        if (manager != null && this instanceof MenuElement element) {
            manager.cancelAll(element.getId());
        }
    }

    /**
     * Cancels a specific animation on this element.
     */
    default void cancelAnimation(String property) {
        AnimationManager manager = getAnimationManager();
        if (manager != null && this instanceof MenuElement element) {
            manager.cancel(element.getId(), property);
        }
    }
}

