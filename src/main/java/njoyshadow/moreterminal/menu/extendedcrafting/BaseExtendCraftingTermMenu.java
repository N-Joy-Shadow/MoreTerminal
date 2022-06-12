package njoyshadow.moreterminal.menu.extendedcrafting;

import appeng.api.storage.ITerminalHost;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.helpers.IContainerCraftingPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.items.IItemHandler;

public class BaseExtendCraftingTermMenu extends  implements IContainerCraftingPacket {
    public BaseExtendCraftingTermMenu(int id, PlayerInventory ip, ITerminalHost monitorable) {
        super(id, ip, monitorable);
    }

    @Override
    public IItemHandler getInventoryByName(String s) {
        return null;
    }

    @Override
    public boolean useRealItems() {
        return false;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }
}
