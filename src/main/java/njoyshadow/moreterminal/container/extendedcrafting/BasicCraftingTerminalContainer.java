package njoyshadow.moreterminal.container.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerNull;
import appeng.container.SlotSemantic;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.container.me.items.CraftingTermContainer;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.container.slot.CraftingMatrixSlot;
import appeng.container.slot.CraftingTermSlot;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.inv.WrapperInvItemHandler;
import com.google.common.base.Preconditions;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

public class BasicCraftingTerminalContainer extends ItemTerminalContainer implements IContainerCraftingPacket {

    public static final ContainerType<BasicCraftingTerminalContainer> TYPE = ContainerTypeBuilder
            .create(BasicCraftingTerminalContainer::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("craftingterm");

    private final ISegmentedInventory craftingInventoryHost;
    private final CraftingMatrixSlot[] craftingSlots = new CraftingMatrixSlot[9];
    private final CraftingTermSlot outputSlot;
    private IRecipe<CraftingInventory> currentRecipe;

    public BasicCraftingTerminalContainer(int id, final PlayerInventory ip, final ITerminalHost host) {
        super(TYPE, id, ip, host, false);
        this.craftingInventoryHost = (ISegmentedInventory) host;

        final IItemHandler craftingGridInv = this.craftingInventoryHost.getInventoryByName("crafting");

        for (int i = 0; i < 9; i++) {
            this.addSlot(this.craftingSlots[i] = new CraftingMatrixSlot(this, craftingGridInv, i),
                    SlotSemantic.CRAFTING_GRID);
        }

        this.addSlot(this.outputSlot = new CraftingTermSlot(this.getPlayerInventory().player, this.getActionSource(),
                this.powerSource, host, craftingGridInv, craftingGridInv, this), SlotSemantic.CRAFTING_RESULT);

        this.createPlayerInventorySlots(ip);

        this.onCraftMatrixChanged(new WrapperInvItemHandler(craftingGridInv));
    }

    /**
     * Callback for when the crafting matrix is changed.
     */

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        final ContainerNull cn = new ContainerNull();
        final CraftingInventory ic = new CraftingInventory(cn, 3, 3);

        for (int x = 0; x < 9; x++) {
            ic.setInventorySlotContents(x, this.craftingSlots[x].getStack());
        }

        World world = this.getPlayerInventory().player.world;
        if (this.currentRecipe == null || !this.currentRecipe.matches(ic, world)) {
            this.currentRecipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, ic, world).orElse(null);
        }

        if (this.currentRecipe == null) {
            this.outputSlot.putStack(ItemStack.EMPTY);
        } else {
            this.outputSlot.putStack(this.currentRecipe.getCraftingResult(ic));
        }
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("player")) {
            return new PlayerInvWrapper(this.getPlayerInventory());
        }
        return this.craftingInventoryHost.getInventoryByName(name);
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    public IRecipe<CraftingInventory> getCurrentRecipe() {
        return this.currentRecipe;
    }

    /**
     * Clears the crafting grid and moves everything back into the network inventory.
     */
    public void clearCraftingGrid() {
        Preconditions.checkState(isClient());
        CraftingMatrixSlot slot = craftingSlots[0];
        final InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slot.slotNumber, 0);
        NetworkHandler.instance().sendToServer(p);
    }

    @Override
    public boolean hasItemType(ItemStack itemStack, int amount) {
        // In addition to the base item repo, also check the crafting grid if it
        // already contains some of the needed items
        for (Slot slot : getSlots(SlotSemantic.CRAFTING_GRID)) {
            ItemStack stackInSlot = slot.getStack();
            if (!stackInSlot.isEmpty() && Platform.itemComparisons().isSameItem(itemStack, stackInSlot)) {
                if (itemStack.getCount() >= amount) {
                    return true;
                }
                amount -= itemStack.getCount();
            }

        }

        return super.hasItemType(itemStack, amount);
    }

}
