package net.jube.drugcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.jube.drugcraft.block.ModBlocks;
import net.jube.drugcraft.item.ModItems;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        List<ItemConvertible> MARIJUANA_DRYABLES = List.of(
                ModItems.MARIJUANA_FLOWER,
                ModBlocks.BLOCK_OF_MARIJUANA);

        offerReversibleCompactingRecipes(exporter, RecipeCategory.BUILDING_BLOCKS, ModItems.MARIJUANA_FLOWER, RecipeCategory.MISC, ModBlocks.BLOCK_OF_MARIJUANA);
        offerReversibleCompactingRecipes(exporter, RecipeCategory.BUILDING_BLOCKS, ModItems.DRIED_MARIJUANA_FLOWER, RecipeCategory.MISC, ModBlocks.BLOCK_OF_DRIED_MARIJUANA);

        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, Items.PAPER, 3)
                .pattern("   ")
                .pattern("LLL")
                .pattern("   ")
                .input('L', ModItems.MARIJUANA_LEAF)
                .criterion(hasItem(ModItems.MARIJUANA_LEAF), conditionsFromItem(ModItems.MARIJUANA_LEAF))
                .offerTo(exporter);

        ShapedRecipeJsonBuilder.create(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DRYING_TABLE,1)
                .pattern("I I")
                .pattern("BBB")
                .pattern("I I")
                .input('I', Items.IRON_INGOT)
                .input('B', Items.IRON_BARS)
                .criterion("has_iron", conditionsFromItem(Items.IRON_INGOT))
                .criterion("has_bars", conditionsFromItem(Items.IRON_BARS))
                .offerTo(exporter);
    }
}
