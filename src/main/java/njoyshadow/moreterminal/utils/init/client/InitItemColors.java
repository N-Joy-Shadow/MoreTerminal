package njoyshadow.moreterminal.utils.init.client;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.ItemDefinition;
import appeng.items.misc.PaintBallItem;
import appeng.items.parts.ColoredPartItem;
import appeng.items.parts.PartItem;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.Item;
import njoyshadow.moreterminal.item.part.MTPartItem;
import njoyshadow.moreterminal.utils.definitions.MTItemDefinition;
import njoyshadow.moreterminal.utils.definitions.MTItems;
import njoyshadow.moreterminal.utils.definitions.MTParts;

public class InitItemColors {
    public static void init(ItemColors itemColors){
        for (MTItemDefinition<?> definition : MTItems.getItems()) {
            Item item = definition.asItem();
            if (item instanceof MTPartItem) {
                AEColor color = AEColor.TRANSPARENT;
                itemColors.register(new StaticItemColor(color), item);
            }
        }
    }
}
