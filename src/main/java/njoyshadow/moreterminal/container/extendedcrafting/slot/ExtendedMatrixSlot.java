package njoyshadow.moreterminal.container.extendedcrafting.slot;

import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import appeng.util.inv.WrapperInvItemHandler;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ExtendedMatrixSlot extends AppEngSlot {
    private final AEBaseContainer c;
    private final IInventory wrappedInventory;

    public ExtendedMatrixSlot(AEBaseContainer c, IItemHandler inv, int invSlot) {
        super(inv, invSlot);
        this.c = c;
        this.wrappedInventory = new WrapperInvItemHandler(inv);
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
