package njoyshadow.moreterminal.utils;

import appeng.api.util.AEColor;
import appeng.bootstrap.components.IBlockRegistrationComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.client.render.cablebus.CableBusModelLoader;
import appeng.core.features.registries.PartModels;
import appeng.parts.automation.PlaneModelLoader;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.IForgeRegistry;
import njoyshadow.moreterminal.Moreterminal;
import njoyshadow.moreterminal.client.ScreenRegistration;
import njoyshadow.moreterminal.container.extendedcrafting.AdvancedCraftingTerminalContainer;
import njoyshadow.moreterminal.container.extendedcrafting.BasicCraftingTerminalContainer;

import appeng.container.slot.AppEngSlot;
import njoyshadow.moreterminal.container.extendedcrafting.EliteCraftingTerminalContainer;
import njoyshadow.moreterminal.container.extendedcrafting.UltimateCraftingTerminalContainer;

public class MTRegisteration {

    @OnlyIn(Dist.CLIENT)
    public void  registerModelsEvent(ModelRegistryEvent event){
        ModelLoaderRegistry.registerLoader(new ResourceLocation(Moreterminal.MOD_ID, "cable_bus"),
                new CableBusModelLoader((PartModels) MTApi.INSTANCE.registries().partModels()));
    }



    public static void registerItems(RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();

        final MTDefinitions definitions = MTApi.INSTANCE.definitions();
        final Dist dist = FMLEnvironment.dist;
        definitions.getRegistry().getBootstrapComponents(IItemRegistrationComponent.class)
                .forEachRemaining(b -> b.itemRegistration(dist, registry));
    }



    public void registerBlocks(RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        // TODO: Do not use the internal API
        final MTDefinitions definitions = MTApi.INSTANCE.definitions();
        final Dist dist = FMLEnvironment.dist;
        definitions.getRegistry().getBootstrapComponents(IBlockRegistrationComponent.class)
                .forEachRemaining(b -> b.blockRegistration(dist, registry));
    }
    public static void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> event) {
        final IForgeRegistry<ContainerType<?>> registry = event.getRegistry();

        registry.registerAll(
                BasicCraftingTerminalContainer.TYPE,
                AdvancedCraftingTerminalContainer.TYPE,
                EliteCraftingTerminalContainer.TYPE,
                UltimateCraftingTerminalContainer.TYPE
        );
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            ScreenRegistration.register();
        });
    }

        public static void setupInternalRegistries() {
    }

    public static void postInit() {
    }
    @OnlyIn(Dist.CLIENT)
    public  void registerClientEvents() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::registerModelsEvent);
    }
}
