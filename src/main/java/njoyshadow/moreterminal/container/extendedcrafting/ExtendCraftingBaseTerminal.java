package njoyshadow.moreterminal.container.extendedcrafting;

import appeng.api.storage.ITerminalHost;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.helpers.IContainerCraftingPacket;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.items.IItemHandler;

public class ExtendCraftingBaseTerminal  extends ItemTerminalContainer implements IContainerCraftingPacket {
    public ExtendCraftingBaseTerminal(int id, PlayerInventory ip, ITerminalHost monitorable) {
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
}
