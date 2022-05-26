package njoyshadow.moreterminal.mixin;

import appeng.container.AEBaseContainer;

import appeng.container.slot.CraftingTermSlot;
import appeng.helpers.InventoryAction;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import njoyshadow.moreterminal.container.extendedcrafting.slot.ExtendedCraftingSlot;
import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = AEBaseContainer.class,remap = false)
public abstract class MixinAEBaseContainer extends Container{


    //remap = false
    protected MixinAEBaseContainer(@Nullable ContainerType<?> type, int id) {
        super(type, id);
    }

    @Shadow protected abstract void updateHeld(ServerPlayerEntity p);

    @Inject(method = "doAction",at = @At(value = "HEAD"))
    public void MTdoAction(ServerPlayerEntity player, InventoryAction action, int slot, long id, CallbackInfo ci) {
        super.inventorySlots.size();
        if (slot >= 0 && slot < this.inventorySlots.size()) {
            Slot s = this.getSlot(slot);
            if (s instanceof ExtendedCraftingSlot) {
                switch (action) {
                    case CRAFT_SHIFT:
                    case CRAFT_ITEM:
                    case CRAFT_STACK:
                        ((ExtendedCraftingSlot) s).doClick(action, player);
                        this.updateHeld(player);
                }
            }
        }

    }



}

