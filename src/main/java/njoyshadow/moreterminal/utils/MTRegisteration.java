package njoyshadow.moreterminal.utils;


import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;

import net.minecraftforge.event.RegistryEvent;


public class MTRegisteration {

    @OnlyIn(Dist.CLIENT)
    public void  registerModelsEvent(ModelRegistryEvent event){

    }



    public static void registerItems(RegistryEvent.Register<Item> event) {

    }



    public static void registerContainerTypes(RegistryEvent.Register<MenuType<?>> event) {

    }

        public static void setupInternalRegistries() {
    }

    public static void postInit() {
    }
    @OnlyIn(Dist.CLIENT)
    public  void registerClientEvents() {
    }
}
