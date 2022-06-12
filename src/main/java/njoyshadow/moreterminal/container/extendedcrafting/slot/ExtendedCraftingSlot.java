package njoyshadow.moreterminal.container.extendedcrafting.slot;

import appeng.container.slot.AppEngSlot;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperInvItemHandler;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import com.blakebr0.extendedcrafting.container.inventory.ExtendedCraftingInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.hooks.BasicEventHooks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class ExtendedCraftingSlot extends AppEngSlot {
    private final IItemHandler craftingGrid;
    private final PlayerEntity player;
    private int amountCrafted;
    private final IInventory matrix;
    private final int GridSize;

    public ExtendedCraftingSlot(PlayerEntity player, IItemHandler craftingGrid, int GridSize, IInventory matrix) {
        super(new ItemStackHandler(1), 0);
        this.player = player;
        this.craftingGrid = craftingGrid;
        this.matrix = matrix;
        this.GridSize = GridSize;
    }


    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    protected void onCrafting(ItemStack par1ItemStack, int par2) {
        this.amountCrafted += par2;
        this.onCrafting(par1ItemStack);

    }

    protected void onCrafting(ItemStack par1ItemStack) {
        par1ItemStack.onCrafting(this.player.world, this.player, this.amountCrafted);
        this.amountCrafted = 0;
    }



    public ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
        BasicEventHooks.firePlayerCraftingEvent(playerIn, stack, new WrapperInvItemHandler(this.craftingGrid));
        this.onCrafting(stack);
        ForgeHooks.setCraftingPlayer(playerIn);
        // CraftingInventory ic = new CraftingInventory(this.getContainer(), 3, 3);
        BaseItemStackHandler Inv = new BaseItemStackHandler(GridSize * GridSize);
        IInventory matrix = new ExtendedCraftingInventory(this.getContainer(), Inv, GridSize);
        for (int x = 0; x < this.craftingGrid.getSlots(); ++x) {
            matrix.setInventorySlotContents(x, this.craftingGrid.getStackInSlot(x));
        }

        //Extended Crafting
        NonNullList remaining = player.world.getRecipeManager().getRecipeNonNull(RecipeTypes.TABLE, this.matrix, player.world);


        NonNullList<ItemStack> aitemstack = this.getRemainingItems(playerIn.world);
        //Warn
        ItemHandlerUtil.copy((CraftingInventory) matrix, this.craftingGrid, false);
        ForgeHooks.setCraftingPlayer(null);


        for (int i = 0; i < aitemstack.size(); ++i) {
            ItemStack itemstack1 = this.craftingGrid.getStackInSlot(i);
            ItemStack itemstack2 = aitemstack.get(i);
            if (!itemstack1.isEmpty()) {
                this.craftingGrid.extractItem(i, 1, false);
            }

            if (!itemstack2.isEmpty()) {
                if (this.craftingGrid.getStackInSlot(i).isEmpty()) {
                    ItemHandlerUtil.setStackInSlot(this.craftingGrid, i, itemstack2);
                } else if (!this.player.inventory.addItemStackToInventory(itemstack2)) {
                    this.player.dropItem(itemstack2, false);
                }
            }
        }

        return stack;
    }

    public void setDisplayedCraftingOutput(ItemStack stack) {
        ((IItemHandlerModifiable) this.getItemHandler()).setStackInSlot(0, stack);
    }

    public ItemStack decrStackSize(int par1) {
        if (this.getHasStack()) {
            this.amountCrafted += Math.min(par1, this.getStack().getCount());
        }

        return super.decrStackSize(par1);
    }

    protected NonNullList<ItemStack> getRemainingItems(World world) {
        return world.getRecipeManager().getRecipe(RecipeTypes.TABLE, this.matrix, world).map((x) -> x.getRemainingItems(this.matrix)).orElse(NonNullList.withSize(GridSize * GridSize, ItemStack.EMPTY));
    }
}
