package net.jube.drugcraft.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.jube.drugcraft.DrugCraft;
import net.jube.drugcraft.block.custom.*;
import net.jube.drugcraft.block.custom.AcaciaWoodDryingTableBlock;
import net.jube.drugcraft.block.custom.BambooWoodDryingTableBlock;
import net.jube.drugcraft.block.custom.BirchWoodDryingTableBlock;
import net.jube.drugcraft.block.custom.CherryWoodDryingTableBlock;
import net.jube.drugcraft.block.custom.DarkOakWoodDryingTableBlock;
import net.jube.drugcraft.block.custom.JungleWoodDryingTableBlock;
import net.jube.drugcraft.block.custom.MangroveWoodDryingTableBlock;
import net.jube.drugcraft.block.custom.OakWoodDryingTableBlock;
import net.jube.drugcraft.block.custom.SpruceWoodDryingTableBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
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

    public static final Block MARIJUANA_PLANT = registerBlockWithoutItem("marijuana_plant",
            new MarijuanaPlantBlock(AbstractBlock.Settings.create().noCollision()
                    .ticksRandomly().breakInstantly().sounds(BlockSoundGroup.CROP).pistonBehavior(PistonBehavior.DESTROY).mapColor(MapColor.DARK_GREEN)));

    public static final Block TABLE = registerBlock("table",
            new TableBlock(AbstractBlock.Settings.create().strength(3f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.WOOD)));

    public static final Block DRYING_TABLE = registerBlock("drying_table",
            new DryingTableBlock((AbstractBlock.Settings.create().strength(3f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.METAL))));

    public static final Block ACACIA_DRYING_TABLE = registerBlock("acacia_drying_table",
            new AcaciaWoodDryingTableBlock((AbstractBlock.Settings.create().strength(2f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.WOOD))));

    public static final Block BAMBOO_DRYING_TABLE = registerBlock("bamboo_drying_table",
            new BambooWoodDryingTableBlock((AbstractBlock.Settings.create().strength(2f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.WOOD))));

    public static final Block BIRCH_DRYING_TABLE = registerBlock("birch_drying_table",
            new BirchWoodDryingTableBlock((AbstractBlock.Settings.create().strength(2f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.WOOD))));

    public static final Block CHERRY_DRYING_TABLE = registerBlock("cherry_drying_table",
            new CherryWoodDryingTableBlock((AbstractBlock.Settings.create().strength(2f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.CHERRY_WOOD))));

    public static final Block DARK_OAK_DRYING_TABLE = registerBlock("dark_oak_drying_table",
            new DarkOakWoodDryingTableBlock((AbstractBlock.Settings.create().strength(2f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.WOOD))));

    public static final Block JUNGLE_DRYING_TABLE = registerBlock("jungle_drying_table",
            new JungleWoodDryingTableBlock((AbstractBlock.Settings.create().strength(2f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.WOOD))));

    public static final Block MANGROVE_DRYING_TABLE = registerBlock("mangrove_drying_table",
            new MangroveWoodDryingTableBlock((AbstractBlock.Settings.create().strength(2f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.WOOD))));

    public static final Block OAK_DRYING_TABLE = registerBlock("oak_drying_table",
            new OakWoodDryingTableBlock((AbstractBlock.Settings.create().strength(2f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.WOOD))));

    public static final Block SPRUCE_DRYING_TABLE = registerBlock("spruce_drying_table",
            new SpruceWoodDryingTableBlock((AbstractBlock.Settings.create().strength(2f)
                    .requiresTool().nonOpaque().sounds(BlockSoundGroup.WOOD))));







    protected static Block registerBlockWithoutItem(String name, Block block) {
        return Registry.register(Registries.BLOCK, Identifier.of("drugcraft", name), block);
    }

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(DrugCraft.MOD_ID, name), block);
    }

    private static Block registerBlockWithoutBlockItem(String name, Block block) {
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
