package njoyshadow.moreterminal.item;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MTBaseItem extends Item{
    public MTBaseItem(Item.Properties properties) {
        super(properties.setNoRepair());
    }

    @Override
    public String toString() {
        String regName = this.getRegistryName() != null ? this.getRegistryName().getPath() : "unregistered";
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
        return false;
    }

}
