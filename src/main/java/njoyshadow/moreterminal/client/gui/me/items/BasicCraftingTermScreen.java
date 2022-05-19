package njoyshadow.moreterminal.client.gui.me.items;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.container.me.items.ItemTerminalContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import njoyshadow.moreterminal.container.extendedcrafting.BasicCraftingTerminalContainer;

public class BasicCraftingTermScreen extends ItemTerminalScreen<BasicCraftingTerminalContainer> {

    public BasicCraftingTermScreen(BasicCraftingTerminalContainer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
    }
}
