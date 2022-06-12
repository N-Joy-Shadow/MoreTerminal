package njoyshadow.moreterminal.integration.jei;

import com.blakebr0.extendedcrafting.compat.jei.category.table.AdvancedTableCategory;
import com.blakebr0.extendedcrafting.compat.jei.category.table.BasicTableCategory;
import com.blakebr0.extendedcrafting.compat.jei.category.table.EliteTableCategory;
import com.blakebr0.extendedcrafting.compat.jei.category.table.UltimateTableCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import njoyshadow.moreterminal.menu.extendedcrafting.AdvancedCraftingTerminalContainer;
import njoyshadow.moreterminal.menu.extendedcrafting.BasicCraftingTerminalContainer;
import njoyshadow.moreterminal.menu.extendedcrafting.EliteCraftingTerminalContainer;
import njoyshadow.moreterminal.menu.extendedcrafting.UltimateCraftingTerminalContainer;
import njoyshadow.moreterminal.integration.jei.extendedCrafting.AdvancedCraftingRecipeTransferHandler;
import njoyshadow.moreterminal.integration.jei.extendedCrafting.BasicCraftingRecipeTransferHandler;
import njoyshadow.moreterminal.integration.jei.extendedCrafting.EliteCraftingRecipeTransferHandler;
import njoyshadow.moreterminal.integration.jei.extendedCrafting.UltimateCraftingRecipeTransferHandler;

@JeiPlugin
public class JEIIntergratation implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation("moreterminal", "core");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // Allow Extended Crafting recipe transfer from JEI to Extended Crafting terminal
        registration.addRecipeTransferHandler(
            new BasicCraftingRecipeTransferHandler(BasicCraftingTerminalContainer.class, registration.getTransferHelper(),3),
                BasicTableCategory.UID);
        registration.addRecipeTransferHandler(
            new AdvancedCraftingRecipeTransferHandler(AdvancedCraftingTerminalContainer .class, registration.getTransferHelper(),5),
                AdvancedTableCategory.UID);
        registration.addRecipeTransferHandler(
            new EliteCraftingRecipeTransferHandler(EliteCraftingTerminalContainer.class, registration.getTransferHelper(),7),
                EliteTableCategory.UID);
        registration.addRecipeTransferHandler(
            new UltimateCraftingRecipeTransferHandler(UltimateCraftingTerminalContainer.class, registration.getTransferHelper(),9),
                UltimateTableCategory.UID);
    }
}
