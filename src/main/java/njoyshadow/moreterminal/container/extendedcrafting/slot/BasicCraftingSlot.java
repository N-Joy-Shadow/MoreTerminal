package njoyshadow.moreterminal.container.extendedcrafting.slot;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerNull;
import appeng.container.me.items.CraftingTermContainer;
import appeng.container.slot.AppEngCraftingSlot;
import appeng.container.slot.AppEngSlot;
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
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import njoyshadow.moreterminal.container.extendedcrafting.BasicCraftingTerminalContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BasicCraftingSlot extends MTOutputSlot {
    private final IItemHandler craftInv;
    private final IItemHandler pattern;
    private final IActionSource mySrc;
    private final IEnergySource energySrc;
    private final IStorageMonitorable storage;
    private final IContainerCraftingPacket container;

    public BasicCraftingSlot(PlayerEntity player, IActionSource mySrc, IEnergySource energySrc,
                             IStorageMonitorable storage, IItemHandler cMatrix, IItemHandler secondMatrix, IContainerCraftingPacket ccp,
                             AEBaseContainer container,int GridSize, IInventory matrix) {
        
        //고쳐야함
        super(player, cMatrix,GridSize,matrix,container);
        this.energySrc = energySrc;
        this.storage = storage;
        this.mySrc = mySrc;
        this.pattern = cMatrix;
        this.craftInv = secondMatrix;
        this.container = ccp;
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
                    maxTimesToCraft = (int)Math.floor((double)this.getStack().getMaxStackSize() / (double)howManyPerCraft);
                } else if (action == InventoryAction.CRAFT_STACK) {
                    ia = new AdaptorItemHandler(new WrapperCursorItemHandler(who.inventory));
                    maxTimesToCraft = (int)Math.floor((double)this.getStack().getMaxStackSize() / (double)howManyPerCraft);
                } else {
                    ia = new AdaptorItemHandler(new WrapperCursorItemHandler(who.inventory));
                    maxTimesToCraft = 1;
                }

                maxTimesToCraft = this.capCraftingAttempts(maxTimesToCraft);
                if (ia != null) {
                    ItemStack rs = this.getStack().copy();
                    if (!rs.isEmpty()) {
                        for(int x = 0; x < maxTimesToCraft; ++x) {
                            if (((InventoryAdaptor)ia).simulateAdd(rs).isEmpty()) {
                                IItemList<IAEItemStack> all = inv.getStorageList();
                                ItemStack extra = ((InventoryAdaptor)ia).addItems(this.craftItem(who, rs, inv, all));
                                if (!extra.isEmpty()) {
                                    List<ItemStack> drops = new ArrayList();
                                    drops.add(extra);
                                    Platform.spawnDrops(who.world, new BlockPos((int)who.getPosX(), (int)who.getPosY(), (int)who.getPosZ()), drops);
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
            BasicCraftingTerminalContainer containerTerminal = (BasicCraftingTerminalContainer)this.container;

            Optional<ITableRecipe> recipe = world.getRecipeManager().getRecipe(RecipeTypes.TABLE, ic, world);
            if (recipe != null && recipe.isPresent()) {
                return containerTerminal.getCurrentRecipe();
            }
        }

        return world.getRecipeManager().getRecipe(RecipeTypes.TABLE, ic, world).orElse((ITableRecipe) null);
    }

    protected NonNullList<ItemStack> getRemainingItems(IInventory ic, World world) {
        if (this.container instanceof BasicCraftingTerminalContainer) {
            BasicCraftingTerminalContainer containerTerminal = (BasicCraftingTerminalContainer)this.container;
            Optional<ITableRecipe> recipe = world.getRecipeManager().getRecipe(RecipeTypes.TABLE, ic, world);
            if (recipe != null && recipe.isPresent()) {
                return containerTerminal.getCurrentRecipe().getRemainingItems(ic);
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
                CraftingInventory ic = new CraftingInventory(new ContainerNull(), 3, 3);

                for(int x = 0; x < 9; ++x) {
                    ic.setInventorySlotContents(x, this.getPattern().getStackInSlot(x));
                }

                ITableRecipe r = this.findRecipe(ic, world);
                if (r == null) {
                    Item target = request.getItem();
                    if (target.isDamageable() && target.isRepairable(request)) {
                        boolean isBad = false;

                        for(int x = 0; x < ic.getSizeInventory(); ++x) {
                            ItemStack pis = ic.getStackInSlot(x);
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

                is = r.getCraftingResult(ic);
                if (inv != null) {
                    for(int x = 0; x < this.getPattern().getSlots(); ++x) {
                        if (!this.getPattern().getStackInSlot(x).isEmpty()) {
                            //고쳐야함
                            //set[x] = Platform.extractItemsByRecipe(this.energySrc, this.mySrc, inv, world, r, is, ic, this.getPattern().getStackInSlot(x), x, all, Actionable.MODULATE, ViewCellItem.createFilter(this.container.getViewCells()));
                            ic.setInventorySlotContents(x, set[x]);
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
            for(int x = 0; x < this.craftInv.getSlots(); ++x) {
                if (this.craftInv.getStackInSlot(x).isEmpty()) {
                    ItemHandlerUtil.setStackInSlot(this.craftInv, x, set[x]);
                } else if (!set[x].isEmpty()) {
                    IAEItemStack fail = (IAEItemStack)inv.injectItems(AEItemStack.fromItemStack(set[x]), Actionable.MODULATE, this.mySrc);
                    if (fail != null) {
                        drops.add(fail.createItemStack());
                    }
                }
            }
        }

        if (drops.size() > 0) {
            Platform.spawnDrops(p.world, new BlockPos((int)p.getPosX(), (int)p.getPosY(), (int)p.getPosZ()), drops);
        }

    }

    IItemHandler getPattern() {
        return this.pattern;
    }
}
