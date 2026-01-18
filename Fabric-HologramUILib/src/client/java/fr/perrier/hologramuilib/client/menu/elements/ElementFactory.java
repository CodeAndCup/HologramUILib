package fr.perrier.hologramuilib.client.menu.elements;

import fr.perrier.hologramuilib.client.config.ItemConfig;
import fr.perrier.hologramuilib.client.menu.MenuElement;

/**
 * Factory for creating menu elements from configuration.
 */
public final class ElementFactory {

    private ElementFactory() {
        // Utility class
    }

    /**
     * Creates a menu element from an item configuration.
     *
     * @param config The item configuration
     * @return The created menu element, or null if the type is unknown
     */
    public static MenuElement createElement(ItemConfig config) {
        if (config == null || config.getType() == null) {
            return null;
        }

        return switch (config.getType().toLowerCase()) {
            case "button" -> ButtonElement.fromConfig(config);
            case "text" -> TextElement.fromConfig(config);
            case "separator" -> SeparatorElement.fromConfig(config);
            case "image" -> ImageElement.fromConfig(config);
            case "item" -> ItemElement.fromConfig(config);
            case "container" -> ContainerElement.fromConfig(config);
            case "progressbar", "progress_bar" -> ProgressBarElement.fromConfig(config);
            case "slider" -> SliderElement.fromConfig(config);
            default -> null;
        };
    }
}

