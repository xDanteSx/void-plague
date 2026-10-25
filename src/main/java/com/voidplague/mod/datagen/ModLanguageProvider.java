package com.voidplague.mod.datagen;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.registry.ModBlocks;
import com.voidplague.mod.registry.ModEffects;
import com.voidplague.mod.registry.ModEntities;
import com.voidplague.mod.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    private final String locale;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, VoidPlagueMod.MOD_ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        if ("es_es".equalsIgnoreCase(this.locale)) {
            add("itemGroup.void_plague.void_plague_tab", "La Plaga del Vacío");
            addItem(ModItems.VOID_MATTER, "Materia del Vacío");
            addItem(ModItems.VOID_PURIFIER, "Purificador del Vacío");
            addItem(ModItems.VOID_ESSENCE, "Esencia del Vacío");
            addItem(ModItems.VOID_MASK, "Máscara del Vacío");
            addItem(ModItems.VOID_CHESTPLATE, "Pechera de Caparazón del Vacío");
            addBlock(ModBlocks.VOID_CARAPACE, "Caparazón del Vacío");
            addBlock(ModBlocks.VOID_SPIKE, "Espina del Vacío");
            addBlock(ModBlocks.CALCIFIED_CARAPACE, "Quitina Calcificada");
            addBlock(ModBlocks.VOID_MEMBRANE, "Membrana del Vacío");
            addBlock(ModBlocks.VOID_FLESH, "Carne del Vacío");
            addBlock(ModBlocks.OOZING_FLESH, "Carne Supurante del Vacío");
            addBlock(ModBlocks.VOID_CYST, "Quiste del Vacío");
            addBlock(ModBlocks.VOID_STALK, "Tallo del Vacío");
            addBlock(ModBlocks.VOID_BLOSSOM, "Flor del Vacío");
            addBlock(ModBlocks.VOID_TENDRIL, "Zarcillo del Vacío");
            addBlock(ModBlocks.VOID_TENDRIL_HANGING, "Zarcillo Colgante del Vacío");
            addBlock(ModBlocks.VOID_TENDRIL_CARPET, "Manto de Zarcillos del Vacío");
            addBlock(ModBlocks.VOID_RIFT, "Grieta del Vacío");
            addItem(ModItems.VOID_ICHOR_BUCKET, "Cubo con Icor del Vacío");
            addBlock(ModBlocks.INCUBATION_SAC, "Saco de Incubación");
            addBlock(ModBlocks.CLUSTER_INCUBATION_SAC, "Racimo de Incubación del Vacío");
            addBlock(ModBlocks.RUPTURED_INCUBATION_SAC, "Cascarón Roto de Incubación");
            addBlock(ModBlocks.VOID_POLYPS, "Pólipos del Vacío");
            addBlock(ModBlocks.MIMIC_ORCHID, "Orquídea Mimética");
            addBlock(ModBlocks.CALCIFIED_RIB, "Costilla Calcificada");
            addBlock(ModBlocks.GRASPING_LIMB, "Extremidad Suplicante del Vacío");
            addBlock(ModBlocks.VOID_ICHOR_BLOCK, "Icor del Vacío");
            add("fluid_type.void_plague.void_ichor", "Icor del Vacío");
            addEntityType(ModEntities.VOIDLING, "Engendro del Vacío");
            addItem(ModItems.VOIDLING_SPAWN_EGG, "Huevo generador de Engendro del Vacío");
            addEntityType(ModEntities.VOID_WATCHER, "Vigía del Vacío");
            addItem(ModItems.VOID_WATCHER_SPAWN_EGG, "Huevo generador de Vigía del Vacío");
            addEntityType(ModEntities.VOID_WATCHER_SENTINEL, "Centinela Abisal del Vacío");
            addItem(ModItems.VOID_WATCHER_SENTINEL_SPAWN_EGG, "Generador de Centinela Abisal del Vacío");
            addEffect(ModEffects.VOID_SICKNESS, "Mal del Vacío");
            add("biome.void_plague.void_chasm", "Sima del Vacío");
            add("subtitles.void_plague.void_ambient", "Zumbido del Vacío");
            add("subtitles.void_plague.void_whispers", "Susurros del Vacío");
        } else {
            add("itemGroup.void_plague.void_plague_tab", "The Void Plague");
            addItem(ModItems.VOID_MATTER, "Void Matter");
            addItem(ModItems.VOID_PURIFIER, "Void Purifier");
            addItem(ModItems.VOID_ESSENCE, "Void Essence");
            addItem(ModItems.VOID_MASK, "Void Mask");
            addItem(ModItems.VOID_CHESTPLATE, "Void Carapace Chestplate");
            addBlock(ModBlocks.VOID_CARAPACE, "Void Carapace");
            addBlock(ModBlocks.VOID_SPIKE, "Void Spike");
            addBlock(ModBlocks.CALCIFIED_CARAPACE, "Calcified Carapace");
            addBlock(ModBlocks.VOID_MEMBRANE, "Void Membrane");
            addBlock(ModBlocks.VOID_FLESH, "Void Flesh");
            addBlock(ModBlocks.OOZING_FLESH, "Oozing Void Flesh");
            addBlock(ModBlocks.VOID_CYST, "Void Cyst");
            addBlock(ModBlocks.VOID_STALK, "Void Stalk");
            addBlock(ModBlocks.VOID_BLOSSOM, "Void Blossom");
            addBlock(ModBlocks.VOID_TENDRIL, "Void Tendril");
            addBlock(ModBlocks.VOID_TENDRIL_HANGING, "Hanging Void Tendril");
            addBlock(ModBlocks.VOID_TENDRIL_CARPET, "Void Tendril Carpet");
            addBlock(ModBlocks.VOID_RIFT, "Void Rift");
            addItem(ModItems.VOID_ICHOR_BUCKET, "Void Ichor Bucket");
            addBlock(ModBlocks.INCUBATION_SAC, "Incubation Sac");
            addBlock(ModBlocks.CLUSTER_INCUBATION_SAC, "Cluster Incubation Sac");
            addBlock(ModBlocks.RUPTURED_INCUBATION_SAC, "Ruptured Incubation Sac");
            addBlock(ModBlocks.VOID_POLYPS, "Void Polyps");
            addBlock(ModBlocks.MIMIC_ORCHID, "Mimic Orchid");
            addBlock(ModBlocks.CALCIFIED_RIB, "Calcified Rib");
            addBlock(ModBlocks.GRASPING_LIMB, "Grasping Void Limb");
            addBlock(ModBlocks.VOID_ICHOR_BLOCK, "Void Ichor");
            add("fluid_type.void_plague.void_ichor", "Void Ichor");
            addEntityType(ModEntities.VOIDLING, "Voidling");
            addItem(ModItems.VOIDLING_SPAWN_EGG, "Voidling Spawn Egg");
            addEntityType(ModEntities.VOID_WATCHER, "Void Watcher");
            addItem(ModItems.VOID_WATCHER_SPAWN_EGG, "Void Watcher Spawn Egg");
            addEntityType(ModEntities.VOID_WATCHER_SENTINEL, "Void Watcher Sentinel");
            addItem(ModItems.VOID_WATCHER_SENTINEL_SPAWN_EGG, "Void Watcher Sentinel Spawn Egg");
            addEffect(ModEffects.VOID_SICKNESS, "Void Sickness");
            add("biome.void_plague.void_chasm", "Void Chasm");
            add("subtitles.void_plague.void_ambient", "Void hums");
            add("subtitles.void_plague.void_whispers", "Void whispers");
        }
    }
}
