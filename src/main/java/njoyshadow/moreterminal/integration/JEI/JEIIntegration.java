package njoyshadow.moreterminal.integration.JEI;

import com.blakebr0.extendedcrafting.compat.jei.category.table.AdvancedTableCategory;
import com.blakebr0.extendedcrafting.compat.jei.category.table.BasicTableCategory;
import com.blakebr0.extendedcrafting.compat.jei.category.table.EliteTableCategory;
import com.blakebr0.extendedcrafting.compat.jei.category.table.UltimateTableCategory;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import njoyshadow.moreterminal.integration.JEI.extendedcrafting.ExtendedCraftingRecipeTransfer;
import njoyshadow.moreterminal.menu.extendedcrafting.AdvancedCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.BasicCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.EliteCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.UltimateCraftingTermMenu;
import njoyshadow.moreterminal.utils.definitions.MTParts;

@JeiPlugin
public class JEIIntegration implements IModPlugin {
    private static final ResourceLocation ID = new ResourceLocation("moreterminal", "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }
    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration){
        registration.addRecipeTransferHandler(new ExtendedCraftingRecipeTransfer<BasicCraftingTermMenu>(BasicCraftingTermMenu.class,registration.getTransferHelper(),3
        ), BasicTableCategory.UID);
        registration.addRecipeTransferHandler(new ExtendedCraftingRecipeTransfer<AdvancedCraftingTermMenu>(AdvancedCraftingTermMenu.class,registration.getTransferHelper(),5
                ), AdvancedTableCategory.UID);
        registration.addRecipeTransferHandler(new ExtendedCraftingRecipeTransfer<EliteCraftingTermMenu>(EliteCraftingTermMenu.class,registration.getTransferHelper(),7
                ), EliteTableCategory.UID);
        registration.addRecipeTransferHandler(new ExtendedCraftingRecipeTransfer<UltimateCraftingTermMenu>(UltimateCraftingTermMenu.class,registration.getTransferHelper(),9
                ), UltimateTableCategory.UID);
        //registration.addRecipeTransferHandler(BasicCraftingTermMenu.class,BasicTableCategory.UID,1,9,10,36);

    }
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        ItemStack[] itemStacks = {MTParts.BASIC_CRAFTING_TERMINAL.stack(),
                MTParts.ADVANCED_CRAFTING_TERMINAL.stack(),
                MTParts.ELITE_CRAFTING_TERMINAL.stack(),
                MTParts.ULTIMATE_CRAFTING_TERMINAL.stack()
        };
        ResourceLocation[] UIDs = {BasicTableCategory.UID,AdvancedTableCategory.UID,EliteTableCategory.UID,UltimateTableCategory.UID};

        for(int i = 0; i < itemStacks.length; i++) registration.addRecipeCatalyst(itemStacks[i],UIDs[i]);

    }

}
