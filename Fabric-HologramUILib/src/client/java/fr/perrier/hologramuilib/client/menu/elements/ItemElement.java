package fr.perrier.hologramuilib.client.menu.elements;

import fr.perrier.hologramuilib.client.config.ItemConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Item element for displaying Minecraft items in hologram menus.
 * Renders items in 3D similar to inventory display.
 */
public class ItemElement extends AbstractMenuElement {

    private static final Logger LOGGER = LoggerFactory.getLogger("HologramUILib");

    private ItemStack itemStack;
    private float itemScale;
    private boolean showCount;

    public ItemElement(String id, ItemStack itemStack) {
        super(id);
        this.itemStack = itemStack;
        this.itemScale = 1.0f;
        this.showCount = false;
        this.width = 16;
        this.height = 16;
    }

    public ItemElement(String id, Item item) {
        this(id, new ItemStack(item));
    }

    /**
     * Creates an item element from configuration.
     */
    public static ItemElement fromConfig(ItemConfig config) {
        ItemStack stack = Items.STONE.getDefaultStack(); // Default

        // Parse item from icon config
        if (config.getIcon() != null) {
            String itemId = config.getIcon().getItem();
            if (itemId != null) {
                try {
                    Identifier id = Identifier.of(itemId);
                    Item item = Registries.ITEM.get(id);
                    if (item != null && item != Items.AIR) {
                        stack = new ItemStack(item);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to parse item ID: {}", itemId, e);
                }
            }
        }

        ItemElement element = new ItemElement(config.getId(), stack);

        ItemConfig.ItemStyleConfig style = config.getStyle();
        if (style != null && style.getHeight() > 0) {
            element.height = style.getHeight();
            element.width = style.getHeight(); // Square
            element.itemScale = style.getHeight() / 16.0f;
        }

        return element;
    }

    @Override
    public void render(MatrixStack matrices, TextRenderer textRenderer, VertexConsumerProvider vertexConsumers,
                       float x, float y, boolean hovered, float tickDelta) {
        this.bounds = new Bounds(x, y, width, height);

        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }

        matrices.push();
        matrices.translate(x, y, 0);

        // Render the item
        renderItem(matrices, vertexConsumers, textRenderer, hovered);

        matrices.pop();
    }

    /**
     * Renders the 3D item model.
     * TODO: Implement proper item rendering once API is clarified
     */
    private void renderItem(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
                           TextRenderer textRenderer, boolean hovered) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }

        matrices.push();

        // For now, render a simple placeholder box with item name
        // TODO: Implement proper 3D item rendering

        // Render a colored box as placeholder
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getDebugFilledBox());

        float brightness = hovered ? 0.8f : 0.6f;

        // Draw placeholder box
        buffer.vertex(matrix, 0, 0, 0.01f).color(brightness, brightness, brightness, 1.0f);
        buffer.vertex(matrix, 0, height, 0.01f).color(brightness, brightness, brightness, 1.0f);
        buffer.vertex(matrix, width, height, 0.01f).color(brightness, brightness, brightness, 1.0f);

        buffer.vertex(matrix, width, height, 0.01f).color(brightness, brightness, brightness, 1.0f);
        buffer.vertex(matrix, width, 0, 0.01f).color(brightness, brightness, brightness, 1.0f);
        buffer.vertex(matrix, 0, 0, 0.01f).color(brightness, brightness, brightness, 1.0f);

        // Render item name
        String itemName = itemStack.getItem().toString();
        if (itemName.length() > 10) {
            itemName = itemName.substring(0, 10);
        }

        float textX = 2;
        float textY = (height - 8) / 2;

        matrices.push();
        matrices.translate(textX, textY, 0.02f);
        matrices.scale(0.5f, 0.5f, 1.0f);

        textRenderer.draw(
            itemName,
            0,
            0,
            0xFFFFFFFF,
            false,
            matrices.peek().getPositionMatrix(),
            vertexConsumers,
            TextRenderer.TextLayerType.SEE_THROUGH,
            0,
            15728880
        );

        matrices.pop();
        matrices.pop();
    }

    // Getters and setters

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setItem(Item item) {
        this.itemStack = new ItemStack(item);
    }

    public float getItemScale() {
        return itemScale;
    }

    public void setItemScale(float itemScale) {
        this.itemScale = itemScale;
    }

    public boolean isShowCount() {
        return showCount;
    }

    public void setShowCount(boolean showCount) {
        this.showCount = showCount;
    }

    public void setWidth(float width) {
        this.width = width;
    }
}

