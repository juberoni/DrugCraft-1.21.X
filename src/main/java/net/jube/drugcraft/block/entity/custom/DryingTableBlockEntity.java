package net.jube.drugcraft.block.entity.custom;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.jube.drugcraft.block.custom.DryingTableBlock;
import net.jube.drugcraft.block.entity.ImplementedInventory;
import net.jube.drugcraft.block.entity.ModBlockEntities;
import net.jube.drugcraft.item.ModItems;
import net.jube.drugcraft.screen.custom.DryingTableScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
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
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.tick.TickPriority;
import org.jetbrains.annotations.Nullable;

public class DryingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos>, ImplementedInventory {

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int MAX_PROGRESS_DEFAULT = 120;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
    private final PropertyDelegate propertyDelegate;

    private int progress = 0;
    private int maxProgress = MAX_PROGRESS_DEFAULT;
    private boolean hasPlayedInputSound = false;

    public DryingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_TABLE_BE, pos, state);
        this.maxProgress = MAX_PROGRESS_DEFAULT;
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0 -> progress = value;
                    case 1 -> maxProgress = value;
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world == null || world.isClient) return;

        ItemStack inputStack = getStack(INPUT_SLOT);

        // Play sound when the input item is added
        if (!inputStack.isEmpty() && !hasPlayedInputSound) {
            playInputSound(world, pos);
            hasPlayedInputSound = true;
        } else if (inputStack.isEmpty()) {
            hasPlayedInputSound = false;
        }

        if (hasRecipe()) {
            increaseCraftingProgress();
            markDirty(world, pos, state);

            // Check if the drying is finished
            if (hasCraftingFinished()) {
                resetProgress();  // Reset progress before any changes
                craftItem();  // Craft the item

                // Update block state to "dried" when the crafting finishes
                BlockState newState = state.with(DryingTableBlock.DRYING, false); // Update to dried state
                world.setBlockState(pos, newState, 3); // Update block state in the world
                world.updateListeners(pos, state, newState, 3); // Notify the world about the block state change

                playCraftingFinishedSound(world, pos);  // Play the crafting finished sound
            }
        } else {
            resetProgress();
        }

        this.syncToClient();  // Sync with the client

        // Schedule next tick
        world.scheduleBlockTick(pos, state.getBlock(), 20, TickPriority.NORMAL);

        // Debugging info
        System.out.println("Input Slot is empty: " + inputStack.isEmpty());
        System.out.println("Has recipe: " + hasRecipe());
        System.out.println("Progress: " + progress + "/" + maxProgress);
    }



    private void playInputSound(World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, pos, SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    private void playCraftingFinishedSound(World world, BlockPos pos) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, pos, SoundEvents.BLOCK_SPORE_BLOSSOM_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    private void resetProgress() {
        progress = 0;
        markDirty();
        syncToClient();
    }

    private void increaseCraftingProgress() {
        progress++;
        markDirty();
        syncToClient();
    }

    private boolean hasCraftingFinished() {
        return progress >= maxProgress;
    }

    private boolean hasRecipe() {
        ItemStack input = getStack(INPUT_SLOT);
        ItemStack output = new ItemStack(ModItems.DRIED_MARIJUANA_FLOWER);
        return input.isOf(ModItems.MARIJUANA_FLOWER) &&
                canInsertAmountIntoOutputSlot(output.getCount()) &&
                canInsertItemIntoOutputSlot(output);
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        ItemStack currentOutput = getStack(OUTPUT_SLOT);
        return currentOutput.isEmpty() || currentOutput.getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        ItemStack output = getStack(OUTPUT_SLOT);
        return output.getCount() + count <= output.getMaxCount();
    }

    private void craftItem() {
        if (world == null || world.isClient) return;

        // Get the input and output stacks
        ItemStack input = getStack(INPUT_SLOT);
        ItemStack output = getStack(OUTPUT_SLOT);

        // Check if the input is valid
        if (input.isEmpty()) return;

        // Create the output item (dried marijuana flower)
        ItemStack result = new ItemStack(ModItems.DRIED_MARIJUANA_FLOWER);

        // Decrement the input stack completely (remove 1 raw flower)
        input.decrement(1);
        if (input.isEmpty()) {
            setStack(INPUT_SLOT, ItemStack.EMPTY); // Explicitly clear the input slot if it's empty
        }

        // Handle output slot - either replace or stack
        if (output.isEmpty()) {
            // If the output slot is empty, insert the result (dried flower)
            setStack(OUTPUT_SLOT, result);
        } else if (output.isOf(ModItems.DRIED_MARIJUANA_FLOWER)) {
            // If the output slot contains dried flowers, increment the count (stack them)
            int newCount = output.getCount() + 1;
            if (newCount <= output.getMaxCount()) {
                output.setCount(newCount);
                setStack(OUTPUT_SLOT, output); // Update the output stack
            }
        }

        // Mark the block entity as dirty to notify the world
        markDirty();

        // Sync the changes to the client to prevent rendering issues
        syncToClient();

        // Debugging info to help track the issue
        System.out.println("Output after crafting: " + getStack(OUTPUT_SLOT));
    }


    public void syncToClient() {
        if (world instanceof ServerWorld serverWorld) {
            // Create the BlockEntity update packet
            BlockEntityUpdateS2CPacket packet = BlockEntityUpdateS2CPacket.create(this);

            // Send the packet to all players in a certain range
            for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                if (player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 64) {
                    // Send the packet to the player
                    player.networkHandler.sendPacket(packet);
                }
            }
        }
    }



    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("drying_table.progress", progress);
        nbt.putInt("drying_table.max_progress", maxProgress);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Inventories.readNbt(nbt, inventory, registryLookup);
        progress = nbt.getInt("drying_table.progress");
        maxProgress = nbt.getInt("drying_table.max_progress");
        super.readNbt(nbt, registryLookup);
    }

    // GUI / Inventory

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.drugcraft.drying_table");
    }

    public float getDryingProgress() {
        // Return the progress as a float, between 0 and 1
        return this.progress / this.maxProgress;
    }


    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new DryingTableScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return pos;
    }



}
