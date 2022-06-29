package njoyshadow.moreterminal.mixin;


import appeng.client.gui.AEBaseScreen;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.slot.CraftingTermSlot;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import njoyshadow.moreterminal.menu.extendedcrafting.slot.ExtendedCraftingTermSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.client.gui.screens.Screen.hasShiftDown;

@Mixin(AbstractContainerScreen.class)
public class MixinAEBaseScreen {
    @Inject(method = "slotClicked",at =@At(value ="HEAD"))
    public void SlotClicked(Slot slot, int slotIdx, int mouseButton, ClickType clickType, CallbackInfo ci){
        if (slot instanceof ExtendedCraftingTermSlot) {
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
}
