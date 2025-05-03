package net.jube.drugcraft;

import net.fabricmc.api.ModInitializer;

import net.jube.drugcraft.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// I don't know what I'm doing.
public class DrugCraft implements ModInitializer {
	public static final String MOD_ID = "drugcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerModItems();

	}
}