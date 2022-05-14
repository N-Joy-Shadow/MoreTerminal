package njoyshadow.moreterminal.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import njoyshadow.moreterminal.Moreterminal;

public class MTItemBase extends Item{

    public MTItemBase(Properties properties) {
        super(properties.setNoRepair());
    }


    @Override
    public String toString() {
        String regName = this.getRegistryName() != null ? this.getRegistryName().getPath() : "unregistered";
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemStack2){
        return false;
    }
}
