package njoyshadow.moreterminal.mixin;

import appeng.client.gui.AEBaseScreen;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import njoyshadow.moreterminal.container.extendedcrafting.slot.ExtendedCraftingSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;

@Mixin(AEBaseScreen.class)
public abstract class MixinAEBaseScreen {
    @Shadow protected abstract ClientPlayerEntity getPlayer();

    @Inject(method = "handleMouseClick", at = @At(
            value = "HEAD"
    ))
    protected void AddclickAction(Slot slot, int slotIdx, int mouseButton, ClickType clickType, CallbackInfo ci){
        if(slot instanceof ExtendedCraftingSlot){
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

}
