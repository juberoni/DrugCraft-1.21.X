package net.jube.drugcraft.block.entity.renderer;

import net.jube.drugcraft.block.entity.custom.DryingTableBlockEntity;
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
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;

public class DryingTableBlockEntityRenderer implements BlockEntityRenderer<DryingTableBlockEntity> {
    public DryingTableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;



    @Override
    public void render(DryingTableBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        ItemStack inputStack = entity.getStack(INPUT_SLOT).copy(); // Raw flowers
        ItemStack outputStack = entity.getStack(OUTPUT_SLOT).copy(); // Dried flowers

        int totalShelves = 6;
        int currentRawFlowerCount = inputStack.getCount();
        int currentDriedFlowerCount = outputStack.getCount();

        matrices.push();
        matrices.translate(0.5f, 0.79f, 0.5f);
        matrices.scale(0.5f, 0.5f, 0.5f);

        for (int shelfIndex = 0; shelfIndex < totalShelves; shelfIndex++) {
            matrices.push();
            float yOffset = shelfIndex * -0.259f;
            matrices.translate(0.0f, yOffset, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));

            ItemStack stackToRenderOnThisShelf = ItemStack.EMPTY;

            // Logic to determine what to render:
            if (shelfIndex < currentDriedFlowerCount) {
                // This shelf is for a dried flower.
                // We need to make sure outputStack.getItem() is actually the dried flower item.
                if (!outputStack.isEmpty() && outputStack.isOf(ModItems.DRIED_MARIJUANA_FLOWER)) { // Explicit check
                    stackToRenderOnThisShelf = outputStack.copyWithCount(1);
                } else if (!outputStack.isEmpty()) {
                    // This case should ideally not happen if logic is correct.
                    // It means output slot has something, but it's not a dried flower.
                    // For debugging, maybe render it anyway to see what it is? Or log.
                    System.out.println("RENDERER WARNING: Shelf " + shelfIndex + " expected dried, but outputStack is: " + outputStack.getItem());
                }
            } else if (shelfIndex < currentDriedFlowerCount + currentRawFlowerCount) {
                // This shelf is for a raw flower (after all dried ones are placed)
                if (!inputStack.isEmpty() && inputStack.isOf(ModItems.MARIJUANA_FLOWER)) { // Explicit check
                    stackToRenderOnThisShelf = inputStack.copyWithCount(1);
                } else if (!inputStack.isEmpty()) {
                    System.out.println("RENDERER WARNING: Shelf " + shelfIndex + " expected raw, but inputStack is: " + inputStack.getItem());
                }
            }

            if (!stackToRenderOnThisShelf.isEmpty()) {
                itemRenderer.renderItem(
                        stackToRenderOnThisShelf,
                        ModelTransformationMode.FIXED,
                        getLightLevel(entity.getWorld(), entity.getPos()),
                        OverlayTexture.DEFAULT_UV,
                        matrices, vertexConsumers,
                        entity.getWorld(), 0
                );
            }
            matrices.pop();
        }
        matrices.pop();
    }

    // Calculate the light level for rendering
    private int getLightLevel(World world, BlockPos pos) {
        int bLight = world.getLightLevel(LightType.BLOCK, pos);
        int sLight = world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(bLight, sLight);
    }
}
