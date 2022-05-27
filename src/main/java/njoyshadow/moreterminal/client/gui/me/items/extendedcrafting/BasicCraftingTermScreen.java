package njoyshadow.moreterminal.client.gui.me.items.extendedcrafting;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.me.items.ItemTerminalScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import njoyshadow.moreterminal.container.extendedcrafting.BasicCraftingTerminalContainer;

import javax.annotation.Nullable;

public class BasicCraftingTermScreen extends ItemTerminalScreen<BasicCraftingTerminalContainer> {

    public BasicCraftingTermScreen(BasicCraftingTerminalContainer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);

    }
}
