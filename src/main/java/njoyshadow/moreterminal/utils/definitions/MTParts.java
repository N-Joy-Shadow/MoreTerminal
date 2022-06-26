package njoyshadow.moreterminal.utils.definitions;

import appeng.api.ids.AEPartIds;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import appeng.parts.p2p.LightP2PTunnelPart;
import net.minecraft.resources.ResourceLocation;
import njoyshadow.moreterminal.Moreterminal;
import njoyshadow.moreterminal.item.part.MTPartItem;
import njoyshadow.moreterminal.item.part.extendedcrafting.AdvancedTerminalPart;
import njoyshadow.moreterminal.item.part.extendedcrafting.BasicTerminalPart;
import njoyshadow.moreterminal.item.part.extendedcrafting.EliteTerminalPart;
import njoyshadow.moreterminal.item.part.extendedcrafting.UltimateTerminalPart;

import java.util.function.Function;

import static njoyshadow.moreterminal.utils.definitions.MTItems.item;


public class MTParts {

    public static final MTItemDefinition<MTPartItem<BasicTerminalPart>> BASIC_CRAFTING_TERMINAL = createPart("basic crafting terminal", new ResourceLocation(Moreterminal.MOD_ID,"basic_crafting_terminal"), BasicTerminalPart.class, BasicTerminalPart::new);
    public static final MTItemDefinition<MTPartItem<AdvancedTerminalPart>> ADVANCED_CRAFTING_TERMINAL = createPart("advanced crafting terminal", new ResourceLocation(Moreterminal.MOD_ID,"advanced_crafting_terminal"), AdvancedTerminalPart.class, AdvancedTerminalPart::new);
    public static final MTItemDefinition<MTPartItem<EliteTerminalPart>> ELITE_CRAFTING_TERMINAL = createPart("elite crafting terminal", new ResourceLocation(Moreterminal.MOD_ID,"elite_crafting_terminal"), EliteTerminalPart.class, EliteTerminalPart::new);
    public static final MTItemDefinition<MTPartItem<UltimateTerminalPart>> ULTIMATE_CRAFTING_TERMINAL = createPart("ultimate crafting terminal", new ResourceLocation(Moreterminal.MOD_ID,"ultimate_crafting_terminal"), UltimateTerminalPart.class, UltimateTerminalPart::new);




    private static <T extends IPart> MTItemDefinition<MTPartItem<T>> createPart(
            String englishName,
            ResourceLocation id,
            Class<T> partClass,
            Function<IPartItem<T>, T> factory) {

        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, props -> new MTPartItem<>(props, partClass, factory));
    }


    public static void init(){}
}
