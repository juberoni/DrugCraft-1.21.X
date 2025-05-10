package net.jube.drugcraft.item.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class DryingFanItem extends Item {
    public DryingFanItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.isDamaged();
    }
}
