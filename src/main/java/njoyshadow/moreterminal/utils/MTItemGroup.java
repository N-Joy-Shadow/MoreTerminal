package njoyshadow.moreterminal.utils;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import njoyshadow.moreterminal.utils.definitions.MTItemDefinition;
import njoyshadow.moreterminal.utils.definitions.MTItems;
import njoyshadow.moreterminal.utils.definitions.MTParts;

import java.util.ArrayList;
import java.util.List;

public class MTItemGroup extends CreativeModeTab {
    private final List<MTItemDefinition<?>> itemDefs;

    public MTItemGroup(String label, List<MTItemDefinition<?>> itemDefs) {
        super(label);
        this.itemDefs = itemDefs;
    }

    @Override
    public ItemStack makeIcon() {
        return AEParts.CRAFTING_TERMINAL.stack();
    }

    public void add(MTItemDefinition<?> itemDef) {
        this.itemDefs.add(itemDef);
    }

    @Override
    public void fillItemList(NonNullList<ItemStack> items) {
        for (MTItemDefinition<?> itemDef : this.itemDefs) {
            itemDef.asItem().fillItemCategory(this, items);
        }
    }
}
