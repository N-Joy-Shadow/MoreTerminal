package njoyshadow.moreterminal.datagen;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import njoyshadow.moreterminal.Moreterminal;
import njoyshadow.moreterminal.datagen.provider.CraftingRecipe;

@Mod.EventBusSubscriber(modid = Moreterminal.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class MTDataGenerator {
    @SubscribeEvent
    public void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var existingFileHeler = event.getExistingFileHelper();
        generator.addProvider(new CraftingRecipe(generator));

    }
}
