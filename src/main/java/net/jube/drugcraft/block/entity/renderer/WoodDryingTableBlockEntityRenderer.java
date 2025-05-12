package net.jube.drugcraft.block.entity.renderer;

import net.jube.drugcraft.block.entity.custom.WoodDryingTableBlockEntity;
import net.jube.drugcraft.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class WoodDryingTableBlockEntityRenderer implements BlockEntityRenderer<WoodDryingTableBlockEntity> {
    public WoodDryingTableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    private static final int [] SHELF_SLOT_INDICES = {0, 1, 2, 3};


    @Override
    public void render(WoodDryingTableBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        matrices.push();
        // Adjust initial translation/scale if needed for your block model
        matrices.translate(0.5f, 0.79f, 0.5f); // Center of the block, adjusted height
        matrices.scale(0.2f, 0.2f, 0.2f); // Scale down item rendering

        // Loop through the shelves we want to render on (0 to 3)
        for (int shelfIndex = 0; shelfIndex < SHELF_SLOT_INDICES.length; shelfIndex++) {
            int inventorySlotIndex = SHELF_SLOT_INDICES[shelfIndex]; // Get the BE slot index for this shelf

            ItemStack stackToRender = entity.getStack(inventorySlotIndex); // Get the item from the BE inventory

            // Only render if the slot is not empty
            if (!stackToRender.isEmpty()) {
                matrices.push();

                // Calculate the vertical offset for this shelf
                float yOffset = shelfIndex * -0.259f; // Assuming shelves are stacked vertically

                // Apply translation for the current shelf position
                matrices.translate(0.0f, yOffset, 0.0f);

                // Optional: Add rotation for flavor (e.g., random slight rotation)
                // matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(entity.hashCode() % 360 + shelfIndex * 45f)); // Example rotation

                // Optional: Add translation for placement on the shelf (e.g., slight shift from center)
                // matrices.translate(0.0f, 0.0f, 0.0f); // Adjust Z if needed

                // Render the item (copying the stack with count 1 is good practice for rendering)
                itemRenderer.renderItem(
                        stackToRender.copyWithCount(1), // Render only one item
                        ModelTransformationMode.FIXED, // Or GUI, or THIRD_PERSON_RIGHT_HAND, etc. FIXED is common for fixed displays.
                        getLightLevel(entity.getWorld(), entity.getPos()), // Light calculation
                        OverlayTexture.DEFAULT_UV, // Overlay texture (fire, damage, etc.)
                        matrices, vertexConsumers,
                        entity.getWorld(), 0 // Last parameter is seed, 0 is fine
                );

                matrices.pop(); // Pop shelf transformation
            }
        }

        matrices.pop(); // Pop main transformation
    }




    // Calculate the light level for rendering
        private int getLightLevel (World world, BlockPos pos){
            int bLight = world.getLightLevel(LightType.BLOCK, pos);
            int sLight = world.getLightLevel(LightType.SKY, pos);
            return LightmapTextureManager.pack(bLight, sLight);
        }
    }

