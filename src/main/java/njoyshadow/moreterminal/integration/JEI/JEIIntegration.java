package njoyshadow.moreterminal.integration.JEI;

import com.blakebr0.extendedcrafting.compat.jei.category.table.AdvancedTableCategory;
import com.blakebr0.extendedcrafting.compat.jei.category.table.BasicTableCategory;
import com.blakebr0.extendedcrafting.compat.jei.category.table.EliteTableCategory;
import com.blakebr0.extendedcrafting.compat.jei.category.table.UltimateTableCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import njoyshadow.moreterminal.integration.JEI.extendedcrafting.ExtendedCraftingRecipeTransfer;
import njoyshadow.moreterminal.menu.extendedcrafting.AdvancedCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.BasicCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.EliteCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.UltimateCraftingTermMenu;

@JeiPlugin
public class JEIIntegration implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation("moreterminal", "core");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration){
        registration.addRecipeTransferHandler(new ExtendedCraftingRecipeTransfer<>(BasicCraftingTermMenu.class,registration.getTransferHelper(),3
                ), BasicTableCategory.UID);
    //    registration.addRecipeTransferHandler(BasicCraftingTermMenu.class,BasicTableCategory.UID,1,9,10,36);

        registration.addRecipeTransferHandler(new ExtendedCraftingRecipeTransfer<>(AdvancedCraftingTermMenu.class,registration.getTransferHelper(),5
                ), AdvancedTableCategory.UID);
        registration.addRecipeTransferHandler(new ExtendedCraftingRecipeTransfer<>(EliteCraftingTermMenu.class,registration.getTransferHelper(),7
                ), EliteTableCategory.UID);
        registration.addRecipeTransferHandler(new ExtendedCraftingRecipeTransfer<>(UltimateCraftingTermMenu.class,registration.getTransferHelper(),9
                ), UltimateTableCategory.UID);
    }

}
