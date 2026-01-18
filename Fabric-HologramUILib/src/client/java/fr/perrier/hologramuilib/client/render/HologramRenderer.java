package fr.perrier.hologramuilib.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.perrier.hologramuilib.client.interaction.InteractionHandler;
import fr.perrier.hologramuilib.client.interaction.RaycastHelper;
import fr.perrier.hologramuilib.client.menu.HologramMenu;
import fr.perrier.hologramuilib.client.menu.MenuElement;
import fr.perrier.hologramuilib.client.menu.MenuManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Main renderer for holographic menus in 3D world space.
 * Handles billboard rotation, distance-based rendering, and layer rendering.
 */
public class HologramRenderer {

    private static HologramRenderer INSTANCE;

    private final MinecraftClient client;
    private boolean debugMode = false;

    private HologramRenderer() {
        this.client = MinecraftClient.getInstance();
    }

    public static HologramRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HologramRenderer();
        }
        return INSTANCE;
    }

    /**
     * Registers the renderer with Fabric's world render events.
     * Call this during client initialization.
     */
    public void register() {
        WorldRenderEvents.LAST.register(this::onWorldRender);
    }

    /**
     * Called by Fabric's world render event system.
     */
    private void onWorldRender(WorldRenderContext context) {
        render(context.camera(), context.tickCounter().getTickDelta(true));
    }

    /**
     * Toggles debug mode for showing hitboxes.
     */
    public void toggleDebugMode() {
        this.debugMode = !this.debugMode;
    }

    /**
     * Returns whether debug mode is enabled.
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Sets debug mode.
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Main render method.
     *
     * @param camera The camera used for rendering
     * @param tickDelta Partial tick for smooth interpolation
     */
    public void render(Camera camera, float tickDelta) {
        if (client.world == null || client.player == null) {
            return;
        }

        MenuManager menuManager = MenuManager.getInstance();
        if (menuManager.getActiveMenus().isEmpty()) {
            return;
        }

        Vec3d cameraPos = camera.getPos();

        // Setup render state for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        for (HologramMenu menu : menuManager.getActiveMenus()) {
            renderMenu(menu, camera, cameraPos, tickDelta);
        }

        // Render debug raycast line if debug mode is enabled
        if (debugMode) {
            renderDebugRaycast(camera, cameraPos);
        }

        // Restore render state
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Renders a single hologram menu.
     */
    private void renderMenu(HologramMenu menu, Camera camera, Vec3d cameraPos, float tickDelta) {
        Vec3d menuPos = menu.getPosition();

        // Distance-based culling
        double distance = cameraPos.distanceTo(menuPos);
        if (distance > menu.getMaxRenderDistance()) {
            return;
        }

        // Update menu if auto-update is enabled
        menu.update();

        // Update hover state from InteractionHandler
        InteractionHandler interactionHandler = InteractionHandler.getInstance();
        if (interactionHandler.isMenuHovered(menu)) {
            menu.setHoveredElement(interactionHandler.getHoveredElement());
        } else {
            menu.setHoveredElement(null);
        }

        // Create matrix stack for transformations
        MatrixStack matrices = new MatrixStack();

        // Translate to menu position relative to camera
        matrices.translate(
            menuPos.x - cameraPos.x,
            menuPos.y - cameraPos.y,
            menuPos.z - cameraPos.z
        );

        // Apply menu rotation (fixed orientation based on menu's yaw)
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180-menu.getYaw()));

        // Apply menu scale (negative X to flip horizontally for correct text orientation)
        float scale = menu.getScale();
        matrices.scale(scale, -scale, scale); //That is good don't touch

        // Get vertex consumers for rendering
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();

        // Render the menu content
        renderMenuContent(menu, matrices, immediate, tickDelta, distance);

        // Draw all buffered vertices
        immediate.draw();
    }


    /**
     * Renders the actual content of a menu (background, elements, etc.)
     */
    private void renderMenuContent(HologramMenu menu, MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, float tickDelta, double distance) {
        TextRenderer textRenderer = client.textRenderer;

        float width = menu.getWidth();
        float height = menu.getHeight();

        // Center the menu
        float offsetX = -width / 2f;
        float offsetY = -height / 2f;

        matrices.push();
        matrices.translate(offsetX, offsetY, 0);

        // Render background first (behind everything)
        if (menu.hasBackground()) {
            renderBackground(matrices, vertexConsumers, width, height, menu.getBackgroundColor());
        }

        // Render border
        if (menu.hasBorder()) {
            renderBorder(matrices, vertexConsumers, width, height, menu.getBorderColor(), menu.getBorderWidth());
        }

        // Render elements
        menu.render(matrices, textRenderer, vertexConsumers, tickDelta);

        // Render debug hitboxes if enabled
        if (debugMode) {
            renderDebugHitboxes(menu, matrices, vertexConsumers);
            renderDirectionIndicator(matrices, vertexConsumers, width, height);
        }

        matrices.pop();
    }

    /**
     * Renders a semi-transparent background for the menu.
     */
    private void renderBackground(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float width, float height, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // Use a render layer that works in 3D world space
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());

        // Draw background quad (two triangles)
        // First triangle
        buffer.vertex(matrix, 0, 0, -0.01f).color(r, g, b, a);
        buffer.vertex(matrix, 0, height, -0.01f).color(r, g, b, a);
        buffer.vertex(matrix, width, height, -0.01f).color(r, g, b, a);
        // Second triangle
        buffer.vertex(matrix, width, height, -0.01f).color(r, g, b, a);
        buffer.vertex(matrix, width, 0, -0.01f).color(r, g, b, a);
        buffer.vertex(matrix, 0, 0, -0.01f).color(r, g, b, a);
    }

    /**
     * Renders a border around the menu.
     */
    private void renderBorder(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float width, float height, int color, float borderWidth) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(borderWidth));

        // Draw border lines
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a);
        buffer.vertex(matrix, width, 0, 0).color(r, g, b, a);
        buffer.vertex(matrix, width, height, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, height, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a);
    }

    /**
     * Renders debug hitboxes for all elements in the menu.
     */
    private void renderDebugHitboxes(HologramMenu menu, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        matrices.push();
        matrices.translate(menu.getPadding(), menu.getPadding(), 0.02f);

        float currentY = 0;
        int colorIndex = 0;
        int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF, 0xFF00FFFF};

        List<MenuElement> elements = menu.getElements();
        for (int i = 0; i < elements.size(); i++) {
            MenuElement element = elements.get(i);
            int color = colors[colorIndex % colors.length];
            renderElementHitbox(matrices, vertexConsumers, element, currentY, color);

            currentY += element.getHeight();
            // Only add spacing if not the last element
            if (i < elements.size() - 1) {
                currentY += menu.getSpacing();
            }

            colorIndex++;
        }

        matrices.pop();
    }

    /**
     * Renders a debug hitbox outline for a single element.
     */
    private void renderElementHitbox(MatrixStack matrices, VertexConsumerProvider vertexConsumers, MenuElement element, float y, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        float w = element.getWidth();
        float h = element.getHeight();

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(2.0));

        // Draw element hitbox outline
        buffer.vertex(matrix, 0, y, 0).color(r, g, b, a);
        buffer.vertex(matrix, w, y, 0).color(r, g, b, a);
        buffer.vertex(matrix, w, y + h, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, y + h, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, y, 0).color(r, g, b, a);
    }

    /**
     * Renders a direction indicator line showing which way the menu is facing.
     * The line extends from the center of the menu outward in the direction it's facing.
     */
    private void renderDirectionIndicator(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float width, float height) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Bright cyan color for the direction line
        float r = 0.0f;
        float g = 1.0f;
        float b = 1.0f;
        float a = 1.0f;

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(3.0));

        // Start from center of menu
        float centerX = width / 2f;
        float centerY = height / 2f;

        // End point in front of the menu (negative Z because menu faces -Z direction)
        float lineLength = 20f; // 20 pixels in front

        // Draw line from center pointing forward (in the direction the menu faces)
        buffer.vertex(matrix, centerX, centerY, 0).color(r, g, b, a);
        buffer.vertex(matrix, centerX, centerY, -lineLength).color(r, g, b, a);
    }

    /**
     * Renders a neon line showing the player's raycast in debug mode.
     * The line goes from the player's eye position to either:
     * - The hit point on a menu (if hit)
     * - A point far away in the look direction (if no hit)
     */
    private void renderDebugRaycast(Camera camera, Vec3d cameraPos) {
        if (client.player == null || client.cameraEntity == null) {
            return;
        }

        Entity cameraEntity = client.cameraEntity;
        Vec3d eyePos = cameraEntity.getCameraPosVec(1.0f);
        Vec3d lookVec = cameraEntity.getRotationVec(1.0f);

        // Check for raycast hits on menus
        InteractionHandler interactionHandler = InteractionHandler.getInstance();
        RaycastHelper.RaycastResult raycastResult = interactionHandler.getLastRaycastResult();

        Vec3d endPos;
        float lineR, lineG, lineB;

        if (raycastResult != null && raycastResult.getHitPos() != null) {
            // Hit a menu - draw neon green line to hit point
            endPos = raycastResult.getHitPos();
            lineR = 0.0f;
            lineG = 1.0f;
            lineB = 0.3f;
        } else {
            // No hit - draw neon cyan line extending far
            double maxDistance = 50.0;
            endPos = eyePos.add(lookVec.multiply(maxDistance));
            lineR = 0.0f;
            lineG = 0.8f;
            lineB = 1.0f;
        }

        // Create matrix stack for world space rendering
        MatrixStack matrices = new MatrixStack();
        matrices.translate(
            eyePos.x - cameraPos.x,
            eyePos.y - cameraPos.y,
            eyePos.z - cameraPos.z
        );

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Get vertex consumer for rendering lines
        VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = immediate.getBuffer(RenderLayer.getDebugLineStrip(5.0)); // Thicker line for better visibility

        // Calculate end position relative to start
        Vec3d relativeEnd = endPos.subtract(eyePos);

        // Draw the neon raycast line with gradient effect
        float alpha = 1.0f;

        // Start point (brighter)
        buffer.vertex(matrix, 0, 0, 0).color(lineR * 1.5f, lineG * 1.5f, lineB * 1.5f, alpha);

        // End point (normal brightness)
        buffer.vertex(matrix, (float)relativeEnd.x, (float)relativeEnd.y, (float)relativeEnd.z)
              .color(lineR, lineG, lineB, alpha * 0.7f);

        // Draw a hit point marker if we hit something
        if (raycastResult != null && raycastResult.getHitPos() != null) {
            renderHitMarker(raycastResult.getHitPos(), cameraPos, immediate);
        }

        immediate.draw();
    }

    /**
     * Renders a small cross marker at the raycast hit point.
     */
    private void renderHitMarker(Vec3d hitPos, Vec3d cameraPos, VertexConsumerProvider.Immediate vertexConsumers) {
        MatrixStack matrices = new MatrixStack();
        matrices.translate(
            hitPos.x - cameraPos.x,
            hitPos.y - cameraPos.y,
            hitPos.z - cameraPos.z
        );

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugLineStrip(6.0)); // Thicker marker to match raycast line

        float size = 0.1f;
        float r = 1.0f;
        float g = 0.0f;
        float b = 0.0f;
        float a = 1.0f;

        // Draw X axis line
        buffer.vertex(matrix, -size, 0, 0).color(r, g, b, a);
        buffer.vertex(matrix, size, 0, 0).color(r, g, b, a);

        // Draw Y axis line
        buffer.vertex(matrix, 0, -size, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, size, 0).color(r, g, b, a);

        // Draw Z axis line
        buffer.vertex(matrix, 0, 0, -size).color(r, g, b, a);
        buffer.vertex(matrix, 0, 0, size).color(r, g, b, a);
    }
}
