package njoyshadow.moreterminal.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.container.AEBaseContainer;
import com.google.common.annotations.VisibleForTesting;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.text.ITextComponent;
import njoyshadow.moreterminal.client.gui.me.items.BasicCraftingTermScreen;
import njoyshadow.moreterminal.container.extendedcrafting.BasicCraftingTerminalContainer;

import java.io.FileNotFoundException;
import java.util.IdentityHashMap;
import java.util.Map;

public class ScreenRegistration {
    @VisibleForTesting
    static final Map<ContainerType<?>, String> CONTAINER_STYLES = new IdentityHashMap<>();

    public static void register() {
        register(BasicCraftingTerminalContainer.TYPE, BasicCraftingTermScreen::new,"screens/basic_terminal.json");
    }
    private static <M extends AEBaseContainer, U extends AEBaseScreen<M>> void register(ContainerType<M> type,
                                                                                        StyledScreenFactory<M, U> factory,
                                                                                        String stylePath) {
        CONTAINER_STYLES.put(type, stylePath);
        ScreenManager.<M, U>registerFactory(type, (container, playerInv, title) -> {
            ScreenStyle style;
            try {
                style = StyleManager.loadStyleDoc(stylePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Failed to read Screen JSON file: " + stylePath + ": " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException("Failed to read Screen JSON file: " + stylePath, e);
            }

            return factory.create(container, playerInv, title, style);
        });
    }

    /**
     * A type definition that matches the constructors of our screens, which take an additional {@link ScreenStyle}
     * argument.
     */
    @FunctionalInterface
    public interface StyledScreenFactory<T extends Container, U extends Screen & IHasContainer<T>> {
        U create(T t, PlayerInventory pi, ITextComponent title, ScreenStyle style);
    }

}
