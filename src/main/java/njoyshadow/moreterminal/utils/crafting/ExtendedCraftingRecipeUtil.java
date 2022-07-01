package njoyshadow.moreterminal.utils.crafting;

import com.blakebr0.extendedcrafting.crafting.recipe.ShapedTableRecipe;
import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class ExtendedCraftingRecipeUtil {
    public static NonNullList<Ingredient> ensureExtendedCraftingMatrix(Recipe<?> recipe, int Grid_Size){
        int Grid_Matrix = Grid_Size * Grid_Size;


        var ingredients = recipe.getIngredients();
        var expandedIngredients = NonNullList.withSize(Grid_Matrix, Ingredient.EMPTY);

        Preconditions.checkArgument(ingredients.size() <= Grid_Matrix);

        // shaped recipes can be smaller than 3x3, expand to 3x3 to match the crafting
        // matrix
        if (recipe instanceof ShapedTableRecipe shapedRecipe) {
            var width = shapedRecipe.getWidth();
            var height = shapedRecipe.getHeight();
            Preconditions.checkArgument(width <= Grid_Size && height <= Grid_Size);

            for (var h = 0; h < height; h++) {
                for (var w = 0; w < width; w++) {
                    var source = w + h * width;
                    var target = w + h * Grid_Size;
                    var i = ingredients.get(source);
                    expandedIngredients.set(target, i);
                }
            }
        }
        // Anything else should be a flat list
        else {
            for (var i = 0; i < ingredients.size(); i++) {
                expandedIngredients.set(i, ingredients.get(i));
            }
        }

        return expandedIngredients;
    }
}
