package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import com.voidplague.mod.entity.RupturedIncubationSacBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class RupturedIncubationSacBlock extends BaseEntityBlock {
    public static final MapCodec<RupturedIncubationSacBlock> CODEC = simpleCodec(RupturedIncubationSacBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // VoxelShapes according to support face (cúpula abierta reventada)
    private static final VoxelShape SHAPE_UP    = Block.box(2.0, 5.0, 2.0, 14.0, 16.0, 14.0); // Colgante de techo
    private static final VoxelShape SHAPE_DOWN  = Block.box(2.0, 0.0, 2.0, 14.0, 11.0, 14.0); // Brota del suelo
    private static final VoxelShape SHAPE_NORTH = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 11.0); // Pared norte
    private static final VoxelShape SHAPE_SOUTH = Block.box(2.0, 2.0, 5.0, 14.0, 14.0, 16.0); // Pared sur
    private static final VoxelShape SHAPE_WEST  = Block.box(0.0, 2.0, 2.0, 11.0, 14.0, 14.0); // Pared oeste
    private static final VoxelShape SHAPE_EAST  = Block.box(5.0, 2.0, 2.0, 16.0, 14.0, 14.0); // Pared este

    public RupturedIncubationSacBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        Direction supportDir = clickedFace.getOpposite();
        BlockState state = this.defaultBlockState().setValue(FACING, supportDir);
        if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
            return state;
        }
        for (Direction dir : context.getNearestLookingDirections()) {
            state = this.defaultBlockState().setValue(FACING, dir);
            if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
                return state;
            }
        }
        return null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction supportDir = state.getValue(FACING);
        BlockPos supportPos = pos.relative(supportDir);
        BlockState supportState = level.getBlockState(supportPos);
        return supportState.isFaceSturdy(level, supportPos, supportDir.getOpposite(), SupportType.CENTER);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.getValue(FACING) && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP    -> SHAPE_UP;
            case DOWN  -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST  -> SHAPE_WEST;
            case EAST  -> SHAPE_EAST;
        };
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RupturedIncubationSacBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Emite periódicamente partículas tenues de vapor fétido saliendo de la cavidad vacía
        if (random.nextInt(3) == 0) {
            Direction facing = state.getValue(FACING);
            double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3;

            if (facing == Direction.UP) {
                y = pos.getY() + 0.35;
            } else if (facing == Direction.DOWN) {
                y = pos.getY() + 0.65;
            }

            if (random.nextBoolean()) {
                level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, -0.015, 0.0);
            } else {
                level.addParticle(ParticleTypes.MYCELIUM, x, y, z,
                        (random.nextDouble() - 0.5) * 0.03,
                        0.01,
                        (random.nextDouble() - 0.5) * 0.03);
            }
        }
    }
}
