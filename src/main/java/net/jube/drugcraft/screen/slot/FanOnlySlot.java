package net.jube.drugcraft.screen.slot;

import net.jube.drugcraft.item.custom.DryingFanItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class FanOnlySlot extends Slot {
    public FanOnlySlot(Inventory inventory, int index, int x) {
        super(inventory, index, 17, 34);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem() instanceof DryingFanItem;
    }
}
