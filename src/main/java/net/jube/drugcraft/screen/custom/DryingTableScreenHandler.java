package net.jube.drugcraft.screen.custom;

import net.jube.drugcraft.block.entity.custom.DryingTableBlockEntity;
// Assuming DryingFanItem is your specific class for fans.
// If not, you might check against ModItems.DRYING_FAN or rely solely on FanOnlySlot.
import net.jube.drugcraft.item.custom.DryingFanItem;
import net.jube.drugcraft.screen.ModScreenHandlers;
import net.jube.drugcraft.screen.slot.FanOnlySlot;
import net.jube.drugcraft.screen.slot.OutputOnlySlot; // Import the new OutputOnlySlot
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class DryingTableScreenHandler extends ScreenHandler {
    private final Inventory inventory; // This is the DryingTableBlockEntity's inventory
    private final PropertyDelegate propertyDelegate;
    public final DryingTableBlockEntity blockEntity; // Keep for convenience, e.g. isAcceptedRawMaterial

    // Slot index constants for clarity in quickMove
    private static final int INPUT_SLOT_INDEX = 0;
    private static final int OUTPUT_SLOT_INDEX = 1;
    private static final int FAN_SLOT_INDEX = 2;
    private static final int BE_INVENTORY_SIZE = 3;

    private static final int PLAYER_INVENTORY_FIRST_SLOT_INDEX = BE_INVENTORY_SIZE;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = 27;
    private static final int PLAYER_HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_MAIN_INV_LAST_SLOT_INDEX = PLAYER_INVENTORY_FIRST_SLOT_INDEX + PLAYER_INVENTORY_SLOT_COUNT - 1;
    private static final int PLAYER_HOTBAR_FIRST_SLOT_INDEX = PLAYER_MAIN_INV_LAST_SLOT_INDEX + 1;
    private static final int PLAYER_HOTBAR_LAST_SLOT_INDEX = PLAYER_HOTBAR_FIRST_SLOT_INDEX + PLAYER_HOTBAR_SLOT_COUNT - 1;


    /**
     * Constructor called on the client side when the screen is opened.
     * It receives the BlockPos from the server via ExtendedScreenHandlerFactory.
     */
    public DryingTableScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, getBlockEntity(playerInventory.player.getWorld(), pos), new ArrayPropertyDelegate(2));
    }

    private static DryingTableBlockEntity getBlockEntity(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof DryingTableBlockEntity) {
            return (DryingTableBlockEntity) be;
        }
        // Fallback or error handling if BE is not found or wrong type on client
        // This could happen if the block was broken between opening screen and this constructor running
        // For simplicity, we might throw or return a dummy, but proper handling depends on desired robustness
        throw new IllegalStateException("Expected DryingTableBlockEntity at " + pos + " but found " + be);
    }


    /**
     * Constructor called on the server side by DryingTableBlockEntity.createMenu().
     */
    public DryingTableScreenHandler(int syncId, PlayerInventory playerInventory,
                                    BlockEntity blockEntity, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.DRYING_TABLE_SCREEN_HANDLER, syncId);
        checkDataCount(propertyDelegate, 2); // Ensure property delegate has expected size
        this.inventory = (Inventory) blockEntity; // BE implements ImplementedInventory which is an Inventory
        this.blockEntity = (DryingTableBlockEntity) blockEntity;
        this.propertyDelegate = propertyDelegate;

        // Block Entity Slots
        this.addSlot(new Slot(this.inventory, INPUT_SLOT_INDEX, 54, 34)); // Input
        this.addSlot(new OutputOnlySlot(this.inventory, OUTPUT_SLOT_INDEX, 104, 34)); // Output (uses custom OutputOnlySlot)
        this.addSlot(new FanOnlySlot(this.inventory, FAN_SLOT_INDEX, 17, 34)); // Fan (uses your FanOnlySlot)

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addProperties(propertyDelegate);
    }

    public boolean isCrafting() {
        return propertyDelegate.get(0) > 0;
    }

    public int getScaledArrowProgress() {
        int progress = this.propertyDelegate.get(0);
        int maxProgress = this.propertyDelegate.get(1);
        int arrowPixelSize = 24; // Width of your progress arrow texture

        return maxProgress != 0 && progress != 0 ? progress * arrowPixelSize / maxProgress : 0;
    }

    // Removed this.blockEntity.syncToClient() from sendContentUpdates and onContentChanged.
    // PropertyDelegate updates are handled by super.sendContentUpdates().
    // Inventory slot updates are handled by Minecraft when inventory.markDirty() is called.
    // BlockEntity data (for renderer) is synced by the BlockEntity itself.

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();

            if (slotIndex < BE_INVENTORY_SIZE) { // Clicked in BE inventory (slots 0-2: Input, Output, Fan)
                // Try to move from BE slot to player inventory (slots 3-38)
                if (!this.insertItem(originalStack, PLAYER_INVENTORY_FIRST_SLOT_INDEX, PLAYER_HOTBAR_LAST_SLOT_INDEX + 1, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(originalStack, newStack);
            } else { // Clicked in player inventory (slots 3-38)
                // Try to move from player inventory to specific BE slots based on item type
                // Order of attempts: Fan Slot, then Input Slot. Output Slot is not a target.

                // 1. Try to move to Fan Slot (if it's a fan item)
                // FanOnlySlot.canInsert should verify the item type.
                if (this.slots.get(FAN_SLOT_INDEX).canInsert(originalStack)) {
                    if (this.insertItem(originalStack, FAN_SLOT_INDEX, FAN_SLOT_INDEX + 1, false)) {
                        // Successfully moved to fan slot
                    } else {
                        // Failed to move to fan slot, try moving within player inventory
                        tryMoveWithinPlayerInventory(originalStack, slotIndex);
                    }
                }
                // 2. Else, try to move to Input Slot (if it's an accepted raw material)
                else if (this.blockEntity.isAcceptedRawMaterial(originalStack)) {
                    if (this.insertItem(originalStack, INPUT_SLOT_INDEX, INPUT_SLOT_INDEX + 1, false)) {
                        // Successfully moved to input slot
                    } else {
                        // Failed to move to input slot, try moving within player inventory
                        tryMoveWithinPlayerInventory(originalStack, slotIndex);
                    }
                }
                // 3. Else (not a fan, not a raw material, or failed above), try to move within player inventory
                else {
                    tryMoveWithinPlayerInventory(originalStack, slotIndex);
                }
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY; // No change in stack size means transfer failed or was partial and couldn't complete.
            }

            slot.onTakeItem(player, originalStack);
        }
        return newStack;
    }

    private void tryMoveWithinPlayerInventory(ItemStack originalStack, int sourcePlayerSlotIndex) {
        if (sourcePlayerSlotIndex >= PLAYER_INVENTORY_FIRST_SLOT_INDEX && sourcePlayerSlotIndex <= PLAYER_MAIN_INV_LAST_SLOT_INDEX) {
            // Clicked in player's main inventory (3-29), try to move to hotbar (30-38)
            if (!this.insertItem(originalStack, PLAYER_HOTBAR_FIRST_SLOT_INDEX, PLAYER_HOTBAR_LAST_SLOT_INDEX + 1, false)) {
                // Failed to move to hotbar (originalStack may not be empty if partially moved)
            }
        } else if (sourcePlayerSlotIndex >= PLAYER_HOTBAR_FIRST_SLOT_INDEX && sourcePlayerSlotIndex <= PLAYER_HOTBAR_LAST_SLOT_INDEX) {
            // Clicked in player's hotbar (30-38), try to move to main inventory (3-29)
            if (!this.insertItem(originalStack, PLAYER_INVENTORY_FIRST_SLOT_INDEX, PLAYER_MAIN_INV_LAST_SLOT_INDEX + 1, false)) {
                // Failed to move to main inventory
            }
        }
    }


    @Override
    public boolean canUse(PlayerEntity player) {
        // Ensure the inventory (our BlockEntity) can still be used.
        // Vanilla check often involves distance and if the block entity still exists.
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}