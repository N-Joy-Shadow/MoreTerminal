package njoyshadow.moreterminal.utils.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import njoyshadow.moreterminal.utils.definitions.MTItems;

public class InitItems {

    public static void init(IForgeRegistry<Item> registry){
        for(var definition : MTItems.getItems()){
            System.out.println(String.format("Moreterminal Items : %s",definition.getEnglishName()));

            var item = definition.asItem();
            item.setRegistryName(definition.id());
            registry.register(item);
        }
    }
}
