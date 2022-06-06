package njoyshadow.moreterminal.item.part.terminalPart;

import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.me.GridAccessException;
import appeng.parts.reporting.AbstractDisplayPart;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.items.IItemHandler;

import java.util.Iterator;
import java.util.List;

public class MTAbstractTerminalPart extends MTAbstractDisplayPart implements ITerminalHost, IConfigManagerHost, IViewCellStorage, IAEAppEngInventory {
    private final IConfigManager cm = new ConfigManager(this);
    private final AppEngInternalInventory viewCell = new AppEngInternalInventory(this, 5);

    public MTAbstractTerminalPart(ItemStack is) {
        super(is);
        this.cm.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.cm.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.cm.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
    }

    public void getDrops(List<ItemStack> drops, boolean wrenched) {
        super.getDrops(drops, wrenched);
        Iterator var3 = this.viewCell.iterator();

        while(var3.hasNext()) {
            ItemStack is = (ItemStack)var3.next();
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }

    }

    public IConfigManager getConfigManager() {
        return this.cm;
    }

    public void readFromNBT(CompoundNBT data) {
        super.readFromNBT(data);
        this.cm.readFromNBT(data);
        this.viewCell.readFromNBT(data, "viewCell");
    }

    public void writeToNBT(CompoundNBT data) {
        super.writeToNBT(data);
        this.cm.writeToNBT(data);
        this.viewCell.writeToNBT(data, "viewCell");
    }

    public boolean onPartActivate(PlayerEntity player, Hand hand, Vector3d pos) {
        if (!super.onPartActivate(player, hand, pos) && !player.world.isRemote) {
            ContainerOpener.openContainer(this.getContainerType(player), player, ContainerLocator.forPart(this));
        }

        return true;
    }

    public ContainerType<?> getContainerType(PlayerEntity player) {
        return ItemTerminalContainer.TYPE;
    }

    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        try {
            return this.getProxy().getStorage().getInventory(channel);
        } catch (GridAccessException var3) {
            return null;
        }
    }

    public void updateSetting(IConfigManager manager, Settings settingName, Enum<?> newValue) {
    }

    public IItemHandler getViewCellStorage() {
        return this.viewCell;
    }

    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
        this.getHost().markForSave();
    }
}
