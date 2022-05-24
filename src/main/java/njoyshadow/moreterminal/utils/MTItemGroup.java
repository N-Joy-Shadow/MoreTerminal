package njoyshadow.moreterminal.utils;

import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;
import appeng.core.AEItemGroup;
import appeng.core.Api;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import njoyshadow.moreterminal.item.part.IMTParts;

import java.util.ArrayList;
import java.util.List;

public class MTItemGroup extends ItemGroup {
    private final List<IItemDefinition> itemDefs = new ArrayList<>();


    public MTItemGroup(String label) {
        super(label);
    }



    @Override
    public ItemStack createIcon() {
        IDefinitions definitions = Api.instance().definitions();
        IParts parts = definitions.parts();
        return parts.craftingTerminal().stack(1);
    }

    public void add(IItemDefinition itemDef) {
        this.itemDefs.add(itemDef);
    }

    @Override
    public void fill(NonNullList<ItemStack> items) {
        for (IItemDefinition itemDef : this.itemDefs) {
            itemDef.item().fillItemGroup(this, items);
        }
    }
}
