package njoyshadow.moreterminal.menu.extendedcrafting.slot;

import appeng.api.inventories.InternalInventory;
import appeng.crafting.CraftingEvent;
import appeng.helpers.Inventories;
import appeng.menu.slot.AppEngSlot;
import appeng.util.inv.AppEngInternalInventory;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.item.BaseItem;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import com.blakebr0.extendedcrafting.container.inventory.ExtendedCraftingInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;

public class ExtendedCraftingSlot extends AppEngSlot {
    /**
     * The craft matrix inventory linked to this result slot.
     */
    private final InternalInventory craftingGrid;

    /**
     * The player that is using the GUI where this slot resides.
     */
    private final Player player;

    /**
     * The number of items that have been crafted so far. Gets passed to ItemStack.onCrafting before being reset.
     */
    private int amountCrafted;

    private final int GridSize;
    private final int Grid_Matrix;

    public ExtendedCraftingSlot(Player player, InternalInventory craftingGrid, int GridSize) {
        super(new AppEngInternalInventory(1), 0);
        this.player = player;
        this.craftingGrid = craftingGrid;
        this.GridSize = GridSize;
        this.Grid_Matrix = GridSize * GridSize;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    @Override
    protected void onQuickCraft(ItemStack par1ItemStack, int par2) {
        this.amountCrafted += par2;
        this.checkTakeAchievements(par1ItemStack);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
     */
    @Override
    protected void checkTakeAchievements(ItemStack par1ItemStack) {
        par1ItemStack.onCraftedBy(this.player.level, this.player, this.amountCrafted);
        this.amountCrafted = 0;
    }

    @Override
    public void onTake(Player playerIn, ItemStack stack) {
        CraftingEvent.fireCraftingEvent(playerIn, stack, this.craftingGrid.toContainer());
        this.checkTakeAchievements(stack);
        ForgeHooks.setCraftingPlayer(playerIn);

        final CraftingContainer matrix = new ExtendedCraftingInventory(this.getMenu(),new BaseItemStackHandler(Grid_Matrix),GridSize);
        //final CraftingContainer matrix = new CraftingContainer(this.getMenu(), GridSize, GridSize);

        for (int x = 0; x < this.craftingGrid.size(); x++) {
            matrix.setItem(x, this.craftingGrid.getStackInSlot(x));
        }

        var aitemstack = this.getRemainingItems(matrix, playerIn.level);

        Inventories.copy(matrix, this.craftingGrid, false);

        ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < aitemstack.size(); ++i) {
            final ItemStack slotStack = this.craftingGrid.getStackInSlot(i);
            final ItemStack remainingStack = aitemstack.get(i);

            if (!slotStack.isEmpty()) {
                this.craftingGrid.extractItem(i, 1, false);
            }

            if (!remainingStack.isEmpty()) {
                if (this.craftingGrid.getStackInSlot(i).isEmpty()) {
                    this.craftingGrid.setItemDirect(i, remainingStack);
                }
                else if (!this.player.getInventory().add(remainingStack)) {
                    this.player.drop(remainingStack, false);
                }
            }
        }
    }

    /**
     * Overrides what is being shown as the crafting output, but doesn't notify parent menu.
     */
    public void setDisplayedCraftingOutput(ItemStack stack) {
        getInventory().setItemDirect(0, stack);
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public ItemStack remove(int par1) {
        if (this.hasItem()) {
            this.amountCrafted += Math.min(par1, this.getItem().getCount());
        }

        return super.remove(par1);
    }

    // refactoring.
    protected NonNullList<ItemStack> getRemainingItems(CraftingContainer ic, Level level) {
        //return player.level.getRecipeManager().getRemainingItemsFor(RecipeTypes.TABLE,ic,player.level);

        return level.getRecipeManager().getRecipeFor(RecipeTypes.TABLE, ic, level)
                .map(iCraftingRecipe -> iCraftingRecipe.getRemainingItems(ic))
                .orElse(NonNullList.withSize(Grid_Matrix, ItemStack.EMPTY));
    }
}