package njoyshadow.moreterminal.integration.jei;

import appeng.helpers.IContainerCraftingPacket;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapedTableRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapelessTableRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;
import njoyshadow.moreterminal.integration.AppEng.sync.JEIExtendedRecipePacket;
import njoyshadow.moreterminal.integration.AppEng.sync.MTNetworkHandler;

import java.util.Iterator;
import java.util.Map;

public abstract class ExtendedCraftingRecipeHandler <T extends Container & IContainerCraftingPacket> implements  IRecipeTransferHandler<T>{
    private final Class<T> containerClass;
    protected final IRecipeTransferHandlerHelper helper;
    private int Gridsize;
    public ExtendedCraftingRecipeHandler(Class<T> containerClass, IRecipeTransferHandlerHelper helper, int gridSize) {
        this.containerClass = containerClass;
        this.helper = helper;
        this.Gridsize = gridSize;
    }

    public final Class<T> getContainerClass() {
        return this.containerClass;
    }

    public final IRecipeTransferError transferRecipe(T container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
        if (!(recipe instanceof ITableRecipe)) {
            return this.helper.createInternalError();
        } else {
            ITableRecipe irecipe = (ITableRecipe) recipe;
            ResourceLocation recipeId = irecipe.getId();

            if (recipeId == null) {
                return this.helper.createUserErrorWithTooltip(new TranslationTextComponent("jei.appliedenergistics2.missing_id"));
            } else {

                boolean canSendReference = true;
                boolean isPresentRecipe = player.getEntityWorld().getRecipeManager().getRecipe(recipeId).isPresent();
                if (!isPresentRecipe) {
                    if (!(recipe instanceof ShapedTableRecipe) && !(recipe instanceof ShapelessTableRecipe)) {
                        return this.helper.createUserErrorWithTooltip(new TranslationTextComponent("jei.appliedenergistics2.missing_id"));
                    }
                    canSendReference = false;
                }
                if (!irecipe.canFit(Gridsize, Gridsize)) {
                    return this.helper.createUserErrorWithTooltip(new TranslationTextComponent("jei.appliedenergistics2.recipe_too_large"));
                } else {

                    IRecipeTransferError error = this.doTransferRecipe(container, irecipe, recipeLayout, player, maxTransfer);
                    if (doTransfer && this.canTransfer(error)) {
                        if (canSendReference) {
                            MTNetworkHandler.instance().sendToServer(new JEIExtendedRecipePacket(recipeId, this.isCrafting(),Gridsize));
                        } else {
                            NonNullList<Ingredient> flatIngredients = NonNullList.withSize(Gridsize*Gridsize, Ingredient.EMPTY);
                            ItemStack output = ItemStack.EMPTY;
                            int firstInputSlot = recipeLayout.getItemStacks().getGuiIngredients().entrySet().stream().filter((e) -> {
                                return ((IGuiIngredient)e.getValue()).isInput();
                            }).mapToInt(Map.Entry::getKey).min().orElse(0);
                            Iterator var14 = recipeLayout.getItemStacks().getGuiIngredients().entrySet().iterator();


                            while(true) {
                                while(true) {
                                    Map.Entry entry;
                                    IGuiIngredient item;
                                    do {
                                        if (!var14.hasNext()) {
                                            ShapedRecipe fallbackRecipe = new ShapedRecipe(recipeId,"", Gridsize, Gridsize, flatIngredients, output);

                                            MTNetworkHandler.instance().sendToServer(new JEIExtendedRecipePacket(fallbackRecipe, this.isCrafting(),Gridsize));
                                            return error;
                                        }

                                        entry = (Map.Entry)var14.next();
                                        item = (IGuiIngredient)entry.getValue();
                                    } while(item.getDisplayedIngredient() == null);

                                    int inputIndex = (Integer)entry.getKey() - firstInputSlot;
                                    if (item.isInput() && inputIndex < flatIngredients.size()) {
                                        ItemStack displayedIngredient = (ItemStack)item.getDisplayedIngredient();
                                        if (displayedIngredient != null) {
                                            flatIngredients.set(inputIndex, Ingredient.fromStacks(new ItemStack[]{displayedIngredient}));
                                        }
                                    } else if (!item.isInput() && output.isEmpty()) {
                                        output = (ItemStack)item.getDisplayedIngredient();
                                    }
                                }
                            }
                        }
                    }

                    return error;
                }
            }
        }
    }

    protected abstract IRecipeTransferError doTransferRecipe(T var1, IRecipe<?> var2, IRecipeLayout var3, PlayerEntity var4, boolean var5);

    protected abstract boolean isCrafting();

    private boolean canTransfer(IRecipeTransferError error) {
        return error == null || error.getType() == IRecipeTransferError.Type.COSMETIC;
    }
}
