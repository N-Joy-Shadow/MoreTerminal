package njoyshadow.moreterminal.utils.definitions;

import appeng.core.CreativeTab;
import appeng.core.definitions.ItemDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import njoyshadow.moreterminal.utils.MTCreativeTab;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MTItems {
    private static final List<MTItemDefinition<?>> ITEMS = new ArrayList<>();

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
}
