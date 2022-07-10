package njoyshadow.moreterminal.mixin;


import appeng.helpers.InventoryAction;
import appeng.menu.AEBaseMenu;
import appeng.menu.slot.CraftingTermSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import njoyshadow.moreterminal.menu.extendedcrafting.slot.ExtendedCraftingTermSlot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = AEBaseMenu.class,remap = false)
public class MixinAEBaseMenu extends AbstractContainerMenu {


    protected MixinAEBaseMenu(@Nullable MenuType<?> p_38851_, int p_38852_) {
        super(p_38851_, p_38852_);
    }

    @Inject(method = "doAction",at =@At(value = "HEAD"))
    public void MTdoAction(ServerPlayer player, InventoryAction action, int slot, long id, CallbackInfo ci){
        var s = this.getSlot(slot);

        if (s instanceof ExtendedCraftingTermSlot) {
            switch (action) {
                case CRAFT_SHIFT:
                case CRAFT_ITEM:
                case CRAFT_STACK:
                    ((ExtendedCraftingTermSlot) s).doClick(action, player);
                default:
            }
        }


    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return true;
    }
}
