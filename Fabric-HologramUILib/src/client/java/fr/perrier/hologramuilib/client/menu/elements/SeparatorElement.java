package fr.perrier.hologramuilib.client.menu.elements;

import fr.perrier.hologramuilib.client.config.ItemConfig;
import fr.perrier.hologramuilib.util.ColorHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

/**
 * Separator element for visual separation between menu items.
 * Renders as a horizontal line.
 */
public class SeparatorElement extends AbstractMenuElement {

    private int color;

    public SeparatorElement(String id) {
        super(id);
        this.color = 0x40FFFFFF;
        this.height = 3; // Increased from 1 to 3 for better visibility
        this.width = 180;
    }

    /**
     * Creates a separator element from configuration.
     */
    public static SeparatorElement fromConfig(ItemConfig config) {
        SeparatorElement separator = new SeparatorElement(config.getId());

        if (config.getHeight() > 0) {
            separator.height = config.getHeight();
        }

        if (config.getColor() != null) {
            separator.color = config.getColorInt();
        }

        return separator;
    }

    @Override
    public void render(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers,
                       float x, float y, boolean hovered, float tickDelta) {
        this.bounds = new Bounds(x, y, width, height);

        matrices.push();
        matrices.translate(x, y, 0);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float a = ColorHelper.getAlpha(color) / 255f;
        float r = ColorHelper.getRed(color) / 255f;
        float g = ColorHelper.getGreen(color) / 255f;
        float b = ColorHelper.getBlue(color) / 255f;

        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getGui());

        // Draw horizontal line
        buffer.vertex(matrix, 0, height, 0).color(r, g, b, a);
        buffer.vertex(matrix, width, height, 0).color(r, g, b, a);
        buffer.vertex(matrix, width, 0, 0).color(r, g, b, a);
        buffer.vertex(matrix, 0, 0, 0).color(r, g, b, a);

        matrices.pop();
    }

    // Getters and setters

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}

