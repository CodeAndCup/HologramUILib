package fr.perrier.hologramuilib.client.interaction;

import fr.perrier.hologramuilib.client.menu.HologramMenu;
import fr.perrier.hologramuilib.client.menu.MenuElement;
import fr.perrier.hologramuilib.client.menu.MenuManager;
import fr.perrier.hologramuilib.client.menu.elements.ButtonElement;
import fr.perrier.hologramuilib.client.menu.elements.SliderElement;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles player interaction with hologram menus.
 * Manages hover detection, click handling, and cooldowns.
 */
public class InteractionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib");
    private static InteractionHandler INSTANCE;

    private final MinecraftClient client;

    // Current hover state
    private HologramMenu hoveredMenu = null;
    private MenuElement hoveredElement = null;
    private RaycastHelper.RaycastResult lastRaycastResult = null;

    // Click cooldown (in ticks)
    private static final int CLICK_COOLDOWN_TICKS = 5;
    private int cooldownTimer = 0;

    // Mouse button states (for detecting clicks)
    private boolean leftMousePressed = false;
    private boolean rightMousePressed = false;

    // Slider dragging state
    private SliderElement draggingSlider = null;

    // Maximum raycast distance
    private static final double MAX_RAYCAST_DISTANCE = 50.0;

    private InteractionHandler() {
        this.client = MinecraftClient.getInstance();
    }

    public static InteractionHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InteractionHandler();
        }
        return INSTANCE;
    }

    /**
     * Registers the interaction handler with Fabric events.
     */
    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        LOGGER.info("InteractionHandler registered");
    }

    /**
     * Called every client tick to update hover state and handle clicks.
     */
    private void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        // Decrease cooldown timer
        if (cooldownTimer > 0) {
            cooldownTimer--;
        }

        // Update hover state
        updateHoverState();

        // Handle mouse clicks
        handleMouseInput();
    }

    /**
     * Updates which menu and element the player is currently hovering over.
     */
    private void updateHoverState() {
        MenuManager manager = MenuManager.getInstance();
        MenuInteractionTracker tracker = MenuInteractionTracker.getInstance();

        HologramMenu previousHoveredMenu = hoveredMenu;
        MenuElement previousHoveredElement = hoveredElement;

        hoveredMenu = null;
        hoveredElement = null;
        lastRaycastResult = null;

        // Find closest menu that the player is looking at
        double closestDistance = Double.MAX_VALUE;
        RaycastHelper.RaycastResult closestResult = null;

        for (HologramMenu menu : manager.getActiveMenus()) {
            RaycastHelper.RaycastResult result = RaycastHelper.raycastMenu(menu, MAX_RAYCAST_DISTANCE);

            if (result != null) {
                double distance = client.player.getPos().distanceTo(result.getHitPos());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestResult = result;
                }
            }
        }

        // Update hover state
        if (closestResult != null) {
            hoveredMenu = closestResult.getMenu();
            hoveredElement = closestResult.getHoveredElement();
            lastRaycastResult = closestResult;
        }

        // Track interaction state - START dès qu'on regarde un MENU
        if (hoveredMenu != null) {
            // Start tracking si on regarde n'importe quel menu
            if (!tracker.isInteracting()) {
                tracker.startInteraction();
            }
        } else {
            // End tracking seulement si on ne regarde AUCUN menu ET qu'on ne drag pas
            if (tracker.isInteracting() && draggingSlider == null) {
                tracker.endInteraction();
            }
        }

        // Trigger hover events if element changed
        if (hoveredElement != previousHoveredElement) {
            if (previousHoveredElement != null) {
                previousHoveredElement.onHoverEnd();
            }
            if (hoveredElement != null) {
                hoveredElement.onHoverStart();
                if(hoveredElement instanceof ButtonElement)
                    playHoverSound();
            }
        }
    }

    /**
     * Handles mouse button input for clicking elements.
     */
    private void handleMouseInput() {
        long windowHandle = client.getWindow().getHandle();

        // Check left mouse button
        boolean leftMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightMouseDown = GLFW.glfwGetMouseButton(windowHandle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        // Handle slider dragging
        if (draggingSlider != null) {
            if (leftMouseDown && lastRaycastResult != null && hoveredMenu != null) {
                // Update slider value based on current mouse position
                // menuX is in menu-space (0 = left edge of menu)
                // We need to convert to element-space
                float menuX = lastRaycastResult.getLocalX();
                float padding = hoveredMenu.getPadding();

                // Subtract padding to get position in element area
                // Then subtract element's X position to get position within element
                float elementAreaX = menuX - padding;
                float elementX = elementAreaX - draggingSlider.getBounds().x();

                draggingSlider.updateValueFromMouse(elementX);
            } else {
                // Release slider
                draggingSlider.onRelease();
                draggingSlider = null;
            }
        }

        // Detect left click (button pressed this tick but not last tick)
        if (leftMouseDown && !leftMousePressed) {
            handleClick(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        }

        // Detect right click
        if (rightMouseDown && !rightMousePressed) {
            handleClick(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        }

        // Update button states
        leftMousePressed = leftMouseDown;
        rightMousePressed = rightMouseDown;
    }

    /**
     * Handles a mouse click on the currently hovered element.
     *
     * @param button The mouse button that was clicked
     */
    private void handleClick(int button) {
        // Check cooldown
        if (cooldownTimer > 0) {
            return;
        }

        // Check if player is in a screen (don't handle clicks if in GUI)
        if (client.currentScreen != null) {
            return;
        }

        // Check if hovering an element
        if (hoveredElement != null && hoveredMenu != null) {
            LOGGER.debug("Clicked element: {} in menu: {} with button: {}",
                hoveredElement.getId(), hoveredMenu.getId(), button);

            // Update interaction tracker
            MenuInteractionTracker tracker = MenuInteractionTracker.getInstance();
            tracker.updateInteractionTime();

            // Play click sound
            playClickSound();

            // Check if element is a slider and start dragging
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && hoveredElement instanceof SliderElement slider) {
                draggingSlider = slider;
                if (lastRaycastResult != null && hoveredMenu != null) {
                    // Convert from menu-space coordinates to element-space coordinates
                    float menuX = lastRaycastResult.getLocalX();
                    float padding = hoveredMenu.getPadding();
                    // Account for padding and element position
                    float elementX = (menuX - padding) - slider.getBounds().x();
                    slider.updateValueFromMouse(elementX);
                }
            }

            // Trigger element click
            hoveredElement.onClick(button);

            // Set cooldown
            cooldownTimer = CLICK_COOLDOWN_TICKS;
        }
    }

    /**
     * Plays a hover sound effect.
     */
    private void playHoverSound() {
        if (client.player != null && client.world != null) {
            client.world.playSound(
                client.player,
                client.player.getBlockPos(),
                SoundEvents.UI_BUTTON_CLICK.value(),
                SoundCategory.MASTER,
                0.1f, // volume
                2.0f  // pitch (high pitch for hover)
            );
        }
    }

    /**
     * Plays a click sound effect.
     */
    private void playClickSound() {
        if (client.player != null && client.world != null) {
            client.world.playSound(
                client.player,
                client.player.getBlockPos(),
                SoundEvents.UI_BUTTON_CLICK.value(),
                SoundCategory.MASTER,
                0.3f, // volume
                1.0f  // pitch
            );
        }
    }

    /**
     * Gets the currently hovered menu.
     */
    public HologramMenu getHoveredMenu() {
        return hoveredMenu;
    }

    /**
     * Gets the currently hovered element.
     */
    public MenuElement getHoveredElement() {
        return hoveredElement;
    }

    /**
     * Gets the last raycast result.
     */
    public RaycastHelper.RaycastResult getLastRaycastResult() {
        return lastRaycastResult;
    }

    /**
     * Checks if an element is currently hovered.
     */
    public boolean isElementHovered(MenuElement element) {
        return hoveredElement == element;
    }

    /**
     * Checks if a menu is currently hovered.
     */
    public boolean isMenuHovered(HologramMenu menu) {
        return hoveredMenu == menu;
    }
}

