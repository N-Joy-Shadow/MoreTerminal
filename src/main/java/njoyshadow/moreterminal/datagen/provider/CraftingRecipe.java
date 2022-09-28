package njoyshadow.moreterminal.datagen.provider;

import appeng.core.definitions.AEParts;
import com.blakebr0.extendedcrafting.init.ModBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import njoyshadow.moreterminal.utils.definitions.MTItems;
import njoyshadow.moreterminal.utils.definitions.MTParts;

import java.util.function.Consumer;

public class CraftingRecipe extends RecipeProvider {
    public CraftingRecipe(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected final void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        buildShapedCraftingRecipes(consumer);
    }


    public void buildShapedCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(MTParts.BASIC_CRAFTING_TERMINAL)
                .pattern("ab")
                .pattern("c ")
                .define('a', AEParts.CRAFTING_TERMINAL)
                .define('b', ModBlocks.BASIC_TABLE.get())
                .define('c', MTItems.INTEGRATION_PROCCESSOR)
                .unlockedBy("has_basic_extended_crafting_table", has(ModBlocks.BASIC_TABLE.get()))
                        .save(consumer);
                //.save(consumer, Moreterminal.MakeID("extended/basic_crafting_terminal"));

        ShapedRecipeBuilder.shaped(MTParts.ADVANCED_CRAFTING_TERMINAL)
                .pattern("ab")
                .pattern("c ")
                .define('a', AEParts.CRAFTING_TERMINAL)
                .define('b', ModBlocks.ADVANCED_TABLE.get())
                .define('c', MTItems.INTEGRATION_PROCCESSOR)
                .unlockedBy("has_basic_extended_crafting_table", has(ModBlocks.ADVANCED_TABLE.get()))
                //.save(consumer, Moreterminal.MakeID("extended/advanced_crafting_terminal"));
                .save(consumer);
        ShapedRecipeBuilder.shaped(MTParts.ELITE_CRAFTING_TERMINAL)
                .pattern("ab")
                .pattern("c ")
                .define('a', AEParts.CRAFTING_TERMINAL)
                .define('b', ModBlocks.ELITE_TABLE.get())
                .define('c', MTItems.INTEGRATION_PROCCESSOR)
                .unlockedBy("has_basic_extended_crafting_table", has(ModBlocks.ELITE_TABLE.get()))
                //.save(consumer, Moreterminal.MakeID("extended/elite_crafting_terminal"));
                .save(consumer);
        ShapedRecipeBuilder.shaped(MTParts.ULTIMATE_CRAFTING_TERMINAL)
                .pattern("ab")
                .pattern("c ")
                .define('a', AEParts.CRAFTING_TERMINAL)
                .define('b', ModBlocks.ULTIMATE_TABLE.get())
                .define('c', MTItems.INTEGRATION_PROCCESSOR)
                .unlockedBy("has_basic_extended_crafting_table", has(ModBlocks.ULTIMATE_TABLE.get()))
                //.save(consumer, Moreterminal.MakeID("extended/ultimate_crafting_terminal"));
                .save(consumer);
    }

    public String criterionName(RegistryObject<Block> block) {
        return String.format("has_%s", block.getId().getPath());
    }


}
