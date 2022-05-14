package njoyshadow.moreterminal.item.part;

import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IParts;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.util.AEColoredItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.core.CreativeTab;
import appeng.core.features.ItemDefinition;
import appeng.core.features.registries.PartModels;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartItemRendering;
import appeng.items.parts.PartModelsHelper;
import appeng.parts.automation.ExportBusPart;
import appeng.parts.reporting.CraftingTerminalPart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import njoyshadow.moreterminal.utils.MTCreativeTab;

import java.util.function.Function;

public final class MTParts implements IMTParts {
    private final IItemDefinition basicTerminal;

    private FeatureFactory registry;
    private PartModels partModels;

    public MTParts(FeatureFactory registry, PartModels partModels){
        this.registry = registry;
        this.partModels = partModels;

        this.basicTerminal = createPart("basic_crafting_terminal", CraftingTerminalPart.class, CraftingTerminalPart::new);

    }

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
