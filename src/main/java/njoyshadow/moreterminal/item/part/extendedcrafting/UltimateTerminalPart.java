package njoyshadow.moreterminal.item.part.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.InternalInventory;
import appeng.api.parts.IPartItem;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.Platform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import njoyshadow.moreterminal.menu.extendedcrafting.UltimateCraftingTermMenu;

public class UltimateTerminalPart extends BaseExtendCraftingTermPart {

    public UltimateTerminalPart(IPartItem<?> partItem) {
        super(partItem, 9);
    }
    @Override
    public MenuType<?> getMenuType(Player p) {
        if (Platform.checkPermissions(p, this, SecurityPermissions.CRAFT, false, false)) {
            return UltimateCraftingTermMenu.TYPE;
        }
        return MEStorageMenu.TYPE;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ULTIMATE_INV_CRAFTING)) {
            return craftingGrid;
        } else {
            return super.getSubInventory(id);
        }
    }
}
