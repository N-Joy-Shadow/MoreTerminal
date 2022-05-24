package njoyshadow.moreterminal.container.extendedcrafting.slot;

import appeng.container.AEBaseContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class MTMatrixSlot extends MTBaseSlot{
    private final AEBaseContainer c;
    private final IInventory wrappedInventory;


    public MTMatrixSlot(PlayerEntity player, IItemHandler craftingGrid, int GridSize, IInventory matrix, AEBaseContainer container) {
        super(player, craftingGrid, GridSize, matrix);
        this.c = container;
        this.wrappedInventory = matrix;
    }
    public void clearStack() {
        super.clearStack();
        this.c.onCraftMatrixChanged(this.wrappedInventory);
    }

    public void putStack(ItemStack par1ItemStack) {
        super.putStack(par1ItemStack);
        this.c.onCraftMatrixChanged(this.wrappedInventory);
    }

    public ItemStack decrStackSize(int par1) {
        ItemStack is = super.decrStackSize(par1);
        this.c.onCraftMatrixChanged(this.wrappedInventory);
        return is;
    }

}
