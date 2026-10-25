package com.voidplague.mod.datagen;

import com.voidplague.mod.registry.ModBlocks;
import com.voidplague.mod.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        // Void Purifier: cross pattern with amethyst core, blaze rods, and ghast tears
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.VOID_PURIFIER.get(), 1)
                .pattern(" B ")
                .pattern("GAG")
                .pattern(" B ")
                .define('B', Items.BLAZE_ROD)
                .define('A', Items.AMETHYST_SHARD)
                .define('G', Items.GHAST_TEAR)
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(output);

        // Void Mask: helmet made of void matter and void essence
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.VOID_MASK.get())
                .pattern("MEM")
                .pattern("M M")
                .define('M', ModItems.VOID_MATTER.get())
                .define('E', ModItems.VOID_ESSENCE.get())
                .unlockedBy("has_void_essence", has(ModItems.VOID_ESSENCE.get()))
                .save(output);

        // Void Carapace Chestplate: chestplate made of void matter and void essence
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.VOID_CHESTPLATE.get())
                .pattern("M M")
                .pattern("MEM")
                .pattern("MMM")
                .define('M', ModItems.VOID_MATTER.get())
                .define('E', ModItems.VOID_ESSENCE.get())
                .unlockedBy("has_void_essence", has(ModItems.VOID_ESSENCE.get()))
                .save(output);

        // Void Ichor Bucket: transmuting water with void essence and matter
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VOID_ICHOR_BUCKET.get())
                .requires(Items.WATER_BUCKET)
                .requires(ModItems.VOID_ESSENCE.get(), 2)
                .requires(ModItems.VOID_MATTER.get(), 2)
                .unlockedBy("has_void_essence", has(ModItems.VOID_ESSENCE.get()))
                .save(output);

        // Calcified Rib: bone blocks reinforced with calcified carapace
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CALCIFIED_RIB.get(), 4)
                .pattern(" B ")
                .pattern("BCB")
                .pattern(" B ")
                .define('B', Items.BONE_BLOCK)
                .define('C', ModBlocks.CALCIFIED_CARAPACE.get())
                .unlockedBy("has_calcified_carapace", has(ModBlocks.CALCIFIED_CARAPACE.get()))
                .save(output);
    }
}
