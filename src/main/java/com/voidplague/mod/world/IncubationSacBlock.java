package com.voidplague.mod.world;

import com.mojang.serialization.MapCodec;
import com.voidplague.mod.entity.IncubationSacBlockEntity;
import com.voidplague.mod.entity.VoidlingEntity;
import com.voidplague.mod.registry.ModBlocks;
import com.voidplague.mod.registry.ModEffects;
import com.voidplague.mod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class IncubationSacBlock extends BaseEntityBlock {
    public static final MapCodec<IncubationSacBlock> CODEC = simpleCodec(IncubationSacBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    // VoxelShapes according to support face (bulbo hinchado y tallo corto)
    private static final VoxelShape SHAPE_UP    = Block.box(2.0, 1.0, 2.0, 14.0, 16.0, 14.0); // Colgante de techo
    private static final VoxelShape SHAPE_DOWN  = Block.box(2.0, 0.0, 2.0, 14.0, 15.0, 14.0); // Brota del suelo
    private static final VoxelShape SHAPE_NORTH = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 15.0); // Pared norte
    private static final VoxelShape SHAPE_SOUTH = Block.box(2.0, 2.0, 1.0, 14.0, 14.0, 16.0); // Pared sur
    private static final VoxelShape SHAPE_WEST  = Block.box(0.0, 2.0, 2.0, 15.0, 14.0, 14.0); // Pared oeste
    private static final VoxelShape SHAPE_EAST  = Block.box(1.0, 2.0, 2.0, 16.0, 14.0, 14.0); // Pared este

    public IncubationSacBlock(Properties properties) {
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
        return new IncubationSacBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(FACING) == Direction.UP) {
            // Con una probabilidad de 1 en 3 ticks, genera una partícula pesada en la punta inferior del bulbo
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, pos.getX() + 0.5, pos.getY() + 0.15, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
            }
            // Ocasionalmente añade una partícula de ParticleTypes.PORTAL que ascienda suavemente por el cuerpo del saco
            if (random.nextInt(5) == 0) {
                level.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                        pos.getY() + 0.3,
                        pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                        0.0, 0.04, 0.0);
            }
        } else {
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 0.0, 0.0, 0.0);
            }
            if (random.nextInt(5) == 0) {
                level.addParticle(ParticleTypes.PORTAL, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                        pos.getY() + 0.4,
                        pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                        0.0, 0.04, 0.0);
            }
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof Player player && player.isSprinting()) {
            rupture(level, pos, player);
            return;
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof Player player && player.isSprinting()) {
            rupture(level, pos, player);
            return;
        }
        super.entityInside(state, level, pos, entity);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && !player.isCreative()) {
            boolean hasSilkTouch = false;
            try {
                Holder<Enchantment> silkTouch = level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SILK_TOUCH);
                hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(silkTouch, player.getMainHandItem()) > 0;
            } catch (Exception ignored) {
            }

            if (!hasSilkTouch) {
                // 1. Spawnea los Voidlings agresivos y aplica efectos
                spawnVoidlingsOnRupture(level, pos, state, player);

                // 2. Transmuta la posición a cascarón roto conservando la orientación
                Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.UP;
                BlockState rupturedState = ModBlocks.RUPTURED_INCUBATION_SAC.get().defaultBlockState()
                        .setValue(RupturedIncubationSacBlock.FACING, facing);
                level.setBlock(pos, rupturedState, Block.UPDATE_ALL);

                // Sonido de rotura húmeda de quitina
                level.playSound(null, pos, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.BLOCKS, 1.0F, 0.7F);

                // Evita la destrucción estándar a AIR devolviendo el nuevo estado
                return rupturedState;
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        this.playerWillDestroy(level, pos, state, player);
        if (!level.isClientSide && !player.isCreative()) {
            boolean hasSilkTouch = false;
            try {
                Holder<Enchantment> silkTouch = level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.SILK_TOUCH);
                hasSilkTouch = EnchantmentHelper.getItemEnchantmentLevel(silkTouch, player.getMainHandItem()) > 0;
            } catch (Exception ignored) {
            }

            if (!hasSilkTouch) {
                // Evita que NeoForge sobreescriba la posición transmutada con AIR
                return false;
            }
        }
        return level.setBlock(pos, fluid.createLegacyBlock(), level.isClientSide ? 11 : 3);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide) {
            rupture(level, pos, null);
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    public void spawnVoidlingsOnRupture(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity triggerer) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.08);
            serverLevel.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.5, 0.5, 0.5, 0.2);
            serverLevel.playSound(null, pos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 1.5F, 1.8F);
            serverLevel.playSound(null, pos, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.BLOCKS, 1.2F, 0.6F);

            AABB area = new AABB(pos).inflate(4.0);
            List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, area);
            for (LivingEntity e : entities) {
                if (e instanceof VoidlingEntity) continue;
                e.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));
                e.addEffect(new MobEffectInstance(ModEffects.VOID_SICKNESS, 160, 0));
            }

            int count = 1 + serverLevel.random.nextInt(3);
            for (int i = 0; i < count; i++) {
                VoidlingEntity voidling = ModEntities.VOIDLING.get().create(serverLevel);
                if (voidling != null) {
                    double ox = pos.getX() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 0.8;
                    double oz = pos.getZ() + 0.5 + (serverLevel.random.nextDouble() - 0.5) * 0.8;
                    voidling.moveTo(ox, pos.getY() + 0.1, oz, serverLevel.random.nextFloat() * 360F, 0.0F);
                    if (triggerer != null && !(triggerer instanceof VoidlingEntity)) {
                        voidling.setTarget(triggerer);
                    }
                    serverLevel.addFreshEntity(voidling);
                }
            }
        }
    }

    public void rupture(Level level, BlockPos pos, @Nullable LivingEntity triggerer) {
        BlockState state = level.getBlockState(pos);
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.UP;
        spawnVoidlingsOnRupture(level, pos, state, triggerer);
        BlockState rupturedState = ModBlocks.RUPTURED_INCUBATION_SAC.get().defaultBlockState()
                .setValue(RupturedIncubationSacBlock.FACING, facing);
        level.setBlock(pos, rupturedState, Block.UPDATE_ALL);
    }
}
