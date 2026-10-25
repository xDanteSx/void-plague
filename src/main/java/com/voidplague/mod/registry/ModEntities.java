package com.voidplague.mod.registry;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.entity.VoidWatcherEntity;
import com.voidplague.mod.entity.VoidWatcherSentinelEntity;
import com.voidplague.mod.entity.VoidlingEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, VoidPlagueMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<VoidlingEntity>> VOIDLING =
            ENTITIES.register("voidling", () -> EntityType.Builder.of(VoidlingEntity::new, MobCategory.MONSTER)
                    .sized(1.2f, 0.9f)
                    .clientTrackingRange(8)
                    .build("voidling"));

    public static final DeferredHolder<EntityType<?>, EntityType<VoidWatcherEntity>> VOID_WATCHER =
            ENTITIES.register("void_watcher", () -> EntityType.Builder.of(VoidWatcherEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 0.8F)
                    .clientTrackingRange(10)
                    .build("void_watcher"));

    public static final DeferredHolder<EntityType<?>, EntityType<VoidWatcherSentinelEntity>> VOID_WATCHER_SENTINEL =
            ENTITIES.register("void_watcher_sentinel", () -> EntityType.Builder.of(VoidWatcherSentinelEntity::new, MobCategory.MONSTER)
                    .sized(2.0F, 3.5F)
                    .clientTrackingRange(16)
                    .build("void_watcher_sentinel"));
}
