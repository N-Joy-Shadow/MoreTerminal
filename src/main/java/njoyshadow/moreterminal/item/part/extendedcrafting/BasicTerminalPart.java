package njoyshadow.moreterminal.item.part.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.InternalInventory;
import appeng.api.parts.IPartItem;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.Platform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import njoyshadow.moreterminal.menu.extendedcrafting.BasicCraftingTermMenu;


public class BasicTerminalPart extends BaseExtendCraftingTermPart {

    public BasicTerminalPart(IPartItem<?> partItem) {
        super(partItem, 3);
    }


    @Override
    public MenuType<?> getMenuType(Player p) {
        if (Platform.checkPermissions(p, this, SecurityPermissions.CRAFT, false, false)) {
            return BasicCraftingTermMenu.TYPE;
        }
        return MEStorageMenu.TYPE;
    }
    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(BASIC_INV_CRAFTING)) {
            return craftingGrid;
        } else {
            return super.getSubInventory(id);
        }
    }

}
