package njoyshadow.moreterminal.utils.definitions;


import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.item.Item;
import njoyshadow.moreterminal.Moreterminal;
import njoyshadow.moreterminal.item.item.MaterialItem;
import njoyshadow.moreterminal.utils.MTCreativeTab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;


public class MTItems {
    private static final List<MTItemDefinition<?>> ITEMS = new ArrayList<>();

    public static final MTItemDefinition<MaterialItem>  INTEGRATION_PROCCESSOR_PRINT = item("Inscriber Integration printed",new ResourceLocation(Moreterminal.MOD_ID,"printed_integration_process"), MaterialItem::new);
    public static final MTItemDefinition<MaterialItem>  INTEGRATION_PROCCESSOR = item("Integration Processor",new ResourceLocation(Moreterminal.MOD_ID,"integration_processor"), MaterialItem::new);


    public static List<MTItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static <T extends Item> MTItemDefinition<T> item(String name, ResourceLocation id,
                                                   Function<Item.Properties, T> factory
                                                   ) {

        Item.Properties p = new Item.Properties().tab(MTCreativeTab.INSTANCE);

        T item = factory.apply(p);

        MTItemDefinition<T> definition = new MTItemDefinition<>(name, id, item);
        MTCreativeTab.add(definition);


        ITEMS.add(definition);

        return definition;
    }

    public static void init() {
    }
}
