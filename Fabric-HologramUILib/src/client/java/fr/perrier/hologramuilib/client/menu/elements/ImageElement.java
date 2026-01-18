package fr.perrier.hologramuilib.client.menu.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.perrier.hologramuilib.client.config.ItemConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

/**
 * Image element for displaying textures in hologram menus.
 */
public class ImageElement extends AbstractMenuElement {

    private Identifier texture;
    private float imageWidth;
    private float imageHeight;
    private boolean maintainAspectRatio;

    public ImageElement(String id, Identifier texture) {
        super(id);
        this.texture = texture;
        this.imageWidth = 16;
        this.imageHeight = 16;
        this.maintainAspectRatio = true;
        this.width = 16;
        this.height = 16;
    }

    /**
     * Creates an image element from configuration.
     */
    public static ImageElement fromConfig(ItemConfig config) {
        // Assuming texture path is in icon.item field for now
        // TODO: Add proper texture field to ItemConfig
        Identifier texture = Identifier.of("minecraft", "textures/block/stone.png");
        ImageElement image = new ImageElement(config.getId(), texture);

        ItemConfig.ItemStyleConfig style = config.getStyle();
        if (style != null && style.getHeight() > 0) {
            image.height = style.getHeight();
            image.width = style.getHeight(); // Square by default
        }

        return image;
    }

    @Override
    public void render(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers,
                       float x, float y, boolean hovered, float tickDelta) {
        this.bounds = new Bounds(x, y, width, height);

        matrices.push();
        matrices.translate(x, y, 0);

        // Render the texture
        renderTexture(matrices, vertexConsumers, hovered);

        matrices.pop();
    }

    /**
     * Renders the texture quad.
     */
    private void renderTexture(MatrixStack matrices, VertexConsumerProvider vertexConsumers, boolean hovered) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Apply hover effect (slight brightness increase)
        float brightness = hovered ? 1.2f : 1.0f;

        // Bind the texture
        RenderSystem.setShaderTexture(0, texture);

        // Use GUI texture layer for proper rendering
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getText(texture));

        // Calculate UV coordinates (0,0 to 1,1 for full texture)
        float u0 = 0.0f;
        float v0 = 0.0f;
        float u1 = 1.0f;
        float v1 = 1.0f;

        // Draw textured quad
        // Note: For textures, we need proper UV mapping
        buffer.vertex(matrix, 0, 0, 0.01f)
              .color(brightness, brightness, brightness, 1.0f)
              .texture(u0, v0)
              .light(15728880);

        buffer.vertex(matrix, 0, height, 0.01f)
              .color(brightness, brightness, brightness, 1.0f)
              .texture(u0, v1)
              .light(15728880);

        buffer.vertex(matrix, width, height, 0.01f)
              .color(brightness, brightness, brightness, 1.0f)
              .texture(u1, v1)
              .light(15728880);

        buffer.vertex(matrix, width, 0, 0.01f)
              .color(brightness, brightness, brightness, 1.0f)
              .texture(u1, v0)
              .light(15728880);
    }

    // Getters and setters

    public Identifier getTexture() {
        return texture;
    }

    public void setTexture(Identifier texture) {
        this.texture = texture;
    }

    public float getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(float imageWidth) {
        this.imageWidth = imageWidth;
        if (maintainAspectRatio) {
            updateAspectRatio();
        }
    }

    public float getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(float imageHeight) {
        this.imageHeight = imageHeight;
        if (maintainAspectRatio) {
            updateAspectRatio();
        }
    }

    public boolean isMaintainAspectRatio() {
        return maintainAspectRatio;
    }

    public void setMaintainAspectRatio(boolean maintainAspectRatio) {
        this.maintainAspectRatio = maintainAspectRatio;
    }

    /**
     * Updates the display size to maintain aspect ratio.
     */
    private void updateAspectRatio() {
        if (imageWidth > 0 && imageHeight > 0) {
            float ratio = imageHeight / imageWidth;
            this.height = this.width * ratio;
        }
    }
}

