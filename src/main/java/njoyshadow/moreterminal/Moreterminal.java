package njoyshadow.moreterminal;

import appeng.bootstrap.components.IInitComponent;
import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.bootstrap.components.IPostInitComponent;
import appeng.core.Api;
import appeng.core.CreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.IForgeRegistry;
import njoyshadow.moreterminal.client.ScreenRegistration;
import njoyshadow.moreterminal.utils.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;


// The value here should match an entry in the META-INF/mods.toml file
@Mod("moreterminal")
public class Moreterminal {
    //TODO : First Code Clean
    public static final String MOD_ID = "moreterminal";
    public static final String MOD_NAME = "More Terminal";
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    private final MTRegisteration registration;


    public Moreterminal() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addGenericListener(Item.class, MTRegisteration::registerItems);
        modEventBus.addGenericListener(ContainerType.class, MTRegisteration::registerContainerTypes);
        modEventBus.addListener(this::commonSetup);

        registration = new MTRegisteration();

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        MTDefinitions definitions = MTApi.INSTANCE.definitions();
        //definitions.getRegistry().getBootstrapComponents(IInitComponent.class)
        //        .forEachRemaining(IInitComponent::initialize);
        //definitions.getRegistry().getBootstrapComponents(IPostInitComponent.class)
        //        .forEachRemaining(IPostInitComponent::postInitialize);

        MTRegisteration.setupInternalRegistries();
        MTRegisteration.postInit();
    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        MTCreativeTab.init();
        LOGGER.info("AE >> {}", MTApi.instance().definitions().parts().basicCraftingTerminal().item().getRegistryName());

    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {

    }


}
