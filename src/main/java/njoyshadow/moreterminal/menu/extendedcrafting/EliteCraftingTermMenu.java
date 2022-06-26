package njoyshadow.moreterminal.menu.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import njoyshadow.moreterminal.item.part.extendedcrafting.BaseExtendCraftingTermPart;

public class EliteCraftingTermMenu extends BaseCraftingTermMenu {
    public static final MenuType<EliteCraftingTermMenu> TYPE = MenuTypeBuilder
            .create(EliteCraftingTermMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("elite_crafting_terminal");

    public EliteCraftingTermMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host, 7, BaseExtendCraftingTermPart.ELITE_INV_CRAFTING);
    }
}
