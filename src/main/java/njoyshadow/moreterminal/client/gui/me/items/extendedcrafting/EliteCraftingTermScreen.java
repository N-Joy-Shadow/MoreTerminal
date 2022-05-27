package njoyshadow.moreterminal.client.gui.me.items.extendedcrafting;

import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import njoyshadow.moreterminal.container.extendedcrafting.BasicCraftingTerminalContainer;
import njoyshadow.moreterminal.container.extendedcrafting.EliteCraftingTerminalContainer;

public class EliteCraftingTermScreen extends ItemTerminalScreen<EliteCraftingTerminalContainer> {

    public EliteCraftingTermScreen(EliteCraftingTerminalContainer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);

    }
}
