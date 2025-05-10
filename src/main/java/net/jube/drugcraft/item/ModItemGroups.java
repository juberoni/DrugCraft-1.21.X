package net.jube.drugcraft.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.jube.drugcraft.DrugCraft;
import net.jube.drugcraft.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static final ItemGroup DRUGCRAFT_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(DrugCraft.MOD_ID,"drugcraft_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.MARIJUANA_LEAF))
                    .displayName(Text.translatable("itemgroup.drugcraft.drugcraft_items"))
                    .entries((displayContext, entries) -> {

                        entries.add(ModBlocks.BLOCK_OF_MARIJUANA);
                        entries.add(ModBlocks.BLOCK_OF_DRIED_MARIJUANA);
                        entries.add(ModBlocks.TABLE);
                        entries.add(ModBlocks.DRYING_TABLE);


                        entries.add(ModItems.MARIJUANA_LEAF);
                        entries.add(ModItems.MARIJUANA_FLOWER);
                        entries.add(ModItems.DRIED_MARIJUANA_FLOWER);
                        entries.add(ModItems.MARIJUANA_SEEDS);
                        entries.add(ModItems.DRYING_FAN);



                    }).build());



    public static void registerItemGroups() {
        DrugCraft.LOGGER.info("Registering Item Groups for " + DrugCraft.MOD_ID);
    }
}
