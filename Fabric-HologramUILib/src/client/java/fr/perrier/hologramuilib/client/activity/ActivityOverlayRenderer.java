package fr.perrier.hologramuilib.client.activity;

import fr.perrier.hologramuilib.client.activity.ActivityTracker.PlayerActivity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Renders activity status above players' heads.
 */
public class ActivityOverlayRenderer {

    private static boolean enabled = true;
    private static double maxRenderDistance = 16.0;
    private static float textScale = 0.02f;

    /**
     * Renders the activity overlay for a player.
     */
    public static void render(PlayerEntity player, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.player.equals(player)) {
            return; // Don't render for self
        }

        // Check distance
        Vec3d playerPos = client.player.getPos();
        Vec3d targetPos = player.getPos();
        double distance = playerPos.distanceTo(targetPos);

        if (distance > maxRenderDistance) {
            return;
        }

        // Get activity
        PlayerActivity activity = ActivityTracker.getInstance().getActivity(player);
        if (activity == null || activity.getType() == ActivityType.IDLE) {
            return; // Don't show idle
        }

        matrices.push();

        // Position above player's head
        float yOffset = player.getHeight() + 0.5f;
        matrices.translate(0, yOffset, 0);

        // Billboard rotation (always face camera)
        matrices.multiply(client.gameRenderer.getCamera().getRotation());
        matrices.scale(-textScale, -textScale, textScale);

        // Prepare text
        String displayText = activity.getType().getFullDisplay();
        if (activity.getDurationSeconds() > 5) {
            displayText += " §7(" + activity.getFormattedDuration() + ")";
        }

        TextRenderer textRenderer = client.textRenderer;
        float textWidth = textRenderer.getWidth(displayText);
        float x = -textWidth / 2f;
        float y = 0;

        // Draw background
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        int backgroundColor = 0x80000000; // Semi-transparent black
        float padding = 2;

        // Note: Background rendering would need proper vertex consumer setup
        // For now, just render the text

        // Draw text
        textRenderer.draw(
            displayText,
            x,
            y,
            0xFFFFFFFF,
            true, // shadow
            matrix,
            vertexConsumers,
            TextRenderer.TextLayerType.SEE_THROUGH,
            0,
            light
        );

        matrices.pop();
    }

    /**
     * Enables or disables the activity overlay rendering.
     */
    public static void setEnabled(boolean enabled) {
        ActivityOverlayRenderer.enabled = enabled;
    }

    /**
     * Gets whether the overlay is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the maximum render distance.
     */
    public static void setMaxRenderDistance(double distance) {
        ActivityOverlayRenderer.maxRenderDistance = distance;
    }

    /**
     * Gets the maximum render distance.
     */
    public static double getMaxRenderDistance() {
        return maxRenderDistance;
    }

    /**
     * Sets the text scale.
     */
    public static void setTextScale(float scale) {
        ActivityOverlayRenderer.textScale = scale;
    }

    /**
     * Gets the text scale.
     */
    public static float getTextScale() {
        return textScale;
    }
}
