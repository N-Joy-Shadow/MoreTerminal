package njoyshadow.moreterminal.client.gui.me.items.extendedcrafting;

import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import njoyshadow.moreterminal.client.gui.me.items.MTItemTerminalScreen;
import njoyshadow.moreterminal.client.gui.me.style.MTScreenStyle;
import njoyshadow.moreterminal.container.extendedcrafting.AdvancedCraftingTerminalContainer;
import njoyshadow.moreterminal.container.extendedcrafting.BasicCraftingTerminalContainer;

public class AdvancedCraftingTermScreen extends MTItemTerminalScreen<AdvancedCraftingTerminalContainer> {

    public AdvancedCraftingTermScreen(AdvancedCraftingTerminalContainer container, PlayerInventory playerInventory, ITextComponent title, MTScreenStyle style) {
        super(container, playerInventory, title, style);

    }
}
