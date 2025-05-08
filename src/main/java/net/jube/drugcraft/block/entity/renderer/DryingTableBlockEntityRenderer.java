package net.jube.drugcraft.block.entity.renderer;

import net.jube.drugcraft.block.entity.custom.DryingTableBlockEntity;
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
    public DryingTableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {}

    @Override
    public void render(DryingTableBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {

        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        ItemStack inputStack = entity.getStack(0).copy(); // Raw flowers
        ItemStack outputStack = entity.getStack(1).copy(); // Dried flowers

        int inputCount = inputStack.isEmpty() ? 0 : inputStack.getCount();
        int outputCount = outputStack.isEmpty() ? 0 : outputStack.getCount();

        // Ensure we're rendering only up to 6 items (input + output)
        int totalCount = Math.min(6, inputCount + outputCount);
        if (totalCount == 0) return;

        matrices.push();
        matrices.translate(0.5f, 0.79f, 0.5f);
        matrices.scale(0.5f, 0.5f, 0.5f);

        int itemsRendered = 0;

        // Render input and output items gradually
        for (int i = 0; i < 6; i++, itemsRendered++) {
            matrices.push();
            float yOffset = itemsRendered * -0.259f;
            matrices.translate(0.0f, yOffset, 0.0f);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));

            if (i < inputCount && i >= outputCount) {
                // Render raw flower
                itemRenderer.renderItem(
                        inputStack.copyWithCount(1),
                        ModelTransformationMode.FIXED,
                        getLightLevel(entity.getWorld(), entity.getPos()),
                        OverlayTexture.DEFAULT_UV,
                        matrices, vertexConsumers,
                        entity.getWorld(), 0
                );
            } else if (i < outputCount) {
                // Render dried flower
                itemRenderer.renderItem(
                        outputStack.copyWithCount(1),
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
