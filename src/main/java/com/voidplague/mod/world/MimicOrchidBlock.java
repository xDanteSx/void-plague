package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import com.voidplague.mod.entity.VoidlingEntity;
import com.voidplague.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class MimicOrchidBlock extends BushBlock {
    public static final MapCodec<MimicOrchidBlock> CODEC = simpleCodec(MimicOrchidBlock::new);
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 4.0, 15.0);

    public MimicOrchidBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any().setValue(OPEN, true));
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(ModBlocks.VOID_CARAPACE.get())
                || state.is(ModBlocks.CALCIFIED_CARAPACE.get())
                || state.is(ModBlocks.VOID_FLESH.get())
                || state.is(ModBlocks.VOID_MEMBRANE.get())
                || state.is(ModBlocks.OOZING_FLESH.get())
                || state.is(BlockTags.DIRT)
                || state.is(BlockTags.STONE_ORE_REPLACEABLES);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living && !(living instanceof VoidlingEntity)) {
            if (state.getValue(OPEN)) {
                // Snap closed
                level.setBlock(pos, state.setValue(OPEN, false), 3);
                level.scheduleTick(pos, this, 120);

                // Slowness III (amplifier 2) for 80 ticks
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 2));

                // 3.0F piercing damage
                living.hurt(level.damageSources().sweetBerryBush(), 3.0F);

                // Trap snapping sound
                level.playSound(null, pos, SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.BLOCKS, 1.0F, 1.2F);

                // Alert nearby Voidlings within 16 blocks
                AABB alertArea = new AABB(pos).inflate(16.0);
                List<VoidlingEntity> voidlings = level.getEntitiesOfClass(VoidlingEntity.class, alertArea);
                for (VoidlingEntity voidling : voidlings) {
                    voidling.setTarget(living);
                }
            }
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(OPEN)) {
            level.setBlock(pos, state.setValue(OPEN, true), 3);
            level.playSound(null, pos, SoundEvents.FLOWERING_AZALEA_PLACE, SoundSource.BLOCKS, 0.8F, 1.2F);
        }
    }
}
