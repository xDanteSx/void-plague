package com.voidplague.mod.registry;

import com.voidplague.mod.VoidPlagueMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VoidPlagueMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VOID_PLAGUE_TAB =
            CREATIVE_MODE_TABS.register("void_plague_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.void_plague.void_plague_tab"))
                    .icon(() -> ModItems.VOID_MATTER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.VOID_MATTER.get());
                        output.accept(ModItems.VOID_PURIFIER.get());
                        output.accept(ModItems.VOID_ESSENCE.get());
                        output.accept(ModItems.VOID_MASK.get());
                        output.accept(ModItems.VOID_CHESTPLATE.get());
                        output.accept(ModBlocks.VOID_CARAPACE.get());
                        output.accept(ModBlocks.VOID_SPIKE.get());
                        output.accept(ModBlocks.CALCIFIED_CARAPACE.get());
                        output.accept(ModBlocks.VOID_MEMBRANE.get());
                        output.accept(ModBlocks.VOID_FLESH.get());
                        output.accept(ModBlocks.OOZING_FLESH.get());
                        output.accept(ModBlocks.VOID_CYST.get());
                        output.accept(ModBlocks.VOID_STALK.get());
                        output.accept(ModBlocks.VOID_BLOSSOM.get());
                        output.accept(ModBlocks.VOID_TENDRIL.get());
                        output.accept(ModBlocks.VOID_TENDRIL_HANGING.get());
                        output.accept(ModBlocks.VOID_TENDRIL_CARPET.get());
                        output.accept(ModBlocks.VOID_RIFT.get());
                        output.accept(ModItems.VOID_ICHOR_BUCKET.get());
                        output.accept(ModBlocks.INCUBATION_SAC.get());
                        output.accept(ModBlocks.CLUSTER_INCUBATION_SAC.get());
                        output.accept(ModBlocks.RUPTURED_INCUBATION_SAC.get());
                        output.accept(ModBlocks.VOID_POLYPS.get());
                        output.accept(ModBlocks.MIMIC_ORCHID.get());
                        output.accept(ModBlocks.CALCIFIED_RIB.get());
                        output.accept(ModBlocks.GRASPING_LIMB.get());
                        output.accept(ModItems.VOIDLING_SPAWN_EGG.get());
                        output.accept(ModItems.VOID_WATCHER_SPAWN_EGG.get());
                        output.accept(ModItems.VOID_WATCHER_SENTINEL_SPAWN_EGG.get());
                    })
                    .build());
}
