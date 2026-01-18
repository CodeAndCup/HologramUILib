package fr.perrier.hologramuilib.client.animation;

/**
 * Pre-built animation presets for common use cases.
 */
public class AnimationPresets {

    // === FADE ANIMATIONS ===

    public static Animation fadeIn(String elementId, long durationMs) {
        return Animation.builder(AnimationProperties.OPACITY)
            .from(0)
            .to(1)
            .duration(durationMs)
            .easing(Easing::easeOutQuad)
            .build();
    }

    public static Animation fadeOut(String elementId, long durationMs) {
        return Animation.builder(AnimationProperties.OPACITY)
            .from(1)
            .to(0)
            .duration(durationMs)
            .easing(Easing::easeInQuad)
            .build();
    }

    // === SCALE ANIMATIONS ===

    public static Animation scaleIn(String elementId, long durationMs) {
        return Animation.builder(AnimationProperties.SCALE)
            .from(0)
            .to(1)
            .duration(durationMs)
            .easing(Easing::easeOutBack)
            .build();
    }

    public static Animation scaleOut(String elementId, long durationMs) {
        return Animation.builder(AnimationProperties.SCALE)
            .from(1)
            .to(0)
            .duration(durationMs)
            .easing(Easing::easeInBack)
            .build();
    }

    public static Animation scaleBounce(String elementId) {
        return Animation.builder(AnimationProperties.SCALE)
            .from(1)
            .to(1.1f)
            .duration(150)
            .easing(Easing::easeOutBounce)
            .build();
    }

    // === SLIDE ANIMATIONS ===

    public static Animation slideInFromLeft(String elementId, float distance, long durationMs) {
        return Animation.builder(AnimationProperties.TRANSLATE_X)
            .from(-distance)
            .to(0)
            .duration(durationMs)
            .easing(Easing::easeOutCubic)
            .build();
    }

    public static Animation slideInFromRight(String elementId, float distance, long durationMs) {
        return Animation.builder(AnimationProperties.TRANSLATE_X)
            .from(distance)
            .to(0)
            .duration(durationMs)
            .easing(Easing::easeOutCubic)
            .build();
    }

    public static Animation slideInFromTop(String elementId, float distance, long durationMs) {
        return Animation.builder(AnimationProperties.TRANSLATE_Y)
            .from(-distance)
            .to(0)
            .duration(durationMs)
            .easing(Easing::easeOutCubic)
            .build();
    }

    public static Animation slideInFromBottom(String elementId, float distance, long durationMs) {
        return Animation.builder(AnimationProperties.TRANSLATE_Y)
            .from(distance)
            .to(0)
            .duration(durationMs)
            .easing(Easing::easeOutCubic)
            .build();
    }

    public static Animation slideOutToLeft(String elementId, float distance, long durationMs) {
        return Animation.builder(AnimationProperties.TRANSLATE_X)
            .from(0)
            .to(-distance)
            .duration(durationMs)
            .easing(Easing::easeInCubic)
            .build();
    }

    public static Animation slideOutToRight(String elementId, float distance, long durationMs) {
        return Animation.builder(AnimationProperties.TRANSLATE_X)
            .from(0)
            .to(distance)
            .duration(durationMs)
            .easing(Easing::easeInCubic)
            .build();
    }

    // === HOVER ANIMATIONS ===

    public static Animation hoverGrow(String elementId) {
        return Animation.builder(AnimationProperties.HOVER_SCALE)
            .from(1)
            .to(1.05f)
            .duration(150)
            .easing(Easing::easeOutQuad)
            .build();
    }

    public static Animation hoverShrink(String elementId) {
        return Animation.builder(AnimationProperties.HOVER_SCALE)
            .from(1.05f)
            .to(1)
            .duration(150)
            .easing(Easing::easeOutQuad)
            .build();
    }

    public static Animation hoverGlowOn(String elementId) {
        return Animation.builder(AnimationProperties.HOVER_GLOW)
            .from(0)
            .to(1)
            .duration(200)
            .easing(Easing::easeOutQuad)
            .build();
    }

    public static Animation hoverGlowOff(String elementId) {
        return Animation.builder(AnimationProperties.HOVER_GLOW)
            .from(1)
            .to(0)
            .duration(200)
            .easing(Easing::easeOutQuad)
            .build();
    }

    // === CLICK ANIMATIONS ===

    public static Animation clickBounce(String elementId) {
        return Animation.builder(AnimationProperties.SCALE)
            .from(1)
            .to(0.95f)
            .duration(100)
            .easing(Easing::easeInOutQuad)
            .onComplete(() -> {
                // Animation back to 1.0 would need to be triggered separately
            })
            .build();
    }

    public static Animation clickFlash(String elementId) {
        return Animation.builder(AnimationProperties.COLOR_ALPHA)
            .from(1)
            .to(0.5f)
            .duration(100)
            .easing(Easing::easeInOutQuad)
            .build();
    }

    // === ROTATION ANIMATIONS ===

    public static Animation rotate360(String elementId, long durationMs) {
        return Animation.builder(AnimationProperties.ROTATION)
            .from(0)
            .to(360)
            .duration(durationMs)
            .easing(Easing::linear)
            .build();
    }

    public static Animation rotateShake(String elementId) {
        return Animation.builder(AnimationProperties.ROTATION)
            .from(-5)
            .to(5)
            .duration(100)
            .easing(Easing::easeInOutQuad)
            .build();
    }

    // === COMBO ANIMATIONS (multiple properties) ===

    /**
     * Creates a fade + scale in combo animation.
     */
    public static void fadeAndScaleIn(AnimationManager manager, String elementId, long durationMs) {
        manager.addAnimation(elementId, fadeIn(elementId, durationMs));
        manager.addAnimation(elementId, scaleIn(elementId, durationMs));
    }

    /**
     * Creates a fade + scale out combo animation.
     */
    public static void fadeAndScaleOut(AnimationManager manager, String elementId, long durationMs) {
        manager.addAnimation(elementId, fadeOut(elementId, durationMs));
        manager.addAnimation(elementId, scaleOut(elementId, durationMs));
    }

    /**
     * Creates a slide + fade in combo animation.
     */
    public static void slideAndFadeIn(AnimationManager manager, String elementId, float distance, long durationMs) {
        manager.addAnimation(elementId, fadeIn(elementId, durationMs));
        manager.addAnimation(elementId, slideInFromLeft(elementId, distance, durationMs));
    }

    private AnimationPresets() {
        // Utility class
    }
}

