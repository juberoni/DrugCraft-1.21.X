package net.jube.drugcraft.screen.custom;

import net.jube.drugcraft.screen.ModScreenHandlers;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class DryingTableScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public DryingTableScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, playerInventory.player.getWorld().getBlockEntity(pos));
    }

    public DryingTableScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity) {
        super(ModScreenHandlers.DRYINGTABLE_SCREEN_HANDLER, syncId);
        this.inventory = ((Inventory) blockEntity);

        this.addSlot(new Slot(inventory, 0, 80, 35) {
            //WHERE YOU CAN CHANGE SLOT LIMIT
            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    //ALLOWS YOU TO SHIFT LEFT-CLICK ITEMS
    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originialStack = slot.getStack();
            newStack = originialStack.copy();
            if (invSlot < this.inventory.size()) {
                if(!this.insertItem(originialStack, this.inventory.size(), this.slots.size(),true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originialStack, 0, this.inventory.size(),false)) {
                return ItemStack.EMPTY;
            }

            if (originialStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }
        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    //ADDS THE PLAYER INVENTORY
    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i= 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }
    //ADDS THE PLAYER HOTBAR
    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
