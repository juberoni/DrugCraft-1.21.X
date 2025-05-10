package net.jube.drugcraft.block.entity.renderer;

import net.jube.drugcraft.block.entity.custom.DryingTableBlockEntity;
import net.jube.drugcraft.item.ModItems; // Keep if ModItems.DRYING_FAN is used explicitly
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
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

public class DryingTableBlockEntityRenderer implements BlockEntityRenderer<DryingTableBlockEntity> {
    public DryingTableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(DryingTableBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        // Fan render
        ItemStack fanStackInSlot = entity.getStack(2); // Slot 2 is FAN_SLOT
        if (!fanStackInSlot.isEmpty()) {
            matrices.push();
            matrices.translate(0.52, 1.4f, 0.51f); // Adjust as needed for your model
            matrices.scale(1.2f, 1.2f, 1.2f);    // Adjust as needed

            if (entity.isCrafting()) { // isCrafting() from BlockEntity
                float spin = 0;
                if (entity.getWorld() != null) { // getWorld can be null during initial load sometimes
                    spin = (float)(entity.getWorld().getTime() + tickDelta) * 10; // Slower spin: * 10 instead of * 50
                }
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(spin % 360));
            } else {
                // Optional: Set a default rotation if not spinning, or remove if not needed.
                // matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(0f));
            }

            itemRenderer.renderItem(
                    fanStackInSlot, // Use the actual ItemStack from the fan slot
                    ModelTransformationMode.GROUND, // Or FIXED, experiment for best look
                    getLightLevel(entity.getWorld(), entity.getPos().up()), // Light from above block
                    OverlayTexture.DEFAULT_UV,
                    matrices, vertexConsumers,
                    entity.getWorld(), 0 // Seed for item model variations
            );
            matrices.pop();
        }

        // Shelf item rendering
        int itemsOnShelves = entity.getItemsTakenForBatch();
        int numDriedItems = entity.getItemsDriedThisBatch();
        ItemStack rawItemToRender = entity.getBatchItemType();
        ItemStack driedItemToRender = entity.getDriedOutputItemType();

        // Only render shelf items if there's a batch active and items defined
        if (itemsOnShelves <= 0 || rawItemToRender.isEmpty()) {
            // System.out.println("No batch active or raw item undefined. Shelves empty.");
            return;
        }

        // Debugging output
        System.out.println("Rendering Shelves: Total In Batch=" + itemsOnShelves +
                ",Dried=" + numDriedItems +
                ", RawType=" + rawItemToRender.getItem() +
                ", DriedType=" + (driedItemToRender.isEmpty() ? "None" : driedItemToRender.getItem()));

        if (itemsOnShelves <= 0 || rawItemToRender.isEmpty()) {
            System.out.println("Render Tick -- CONDITION MET, returning early."); // Add this
            return;
        }


        matrices.push();

        // Center point for the stack of items on shelves
        matrices.translate(0.5f, 0.79f, 0.5f); // Adjust Y for base shelf height
        matrices.scale(0.5f, 0.5f, 0.5f);       // Scale of items on shelves

        // Loop for up to MAX_SHELVES or the number of items in the current batch
        for (int i = 0; i < Math.min(itemsOnShelves, DryingTableBlockEntity.MAX_SHELVES); i++) {
            matrices.push();
            // Y offset for each shelf, items stack downwards.
            // 0.259f is an example, adjust for your item size and desired spacing
            float yOffset = i * -0.259f;
            matrices.translate(0.0f, yOffset, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f)); // Lay items flat

            ItemStack stackToDisplay;

            if (i < numDriedItems) {
                // This shelf (index i) should display a dried item
                if (driedItemToRender.isEmpty()) {
                    // This case should ideally not happen if logic in BE is correct
                    // System.err.println("Shelf " + i + ": Dried item type is empty, but numDriedItems (" + numDriedItems + ") indicates it should be dried.");
                    matrices.pop(); // Pop for this item's matrix
                    continue;     // Skip rendering this problematic item
                }
                stackToDisplay = driedItemToRender.copyWithCount(1);
                // System.out.println("Shelf " + i + ": Rendering DRIED " + stackToDisplay.getItem());
            } else {
                // This shelf (index i) should display a raw item
                // rawItemToRender should not be empty if itemsOnShelves > 0
                stackToDisplay = rawItemToRender.copyWithCount(1);
                // System.out.println("Shelf " + i + ": Rendering RAW " + stackToDisplay.getItem());
            }

            if (!stackToDisplay.isEmpty()) {
                itemRenderer.renderItem(
                        stackToDisplay,
                        ModelTransformationMode.FIXED, // FIXED is often better for items displayed in-world
                        getLightLevel(entity.getWorld(), entity.getPos().up()), // Light from one block above
                        OverlayTexture.DEFAULT_UV,
                        matrices, vertexConsumers,
                        entity.getWorld(), 0 // Seed
                );
            }
            matrices.pop(); // Pop for individual item's transformations
        }
        matrices.pop(); // Pop for shelf group's transformations
    }

    private int getLightLevel(World world, BlockPos pos) {
        if (world == null) return LightmapTextureManager.pack(15, 15); // Full bright if world is somehow null
        int bLight = world.getLightLevel(LightType.BLOCK, pos);
        int sLight = world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(bLight, sLight);
    }
}