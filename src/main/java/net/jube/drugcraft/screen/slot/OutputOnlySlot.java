package net.jube.drugcraft.screen.slot; // Or your appropriate package

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class OutputOnlySlot extends Slot {
    public OutputOnlySlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false; // Key change: Disallow any insertion.
    }
}