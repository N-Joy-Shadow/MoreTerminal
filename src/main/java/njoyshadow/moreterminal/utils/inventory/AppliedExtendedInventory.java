package njoyshadow.moreterminal.utils.inventory;

import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class AppliedExtendedInventory extends CraftingInventory {
    private final Container container;
    private final IItemHandler inventory;
    private final boolean autoTable;

    public AppliedExtendedInventory(Container container, IItemHandler inventory, int size) {
        this(container, inventory, size, false);
    }

    public AppliedExtendedInventory(Container container, IItemHandler inventory, int size, boolean autoTable) {
        super(container, size, size);
        this.container = container;
        this.inventory = inventory;
        this.autoTable = autoTable;
    }


}
