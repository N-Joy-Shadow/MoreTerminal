package njoyshadow.moreterminal.container.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.storage.ITerminalHost;
import appeng.container.ContainerNull;
import appeng.container.SlotSemantic;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.container.slot.CraftingMatrixSlot;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import appeng.util.inv.WrapperInvItemHandler;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import com.blakebr0.extendedcrafting.container.inventory.ExtendedCraftingInventory;
import com.google.common.base.Preconditions;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import njoyshadow.moreterminal.container.extendedcrafting.slot.ExtendedCraftingTermSlot;
import njoyshadow.moreterminal.container.implementations.MTContainerTypeBulder;

import java.util.Optional;

public class AdvancedCraftingTerminalContainer extends ItemTerminalContainer implements IContainerCraftingPacket {


    public static final ContainerType<AdvancedCraftingTerminalContainer> TYPE = MTContainerTypeBulder
            .create(AdvancedCraftingTerminalContainer::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("advancedcraftingeterm");
            //.build("basiccraftingterm");

    private final ISegmentedInventory craftingInventoryHost;
    private final ExtendedCraftingTermSlot outputSlot;
    private final World world;
    private Optional<ITableRecipe> currentRecipe;
    private final int GridSize =5;
    private final CraftingMatrixSlot[] craftingSlots = new CraftingMatrixSlot[GridSize * GridSize];
    public AdvancedCraftingTerminalContainer(int id, final PlayerInventory ip, final ITerminalHost host) {
        super(TYPE, id, ip, host, false);
        this.craftingInventoryHost = (ISegmentedInventory) host;
        this.world = ip.player.world;

        final IItemHandler craftingGridInv = this.craftingInventoryHost.getInventoryByName("crafting");

        BaseItemStackHandler Inv = new BaseItemStackHandler(GridSize * GridSize);

        IInventory matrix = new ExtendedCraftingInventory(this, Inv, GridSize);
        int i;
        for (i = 0; i < GridSize * GridSize; i++) {
                this.addSlot(this.craftingSlots[i] = new CraftingMatrixSlot(this, craftingGridInv,i),SlotSemantic.CRAFTING_GRID);

        }
        this.addSlot(this.outputSlot = new ExtendedCraftingTermSlot(this.getPlayerInventory().player, this.getActionSource(),
                this.powerSource, host, craftingGridInv, craftingGridInv, this,GridSize,matrix), SlotSemantic.CRAFTING_RESULT);
        this.createPlayerInventorySlots(ip);

        this.onCraftMatrixChanged(new WrapperInvItemHandler(craftingGridInv));
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {

        BaseItemStackHandler Inv = new BaseItemStackHandler(GridSize*GridSize);
        ContainerNull cn = new ContainerNull();
        IInventory matrix = new ExtendedCraftingInventory(cn, Inv, GridSize);
        int i;
        for (i = 0; i < GridSize * GridSize; i++) {
            matrix.setInventorySlotContents(i,this.craftingSlots[i].getStack());
        }
        //Extended Recipe
        this.currentRecipe = this.world.getRecipeManager().getRecipe(RecipeTypes.TABLE, matrix, this.world);

        if (this.currentRecipe.isPresent()) {
            ItemStack result = this.currentRecipe.get().getCraftingResult(matrix);
            this.outputSlot.putStack(result);
        }
        else {
            this.outputSlot.putStack(ItemStack.EMPTY);
        }

    }
    public Optional<ITableRecipe> getCurrentRecipe() {    return this.currentRecipe; }

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


    /**
     * Clears the crafting grid and moves everything back into the network inventory.
     * This will Add when finish Gui Tasks
     */
    public void clearCraftingGrid() {
        Preconditions.checkState(isClient());
        CraftingMatrixSlot slot =this.craftingSlots[0];
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
