package njoyshadow.moreterminal.client.gui.me.items;

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
import njoyshadow.moreterminal.container.extendedcrafting.slot.BasicCraftingSlot;

import javax.annotation.Nullable;

public class BasicCraftingTermScreen extends ItemTerminalScreen<BasicCraftingTerminalContainer> {

    public BasicCraftingTermScreen(BasicCraftingTerminalContainer container, PlayerInventory playerInventory, ITextComponent title, ScreenStyle style) {
        super(container, playerInventory, title, style);
    }
/*
    @Override
    protected void handleMouseClick(@Nullable Slot slot, final int slotIdx, final int mouseButton,
                                    final ClickType clickType) {
        if(slot instanceof BasicCraftingSlot){
            if (mouseButton == 6) {
                return; // prevent weird double clicks..
            }

            InventoryAction action;
            if (hasShiftDown()) {
                action = InventoryAction.CRAFT_SHIFT;
            } else {
                // Craft stack on right-click, craft single on left-click
                action = mouseButton == 1 ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
            }

            final InventoryActionPacket p = new InventoryActionPacket(action, slotIdx, 0);
            NetworkHandler.instance().sendToServer(p);

            return;
        }
    }
 */

}
