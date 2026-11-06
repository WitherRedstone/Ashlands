package com.chinaex123.ashlands.data;

import com.chinaex123.ashlands.init.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ModRecipesProvider extends RecipeProvider {
    public ModRecipesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURIFIER.get())
                .pattern("NEN")
                .pattern("ESE")
                .pattern("NEN")
                .define('N', Items.NETHERITE_INGOT)
                .define('E', Items.ECHO_SHARD)
                .define('S', Items.NETHER_STAR)
                .unlockedBy(getHasName(Items.NETHER_STAR), has(Items.NETHER_STAR))
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT))
                .unlockedBy(getHasName(Items.ECHO_SHARD), has(Items.ECHO_SHARD))
                .save(recipeOutput);
    }
}
