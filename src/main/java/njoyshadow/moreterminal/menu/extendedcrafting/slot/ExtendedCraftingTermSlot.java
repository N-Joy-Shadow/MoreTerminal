package njoyshadow.moreterminal.menu.extendedcrafting.slot;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.Api;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ViewCellItem;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import com.blakebr0.extendedcrafting.container.inventory.ExtendedCraftingInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import njoyshadow.moreterminal.menu.extendedcrafting.AdvancedCraftingTerminalContainer;
import njoyshadow.moreterminal.menu.extendedcrafting.BasicCraftingTerminalContainer;
import njoyshadow.moreterminal.menu.extendedcrafting.EliteCraftingTerminalContainer;
import njoyshadow.moreterminal.menu.extendedcrafting.UltimateCraftingTerminalContainer;
import njoyshadow.moreterminal.utils.MTPlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ExtendedCraftingTermSlot extends ExtendedCraftingSlot {
    private final IItemHandler craftInv;
    private final IItemHandler pattern;
    private final IActionSource mySrc;
    private final IEnergySource energySrc;
    private final IStorageMonitorable storage;
    private final IContainerCraftingPacket container;
    private final int Gridsize;

    public ExtendedCraftingTermSlot(PlayerEntity player, IActionSource mySrc, IEnergySource energySrc,
                                    IStorageMonitorable storage, IItemHandler cMatrix, IItemHandler secondMatrix, IContainerCraftingPacket ccp,
                                    int GridSize, IInventory matrix) {

        super(player, cMatrix, GridSize, matrix);

        this.energySrc = energySrc;
        this.storage = storage;
        this.mySrc = mySrc;
        this.pattern = cMatrix;
        this.craftInv = secondMatrix;
        this.container = ccp;
        this.Gridsize = GridSize;
    }

    public IItemHandler getCraftingMatrix() {
        return this.craftInv;
    }

    public boolean canTakeStack(PlayerEntity player) {
        return false;
    }

    public ItemStack onTake(PlayerEntity p, ItemStack is) {
        return is;
    }

    public void doClick(InventoryAction action, PlayerEntity who) {
        if (!this.getStack().isEmpty()) {
            if (!this.isRemote()) {
                IMEMonitor<IAEItemStack> inv = this.storage.getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
                int howManyPerCraft = this.getStack().getCount();
                InventoryAdaptor ia = null;
                int maxTimesToCraft;
                if (action == InventoryAction.CRAFT_SHIFT) {
                    ia = InventoryAdaptor.getAdaptor(who);
                    maxTimesToCraft = (int) Math.floor((double) this.getStack().getMaxStackSize() / (double) howManyPerCraft);
                } else if (action == InventoryAction.CRAFT_STACK) {
                    ia = new AdaptorItemHandler(new WrapperCursorItemHandler(who.inventory));
                    maxTimesToCraft = (int) Math.floor((double) this.getStack().getMaxStackSize() / (double) howManyPerCraft);
                } else {
                    ia = new AdaptorItemHandler(new WrapperCursorItemHandler(who.inventory));
                    maxTimesToCraft = 1;
                }

                maxTimesToCraft = this.capCraftingAttempts(maxTimesToCraft);
                if (ia != null) {
                    ItemStack rs = this.getStack().copy();
                    if (!rs.isEmpty()) {
                        for (int x = 0; x < maxTimesToCraft; ++x) {
                            if (((InventoryAdaptor) ia).simulateAdd(rs).isEmpty()) {
                                IItemList<IAEItemStack> all = inv.getStorageList();
                                ItemStack extra = ((InventoryAdaptor) ia).addItems(this.craftItem(who, rs, inv, all));
                                if (!extra.isEmpty()) {
                                    List<ItemStack> drops = new ArrayList();
                                    drops.add(extra);
                                    Platform.spawnDrops(who.world, new BlockPos((int) who.getPosX(), (int) who.getPosY(), (int) who.getPosZ()), drops);
                                    return;
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    protected ITableRecipe findRecipe(IInventory ic, World world) {

        if (this.container instanceof BasicCraftingTerminalContainer) {
            BasicCraftingTerminalContainer containerTerminal = (BasicCraftingTerminalContainer) this.container;

            Optional<ITableRecipe> recipe = containerTerminal.getCurrentRecipe();
            if (recipe.isPresent()) {
                return containerTerminal.getCurrentRecipe().get();
            }
        } else if (this.container instanceof AdvancedCraftingTerminalContainer) {
            AdvancedCraftingTerminalContainer containerTerminal = (AdvancedCraftingTerminalContainer) this.container;
            Optional<ITableRecipe> recipe = containerTerminal.getCurrentRecipe();
            if (recipe.isPresent()) {
                return containerTerminal.getCurrentRecipe().get();
            }
        } else if (this.container instanceof EliteCraftingTerminalContainer){
            EliteCraftingTerminalContainer containerTerminal = (EliteCraftingTerminalContainer) this.container;
            Optional<ITableRecipe> recipe = containerTerminal.getCurrentRecipe();
            if (recipe.isPresent()) {
                return containerTerminal.getCurrentRecipe().get();
            }
        } else if (this.container instanceof UltimateCraftingTerminalContainer){
            UltimateCraftingTerminalContainer containerTerminal = (UltimateCraftingTerminalContainer) this.container;
            Optional<ITableRecipe> recipe = containerTerminal.getCurrentRecipe();
            if (recipe.isPresent()) {
                return containerTerminal.getCurrentRecipe().get();
            }
        }

        return world.getRecipeManager().getRecipe(RecipeTypes.TABLE, ic, world).orElse(null);
    }

    protected NonNullList<ItemStack> getRemainingItems(IInventory ic, World world) {
        if (this.container instanceof BasicCraftingTerminalContainer) {
            BasicCraftingTerminalContainer containerTerminal = (BasicCraftingTerminalContainer) this.container;
            Optional<ITableRecipe> recipe = world.getRecipeManager().getRecipe(RecipeTypes.TABLE, ic, world);
            if (recipe != null && recipe.isPresent()) {
                return containerTerminal.getCurrentRecipe().get().getRemainingItems(ic);
            }
        }

        return super.getRemainingItems(world);
    }

    private int capCraftingAttempts(int maxTimesToCraft) {
        return maxTimesToCraft;
    }

    private ItemStack craftItem(PlayerEntity p, ItemStack request, IMEMonitor<IAEItemStack> inv, IItemList all) {
        ItemStack is = this.getStack();
        if (!is.isEmpty() && ItemStack.areItemsEqual(request, is)) {
            ItemStack[] set = new ItemStack[this.getPattern().getSlots()];
            Arrays.fill(set, ItemStack.EMPTY);
            World world = p.world;
            if (!world.isRemote()) {
                BaseItemStackHandler Inv = new BaseItemStackHandler(Gridsize * Gridsize);
                IInventory matrix = new ExtendedCraftingInventory(this.getContainer(), Inv, Gridsize);
                for (int x = 0; x < Gridsize * Gridsize; ++x) {
                    matrix.setInventorySlotContents(x, this.getPattern().getStackInSlot(x));
                }

                ITableRecipe r = this.findRecipe(matrix, world);
                if (r == null) {
                    Item target = request.getItem();
                    if (target.isDamageable() && target.isRepairable(request)) {
                        boolean isBad = false;

                        for (int x = 0; x < matrix.getSizeInventory(); ++x) {
                            ItemStack pis = matrix.getStackInSlot(x);
                            if (!pis.isEmpty() && pis.getItem() != target) {
                                isBad = true;
                            }
                        }

                        if (!isBad) {
                            super.onTake(p, is);
                            p.openContainer.onCraftMatrixChanged(new WrapperInvItemHandler(this.craftInv));
                            return request;
                        }
                    }
                    return ItemStack.EMPTY;
                }

                is = r.getCraftingResult(matrix);
                if (inv != null) {
                    for (int x = 0; x < this.getPattern().getSlots(); ++x) {
                        if (!this.getPattern().getStackInSlot(x).isEmpty()) {
                            set[x] = MTPlatform.extractItemsByRecipe(this.energySrc, this.mySrc, inv, world, r, is, matrix, this.getPattern().getStackInSlot(x), x, all, Actionable.MODULATE, ViewCellItem.createFilter(this.container.getViewCells()));
                            matrix.setInventorySlotContents(x, set[x]);
                        }
                    }
                }
            }

            if (this.preCraft(p, inv, set, is)) {
                this.makeItem(p, is);
                this.postCraft(p, inv, set, is);
            }

            p.openContainer.onCraftMatrixChanged(new WrapperInvItemHandler(this.craftInv));
            return is;
        } else {
            return ItemStack.EMPTY;
        }
    }

    private boolean preCraft(PlayerEntity p, IMEMonitor<IAEItemStack> inv, ItemStack[] set, ItemStack result) {
        return true;
    }

    private void makeItem(PlayerEntity p, ItemStack is) {
        super.onTake(p, is);
    }

    private void postCraft(PlayerEntity p, IMEMonitor<IAEItemStack> inv, ItemStack[] set, ItemStack result) {
        List<ItemStack> drops = new ArrayList();
        if (!p.getEntityWorld().isRemote()) {
            for (int x = 0; x < this.craftInv.getSlots(); ++x) {
                if (this.craftInv.getStackInSlot(x).isEmpty()) {
                    ItemHandlerUtil.setStackInSlot(this.craftInv, x, set[x]);
                } else if (!set[x].isEmpty()) {
                    IAEItemStack fail = (IAEItemStack) inv.injectItems(AEItemStack.fromItemStack(set[x]), Actionable.MODULATE, this.mySrc);
                    if (fail != null) {
                        drops.add(fail.createItemStack());
                    }
                }
            }
        }

        if (drops.size() > 0) {
            Platform.spawnDrops(p.world, new BlockPos((int) p.getPosX(), (int) p.getPosY(), (int) p.getPosZ()), drops);
        }

    }

    IItemHandler getPattern() {
        return this.pattern;
    }
}
