package net.jube.drugcraft.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.jube.drugcraft.DrugCraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item MARIJUANA_LEAF = registerItem("marijuana_leaf", new Item(new Item.Settings()));
    public static final Item MARIJUANA_FLOWER = registerItem("marijuana_flower", new Item(new Item.Settings()));


    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(DrugCraft.MOD_ID, name), item);
    }

    public static void registerModItems() {
        DrugCraft.LOGGER.info("Registering Mod items for " + DrugCraft.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(MARIJUANA_LEAF);
            fabricItemGroupEntries.add(MARIJUANA_FLOWER);
        });
    }
}
