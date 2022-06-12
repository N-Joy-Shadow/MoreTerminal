package njoyshadow.moreterminal.client.gui.me.items;




import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.me.items.ItemRepo;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IScrollSource;
import appeng.container.me.common.GridInventoryEntry;
import appeng.container.me.common.MEMonitorableContainer;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ViewCellItem;
import appeng.util.prioritylist.IPartitionList;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import njoyshadow.moreterminal.client.gui.me.style.MTScreenStyle;

public class MTItemTerminalScreen<C extends MEMenu<IAEItemStack>> extends MTMonitorableScreen<IAEItemStack, C> {
    public MTItemTerminalScreen(C container, PlayerInventory playerInventory, ITextComponent title, MTScreenStyle style) {
        super(container, playerInventory, title, style);
    }

    protected Repo<IAEItemStack> createRepo(IScrollSource scrollSource) {
        return new ItemRepo(scrollSource, this);
    }

    protected IPartitionList<IAEItemStack> createPartitionList(List<ItemStack> viewCells) {
        return ViewCellItem.createFilter(viewCells);
    }

    protected void renderGridInventoryEntry(MatrixStack matrices, int x, int y, GridInventoryEntry<IAEItemStack> entry) {
        ItemStack displayStack = ((IAEItemStack)entry.getStack()).asItemStackRepresentation();
        Inventory displayInv = new Inventory(new ItemStack[]{displayStack});
        super.moveItems(matrices, new Slot(displayInv, 0, x, y));
    }

    protected void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry<IAEItemStack> entry, int mouseButton, ClickType clickType) {
        if (entry == null) {
            if (clickType == ClickType.PICKUP && !this.playerInventory.getItemStack().isEmpty()) {
                InventoryAction action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                ((MEMonitorableContainer)this.container).handleInteraction(-1L, action);
            }

        } else {
            long serial = entry.getSerial();
            if (InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 32)) {
                ((MEMonitorableContainer)this.container).handleInteraction(serial, InventoryAction.MOVE_REGION);
            } else {
                InventoryAction action = null;
                switch(clickType) {
                    case PICKUP:
                        action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                        if (action == InventoryAction.PICKUP_OR_SET_DOWN && this.shouldCraftOnClick(entry) && this.playerInventory.getItemStack().isEmpty()) {
                            ((MEMonitorableContainer)this.container).handleInteraction(serial, InventoryAction.AUTO_CRAFT);
                            return;
                        }
                        break;
                    case QUICK_MOVE:
                        action = mouseButton == 1 ? InventoryAction.PICKUP_SINGLE : InventoryAction.SHIFT_CLICK;
                        break;
                    case CLONE:
                        if (entry.isCraftable()) {
                            ((MEMonitorableContainer)this.container).handleInteraction(serial, InventoryAction.AUTO_CRAFT);
                            return;
                        }

                        if (this.playerInventory.player.abilities.isCreativeMode) {
                            action = InventoryAction.CREATIVE_DUPLICATE;
                        }
                    case THROW:
                }

                if (action != null) {
                    ((MEMonitorableContainer)this.container).handleInteraction(serial, action);
                }
            }

        }
    }

    private boolean shouldCraftOnClick(GridInventoryEntry<IAEItemStack> entry) {
        if (this.isViewOnlyCraftable()) {
            return true;
        } else {
            return entry.getStoredAmount() == 0L && entry.isCraftable();
        }
    }
}
