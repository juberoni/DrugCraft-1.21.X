package net.jube.drugcraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.jube.drugcraft.block.entity.custom.DryingTableBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DryingTableBlock extends BlockWithEntity implements BlockEntityProvider {
    private static final VoxelShape SHAPE =
            Block.createCuboidShape(0,0,0,16,16,16);

    public static final MapCodec<DryingTableBlock> CODEC = DryingTableBlock.createCodec(DryingTableBlock::new);


    public DryingTableBlock(Settings settings) {
        super(settings);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new DryingTableBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if(state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof DryingTableBlockEntity) {
                ItemScatterer.spawn(world, pos, ((DryingTableBlockEntity) blockEntity));
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world,pos, newState, moved);
        }
    }

    @Override
    public ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                             PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(world.getBlockEntity(pos) instanceof DryingTableBlockEntity dryingTableBlockEntity) {
            if (dryingTableBlockEntity.isEmpty() && !stack.isEmpty()) {
                dryingTableBlockEntity.setStack(0, stack.copyWithCount(1));
                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 2f);
                stack.decrement(1);

                dryingTableBlockEntity.markDirty();
                world.updateListeners(pos, state, state, 0);
            } else if (stack.isEmpty() && !player.isSneaking()) {
                ItemStack stackonDryingRack = dryingTableBlockEntity.getStack(0);
                player.setStackInHand(Hand.MAIN_HAND, stackonDryingRack);
                world.playSound(player, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 1f, 1f);
                dryingTableBlockEntity.clear();

                dryingTableBlockEntity.markDirty();
                world.updateListeners(pos, state, state, 0);
            } else if (player.isSneaking() && !world.isClient()) {
                player.openHandledScreen(dryingTableBlockEntity);
            }
        }

        return ItemActionResult.SUCCESS;
    }
}