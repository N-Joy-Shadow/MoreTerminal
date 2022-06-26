package njoyshadow.moreterminal.menu.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import njoyshadow.moreterminal.item.part.extendedcrafting.BaseExtendCraftingTermPart;

public class UltimateCraftingTermMenu extends BaseCraftingTermMenu {
    public static final MenuType<UltimateCraftingTermMenu> TYPE = MenuTypeBuilder
            .create(UltimateCraftingTermMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("ultimate_crafting_terminal");

    public UltimateCraftingTermMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host, 9, BaseExtendCraftingTermPart.ULTIMATE_INV_CRAFTING);
    }
}
