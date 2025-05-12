package net.jube.drugcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.jube.drugcraft.block.ModBlocks;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.HOE_MINEABLE)
                .add(ModBlocks.BLOCK_OF_DRIED_MARIJUANA)
                .add(ModBlocks.BLOCK_OF_MARIJUANA);

        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE)
                .add(ModBlocks.TABLE)
                .add(ModBlocks.ACACIA_DRYING_TABLE)
                .add(ModBlocks.BAMBOO_DRYING_TABLE)
                .add(ModBlocks.BIRCH_DRYING_TABLE)
                .add(ModBlocks.CHERRY_DRYING_TABLE)
                .add(ModBlocks.DARK_OAK_DRYING_TABLE)
                .add(ModBlocks.JUNGLE_DRYING_TABLE)
                .add(ModBlocks.MANGROVE_DRYING_TABLE)
                .add(ModBlocks.OAK_DRYING_TABLE)
                .add(ModBlocks.SPRUCE_DRYING_TABLE);


        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.DRYING_TABLE);
    }
}
