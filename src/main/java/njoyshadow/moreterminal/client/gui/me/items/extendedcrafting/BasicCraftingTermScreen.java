package njoyshadow.moreterminal.client.gui.me.items.extendedcrafting;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import njoyshadow.moreterminal.client.gui.me.common.MTMEStorageScreen;
import njoyshadow.moreterminal.client.gui.style.MTScreenStyle;
import njoyshadow.moreterminal.menu.extendedcrafting.BasicCraftingTermMenu;

public class BasicCraftingTermScreen<C extends BasicCraftingTermMenu> extends MTMEStorageScreen<C> {

    public BasicCraftingTermScreen(C menu, Inventory playerInventory, Component title, MTScreenStyle style) {
        super(menu, playerInventory, title, style);


        ActionButton clearBtn = new ActionButton(ActionItems.STASH, btn -> menu.clearCraftingGrid());
        clearBtn.setHalfSize(true);
        widgets.add("clearCraftingGrid", clearBtn);

        ActionButton clearToPlayerInvBtn = new ActionButton(ActionItems.STASH_TO_PLAYER_INV,
                btn -> menu.clearToPlayerInventory());
        clearToPlayerInvBtn.setHalfSize(true);
        widgets.add("clearToPlayerInv", clearToPlayerInvBtn);
    }

}
