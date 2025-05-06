package net.jube.drugcraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.jube.drugcraft.block.ModBlocks;
import net.jube.drugcraft.block.entity.ModBlockEntities;
import net.jube.drugcraft.block.entity.renderer.DryingTableBlockEntityRenderer;
import net.jube.drugcraft.particle.MarijuanaPlantParticle;
import net.jube.drugcraft.particle.ModParticles;
import net.jube.drugcraft.screen.ModScreenHandlers;
import net.jube.drugcraft.screen.custom.DryingTableScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;

public class DrugCraftClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MARIJUANA_PLANT, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DRYING_TABLE, RenderLayer.getCutout());

        ParticleFactoryRegistry.getInstance().register(ModParticles.MARIJUANA_PLANT_PARTICLE, MarijuanaPlantParticle.Factory::new);

        BlockEntityRendererFactories.register(ModBlockEntities.DRYINGTABLE_BE, DryingTableBlockEntityRenderer::new);
        HandledScreens.register(ModScreenHandlers.DRYINGTABLE_SCREEN_HANDLER, DryingTableScreen::new);

    }
}
