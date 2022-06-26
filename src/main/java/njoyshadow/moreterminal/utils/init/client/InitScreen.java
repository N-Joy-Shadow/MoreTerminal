package njoyshadow.moreterminal.utils.init.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;

import appeng.menu.AEBaseMenu;
import com.google.common.annotations.VisibleForTesting;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;

import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import njoyshadow.moreterminal.client.gui.me.items.extendedcrafting.AdvancedCraftingTermScreen;

import njoyshadow.moreterminal.client.gui.me.items.extendedcrafting.BasicCraftingTermScreen;
import njoyshadow.moreterminal.client.gui.me.items.extendedcrafting.EliteCraftingTermScreen;
import njoyshadow.moreterminal.client.gui.me.items.extendedcrafting.UltimateCraftingTermScreen;
import njoyshadow.moreterminal.menu.extendedcrafting.AdvancedCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.BasicCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.EliteCraftingTermMenu;
import njoyshadow.moreterminal.menu.extendedcrafting.UltimateCraftingTermMenu;

import java.util.IdentityHashMap;
import java.util.Map;

public class InitScreen {
    @VisibleForTesting
    static final Map<MenuType<?>, String> MENU_STYLES = new IdentityHashMap<>();

    public static void init() {
        //extended Crafting
        //register(BasicExtendTermMenu.TYPE, BasicCraftingTermScreen::new,"/screens/extendedterminals/basic_crafting_terminal.json");
        //register(AdvancedExtendTermMenu.TYPE, AdvancedCraftingTermScreen::new,"/screens/extendedterminals/advanced_crafting_terminal.json");
        InitScreen.<AdvancedCraftingTermMenu,AdvancedCraftingTermScreen<AdvancedCraftingTermMenu>>register(AdvancedCraftingTermMenu.TYPE, AdvancedCraftingTermScreen::new,"/screens/terminals/crafting_terminal.json");
        InitScreen.<BasicCraftingTermMenu, BasicCraftingTermScreen<BasicCraftingTermMenu>>register(BasicCraftingTermMenu.TYPE, BasicCraftingTermScreen::new,"/screens/terminals/crafting_terminal.json");
        InitScreen.<EliteCraftingTermMenu,EliteCraftingTermScreen<EliteCraftingTermMenu>>register(EliteCraftingTermMenu.TYPE, EliteCraftingTermScreen::new,"/screens/terminals/crafting_terminal.json");
        InitScreen.<UltimateCraftingTermMenu,UltimateCraftingTermScreen<UltimateCraftingTermMenu>>register(UltimateCraftingTermMenu.TYPE, UltimateCraftingTermScreen::new,"/screens/terminals/crafting_terminal.json");
    }
    public static <M extends AEBaseMenu, U extends AEBaseScreen<M>> void register(MenuType<M> type,
                                                                                  StyledScreenFactory<M, U> factory,
                                                                                  String stylePath) {
        MENU_STYLES.put(type, stylePath);
        MenuScreens.<M, U>register(type, (menu, playerInv, title) -> {
            var style = StyleManager.loadStyleDoc(stylePath);

            return factory.create(menu, playerInv, title, style);
        });
    }


    /**
     * A type definition that matches the constructors of our screens, which take an additional {@link ScreenStyle}
     * argument.
     */
    @FunctionalInterface
    public interface StyledScreenFactory<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> {
        U create(T t, Inventory pi, Component title, ScreenStyle style);
    }

}
