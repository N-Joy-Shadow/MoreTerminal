package njoyshadow.moreterminal.mixin;


import appeng.core.sync.network.IPacketHandler;
import appeng.core.sync.network.NetworkHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetworkHandler.class)
public class MixinNetworkHandler {

    @Inject(method = "createServerSide",at = @At(value ="HEAD"))
    public void screatsad(CallbackInfoReturnable<IPacketHandler> cir){

    }
}
