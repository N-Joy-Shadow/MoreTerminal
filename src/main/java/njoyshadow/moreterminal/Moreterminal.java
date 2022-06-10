package njoyshadow.moreterminal;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import njoyshadow.moreterminal.client.gui.me.style.MTStyleManager;
import njoyshadow.moreterminal.integration.AppEng.sync.MTNetworkHandler;
import njoyshadow.moreterminal.utils.MTApi;
import njoyshadow.moreterminal.utils.MTCreativeTab;
import njoyshadow.moreterminal.utils.MTDefinitions;
import njoyshadow.moreterminal.utils.MTRegisteration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


// The value here should match an entry in the META-INF/mods.toml file
@Mod("moreterminal")
public class Moreterminal {

    public static ResourceLocation MakeID(String Path) {
        return new ResourceLocation(MOD_ID, Path);
    }

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

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            MTStyleManager.initialize(minecraft.getResourceManager());
        }
        registration = new MTRegisteration();
        DistExecutor.runWhenOn(Dist.CLIENT, () -> registration::registerClientEvents);

        modEventBus.addListener(this::commonSetup);
        MTCreativeTab.init();


    }

    private void commonSetup(FMLCommonSetupEvent event) {

        MTNetworkHandler.init(new ResourceLocation("moreterminal", "main"));

        MTDefinitions definitions = MTApi.INSTANCE.definitions();
        MTRegisteration.setupInternalRegistries();
        MTRegisteration.postInit();

    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
        LOGGER.info("AE >> {}", MTApi.instance().definitions().parts().basicCraftingTerminal().item().getRegistryName());
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {

    }


}
