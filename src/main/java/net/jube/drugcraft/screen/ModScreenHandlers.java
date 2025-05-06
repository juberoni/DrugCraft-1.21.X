package net.jube.drugcraft.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.jube.drugcraft.DrugCraft;
import net.jube.drugcraft.screen.custom.DryingTableScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ModScreenHandlers {
    public static final ScreenHandlerType<DryingTableScreenHandler> DRYINGTABLE_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(DrugCraft.MOD_ID, "dryingtable_screen_handler"),
                    new ExtendedScreenHandlerType<>(DryingTableScreenHandler::new, BlockPos.PACKET_CODEC));

    public static void registerScreenHandlers() {
        DrugCraft.LOGGER.info("Registering Screen Handlers for " + DrugCraft.MOD_ID);
    }
}
