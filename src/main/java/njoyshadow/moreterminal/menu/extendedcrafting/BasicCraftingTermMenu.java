package njoyshadow.moreterminal.menu.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.ITerminalHost;
import appeng.menu.implementations.MenuTypeBuilder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import njoyshadow.moreterminal.item.part.extendedcrafting.BaseExtendCraftingTermPart;

public class BasicCraftingTermMenu extends BaseCraftingTermMenu {

    public static final MenuType<BasicCraftingTermMenu> TYPE = MenuTypeBuilder
            .create(BasicCraftingTermMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("basic_crafting_terminal");


    public BasicCraftingTermMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host, 3, BaseExtendCraftingTermPart.BASIC_INV_CRAFTING);
    }
}
