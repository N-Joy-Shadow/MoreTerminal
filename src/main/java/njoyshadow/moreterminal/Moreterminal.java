package njoyshadow.moreterminal;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import njoyshadow.moreterminal.client.gui.style.MTScreenStyle;
import njoyshadow.moreterminal.client.gui.style.MTStyleManager;
import njoyshadow.moreterminal.network.handler.MTNetworkHandler;
import njoyshadow.moreterminal.utils.ClientMoreTerminal;
import njoyshadow.moreterminal.utils.ServerMoreTerminal;
import njoyshadow.moreterminal.utils.init.client.InitScreen;
import njoyshadow.moreterminal.utils.MTCreativeTab;
import njoyshadow.moreterminal.utils.definitions.MTParts;
import njoyshadow.moreterminal.utils.init.InitItems;
import njoyshadow.moreterminal.utils.init.InitMenuTypes;
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




    public Moreterminal() {

        DistExecutor.unsafeRunForDist(() -> ClientMoreTerminal::new,() -> ServerMoreTerminal::new);

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Minecraft minecraft = Minecraft.getInstance();


        modEventBus.addListener(this::commonSetup);
        MTCreativeTab.init();
        MTParts.init();
        modEventBus.addGenericListener(MenuType.class, this::registerMenuTypes);
        modEventBus.addGenericListener(Item.class, this::registerItems);

        //InitMenuTypes.init((Registry<MenuType<?>>) Registry.MENU);
    }
    public void registerMenuTypes(RegistryEvent.Register<MenuType<?>> event) {
        InitMenuTypes.init(event.getRegistry());
    }
    public void registerItems(RegistryEvent.Register<Item> event) {
        InitItems.init(event.getRegistry());
    }
    private void commonSetup(FMLCommonSetupEvent event) {

        //MTNetworkHandler.init(new ResourceLocation("moreterminal", "main"));

    }

    private void setup(final FMLCommonSetupEvent event) {
        // some preinit code
    }

}
