package fr.perrier.hologramuilib.client.animation;

/**
 * Easing functions for smooth animations.
 * Based on standard easing equations.
 */
public class Easing {

    /**
     * Linear interpolation (no easing).
     */
    public static float linear(float t) {
        return t;
    }

    // === EASE IN (slow start) ===

    public static float easeInQuad(float t) {
        return t * t;
    }

    public static float easeInCubic(float t) {
        return t * t * t;
    }

    public static float easeInQuart(float t) {
        return t * t * t * t;
    }

    public static float easeInQuint(float t) {
        return t * t * t * t * t;
    }

    public static float easeInSine(float t) {
        return 1 - (float) Math.cos(t * Math.PI / 2);
    }

    public static float easeInExpo(float t) {
        return t == 0 ? 0 : (float) Math.pow(2, 10 * (t - 1));
    }

    public static float easeInCirc(float t) {
        return 1 - (float) Math.sqrt(1 - t * t);
    }

    // === EASE OUT (fast start, slow end) ===

    public static float easeOutQuad(float t) {
        return t * (2 - t);
    }

    public static float easeOutCubic(float t) {
        float f = t - 1;
        return f * f * f + 1;
    }

    public static float easeOutQuart(float t) {
        float f = t - 1;
        return 1 - f * f * f * f;
    }

    public static float easeOutQuint(float t) {
        float f = t - 1;
        return f * f * f * f * f + 1;
    }

    public static float easeOutSine(float t) {
        return (float) Math.sin(t * Math.PI / 2);
    }

    public static float easeOutExpo(float t) {
        return t == 1 ? 1 : 1 - (float) Math.pow(2, -10 * t);
    }

    public static float easeOutCirc(float t) {
        float f = t - 1;
        return (float) Math.sqrt(1 - f * f);
    }

    // === EASE IN OUT (slow start and end) ===

    public static float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    public static float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
    }

    public static float easeInOutQuart(float t) {
        if (t < 0.5f) {
            return 8 * t * t * t * t;
        }
        float f = t - 1;
        return 1 - 8 * f * f * f * f;
    }

    public static float easeInOutQuint(float t) {
        if (t < 0.5f) {
            return 16 * t * t * t * t * t;
        }
        float f = t - 1;
        return 1 + 16 * f * f * f * f * f;
    }

    public static float easeInOutSine(float t) {
        return -(float) (Math.cos(Math.PI * t) - 1) / 2;
    }

    public static float easeInOutExpo(float t) {
        if (t == 0) return 0;
        if (t == 1) return 1;
        if (t < 0.5f) {
            return (float) Math.pow(2, 20 * t - 10) / 2;
        }
        return (2 - (float) Math.pow(2, -20 * t + 10)) / 2;
    }

    public static float easeInOutCirc(float t) {
        if (t < 0.5f) {
            return (1 - (float) Math.sqrt(1 - 4 * t * t)) / 2;
        }
        float f = -2 * t + 2;
        return ((float) Math.sqrt(1 - f * f) + 1) / 2;
    }

    // === SPECIAL EASING ===

    public static float easeInBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return c3 * t * t * t - c1 * t * t;
    }

    public static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        return 1 + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    public static float easeInOutBack(float t) {
        float c1 = 1.70158f;
        float c2 = c1 * 1.525f;
        if (t < 0.5f) {
            return ((float) Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2;
        }
        return ((float) Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2;
    }

    public static float easeInElastic(float t) {
        float c4 = (2 * (float) Math.PI) / 3;
        return t == 0 ? 0 : t == 1 ? 1 :
            -(float) Math.pow(2, 10 * t - 10) * (float) Math.sin((t * 10 - 10.75f) * c4);
    }

    public static float easeOutElastic(float t) {
        float c4 = (2 * (float) Math.PI) / 3;
        return t == 0 ? 0 : t == 1 ? 1 :
            (float) Math.pow(2, -10 * t) * (float) Math.sin((t * 10 - 0.75f) * c4) + 1;
    }

    public static float easeInOutElastic(float t) {
        float c5 = (2 * (float) Math.PI) / 4.5f;
        return t == 0 ? 0 : t == 1 ? 1 : t < 0.5f ?
            -((float) Math.pow(2, 20 * t - 10) * (float) Math.sin((20 * t - 11.125f) * c5)) / 2 :
            ((float) Math.pow(2, -20 * t + 10) * (float) Math.sin((20 * t - 11.125f) * c5)) / 2 + 1;
    }

    public static float easeInBounce(float t) {
        return 1 - easeOutBounce(1 - t);
    }

    public static float easeOutBounce(float t) {
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            return n1 * (t -= 1.5f / d1) * t + 0.75f;
        } else if (t < 2.5 / d1) {
            return n1 * (t -= 2.25f / d1) * t + 0.9375f;
        } else {
            return n1 * (t -= 2.625f / d1) * t + 0.984375f;
        }
    }

    public static float easeInOutBounce(float t) {
        return t < 0.5f ?
            (1 - easeOutBounce(1 - 2 * t)) / 2 :
            (1 + easeOutBounce(2 * t - 1)) / 2;
    }

    /**
     * Gets an easing function by name.
     */
    public static EasingFunction getEasingFunction(String name) {
        return switch (name.toLowerCase()) {
            case "linear" -> Easing::linear;
            case "easeinquad" -> Easing::easeInQuad;
            case "easeincubic" -> Easing::easeInCubic;
            case "easeinquart" -> Easing::easeInQuart;
            case "easeinquint" -> Easing::easeInQuint;
            case "easeinsine" -> Easing::easeInSine;
            case "easeinexpo" -> Easing::easeInExpo;
            case "easeincirc" -> Easing::easeInCirc;
            case "easeoutquad" -> Easing::easeOutQuad;
            case "easeoutcubic" -> Easing::easeOutCubic;
            case "easeoutquart" -> Easing::easeOutQuart;
            case "easeoutquint" -> Easing::easeOutQuint;
            case "easeoutsine" -> Easing::easeOutSine;
            case "easeoutexpo" -> Easing::easeOutExpo;
            case "easeoutcirc" -> Easing::easeOutCirc;
            case "easeinoutquad" -> Easing::easeInOutQuad;
            case "easeinoutcubic" -> Easing::easeInOutCubic;
            case "easeinoutquart" -> Easing::easeInOutQuart;
            case "easeinoutquint" -> Easing::easeInOutQuint;
            case "easeinoutsine" -> Easing::easeInOutSine;
            case "easeinoutexpo" -> Easing::easeInOutExpo;
            case "easeinoutcirc" -> Easing::easeInOutCirc;
            case "easeinback" -> Easing::easeInBack;
            case "easeoutback" -> Easing::easeOutBack;
            case "easeinoutback" -> Easing::easeInOutBack;
            case "easeinelastic" -> Easing::easeInElastic;
            case "easeoutelastic" -> Easing::easeOutElastic;
            case "easeinoutelastic" -> Easing::easeInOutElastic;
            case "easeinbounce" -> Easing::easeInBounce;
            case "easeoutbounce" -> Easing::easeOutBounce;
            case "easeinoutbounce" -> Easing::easeInOutBounce;
            default -> Easing::linear;
        };
    }

    @FunctionalInterface
    public interface EasingFunction {
        float apply(float t);
    }
}

