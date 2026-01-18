package fr.perrier.hologramuilib.client.animation;

/**
 * Pre-defined animation properties that can be animated on menu elements.
 */
public class AnimationProperties {

    // Visual properties
    public static final String OPACITY = "opacity";
    public static final String SCALE = "scale";
    public static final String SCALE_X = "scaleX";
    public static final String SCALE_Y = "scaleY";
    public static final String ROTATION = "rotation";

    // Position properties
    public static final String TRANSLATE_X = "translateX";
    public static final String TRANSLATE_Y = "translateY";
    public static final String TRANSLATE_Z = "translateZ";

    // Color properties
    public static final String COLOR_RED = "colorR";
    public static final String COLOR_GREEN = "colorG";
    public static final String COLOR_BLUE = "colorB";
    public static final String COLOR_ALPHA = "colorA";

    // Hover effects
    public static final String HOVER_SCALE = "hoverScale";
    public static final String HOVER_GLOW = "hoverGlow";

    // Custom properties for specific elements
    public static final String PROGRESS = "progress"; // For progress bars
    public static final String SLIDER_VALUE = "sliderValue"; // For sliders

    private AnimationProperties() {
        // Utility class
    }
}

