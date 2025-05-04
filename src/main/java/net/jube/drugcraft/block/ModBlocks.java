package net.jube.drugcraft.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.jube.drugcraft.DrugCraft;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {
    public static final Block BLOCK_OF_MARIJUANA = registerBlock("block_of_marijuana",
            new Block(AbstractBlock.Settings.create().strength(1f)
                    .sounds(BlockSoundGroup.GRASS)));

    public static final Block BLOCK_OF_DRIED_MARIJUANA = registerBlock("block_of_dried_marijuana",
            new Block(AbstractBlock.Settings.create().strength(2f)
                    .sounds(BlockSoundGroup.GRASS)));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(DrugCraft.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(DrugCraft.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks() {
        DrugCraft.LOGGER.info("Registering Mod Blocks for " + DrugCraft.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(ModBlocks.BLOCK_OF_MARIJUANA);
            fabricItemGroupEntries.add(ModBlocks.BLOCK_OF_DRIED_MARIJUANA);
        });
    }
}
