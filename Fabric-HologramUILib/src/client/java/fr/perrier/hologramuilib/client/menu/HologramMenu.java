package fr.perrier.hologramuilib.client.menu;

import fr.perrier.hologramuilib.client.animation.AnimationManager;
import fr.perrier.hologramuilib.client.animation.Animatable;
import fr.perrier.hologramuilib.client.config.LayoutConfig;
import fr.perrier.hologramuilib.client.config.MenuConfig;
import fr.perrier.hologramuilib.client.config.StyleConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a holographic menu that floats in 3D world space.
 * Supports configurable positioning, styling, and contains multiple menu elements.
 */
public class HologramMenu {

    private final String id;
    private Vec3d position;
    private float yaw; // Rotation around Y axis
    private final List<MenuElement> elements;
    private AnimationManager animationManager;

    // Style properties
    private float width;
    private float height;
    private float scale;
    private double maxRenderDistance;

    // Background
    private boolean hasBackground;
    private int backgroundColor;

    // Border
    private boolean hasBorder;
    private int borderColor;
    private float borderWidth;

    // Layout
    private LayoutConfig.LayoutType layoutType;
    private float padding;
    private float spacing;
    private LayoutConfig.Alignment alignment;

    // State
    private boolean visible;
    private MenuElement hoveredElement;
    private boolean autoHeight; // Track if height should be auto-calculated

    // Auto-update system
    private boolean autoUpdate = false;
    private long updateIntervalMs = 500; // Default 500ms
    private long lastUpdateTime = 0;
    private Runnable updateCallback = null;

    /**
     * Creates a new hologram menu with the given ID.
     *
     * @param id Unique identifier for this menu
     */
    public HologramMenu(String id) {
        this.id = id;
        this.elements = new ArrayList<>();
        this.position = Vec3d.ZERO;
        this.yaw = 0;

        // Default values
        this.width = 200;
        this.height = 100;
        this.scale = 0.025f;
        this.maxRenderDistance = 10.0;

        this.hasBackground = true;
        this.backgroundColor = 0x80000000; // Semi-transparent black

        this.hasBorder = true;
        this.borderColor = 0xFFFFFFFF; // White
        this.borderWidth = 2;

        this.layoutType = LayoutConfig.LayoutType.VERTICAL;
        this.padding = 8;
        this.spacing = 4;
        this.alignment = LayoutConfig.Alignment.CENTER;

        this.visible = true;
        this.autoHeight = false;
    }

    /**
     * Creates a new hologram menu from a configuration.
     *
     * @param id The menu ID
     * @param config The menu configuration
     */
    public HologramMenu(String id, MenuConfig config) {
        this(id);
        applyConfig(config);
    }

    /**
     * Applies a menu configuration to this menu.
     *
     * @param config The configuration to apply
     */
    public void applyConfig(MenuConfig config) {
        StyleConfig style = config.getStyle();
        if (style != null) {
            this.width = style.getWidth();
            this.height = style.getHeight();
            this.scale = style.getScale();
            this.maxRenderDistance = style.getMaxRenderDistance();

            StyleConfig.BackgroundConfig bg = style.getBackground();
            if (bg != null) {
                this.hasBackground = bg.isEnabled();
                this.backgroundColor = bg.getColorInt();
            }

            StyleConfig.BorderConfig border = style.getBorder();
            if (border != null) {
                this.hasBorder = border.isEnabled();
                this.borderColor = border.getColorInt();
                this.borderWidth = border.getWidth();
            }
        }

        LayoutConfig layout = config.getLayout();
        if (layout != null) {
            this.layoutType = layout.getType();
            this.padding = layout.getPadding();
            this.spacing = layout.getSpacing();
            this.alignment = layout.getAlignment();
        }
    }

    /**
     * Renders all elements in this menu.
     */
    public void render(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers, float tickDelta) {
        if (!visible || elements.isEmpty()) {
            return;
        }

        // Apply padding and Z offset to render elements in front of menu background
        matrices.push();
        matrices.translate(padding, padding, 0.1f); // 0.1 Z offset to prevent z-fighting

        float currentY = 0;

        for (MenuElement element : elements) {
            float elementX = calculateElementX(element);
            boolean hovered = element == hoveredElement;

            element.render(matrices, textRenderer, vertexConsumers, elementX, currentY, hovered, tickDelta);

            if (layoutType == LayoutConfig.LayoutType.VERTICAL) {
                currentY += element.getHeight() + spacing;
            }
        }

        matrices.pop();
    }

    /**
     * Calculates the X position for an element based on alignment.
     */
    private float calculateElementX(MenuElement element) {
        float availableWidth = width - (padding * 2);

        return switch (alignment) {
            case CENTER -> (availableWidth - element.getWidth()) / 2;
            case RIGHT -> availableWidth - element.getWidth();
            default -> 0; // LEFT
        };
    }

    /**
     * Calculates the total content height including padding and spacing.
     */
    public float calculateContentHeight() {
        if (elements.isEmpty()) {
            return padding * 2;
        }

        float totalHeight = padding * 2;
        for (int i = 0; i < elements.size(); i++) {
            totalHeight += elements.get(i).getHeight();
            if (i < elements.size() - 1) {
                totalHeight += spacing;
            }
        }
        return totalHeight;
    }

    /**
     * Recalculates the height if auto-height mode is enabled.
     * Also adjusts the position to prevent clipping through blocks.
     */
    public void recalculateAutoHeight() {
        if (autoHeight) {
            this.height = calculateContentHeight();
            // Recalculate position to account for new height
            this.position = adjustPositionForCollision(this.position);
        }
    }

    // Element management

    public void addElement(MenuElement element) {
        elements.add(element);
        // Set animation manager if element supports animations
        if (element instanceof Animatable animatable && animationManager != null) {
            animatable.setAnimationManager(animationManager);
        }
        recalculateAutoHeight();
    }

    public void removeElement(MenuElement element) {
        elements.remove(element);
        recalculateAutoHeight();
    }

    public void clearElements() {
        elements.clear();
    }

    public List<MenuElement> getElements() {
        return elements;
    }

    /**
     * Finds an element by its ID.
     *
     * @param elementId The ID of the element to find
     * @return The element with the given ID, or null if not found
     */
    public MenuElement getElementById(String elementId) {
        for (MenuElement element : elements) {
            if (element.getId().equals(elementId)) {
                return element;
            }
        }
        return null;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public Vec3d getPosition() {
        return position;
    }

    public void setPosition(Vec3d position) {
        this.position = adjustPositionForCollision(position);
    }

    /**
     * Adjusts the menu position to prevent it from clipping through blocks.
     * If the bottom of the menu would be below a solid block, the position is raised.
     *
     * @param requestedPosition The desired position
     * @return The adjusted position
     */
    private Vec3d adjustPositionForCollision(Vec3d requestedPosition) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;

        if (world == null) {
            return requestedPosition;
        }

        // Calculate the bottom Y position of the menu in world space
        // The menu is rendered centered, so we need to account for scale and height
        float menuHeightInWorld = (height / 2f) * scale;
        double bottomY = requestedPosition.y - menuHeightInWorld;

        // Check the block at the bottom of the menu
        BlockPos bottomPos = BlockPos.ofFloored(requestedPosition.x, bottomY, requestedPosition.z);

        // If there's a solid block at or above the bottom, adjust the position
        if (world.getBlockState(bottomPos).isSolidBlock(world, bottomPos)) {
            // Move the menu up so its bottom is just above the block
            double adjustedY = bottomPos.getY() + 1.0 + menuHeightInWorld + 0.1; // +0.1 for small margin
            return new Vec3d(requestedPosition.x, adjustedY, requestedPosition.z);
        }

        return requestedPosition;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        if (height < 0) {
            this.autoHeight = true;
            this.height = calculateContentHeight();
        } else {
            this.autoHeight = false;
            this.height = height;
        }
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public double getMaxRenderDistance() {
        return maxRenderDistance;
    }

    public void setMaxRenderDistance(double maxRenderDistance) {
        this.maxRenderDistance = maxRenderDistance;
    }

    public boolean hasBackground() {
        return hasBackground;
    }

    public void setHasBackground(boolean hasBackground) {
        this.hasBackground = hasBackground;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean hasBorder() {
        return hasBorder;
    }

    public void setHasBorder(boolean hasBorder) {
        this.hasBorder = hasBorder;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int borderColor) {
        this.borderColor = borderColor;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public float getPadding() {
        return padding;
    }

    public void setPadding(float padding) {
        this.padding = padding;
    }

    public float getSpacing() {
        return spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public MenuElement getHoveredElement() {
        return hoveredElement;
    }

    public void setHoveredElement(MenuElement hoveredElement) {
        if (this.hoveredElement != hoveredElement) {
            if (this.hoveredElement != null) {
                this.hoveredElement.onHoverEnd();
            }
            this.hoveredElement = hoveredElement;
            if (hoveredElement != null) {
                hoveredElement.onHoverStart();
            }
        }
    }

    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    public void setAnimationManager(AnimationManager animationManager) {
        this.animationManager = animationManager;
        // Update all existing elements
        for (MenuElement element : elements) {
            if (element instanceof Animatable animatable) {
                animatable.setAnimationManager(animationManager);
            }
        }
    }

    /**
     * Updates the menu if auto-update is enabled.
     * Should be called every frame by the renderer.
     */
    public void update() {
        if (!autoUpdate || updateCallback == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= updateIntervalMs) {
            updateCallback.run();
            lastUpdateTime = currentTime;
        }
    }

    /**
     * Enables auto-update with a callback that will be called at the specified interval.
     *
     * @param intervalMs Update interval in milliseconds
     * @param callback The callback to run on each update
     */
    public void setAutoUpdate(long intervalMs, Runnable callback) {
        this.autoUpdate = true;
        this.updateIntervalMs = intervalMs;
        this.updateCallback = callback;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Disables auto-update.
     */
    public void disableAutoUpdate() {
        this.autoUpdate = false;
        this.updateCallback = null;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public long getUpdateInterval() {
        return updateIntervalMs;
    }
}

