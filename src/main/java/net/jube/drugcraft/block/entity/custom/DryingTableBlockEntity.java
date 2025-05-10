package net.jube.drugcraft.block.entity.custom;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.jube.drugcraft.block.custom.DryingTableBlock;
import net.jube.drugcraft.block.entity.ImplementedInventory;
import net.jube.drugcraft.block.entity.ModBlockEntities;
import net.jube.drugcraft.item.ModItems;
import net.jube.drugcraft.screen.custom.DryingTableScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents; // Example sounds
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class DryingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int FAN_SLOT = 2;
    public static final int MAX_SHELVES = 6;

    private static final int MAX_PROGRESS_DEFAULT = 20 * 30; //30 seconds per item
    private static final int BASE_DRYING_SPEED = 1;
    private static final int FAN_DRYING_MULTIPLIER = 3;


    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private final PropertyDelegate propertyDelegate;

    private int progress = 0;
    private int maxProgress = MAX_PROGRESS_DEFAULT;

    // Batch-specific properties
    private int itemsTakenForBatch = 0;     // How many items are currently being processed "on shelves"
    private int itemsDriedThisBatch = 0;    // How many of those itemsTakenForBatch are now dry
    private ItemStack batchItemType = ItemStack.EMPTY; // The type of raw item in the current batch

    private int lastInputCountForSound = 0;

    public DryingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_TABLE_BE, pos, state);
        this.maxProgress = MAX_PROGRESS_DEFAULT;
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> DryingTableBlockEntity.this.progress;
                    case 1 -> DryingTableBlockEntity.this.maxProgress;
                    // You could add itemsDriedThisBatch and itemsTakenForBatch here
                    // if your ScreenHandler needs them as properties.
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> DryingTableBlockEntity.this.progress = value;
                    case 1 -> DryingTableBlockEntity.this.maxProgress = value;
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }

    // Getters for Renderer and potentially ScreenHandler
    public int getItemsDriedThisBatch() {
        return itemsDriedThisBatch;
    }

    public int getItemsTakenForBatch() {
        return itemsTakenForBatch;
    }

    public ItemStack getBatchItemType() { // This is the RAW item type
        return batchItemType;
    }

    public ItemStack getDriedOutputItemType() { // Helper for renderer to know what dried item to show
        return getDriedVersion(this.batchItemType);
    }

    public boolean isCrafting() { // For fan animation and block state
        return this.progress > 0 && isBatchActive() && this.itemsDriedThisBatch < this.itemsTakenForBatch;
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("Progress", progress);
        nbt.putInt("MaxProgress", maxProgress);
        nbt.putInt("ItemsTakenForBatch", itemsTakenForBatch);
        nbt.putInt("ItemsDriedThisBatch", itemsDriedThisBatch);
        if (!batchItemType.isEmpty()) {
            nbt.put("BatchItemType", batchItemType.encode(registryLookup));
        }
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        inventory.clear();
        Inventories.readNbt(nbt, inventory, registryLookup);
        progress = nbt.getInt("Progress");
        maxProgress = nbt.getInt("MaxProgress");
        itemsTakenForBatch = nbt.getInt("ItemsTakenForBatch");
        itemsDriedThisBatch = nbt.getInt("ItemsDriedThisBatch");
        if (nbt.contains("BatchItemType", NbtCompound.COMPOUND_TYPE)) {
            batchItemType = ItemStack.fromNbt(registryLookup, nbt.getCompound("BatchItemType")).orElse(ItemStack.EMPTY);
        } else {
            batchItemType = ItemStack.EMPTY;
        }
        if (this.maxProgress == 0) this.maxProgress = MAX_PROGRESS_DEFAULT; // Sanity check
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world == null || world.isClient) return;

        ItemStack currentInputStack = getStack(INPUT_SLOT);
        int currentInputCount = currentInputStack.getCount();
        if (currentInputCount > lastInputCountForSound && isAcceptedRawMaterial(currentInputStack)) {
            playInputSound(world, pos);
        }
        lastInputCountForSound = currentInputCount;

        // If current batch is fully dried, reset it.
        if (isBatchActive() && itemsDriedThisBatch >= itemsTakenForBatch) {
            resetBatch();
            markDirtyAndUpdate();
        }

        // Try to start a new batch if no batch is active (or previous just finished and was reset)
        if (!isBatchActive()) {
            if (canStartNewBatch(currentInputStack)) {
                startNewBatch(currentInputStack);
                markDirtyAndUpdate();
            }
        }

        // If a batch is active and not all items are dried yet:
        if (isBatchActive() && itemsDriedThisBatch < itemsTakenForBatch) {
            if (!state.get(DryingTableBlock.DRYING)) {
                world.setBlockState(pos, state.with(DryingTableBlock.DRYING, true), 3);
                // Play start drying sound?
            }

            ItemStack fanStack = getStack(FAN_SLOT);
            boolean hasFan = !getStack(FAN_SLOT).isEmpty();
            int speed = hasFan ? BASE_DRYING_SPEED * FAN_DRYING_MULTIPLIER : BASE_DRYING_SPEED;
            this.progress += speed;

            if (hasCraftingFinished()) {
                craftOneItem(); // Handles output, increments itemsDriedThisBatch
                resetProgress();
                markDirtyAndUpdate();

                if (hasFan && fanStack.isDamageable()) {
                    net.minecraft.entity.LivingEntity nullEntity = null;
                    net.minecraft.entity.EquipmentSlot dummySlot = EquipmentSlot.MAINHAND;

                   if (this.world instanceof ServerWorld serverWorld) {
                       net.minecraft.server.network.ServerPlayerEntity nullPlayer = null;

                       java.util.function.Consumer<net.minecraft.item.Item> breakCallback = itemBroken -> {
                           world.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1.0f, 1.0f);

                       };

                       fanStack.damage(
                               1,
                               serverWorld,
                               nullPlayer,
                               breakCallback
                       );

                       if (fanStack.isEmpty()) {
                        setStack(FAN_SLOT, ItemStack.EMPTY);

                       } else {
                           markDirty();
                       }
                    }
                }
                markDirtyAndUpdate();

                if (itemsDriedThisBatch >= itemsTakenForBatch) {
                    playCraftingFinishedSound(world, pos); // Batch completion sound
                    // Batch will be reset at the start of the next tick.
                }
            }
        } else { // No active drying process (no batch, or batch complete)
            if (state.get(DryingTableBlock.DRYING)) {
                world.setBlockState(pos, state.with(DryingTableBlock.DRYING, false), 3);
                markDirtyAndUpdate();
            }
            // This case also handles when a batch was just completed and needs resetting immediately
            if (isBatchActive() && itemsDriedThisBatch >= itemsTakenForBatch) {
                resetBatch();
                markDirtyAndUpdate();
            }
        }
    }

    private void startNewBatch(ItemStack inputStack) {
        this.itemsTakenForBatch = Math.min(MAX_SHELVES, inputStack.getCount());

        System.out.println("[SERVER BE] Starting new batch. Input count: " + inputStack.getCount() +
                ", MAX_SHELVES: " + MAX_SHELVES +
                ", Calculated itemsTakenForBatch: " + this.itemsTakenForBatch);

        this.batchItemType = inputStack.copyWithCount(1); // Store the type of item
        inputStack.decrement(this.itemsTakenForBatch);    // Consume items from input slot
        setStack(INPUT_SLOT, inputStack.isEmpty() ? ItemStack.EMPTY : inputStack); // Update input slot
        this.itemsDriedThisBatch = 0;
        this.progress = 0;
        // maxProgress could be set here based on batchItemType or fan presence.
    }

    private void resetBatch() {
        this.itemsTakenForBatch = 0;
        this.itemsDriedThisBatch = 0;
        this.batchItemType = ItemStack.EMPTY;
        resetProgress();
    }

    private boolean isBatchActive() {
        return this.itemsTakenForBatch > 0 && !this.batchItemType.isEmpty();
    }

    public boolean isAcceptedRawMaterial(ItemStack stack) {
        // Define what items can be dried
        return stack.isOf(ModItems.MARIJUANA_FLOWER);
        // Example for multiple: return stack.isOf(ModItems.FLOWER_A) || stack.isOf(ModItems.FLOWER_B);
    }

    private ItemStack getDriedVersion(ItemStack rawStack) {
        if (rawStack.isEmpty()) return ItemStack.EMPTY;
        // Define the output for each raw material
        if (rawStack.isOf(ModItems.MARIJUANA_FLOWER)) {
            return new ItemStack(ModItems.DRIED_MARIJUANA_FLOWER);
        }
        // Example for multiple: if (rawStack.isOf(ModItems.FLOWER_A)) return new ItemStack(ModItems.DRIED_FLOWER_A);
        return ItemStack.EMPTY; // Should not happen if canStartNewBatch is correct
    }

    private boolean canStartNewBatch(ItemStack inputStack) {
        if (inputStack.isEmpty() || !isAcceptedRawMaterial(inputStack)) {
            return false;
        }

        ItemStack potentialOutputItem = getDriedVersion(inputStack.copyWithCount(1));
        if (potentialOutputItem.isEmpty()) {
            return false; // No recipe for this input
        }

        // Check if output slot can accept the dried item
        ItemStack outputSlotStack = getStack(OUTPUT_SLOT);
        if (outputSlotStack.isEmpty()) {
            return true;
        }
        if (!ItemStack.areItemsAndComponentsEqual(outputSlotStack, potentialOutputItem)) {
            return false; // Output slot has a different item
        }
        return outputSlotStack.getCount() < outputSlotStack.getMaxCount(); // Output slot has space
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void craftOneItem() {
        if (!isBatchActive() || itemsDriedThisBatch >= itemsTakenForBatch) return;

        ItemStack driedItem = getDriedVersion(this.batchItemType);
        if (driedItem.isEmpty()) {
            System.err.println("DryingTable: Could not determine dried version for: " + this.batchItemType.getItem());
            // To prevent stall, count as processed but don't output. Or error more gracefully.
            // For now, we assume getDriedVersion works for batchItemType.
            return;
        }

        ItemStack outputSlotStack = getStack(OUTPUT_SLOT);
        // Attempt to insert into output slot
        if (outputSlotStack.isEmpty()) {
            setStack(OUTPUT_SLOT, driedItem.copyWithCount(1));
            this.itemsDriedThisBatch++;
        } else if (ItemStack.areItemsAndComponentsEqual(outputSlotStack, driedItem) && outputSlotStack.getCount() < outputSlotStack.getMaxCount()) {
            outputSlotStack.increment(1);
            setStack(OUTPUT_SLOT, outputSlotStack); // Update the slot (important if DefaultedList makes copies)
            this.itemsDriedThisBatch++;
        } else {
            // Output slot is full or contains a different item.
            // The drying process for this item effectively "pauses" until space is made.
            // Progress will reset, but itemsDriedThisBatch won't increment.
            // This means the same item will try to dry again next tick if conditions allow.
            // To prevent item loss, we don't increment itemsDriedThisBatch.
            System.err.println("DryingTable: Output slot full or mismatched. Drying for this item will retry.");
            // No item is "lost", it just hasn't been successfully moved to output.
        }
        // Input decrement was at batch start.
    }

    private void resetProgress() {
        this.progress = 0;
    }

    private void playInputSound(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.BLOCKS, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
    }

    private void playCraftingFinishedSound(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    private void markDirtyAndUpdate() {
        markDirty();
        if (world != null && !world.isClient) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    // --- ImplementedInventory Methods ---
    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.drugcraft.drying_table");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        // Send pos for the ExtendedScreenHandlerFactory
        return new DryingTableScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return this.pos;
    }

    // --- Client Sync ---
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}