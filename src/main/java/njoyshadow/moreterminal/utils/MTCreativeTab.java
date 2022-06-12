package njoyshadow.moreterminal.utils;

import appeng.core.AEItemGroup;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ColorHandlerEvent;
import njoyshadow.moreterminal.utils.definitions.MTItemDefinition;
import org.lwjgl.system.APIUtil;

import java.util.ArrayList;
import java.util.List;

public final class MTCreativeTab {
    private static final List<MTItemDefinition<?>> itemDefs = new ArrayList<>();

    public static CreativeModeTab INSTANCE;

    public static void init() {
        INSTANCE = new MTItemGroup("moreterminal.main", itemDefs);
    }

    public static void add(MTItemDefinition<?> itemDef) {
        itemDefs.add(itemDef);
    }
}
