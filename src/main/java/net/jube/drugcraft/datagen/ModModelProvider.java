package net.jube.drugcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.jube.drugcraft.block.ModBlocks;
import net.jube.drugcraft.block.custom.MarijuanaPlantBlock;
import net.jube.drugcraft.item.ModItems;
import net.minecraft.data.client.*;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Identifier;

import java.util.Optional;


public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {

        itemModelGenerator.register(ModItems.MARIJUANA_LEAF, Models.GENERATED);
        itemModelGenerator.register(ModItems.MARIJUANA_FLOWER, Models.GENERATED);
        itemModelGenerator.register(ModItems.DRIED_MARIJUANA_FLOWER, Models.GENERATED);
        itemModelGenerator.register(ModItems.MARIJUANA_SEEDS, Models.GENERATED);
    }
}
