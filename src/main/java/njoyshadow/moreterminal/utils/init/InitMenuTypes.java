package njoyshadow.moreterminal.utils.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.IForgeRegistry;
import njoyshadow.moreterminal.menu.extendedcrafting.AdvancedCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.BasicCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.EliteCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.UltimateCraftingTermMenu;

public class InitMenuTypes {
    public static void init(IForgeRegistry<MenuType<?>> registry) {
        registry.registerAll(
                BasicCraftingTermMenu.TYPE,
                AdvancedCraftingTermMenu.TYPE,
                EliteCraftingTermMenu.TYPE,
                UltimateCraftingTermMenu.TYPE
        );
    }


}
