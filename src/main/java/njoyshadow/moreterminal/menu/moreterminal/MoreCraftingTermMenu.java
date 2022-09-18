package njoyshadow.moreterminal.menu.moreterminal;

import appeng.api.storage.ITerminalHost;
import appeng.menu.me.items.CraftingTermMenu;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.wrapper.EntityEquipmentInvWrapper;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import njoyshadow.moreterminal.menu.moreterminal.slot.ArmorSlot;

public class MoreCraftingTermMenu extends CraftingTermMenu {
    public MoreCraftingTermMenu(int id, Inventory ip, ITerminalHost host) {
        super(id, ip, host);
        ip.player.getArmorSlots();


        //super.addSlot(new ArmorSlot(,1,1,1));
    }


}
