package njoyshadow.moreterminal.client.gui.me.items.extendedcrafting;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import njoyshadow.moreterminal.menu.extendedcrafting.AdvancedCraftingTermMenu;

public class AdvancedCraftingTermScreen<C extends AdvancedCraftingTermMenu> extends MEStorageScreen<C> {

    public AdvancedCraftingTermScreen(C menu, Inventory playerInventory, Component title, ScreenStyle style) {
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
