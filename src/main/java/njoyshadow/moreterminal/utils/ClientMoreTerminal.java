package njoyshadow.moreterminal.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import njoyshadow.moreterminal.Moreterminal;
import njoyshadow.moreterminal.client.gui.style.MTStyleManager;
import njoyshadow.moreterminal.network.handler.MTNetworkHandler;
import njoyshadow.moreterminal.utils.init.client.InitScreen;

@OnlyIn(Dist.CLIENT)
public class ClientMoreTerminal  {

    public ClientMoreTerminal(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::clientSetup);
    }
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() ->{
            Minecraft minecraft = Minecraft.getInstance();
            postClientSetup(minecraft);
        });

    }
    private void postClientSetup(Minecraft minecraft){
        MTNetworkHandler.init(new ResourceLocation(Moreterminal.MOD_ID,"main"));
        InitScreen.init();
        MTStyleManager.initialize(minecraft.getResourceManager());
    }
}
