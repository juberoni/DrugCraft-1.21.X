package net.jube.drugcraft.block.entity.custom;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.jube.drugcraft.block.entity.ImplementedInventory; // Ensure this provides a working removeStack
import net.jube.drugcraft.block.entity.ModBlockEntities;
import net.jube.drugcraft.item.ModItems;
import net.jube.drugcraft.screen.custom.DryingTableScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Sound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
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
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.windows.INPUT;

public class DryingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int FAN_SLOT = 2;

    protected final PropertyDelegate propertyDelegate;

    private int progress = 0;
    private int maxProgress = 800;

    private int previouslySoundedInputCount = 0;

    private static final int BASE_PROGRESS_PER_TICK = 1;
    private static final int FAN_BOOSTED_PROGRESS_PER_TICK = 2;

    public DryingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_TABLE_BE, pos, state);
        this.propertyDelegate = new PropertyDelegate() {

            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> DryingTableBlockEntity.this.progress;
                    case 1 -> DryingTableBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0: DryingTableBlockEntity.this.progress = value; break;
                    case 1: DryingTableBlockEntity.this.maxProgress = value; break;
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return this.pos;
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }


    @Override
    public ItemStack getStack(int slot) {
        if (slot >= 0 && slot < this.inventory.size()) {
            return this.inventory.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.drugcraft.drying_table");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new DryingTableScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        // if (this.world != null && !this.world.isClient()) {
        //     System.out.println("SERVER WRITE_NBT for " + this.pos +
        //             ": Writing Input(0): " + this.inventory.get(INPUT_SLOT) +
        //             " | Writing Output(1): " + this.inventory.get(OUTPUT_SLOT));
        // }

        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("drugcraft.drying_table.progress", progress);
        nbt.putInt("drugcraft.drying_table.max_progress", maxProgress);
    }


    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains("Items", NbtElement.LIST_TYPE)) {
            NbtList nbtList = nbt.getList("Items", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < this.inventory.size(); i++) { this.inventory.set(i, ItemStack.EMPTY); }
            for(int i = 0; i < nbtList.size(); ++i) {
                NbtCompound itemNbt = nbtList.getCompound(i);
                byte slot = itemNbt.getByte("Slot");
                if (slot >= 0 && slot < this.inventory.size()) {
                    this.inventory.set(slot, ItemStack.fromNbt(registryLookup, itemNbt).orElse(ItemStack.EMPTY));
                }
            }
        } else {
            for (int i = 0; i < this.inventory.size(); i++) { this.inventory.set(i, ItemStack.EMPTY); }
        }

        progress = nbt.getInt("drugcraft.drying_table.progress");
        maxProgress = nbt.getInt("drugcraft.drying_table.max_progress");
        if (this.maxProgress <= 0) {
            this.maxProgress = 400;
        }

        if (this.world != null && this.world.isClient) {
            MinecraftClient.getInstance().worldRenderer.scheduleBlockRenders(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }
        // Initialize previouslySoundedInputCount on load based on current valid items
        if (this.world != null && !this.world.isClient()) {
            ItemStack stack = this.inventory.get(INPUT_SLOT);
            if (stack.isOf(ModItems.MARIJUANA_FLOWER)) {
                this.previouslySoundedInputCount = Math.min(stack.getCount(), 6); // Cap at 6
            } else {
                this.previouslySoundedInputCount = 0;
            }
        }
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient()) { // Simplified server-side check
            return;
        }

        ItemStack currentInputStack = this.inventory.get(INPUT_SLOT);
        int currentActualInputCount = currentInputStack.getCount(); // Actual total count in slot
        int currentValidItemCount = 0;

        if (currentInputStack.isOf(ModItems.MARIJUANA_FLOWER)) {
            currentValidItemCount = Math.min(currentActualInputCount, 6); // Consider only up to 6 valid items
        }

        // --- Sound for Item Insertion ---
        if (currentValidItemCount > this.previouslySoundedInputCount) {
            // Play sound for each step of increase.
            int soundsToPlay = currentValidItemCount - this.previouslySoundedInputCount;
            for (int i = 0; i < soundsToPlay; i++) {
                // System.out.println("SERVER TICK: Playing insertion sound. Valid items increased from " + this.previouslySoundedInputCount + " to " + currentValidItemCount); // DEBUG
                world.playSound(
                        null,
                        pos,
                        SoundEvents.BLOCK_CHAIN_PLACE, // Your chosen sound
                        SoundCategory.BLOCKS,
                        0.4f, // Slightly lower volume if multiple can play close together
                        0.9f + world.random.nextFloat() * 0.2f + (i * 0.05f) // Slightly vary pitch for multiple sounds
                );
            }
        }
        this.previouslySoundedInputCount = currentValidItemCount;


        // --- Drying Logic ---
        boolean hasSignificantChange = false;

        if (this.canCraftIgnoringFan()) {
            this.increaseCraftingProgress();
            hasSignificantChange = true;
            if (this.hasCraftingFinished()) {
                this.craftItem();
                this.resetProgress();

            }
        } else {
            if (this.progress > 0) {
                this.resetProgress();
                hasSignificantChange = true;
            }
        }

        // --- Update Client if Needed ---
        if (hasSignificantChange) {
            this.markDirty();
            world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
        }
    }

    // Ensure craftItem still has its completion sound.
    private void resetProgress() {
        this.progress = 0;
    }

    private void craftItem() {
        ItemStack recipeOutput = new ItemStack(ModItems.DRIED_MARIJUANA_FLOWER, 1);
        this.removeStack(INPUT_SLOT, 1);

        ItemStack currentOutputStack = this.getStack(OUTPUT_SLOT);
        if (currentOutputStack.isEmpty()) {
            this.setStack(OUTPUT_SLOT, recipeOutput.copy());
        } else if (ItemStack.areItemsAndComponentsEqual(currentOutputStack, recipeOutput)) {
            currentOutputStack.increment(recipeOutput.getCount());
            this.setStack(OUTPUT_SLOT, currentOutputStack);
        }

        //DAMAGE THE FAN
        ItemStack fanStack = this.getStack(FAN_SLOT);
        if (fanStack.isOf(ModItems.DRYING_FAN) && fanStack.isDamageable() && fanStack.getDamage() < fanStack.getMaxDamage()) {
            int currentDamage = fanStack.getDamage();
            int maxDamage = fanStack.getMaxDamage();

            currentDamage++;

            if (currentDamage >= maxDamage) {
                this.setStack(FAN_SLOT, ItemStack.EMPTY); // Fan broke, remove it

                if (this.world != null) {
                    this.world.playSound(null, this.pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 0.8f, 0.8f + this.world.random.nextFloat() * 0.4f);
                }
            } else {
                fanStack.setDamage(currentDamage);
                this.setStack(FAN_SLOT, fanStack);
            }
        }
        if (this.world != null) {
            this.world.playSound(null, this.pos, SoundEvents.BLOCK_SPORE_BLOSSOM_PLACE, SoundCategory.BLOCKS, 0.7f, 1.0f + this.world.random.nextFloat() * 0.2f);

        }
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProgress() {
        ItemStack fanStack = this.getStack(FAN_SLOT);
        // Check if a working fan is present
        if (fanStack.isOf(ModItems.DRYING_FAN) && (!fanStack.isDamageable() || fanStack.getDamage() < fanStack.getMaxDamage())) {
            this.progress += FAN_BOOSTED_PROGRESS_PER_TICK; // Apply boosted progress
        } else {
            this.progress += BASE_PROGRESS_PER_TICK; // Apply base progress
        }
    }

    public boolean isCurrentlyCrafting() {
        return this.progress > 0 && this.canCraftIgnoringFan();
    }

    private boolean canCraftIgnoringFan() {
        ItemStack inputStack = this.getStack(INPUT_SLOT);
        if (inputStack.isEmpty() || !inputStack.isOf(ModItems.MARIJUANA_FLOWER)) {
            return false;
        }
        ItemStack recipeOutput = new ItemStack(ModItems.DRIED_MARIJUANA_FLOWER, 1);
        ItemStack currentOutputStack = this.getStack(OUTPUT_SLOT);
        if (currentOutputStack.isEmpty()) { return true; }
        if (!ItemStack.areItemsAndComponentsEqual(currentOutputStack, recipeOutput)) { return false; }
        return currentOutputStack.getCount() + recipeOutput.getCount() <= currentOutputStack.getMaxCount() &&
                currentOutputStack.getCount() + recipeOutput.getCount() <= this.getMaxCountPerStack();
    }

    // hasRecipe() will now just be an alias for canCraftIgnoringFan(), as the fan's presence only affects speed and durability usage, not the ability to craft slowly.

    public boolean hasRecipe() {
        return canCraftIgnoringFan();
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