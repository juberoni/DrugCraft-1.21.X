package net.jube.drugcraft.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.jube.drugcraft.DrugCraft;
import net.jube.drugcraft.block.ModBlocks;
import net.jube.drugcraft.item.custom.DryingFanItem;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item MARIJUANA_LEAF =
            registerItem("marijuana_leaf", new Item(new Item.Settings()));

    public static final Item MARIJUANA_FLOWER =
            registerItem("marijuana_flower", new Item(new Item.Settings()));

    public static final Item DRIED_MARIJUANA_FLOWER =
            registerItem("dried_marijuana_flower", new Item(new Item.Settings()));

    public static final Item DRYING_FAN =
            registerItem("drying_fan", new DryingFanItem(new Item.Settings().maxDamage(128)));


    public static final Item MARIJUANA_SEEDS = registerItem("marijuana_seeds",
        new AliasedBlockItem(ModBlocks.MARIJUANA_PLANT, new Item.Settings()));


    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(DrugCraft.MOD_ID, name), item);
    }

    public static void registerModItems() {
        DrugCraft.LOGGER.info("Registering Mod items for " + DrugCraft.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(MARIJUANA_LEAF);
            fabricItemGroupEntries.add(MARIJUANA_FLOWER);
            fabricItemGroupEntries.add(DRIED_MARIJUANA_FLOWER);
            fabricItemGroupEntries.add(MARIJUANA_SEEDS);
            fabricItemGroupEntries.add(DRYING_FAN);
        });
    }
}
