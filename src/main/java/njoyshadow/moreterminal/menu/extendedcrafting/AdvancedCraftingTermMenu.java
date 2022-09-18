package njoyshadow.moreterminal.menu.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import njoyshadow.moreterminal.item.part.extendedcrafting.BaseExtendCraftingTermPart;
import njoyshadow.moreterminal.menu.implementation.MTMenuTypeBuilder;
public class AdvancedCraftingTermMenu extends BaseCraftingTermMenu {
    public static final MenuType<AdvancedCraftingTermMenu> TYPE = MTMenuTypeBuilder
            .create(AdvancedCraftingTermMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("advanced_crafting_terminal");


    public AdvancedCraftingTermMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host, 5, BaseExtendCraftingTermPart.ADVANCED_INV_CRAFTING);
    }
}
