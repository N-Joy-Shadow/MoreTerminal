package njoyshadow.moreterminal.item.part;

import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import njoyshadow.moreterminal.item.MTItemBase;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.core.Api;

@OnlyIn(Dist.CLIENT)
public class MTPartItem<T extends IPart> extends MTItemBase implements IPartItem<T> {


    private final Function<ItemStack, T> factory;

    public MTPartItem(Properties properties, Function<ItemStack, T> factory) {
        super(properties);
        this.factory = factory;
    }


    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        ItemStack held = player.getHeldItem(context.getHand());
        if (held.getItem() != this) {
            return ActionResultType.PASS;
        }

        return Api.instance().partHelper().placeBus(held, context.getPos(), context.getFace(), player,
                context.getHand(), context.getWorld());
    }

    @Override
    public T createPart(ItemStack is) {
        return factory.apply(is);
    }
}
