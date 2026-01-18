package fr.perrier.hologramuilib.util;

/**
 * Helper class for parsing and manipulating colors.
 * Supports hex color codes with alpha (#RRGGBB, #AARRGGBB, #RGB).
 */
public final class ColorHelper {

    private ColorHelper() {
        // Utility class
    }

    /**
     * Parses a hex color string to an integer.
     * Supports formats: #RGB, #RRGGBB, #AARRGGBB
     *
     * @param colorString The color string to parse
     * @return The color as an integer (ARGB format)
     */
    public static int parseColor(String colorString) {
        if (colorString == null || colorString.isEmpty()) {
            return 0xFFFFFFFF; // Default white
        }

        String hex = colorString.startsWith("#") ? colorString.substring(1) : colorString;

        try {
            return switch (hex.length()) {
                case 3 -> {
                    // #RGB -> #FFRRGGBB
                    int r = Integer.parseInt(hex.substring(0, 1), 16) * 17;
                    int g = Integer.parseInt(hex.substring(1, 2), 16) * 17;
                    int b = Integer.parseInt(hex.substring(2, 3), 16) * 17;
                    yield 0xFF000000 | (r << 16) | (g << 8) | b;
                }
                case 6 -> {
                    // #RRGGBB -> #FFRRGGBB
                    yield 0xFF000000 | Integer.parseInt(hex, 16);
                }
                case 8 -> {
                    // #AARRGGBB
                    yield (int) Long.parseLong(hex, 16);
                }
                default -> 0xFFFFFFFF;
            };
        } catch (NumberFormatException e) {
            return 0xFFFFFFFF;
        }
    }

    /**
     * Converts a color integer to a hex string.
     *
     * @param color The color integer (ARGB format)
     * @return The hex color string (#AARRGGBB)
     */
    public static String toHexString(int color) {
        return String.format("#%08X", color);
    }

    /**
     * Extracts the alpha component from a color.
     */
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    /**
     * Extracts the red component from a color.
     */
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Extracts the green component from a color.
     */
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Extracts the blue component from a color.
     */
    public static int getBlue(int color) {
        return color & 0xFF;
    }

    /**
     * Creates a color from RGBA components.
     */
    public static int fromRGBA(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    /**
     * Interpolates between two colors.
     *
     * @param color1 The first color
     * @param color2 The second color
     * @param t The interpolation factor (0.0 to 1.0)
     * @return The interpolated color
     */
    public static int lerp(int color1, int color2, float t) {
        t = Math.max(0, Math.min(1, t));

        int a = (int) (getAlpha(color1) + (getAlpha(color2) - getAlpha(color1)) * t);
        int r = (int) (getRed(color1) + (getRed(color2) - getRed(color1)) * t);
        int g = (int) (getGreen(color1) + (getGreen(color2) - getGreen(color1)) * t);
        int b = (int) (getBlue(color1) + (getBlue(color2) - getBlue(color1)) * t);

        return fromRGBA(r, g, b, a);
    }

    /**
     * Applies alpha to a color.
     *
     * @param color The base color
     * @param alpha The alpha value (0-255)
     * @return The color with new alpha
     */
    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    /**
     * Multiplies the alpha of a color.
     *
     * @param color The base color
     * @param multiplier The alpha multiplier (0.0 to 1.0)
     * @return The color with modified alpha
     */
    public static int multiplyAlpha(int color, float multiplier) {
        int alpha = (int) (getAlpha(color) * multiplier);
        return withAlpha(color, alpha);
    }
}

