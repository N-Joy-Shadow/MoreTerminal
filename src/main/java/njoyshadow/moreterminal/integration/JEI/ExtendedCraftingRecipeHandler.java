package njoyshadow.moreterminal.integration.JEI;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public abstract class ExtendedCraftingRecipeHandler {
    public final int GRID_WIDTH;
    public final int GRID_HEIGHT;
    public final int GRID_MATRIX;


    public ExtendedCraftingRecipeHandler(int GridSize){
        this.GRID_HEIGHT = GridSize;
        this.GRID_WIDTH = GridSize;
        this.GRID_MATRIX = GridSize * GridSize;
    }

    protected final boolean isCraftingRecipe(Recipe<?> recipe, IRecipeLayout display) {
        return recipe != null && recipe.getType() == RecipeTypes.TABLE;
    }

    protected final boolean fitsIn3x3Grid(Recipe<?> recipe, IRecipeLayout display) {
        if (recipe != null) {
            return recipe.canCraftInDimensions(GRID_WIDTH, GRID_HEIGHT);
        } else {
            return true;
        }
    }

    protected final Map<AEKey, Integer> getIngredientPriorities(MEStorageMenu menu,
                                                                Comparator<GridInventoryEntry> comparator) {
        var orderedEntries = menu.getClientRepo().getAllEntries()
                .stream()
                .sorted(comparator)
                .map(GridInventoryEntry::getWhat)
                .toList();

        var result = new HashMap<AEKey, Integer>(orderedEntries.size());
        for (int i = 0; i < orderedEntries.size(); i++) {
            result.put(orderedEntries.get(i), i);
        }

        // Also consider the player inventory, but only as the last resort
        for (var item : menu.getPlayerInventory().items) {
            var key = AEItemKey.of(item);
            if (key != null) {
                // Use -1 as lower priority than the lowest network entry (which start at 0)
                result.putIfAbsent(key, -1);
            }
        }

        return result;
    }
}
