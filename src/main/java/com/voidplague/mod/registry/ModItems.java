package com.voidplague.mod.registry;

import com.voidplague.mod.VoidPlagueMod;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(VoidPlagueMod.MOD_ID);

    // Foundational items
    public static final DeferredItem<Item> VOID_MATTER = ITEMS.registerSimpleItem(
            "void_matter",
            new Item.Properties()
    );

    public static final DeferredItem<Item> VOID_PURIFIER = ITEMS.registerSimpleItem(
            "void_purifier",
            new Item.Properties().stacksTo(16)
    );

    public static final DeferredItem<Item> VOID_ESSENCE = ITEMS.registerSimpleItem(
            "void_essence",
            new Item.Properties().stacksTo(64)
    );

    // Symbiotic Armor
    public static final DeferredItem<ArmorItem> VOID_MASK = ITEMS.register(
            "void_mask",
            () -> new ArmorItem(
                    ModArmorMaterials.VOID_CARAPACE,
                    ArmorItem.Type.HELMET,
                    new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(37))
            )
    );

    public static final DeferredItem<ArmorItem> VOID_CHESTPLATE = ITEMS.register(
            "void_chestplate",
            () -> new ArmorItem(
                    ModArmorMaterials.VOID_CARAPACE,
                    ArmorItem.Type.CHESTPLATE,
                    new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(37))
            )
    );

    // BlockItems
    public static final DeferredItem<BlockItem> VOID_CARAPACE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_CARAPACE
    );

    public static final DeferredItem<BlockItem> VOID_SPIKE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_SPIKE
    );

    public static final DeferredItem<BlockItem> VOID_MEMBRANE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_MEMBRANE
    );

    public static final DeferredItem<BlockItem> VOID_FLESH_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_FLESH
    );

    public static final DeferredItem<BlockItem> VOID_CYST_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_CYST
    );

    public static final DeferredItem<BlockItem> VOID_TENDRIL_HANGING_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_TENDRIL_HANGING
    );

    public static final DeferredItem<BlockItem> VOID_TENDRIL_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_TENDRIL
    );

    public static final DeferredItem<BlockItem> VOID_RIFT_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_RIFT
    );

    public static final DeferredItem<BlockItem> CALCIFIED_CARAPACE_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.CALCIFIED_CARAPACE
    );

    public static final DeferredItem<BlockItem> OOZING_FLESH_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.OOZING_FLESH
    );

    public static final DeferredItem<BlockItem> VOID_STALK_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_STALK
    );

    public static final DeferredItem<BlockItem> VOID_BLOSSOM_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_BLOSSOM
    );

    public static final DeferredItem<BlockItem> VOID_TENDRIL_CARPET_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_TENDRIL_CARPET
    );

    public static final DeferredItem<BucketItem> VOID_ICHOR_BUCKET = ITEMS.register(
            "void_ichor_bucket",
            () -> new BucketItem(ModFluids.VOID_ICHOR_SOURCE.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1))
    );

    public static final DeferredItem<BlockItem> INCUBATION_SAC_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.INCUBATION_SAC
    );

    public static final DeferredItem<BlockItem> CLUSTER_INCUBATION_SAC_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.CLUSTER_INCUBATION_SAC
    );

    public static final DeferredItem<BlockItem> RUPTURED_INCUBATION_SAC_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.RUPTURED_INCUBATION_SAC
    );

    public static final DeferredItem<BlockItem> VOID_POLYPS_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.VOID_POLYPS
    );

    public static final DeferredItem<BlockItem> MIMIC_ORCHID_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.MIMIC_ORCHID
    );

    public static final DeferredItem<BlockItem> CALCIFIED_RIB_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.CALCIFIED_RIB
    );

    public static final DeferredItem<BlockItem> GRASPING_LIMB_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.GRASPING_LIMB
    );

    // Spawn eggs
    public static final DeferredItem<Item> VOIDLING_SPAWN_EGG = ITEMS.register(
            "voidling_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.VOIDLING, 0x1A0926, 0x9B26B6, new Item.Properties())
    );

    public static final DeferredItem<Item> VOID_WATCHER_SPAWN_EGG = ITEMS.register(
            "void_watcher_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.VOID_WATCHER, 0x0A0512, 0x5C1C8F, new Item.Properties())
    );

    public static final DeferredItem<Item> VOID_WATCHER_SENTINEL_SPAWN_EGG = ITEMS.register(
            "void_watcher_sentinel_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.VOID_WATCHER_SENTINEL, 0x0A0512, 0x4E1F94, new Item.Properties())
    );
}
