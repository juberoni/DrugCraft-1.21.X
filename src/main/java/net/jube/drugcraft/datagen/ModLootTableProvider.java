package net.jube.drugcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.jube.drugcraft.block.ModBlocks;
import net.jube.drugcraft.block.custom.MarijuanaPlantBlock;
import net.jube.drugcraft.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        addDrop(ModBlocks.BLOCK_OF_MARIJUANA);
        addDrop(ModBlocks.BLOCK_OF_DRIED_MARIJUANA);
        addDrop(ModBlocks.TABLE);
        addDrop(ModBlocks.DRYING_TABLE);
        addDrop(ModBlocks.ACACIA_DRYING_TABLE);
        addDrop(ModBlocks.BAMBOO_DRYING_TABLE);
        addDrop(ModBlocks.BIRCH_DRYING_TABLE);
        addDrop(ModBlocks.CHERRY_DRYING_TABLE);
        addDrop(ModBlocks.DARK_OAK_DRYING_TABLE);
        addDrop(ModBlocks.JUNGLE_DRYING_TABLE);
        addDrop(ModBlocks.MANGROVE_DRYING_TABLE);
        addDrop(ModBlocks.OAK_DRYING_TABLE);
        addDrop(ModBlocks.SPRUCE_DRYING_TABLE);


        BlockStatePropertyLootCondition.Builder builder2 = BlockStatePropertyLootCondition.builder(ModBlocks.MARIJUANA_PLANT)
                .properties(StatePredicate.Builder.create().exactMatch(MarijuanaPlantBlock.AGE, 7));
        this.addDrop(ModBlocks.MARIJUANA_PLANT,this.cropDrops(ModBlocks.MARIJUANA_PLANT, ModItems.MARIJUANA_FLOWER, ModItems.MARIJUANA_SEEDS,builder2));

    }

    public LootTable.Builder multipleOreDrops(Block drop, Item item, float minDrops, float maxDrops) {
        RegistryWrapper.Impl<Enchantment> impl = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(drop, this.applyExplosionDecay(drop,((LeafEntry.Builder<?>)
                ItemEntry.builder(item).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(minDrops, maxDrops))))
                .apply(ApplyBonusLootFunction.oreDrops(impl.getOrThrow(Enchantments.FORTUNE)))));
    }
}
