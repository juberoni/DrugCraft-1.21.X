package net.jube.drugcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.jube.drugcraft.block.ModBlocks;
import net.jube.drugcraft.item.ModItems;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        //put SIMPLE blockstates here (no complex UV maps)
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(ModItems.MARIJUANA_LEAF, Models.GENERATED);
        itemModelGenerator.register(ModItems.MARIJUANA_FLOWER, Models.GENERATED);
        itemModelGenerator.register(ModItems.MARIJUANA_SEEDS, Models.GENERATED);
        itemModelGenerator.register(ModItems.DRIED_MARIJUANA_FLOWER, Models.GENERATED);


    }
}
