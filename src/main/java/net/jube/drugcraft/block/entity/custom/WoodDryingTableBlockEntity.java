package net.jube.drugcraft.block.entity.custom;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.jube.drugcraft.block.entity.ImplementedInventory; // Ensure this is correctly implemented
import net.jube.drugcraft.block.entity.ModBlockEntities;
import net.jube.drugcraft.item.ModItems;
import net.jube.drugcraft.screen.custom.WoodDryingTableScreenHandler; // You'll need to create/update this
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WoodDryingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {

    private static final Logger LOGGER = LoggerFactory.getLogger("Drugcraft Drying Table BE");

    private static final int[] INPUT_SLOTS_INDICES = {0, 1, 2, 3};
    private static final int OUTPUT_SLOT_INDEX = 4;
    private static final int INVENTORY_SIZE = 5; // 4 inputs + 1 output

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);

    protected final PropertyDelegate propertyDelegate;
    private int progress = 0;
    private int maxProgress = 600; // Adjusted default, can be changed
    private int currentlyDryingSlot = -1; // -1 means no slot is currently being processed

    // For handling insertion sounds correctly for individual slots
    private final boolean[] inputSlotPreviouslyHadItem = new boolean[INPUT_SLOTS_INDICES.length];

    private static final int TICKS_PER_ITEM = 1; // How much progress is made per tick

    public WoodDryingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WOOD_DRYING_TABLE_BE, pos, state); // Ensure WOOD_DRYING_TABLE_BE is registered
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> WoodDryingTableBlockEntity.this.progress;
                    case 1 -> WoodDryingTableBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> WoodDryingTableBlockEntity.this.progress = value;
                    case 1 -> WoodDryingTableBlockEntity.this.maxProgress = value;
                }
            }

            @Override
            public int size() {
                return 2; // We are tracking progress and maxProgress
            }
        };
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    public int getMaxCountPerStack() {
        return 1; // Each slot can only hold 1 item
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT_INDEX) {
            return false; // Output slot should not accept items from players directly this way
        }
        // For input slots, check if it's the required item
        if (slot >= INPUT_SLOTS_INDICES[0] && slot <= INPUT_SLOTS_INDICES[INPUT_SLOTS_INDICES.length -1]) {
            return stack.isOf(ModItems.MARIJUANA_FLOWER); // Assuming this is the item to dry
        }
        return ImplementedInventory.super.isValid(slot, stack);
    }


    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return this.pos;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("Wooden Drying Table");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        // You will need to create/update WoodDryingTableScreenHandler
        // to handle 4 input + 1 output slots, each with stack size 1.
        return new WoodDryingTableScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, this.inventory, registryLookup);
        nbt.putInt("drugcraft.wood_drying_table.progress", this.progress);
        nbt.putInt("drugcraft.wood_drying_table.max_progress", this.maxProgress);
        nbt.putInt("drugcraft.wood_drying_table.currently_drying_slot", this.currentlyDryingSlot);
        // inputSlotPreviouslyHadItem is transient, re-evaluated on load/tick
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        // Clear inventory before loading to prevent issues with size changes if any
        for(int i = 0; i < this.inventory.size(); i++) { this.inventory.set(i, ItemStack.EMPTY); }
        Inventories.readNbt(nbt, this.inventory, registryLookup);

        this.progress = nbt.getInt("drugcraft.wood_drying_table.progress");
        this.maxProgress = nbt.getInt("drugcraft.wood_drying_table.max_progress");
        if (this.maxProgress <= 0) {
            this.maxProgress = 600; // Default if invalid
        }
        this.currentlyDryingSlot = nbt.getInt("drugcraft.wood_drying_table.currently_drying_slot");

        // Initialize sound tracking state based on loaded inventory
        if (this.world != null && !this.world.isClient()) {
            for (int i = 0; i < INPUT_SLOTS_INDICES.length; i++) {
                int slotIndex = INPUT_SLOTS_INDICES[i];
                inputSlotPreviouslyHadItem[i] = !this.inventory.get(slotIndex).isEmpty() &&
                        this.inventory.get(slotIndex).isOf(ModItems.MARIJUANA_FLOWER);
            }
        }

        if (this.world != null && this.world.isClient) {
            MinecraftClient.getInstance().worldRenderer.scheduleBlockRenders(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient()) {
            return;
        }

        boolean changed = false;

        // --- Sound for Item Insertion ---
        for (int i = 0; i < INPUT_SLOTS_INDICES.length; i++) {
            int slotIndex = INPUT_SLOTS_INDICES[i];
            ItemStack stackInSlot = this.inventory.get(slotIndex);
            boolean currentlyHasItem = !stackInSlot.isEmpty() && stackInSlot.isOf(ModItems.MARIJUANA_FLOWER);

            if (currentlyHasItem && !this.inputSlotPreviouslyHadItem[i]) {
                world.playSound(
                        null,
                        pos,
                        SoundEvents.BLOCK_CHISELED_BOOKSHELF_INSERT, // Sound for placing item
                        SoundCategory.BLOCKS,
                        0.6f,
                        1.0f + world.random.nextFloat() * 0.2f
                );
                changed = true; // Sound played, client might need update for other visual cues if any
            }
            this.inputSlotPreviouslyHadItem[i] = currentlyHasItem;
        }


        // --- Drying Logic ---
        if (this.isOutputSlotAvailable()) {
            if (this.currentlyDryingSlot != -1) { // Currently processing an item
                ItemStack dryingStack = this.inventory.get(this.currentlyDryingSlot);
                if (dryingStack.isEmpty() || !dryingStack.isOf(ModItems.MARIJUANA_FLOWER)) {

                    LOGGER.warn("Item in drying slot {} is invalid/gone! Resetting.", this.currentlyDryingSlot);


                    resetProgress();
                    changed = true;
                } else {
                    this.progress += TICKS_PER_ITEM;
                    changed = true;

                    LOGGER.info("Drying slot {}: progress={}/{}", this.currentlyDryingSlot, this.progress, this.maxProgress);

                    if (this.progress >= this.maxProgress) {

                        LOGGER.info("Drying complete for slot {}. Crafting.", this.currentlyDryingSlot);

                        craftItem(this.currentlyDryingSlot);
                        resetProgress(); // Also resets currentlyDryingSlot
                        // The item crafted and removed, update its sound tracking state
                        if (this.currentlyDryingSlot >= INPUT_SLOTS_INDICES[0] && this.currentlyDryingSlot <= INPUT_SLOTS_INDICES[INPUT_SLOTS_INDICES.length-1]) {
                            this.inputSlotPreviouslyHadItem[this.currentlyDryingSlot - INPUT_SLOTS_INDICES[0]] = false;
                        }
                    }
                }
            } else { // Idle, try to find a new item to process
                for (int inputSlot : INPUT_SLOTS_INDICES) {
                    ItemStack potentialStack = this.inventory.get(inputSlot);
                    if (!potentialStack.isEmpty() && potentialStack.isOf(ModItems.MARIJUANA_FLOWER)) {

                        LOGGER.info("Found item to dry in slot {}. Starting process.", inputSlot);

                        this.currentlyDryingSlot = inputSlot;
                        this.progress += TICKS_PER_ITEM; // Start progress
                        changed = true;
                        break; // Process one item at a time
                    }
                }
            }
        } else {
            if (this.currentlyDryingSlot != -1 || this.progress > 0) { // Log only if processing/paused
                LOGGER.info("Output slot full. Drying paused. progress={}, slot={}", this.progress, this.currentlyDryingSlot);
            }
        }
            // Output slot is full

        if (this.progress > 0 && this.currentlyDryingSlot == -1) {

            LOGGER.warn("Inconsistent state: progress > 0 but no drying slot set. Trying to find item or resetting.");

            boolean foundItemToContinueDrying = false;
            for (int inputSlot : INPUT_SLOTS_INDICES) {
                ItemStack potentialStack = this.inventory.get(inputSlot);
                if (!potentialStack.isEmpty() && potentialStack.isOf(ModItems.MARIJUANA_FLOWER)) {
                    this.currentlyDryingSlot = inputSlot;
                    foundItemToContinueDrying = true;
                    break;
                }
            }
            if (!foundItemToContinueDrying) {

                LOGGER.warn("No item found to continue. Resetting progress.");

                resetProgress(); // No valid item found for the current progress.
            }
            changed = true;
        }


        if (changed) {

            LOGGER.debug("State changed. Marking dirty and updating listeners.");

            this.markDirty();
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
    }

    private void resetProgress() {
        this.progress = 0;
        this.currentlyDryingSlot = -1;
    }

    private void craftItem(int inputSlotIndex) {
        ItemStack recipeOutput = new ItemStack(ModItems.DRIED_MARIJUANA_FLOWER, 1); // Output one dried item

        this.removeStack(inputSlotIndex, 1); // Consume the input item
        this.setStack(OUTPUT_SLOT_INDEX, recipeOutput.copy()); // Place result in output

        if (this.world != null) {
            this.world.playSound(null, this.pos, SoundEvents.BLOCK_SPORE_BLOSSOM_PLACE, SoundCategory.BLOCKS, 0.7f, 1.0f + this.world.random.nextFloat() * 0.2f);
        }

    }

    private boolean isOutputSlotAvailable() {
        return this.inventory.get(OUTPUT_SLOT_INDEX).isEmpty();
    }

    // Public method to check if any recipe can be processed (useful for hoppers/comparators)
    public boolean hasRecipe() {
        if (!isOutputSlotAvailable()) {
            return false;
        }
        for (int inputSlot : INPUT_SLOTS_INDICES) {
            if (!this.inventory.get(inputSlot).isEmpty() && this.inventory.get(inputSlot).isOf(ModItems.MARIJUANA_FLOWER)) {
                return true; // Found a valid item to process
            }
        }
        return false;
    }

    // Getter for rendering purposes if needed
    public boolean isCurrentlyCrafting() {
        return this.progress > 0 && this.currentlyDryingSlot != -1;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt, registryLookup);
        return nbt;
    }
}