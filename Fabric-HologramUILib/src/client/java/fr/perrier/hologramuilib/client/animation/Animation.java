package fr.perrier.hologramuilib.client.animation;

import fr.perrier.hologramuilib.client.animation.Easing.EasingFunction;

/**
 * Represents a single animation that can be applied to properties.
 */
public class Animation {

    private final String propertyName;
    private final float startValue;
    private final float endValue;
    private final long durationMs;
    private final long delayMs;
    private final EasingFunction easingFunction;
    private final Runnable onComplete;

    private long startTime = -1;
    private boolean completed = false;
    private boolean cancelled = false;

    private Animation(Builder builder) {
        this.propertyName = builder.propertyName;
        this.startValue = builder.startValue;
        this.endValue = builder.endValue;
        this.durationMs = builder.durationMs;
        this.delayMs = builder.delayMs;
        this.easingFunction = builder.easingFunction;
        this.onComplete = builder.onComplete;
    }

    /**
     * Starts the animation.
     */
    public void start() {
        this.startTime = System.currentTimeMillis();
        this.completed = false;
        this.cancelled = false;
    }

    /**
     * Updates the animation and returns the current value.
     *
     * @return The current interpolated value, or null if animation hasn't started yet
     */
    public Float update() {
        if (cancelled) {
            return null;
        }

        if (startTime < 0) {
            return startValue;
        }

        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;

        // Handle delay
        if (elapsed < delayMs) {
            return startValue;
        }

        long animationElapsed = elapsed - delayMs;

        // Check if animation is complete
        if (animationElapsed >= durationMs) {
            if (!completed) {
                completed = true;
                if (onComplete != null) {
                    onComplete.run();
                }
            }
            return endValue;
        }

        // Calculate progress (0.0 to 1.0)
        float progress = (float) animationElapsed / durationMs;

        // Apply easing
        float easedProgress = easingFunction.apply(progress);

        // Interpolate
        return startValue + (endValue - startValue) * easedProgress;
    }

    /**
     * Cancels the animation.
     */
    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Checks if the animation is completed.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Checks if the animation is cancelled.
     */
    public boolean isCancelled() {
        return cancelled;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public float getStartValue() {
        return startValue;
    }

    public float getEndValue() {
        return endValue;
    }

    /**
     * Creates a new animation builder.
     */
    public static Builder builder(String propertyName) {
        return new Builder(propertyName);
    }

    /**
     * Builder for creating animations.
     */
    public static class Builder {
        private final String propertyName;
        private float startValue = 0;
        private float endValue = 1;
        private long durationMs = 300;
        private long delayMs = 0;
        private EasingFunction easingFunction = Easing::easeOutQuad;
        private Runnable onComplete;

        private Builder(String propertyName) {
            this.propertyName = propertyName;
        }

        public Builder from(float startValue) {
            this.startValue = startValue;
            return this;
        }

        public Builder to(float endValue) {
            this.endValue = endValue;
            return this;
        }

        public Builder duration(long milliseconds) {
            this.durationMs = milliseconds;
            return this;
        }

        public Builder delay(long milliseconds) {
            this.delayMs = milliseconds;
            return this;
        }

        public Builder easing(EasingFunction function) {
            this.easingFunction = function;
            return this;
        }

        public Builder easing(String easingName) {
            this.easingFunction = Easing.getEasingFunction(easingName);
            return this;
        }

        public Builder onComplete(Runnable callback) {
            this.onComplete = callback;
            return this;
        }

        public Animation build() {
            return new Animation(this);
        }

        public Animation buildAndStart() {
            Animation animation = new Animation(this);
            animation.start();
            return animation;
        }
    }
}

