package net.jube.drugcraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.jube.drugcraft.block.ModBlocks;
import net.minecraft.client.render.RenderLayer;

public class DrugCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MARIJUANA_PLANT, RenderLayer.getCutout());

    }
}
