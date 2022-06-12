package njoyshadow.moreterminal.client.gui.me.items.extendedcrafting;

import appeng.api.config.ActionItems;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.widgets.ActionButton;
import appeng.container.me.items.ItemTerminalContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import njoyshadow.moreterminal.client.gui.me.items.MTItemTerminalScreen;
import njoyshadow.moreterminal.client.gui.me.style.MTScreenStyle;
import njoyshadow.moreterminal.menu.extendedcrafting.BasicCraftingTerminalContainer;

public class BasicCraftingTermScreen extends MTItemTerminalScreen<BasicCraftingTerminalContainer> {

    public BasicCraftingTermScreen(BasicCraftingTerminalContainer container, PlayerInventory playerInventory, ITextComponent title, MTScreenStyle style) {
        super(container, playerInventory, title, style);
        ActionButton clearBtn = new ActionButton(ActionItems.STASH, (btn) -> {
            container.clearCraftingGrid();
        });
        clearBtn.setHalfSize(true);
        this.widgets.add("clearCraftingGrid", clearBtn);
    }
}
