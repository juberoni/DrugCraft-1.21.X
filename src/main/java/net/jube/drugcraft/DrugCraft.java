package net.jube.drugcraft;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.jube.drugcraft.block.ModBlocks;
import net.jube.drugcraft.item.ModItemGroups;
import net.jube.drugcraft.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// I don't know what I'm doing.
public class DrugCraft implements ModInitializer {
	public static final String MOD_ID = "drugcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItemGroups.registerItemGroups();

		ModItems.registerModItems();
		ModBlocks.registerModBlocks();


		CompostingChanceRegistry.INSTANCE.add(ModItems.MARIJUANA_FLOWER, 0.5f);
		CompostingChanceRegistry.INSTANCE.add(ModItems.MARIJUANA_SEEDS, 0.25f);
		CompostingChanceRegistry.INSTANCE.add(ModItems.MARIJUANA_LEAF, 0.3f);

	}
}