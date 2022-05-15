package njoyshadow.moreterminal.utils;

import appeng.bootstrap.components.IBlockRegistrationComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import net.minecraft.block.Block;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.IForgeRegistry;
import njoyshadow.moreterminal.container.extendedcrafting.BasicCraftingTerminalContainer;

public class MTRegisteration {

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
                BasicCraftingTerminalContainer.TYPE
        );
    }

        public static void setupInternalRegistries() {
    }

    public static void postInit() {
    }
}
