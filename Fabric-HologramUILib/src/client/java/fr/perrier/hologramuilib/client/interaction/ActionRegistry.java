package fr.perrier.hologramuilib.client.interaction;

import fr.perrier.hologramuilib.client.menu.MenuElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Registry for action handlers.
 * Allows registering custom actions that can be triggered by menu elements.
 */
public class ActionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib");
    private static ActionRegistry INSTANCE;

    private final Map<String, Consumer<MenuElement>> actions;

    private ActionRegistry() {
        this.actions = new HashMap<>();
        registerDefaultActions();
    }

    public static ActionRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ActionRegistry();
        }
        return INSTANCE;
    }

    /**
     * Registers default/built-in actions.
     */
    private void registerDefaultActions() {
        // Example action - just logs
        register("example_action", element -> {
            LOGGER.info("Example action triggered by element: {}", element.getId());
        });
    }

    /**
     * Registers a new action handler.
     *
     * @param actionId The unique action identifier
     * @param handler The handler to execute when the action is triggered
     */
    public void register(String actionId, Consumer<MenuElement> handler) {
        actions.put(actionId, handler);
        LOGGER.debug("Registered action: {}", actionId);
    }

    /**
     * Unregisters an action handler.
     *
     * @param actionId The action identifier to remove
     */
    public void unregister(String actionId) {
        actions.remove(actionId);
    }

    /**
     * Checks if an action is registered.
     *
     * @param actionId The action identifier
     * @return true if the action is registered
     */
    public boolean hasAction(String actionId) {
        return actions.containsKey(actionId);
    }

    /**
     * Executes an action.
     *
     * @param actionId The action identifier
     * @param element The element that triggered the action
     */
    public void executeAction(String actionId, MenuElement element) {
        Consumer<MenuElement> handler = actions.get(actionId);

        if (handler != null) {
            try {
                handler.accept(element);
            } catch (Exception e) {
                LOGGER.error("Error executing action '{}': {}", actionId, e.getMessage(), e);
            }
        } else {
            LOGGER.warn("Unknown action: {}", actionId);
        }
    }

    /**
     * Clears all registered actions.
     */
    public void clearAll() {
        actions.clear();
        registerDefaultActions();
    }
}

