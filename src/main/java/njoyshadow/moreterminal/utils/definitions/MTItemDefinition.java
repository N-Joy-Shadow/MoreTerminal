package njoyshadow.moreterminal.utils.definitions;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.util.helpers.ItemComparisonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.event.ColorHandlerEvent;

import java.util.Objects;


public class MTItemDefinition<T extends Item> implements ItemLike {
    private final ResourceLocation id;
    private final String englishName;
    private final T item;

    public MTItemDefinition(String englishName, ResourceLocation id, T item) {
        Objects.requireNonNull(id, "id");
        this.id = id;
        this.englishName = englishName;
        this.item = item;
    }

    public String getEnglishName() {
        return englishName;
    }

    public ResourceLocation id() {
        return this.id;
    }

    public ItemStack stack() {
        return stack(1);
    }

    public ItemStack stack(int stackSize) {
        return new ItemStack(item, stackSize);
    }

    public GenericStack genericStack(long stackSize) {
        return new GenericStack(AEItemKey.of(item), stackSize);
    }

    /**
     * Compare {@link ItemStack} with this
     *
     * @param comparableStack compared item
     *
     * @return true if the item stack is a matching item.
     */
    public final boolean isSameAs(ItemStack comparableStack) {
        return ItemComparisonHelper.isEqualItemType(comparableStack, this.stack());
    }

    /**
     * @return True if this item is represented by the given key.
     */
    public final boolean isSameAs(AEKey key) {
        if (key instanceof AEItemKey itemKey) {
            return item == itemKey.getItem();
        }
        return false;
    }

    @Override
    public T asItem() {
        return item;
    }
}
