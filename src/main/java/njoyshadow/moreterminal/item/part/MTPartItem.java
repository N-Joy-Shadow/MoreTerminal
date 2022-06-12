package njoyshadow.moreterminal.item.part;

import java.util.function.Function;

import appeng.api.parts.PartHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import njoyshadow.moreterminal.item.MTBaseItem;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;

@OnlyIn(Dist.CLIENT)
public class MTPartItem<T extends IPart> extends MTBaseItem implements IPartItem<T> {

    private final Class<T> partClass;
    private final Function<IPartItem<T>, T> factory;

    public MTPartItem(Item.Properties properties, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        super(properties);
        this.partClass = partClass;
        this.factory = factory;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return PartHelper.usePartItem(context);
    }

    @Override
    public Class<T> getPartClass() {
        return partClass;
    }

    @Override
    public T createPart() {
        return factory.apply(this);
    }
}
