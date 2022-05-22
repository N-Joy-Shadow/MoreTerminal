package njoyshadow.moreterminal.item.part;

import appeng.api.definitions.IItemDefinition;
import appeng.api.parts.IPart;
import appeng.bootstrap.FeatureFactory;
import appeng.core.CreativeTab;
import appeng.core.features.registries.PartModels;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartItemRendering;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.item.ItemStack;
import njoyshadow.moreterminal.item.part.extendedcrafting.BasicTerminalPart;
import njoyshadow.moreterminal.utils.MTCreativeTab;

import java.util.function.Function;

public final class MTParts implements IMTParts {
    private final IItemDefinition basicTerminal;

    private FeatureFactory registry;
    private PartModels partModels;

    public MTParts(FeatureFactory registry, PartModels partModels){
        this.registry = registry;
        this.partModels = partModels;

        this.basicTerminal = createPart("basic_crafting_terminal", BasicTerminalPart.class, BasicTerminalPart::new);

    }
    //
    private <T extends IPart> IItemDefinition createPart(String id, Class<T> partClass,
                                                         Function<ItemStack, T> factory) {
        partModels.registerModels(PartModelsHelper.createModels(partClass));

        return registry.item(id, props -> new PartItem<>(props, factory)).itemGroup(CreativeTab.INSTANCE)
                .rendering(new PartItemRendering()).build();    }


    @Override
    public IItemDefinition basicCraftingTerminal() {
        return this.basicTerminal;
    }
}
