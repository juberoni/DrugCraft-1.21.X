package net.jube.drugcraft.block.entity;

import net.jube.drugcraft.DrugCraft;
import net.jube.drugcraft.block.ModBlocks;
import net.jube.drugcraft.block.entity.custom.DryingTableBlockEntity;
import net.jube.drugcraft.block.entity.custom.TableBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<TableBlockEntity> TABLE_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(DrugCraft.MOD_ID,"table_be"),
                    BlockEntityType.Builder.create(TableBlockEntity::new, ModBlocks.TABLE).build(null));

        public static final BlockEntityType<DryingTableBlockEntity> DRYING_TABLE_BE =
                Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(DrugCraft.MOD_ID,"drying_table_be"),
                        BlockEntityType.Builder.create(DryingTableBlockEntity::new, ModBlocks.DRYING_TABLE).build(null));


    public static void registerBlockEntities() {
        DrugCraft.LOGGER.info("Registering Block Entities for " + DrugCraft.MOD_ID);
    }
}
