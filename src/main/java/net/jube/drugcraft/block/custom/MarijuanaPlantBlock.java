package net.jube.drugcraft.block.custom;

import it.unimi.dsi.fastutil.doubles.Double2ShortOpenHashMap;
import net.jube.drugcraft.item.ModItems;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.util.Hand;

public class MarijuanaPlantBlock extends CropBlock {
    public static final IntProperty AGE = IntProperty.of("age", 0, 7);
    public static final BooleanProperty TOP = BooleanProperty.of("top");

    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.createCuboidShape(2, 0, 2, 14, 2, 14),
            Block.createCuboidShape(2, 0, 2, 14, 4, 14),
            Block.createCuboidShape(2, 0, 2, 14, 6, 14),
            Block.createCuboidShape(2, 0, 2, 14, 8, 14),
            Block.createCuboidShape(2, 0, 2, 14, 10, 14),
            Block.createCuboidShape(2, 0, 2, 14, 12, 14),
            Block.createCuboidShape(2, 0, 2, 14, 16, 14),
            VoxelShapes.fullCube()
    };

    public MarijuanaPlantBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(getAgeProperty(), 0)
                .with(TOP, false));
    }

    @Override
    protected ItemConvertible getSeedsItem() {
        return ModItems.MARIJUANA_SEEDS;
    }

    @Override
    public IntProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return 7;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE, TOP);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES[state.get(getAgeProperty())];
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.isClient) return;

        if (state.get(TOP)) return; // only tick bottom block


        int age = state.get(AGE);
        if (age >= getMaxAge()) return; // No further growth if the plant is fully grown

        if (world.getBaseLightLevel(pos, 0) >= 9) {
            float growthChance = CropBlock.getAvailableMoisture(this, world, pos);

            if (random.nextInt((int) (25.0F / growthChance) + 1) == 0) {
                int newAge = age + 1;

                BlockState newState = state.with(AGE, newAge);
                world.setBlockState(pos, newState, 2);

                // If the bottom block reaches age 6, try to place the top block
                if (newAge == 6) {
                    BlockPos above = pos.up();
                    if (world.getBlockState(above).isAir()) {
                        world.setBlockState(above, this.getDefaultState().with(AGE, 6).with(TOP, true), 2);
                    } else {
                        // Prevent top block from being replaced by something else
                        world.setBlockState(pos, state.with(AGE, age), 2); // revert growth
                    }
                }

                if (newAge == 7) {
                    BlockPos above = pos.up();
                    BlockState aboveState = world.getBlockState(above);
                    if (aboveState.getBlock() == this && aboveState.get(TOP)) {
                        world.setBlockState(above, this.getDefaultState().with(AGE, 7).with(TOP, true), 2);
                    }
                }
            }
        }
    }

    public void applyGrowth(ServerWorld world, BlockState state, BlockPos pos) {
        if (state.get(TOP)) return;

        int age = state.get(AGE);
        if (age >= getMaxAge()) return;

        int newAge = age + 1;
        BlockState newState = state.with(AGE, newAge);
        world.setBlockState(pos, newState, 2);

        BlockPos above = pos.up();
        if (newAge == 6) {
            if (world.getBlockState(above).isAir()) {
                world.setBlockState(above, getDefaultState().with(AGE, 6).with(TOP, true), 2);
            }
        }

        if (newAge == 7) {
            BlockState aboveState = world.getBlockState(above);
            if (aboveState.getBlock() == this && aboveState.get(TOP)) {
                world.setBlockState(above, getDefaultState().with(AGE, 7).with(TOP, true), 2);
            }
        }
    }




    private void dropHarvest(World world, BlockPos pos, boolean fullyGrown) {
        if (fullyGrown) {
            dropStack(world, pos, new ItemStack(ModItems.MARIJUANA_FLOWER, 1 + world.random.nextInt(2)));
            dropStack(world, pos, new ItemStack(ModItems.MARIJUANA_SEEDS, 1 + world.random.nextInt(2)));
        } else {
            dropStack(world, pos, new ItemStack(ModItems.MARIJUANA_SEEDS));
        }
    }

    private boolean isTop(BlockState state) {
        return state.get(TOP);
    }


    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        boolean isTop = state.get(TOP);

        if (isTop) {
            BlockState below = world.getBlockState(pos.down());
            return below.getBlock() == this && !below.get(TOP);
        } else {
            int age = state.get(AGE);
            // allow bottom half at all ages—even if the top is missing temporarily
            return super.canPlaceAt(state, world, pos)
                    || age >= 6;
        }
    }


    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        boolean isTop = state.get(TOP);

        if (isTop && direction == Direction.DOWN) {
            if (neighborState.getBlock() != this || neighborState.get(TOP)) {
                return Blocks.AIR.getDefaultState();
            }
        }

        if (!isTop && direction == Direction.UP) {
            if (state.get(AGE) >= 6 && (neighborState.getBlock() != this || !neighborState.get(TOP))) {
                return Blocks.AIR.getDefaultState();
            }
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        int age = state.get(AGE);

        if (isTop(state)) {
            BlockPos below = pos.down();
            BlockState belowState = world.getBlockState(below);

            if (belowState.getBlock() == this) {
                dropHarvest(world, pos, belowState.get(AGE) >= getMaxAge());
                world.breakBlock(below, false);
            }
        } else {
            dropHarvest(world, pos, age >= getMaxAge());

            BlockPos above = pos.up();
            BlockState aboveState = world.getBlockState(above);
            if (aboveState.getBlock() == this && isTop(aboveState)) {
                world.breakBlock(above, false);
            }
        }

        super.afterBreak(world, player, pos, state, blockEntity, tool);
    }
}


