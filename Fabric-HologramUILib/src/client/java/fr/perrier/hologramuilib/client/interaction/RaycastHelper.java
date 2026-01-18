package fr.perrier.hologramuilib.client.interaction;

import fr.perrier.hologramuilib.client.menu.HologramMenu;
import fr.perrier.hologramuilib.client.menu.MenuElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class for raycasting against hologram menus in 3D space.
 * Performs ray-plane intersection tests to determine if the player is looking at a menu.
 */
public class RaycastHelper {

    /**
     * Result of a raycast test against a menu.
     */
    public static class RaycastResult {
        private final HologramMenu menu;
        private final Vec3d hitPos;
        private final float localX;
        private final float localY;
        private final MenuElement hoveredElement;

        public RaycastResult(HologramMenu menu, Vec3d hitPos, float localX, float localY, MenuElement hoveredElement) {
            this.menu = menu;
            this.hitPos = hitPos;
            this.localX = localX;
            this.localY = localY;
            this.hoveredElement = hoveredElement;
        }

        public HologramMenu getMenu() {
            return menu;
        }

        public Vec3d getHitPos() {
            return hitPos;
        }

        public float getLocalX() {
            return localX;
        }

        public float getLocalY() {
            return localY;
        }

        @Nullable
        public MenuElement getHoveredElement() {
            return hoveredElement;
        }

        public boolean hasElement() {
            return hoveredElement != null;
        }
    }

    /**
     * Performs a raycast from the player's view to check if they are looking at a menu.
     *
     * @param menu The menu to test against
     * @param maxDistance Maximum raycast distance
     * @return RaycastResult if hit, null otherwise
     */
    @Nullable
    public static RaycastResult raycastMenu(HologramMenu menu, double maxDistance) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.cameraEntity == null) {
            return null;
        }

        Entity camera = client.cameraEntity;
        Vec3d cameraPos = camera.getCameraPosVec(1.0f);
        Vec3d lookVec = camera.getRotationVec(1.0f);

        // Check if menu is within range first
        Vec3d menuPos = menu.getPosition();
        double distanceToMenu = cameraPos.distanceTo(menuPos);
        if (distanceToMenu > maxDistance || distanceToMenu > menu.getMaxRenderDistance()) {
            return null;
        }

        // Calculate menu's orientation
        float menuYaw = menu.getYaw();

        // Menu plane normal (the direction the menu faces)
        // The menu faces in the direction of its yaw
        double yawRad = Math.toRadians(menuYaw);
        Vec3d planeNormal = new Vec3d(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();

        // Ray-plane intersection
        Vec3d rayStart = cameraPos;
        Vec3d rayDir = lookVec.normalize();

        // Calculate intersection
        Vec3d planePoint = menuPos;
        double denominator = rayDir.dotProduct(planeNormal);

        // Check if ray is parallel to plane (or facing away)
        if (Math.abs(denominator) < 0.0001) {
            return null;
        }

        // Calculate distance along ray to intersection point
        double t = planePoint.subtract(rayStart).dotProduct(planeNormal) / denominator;

        // Check if intersection is behind camera or too far
        if (t < 0 || t > maxDistance) {
            return null;
        }

        // Calculate intersection point in world space
        Vec3d hitPos = rayStart.add(rayDir.multiply(t));

        // Transform hit position to menu's local space
        Vec3d localPos = worldToMenuLocal(hitPos, menu);

        float localX = (float) localPos.x;
        float localY = (float) localPos.y;

        // Check if hit is within menu bounds (centered at origin)
        float halfWidth = menu.getWidth() / 2f;
        float halfHeight = menu.getHeight() / 2f;

        if (localX < -halfWidth || localX > halfWidth ||
            localY < -halfHeight || localY > halfHeight) {
            return null;
        }

        // Convert to menu coordinates (top-left origin)
        float menuX = localX + halfWidth;
        float menuY = localY + halfHeight;

        // Find which element is hovered
        MenuElement hoveredElement = findElementAt(menu, menuX, menuY);

        return new RaycastResult(menu, hitPos, menuX, menuY, hoveredElement);
    }

    /**
     * Transforms a world position to menu local space.
     *
     * @param worldPos Position in world space
     * @param menu The menu
     * @return Position in menu local space (centered at origin)
     */
    private static Vec3d worldToMenuLocal(Vec3d worldPos, HologramMenu menu) {
        Vec3d menuPos = menu.getPosition();
        float menuYaw = menu.getYaw();

        // Translate to menu origin
        Vec3d translated = worldPos.subtract(menuPos);

        // Rotate by inverse of menu yaw
        // The menu is rendered with rotation (180 - menuYaw), so the inverse is -(180 - menuYaw) = menuYaw - 180
        // BUT, we need to negate the rotation to get the proper inverse transformation
        double yawRad = Math.toRadians(180 - menuYaw);
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);

        double rotatedX = translated.x * cos - translated.z * sin;
        double rotatedZ = translated.x * sin + translated.z * cos;

        // Scale by inverse of menu scale
        float invScale = 1.0f / menu.getScale();

        return new Vec3d(
            rotatedX * invScale,
            -translated.y * invScale, // Invert Y because menu rendering uses negative Y scale
            rotatedZ * invScale
        );
    }

    /**
     * Finds the menu element at the given menu coordinates.
     *
     * @param menu The menu
     * @param menuX X coordinate in menu space (0 = left edge)
     * @param menuY Y coordinate in menu space (0 = top edge)
     * @return The element at the position, or null if none
     */
    @Nullable
    private static MenuElement findElementAt(HologramMenu menu, float menuX, float menuY) {
        float padding = menu.getPadding();
        float spacing = menu.getSpacing();

        // Adjust for padding
        float elementAreaX = menuX - padding;
        float elementAreaY = menuY - padding;

        // Check if within element area
        if (elementAreaX < 0 || elementAreaY < 0) {
            return null;
        }

        float currentY = 0;

        for (MenuElement element : menu.getElements()) {
            float elementHeight = element.getHeight();

            // Check if Y is within this element
            if (elementAreaY >= currentY && elementAreaY < currentY + elementHeight) {
                // Check X bounds
                float elementWidth = element.getWidth();
                if (elementAreaX >= 0 && elementAreaX < elementWidth) {
                    return element;
                }
            }

            currentY += elementHeight + spacing;
        }

        return null;
    }
}

