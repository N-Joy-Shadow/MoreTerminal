package njoyshadow.moreterminal.utils.terminalcontainer;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.storage.ITerminalHost;
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
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import com.google.common.base.Preconditions;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import java.util.Iterator;
import java.util.Optional;

public class ExtendedContainer extends ItemTerminalContainer implements IContainerCraftingPacket {

    public static final ContainerType<ExtendedContainer> TYPE;
    private final ISegmentedInventory craftingInventoryHost;
    private final CraftingMatrixSlot[] craftingSlots = new CraftingMatrixSlot[9];
    private final CraftingTermSlot outputSlot;
    private IRecipe<CraftingInventory> currentRecipe;

    public ExtendedContainer(int id, PlayerInventory ip, ITerminalHost host) {
        super(TYPE, id, ip, host, false);


        this.craftingInventoryHost = (ISegmentedInventory)host;
        IItemHandler craftingGridInv = this.craftingInventoryHost.getInventoryByName("crafting");

        for(int i = 0; i < 9; ++i) {
            this.addSlot(this.craftingSlots[i] = new CraftingMatrixSlot(this, craftingGridInv, i), SlotSemantic.CRAFTING_GRID);
        }

        this.addSlot(this.outputSlot = new CraftingTermSlot(this.getPlayerInventory().player, this.getActionSource(), this.powerSource, host, craftingGridInv, craftingGridInv, this), SlotSemantic.CRAFTING_RESULT);
        this.createPlayerInventorySlots(ip);
        this.onCraftMatrixChanged(new WrapperInvItemHandler(craftingGridInv));
    }

    public void onCraftMatrixChanged(IInventory inventory) {
        World world = this.getPlayerInventory().player.world;

        Optional<ITableRecipe> recipe = world.getRecipeManager().getRecipe(RecipeTypes.TABLE, inventory, world);
        if (recipe.isPresent()) {
            ItemStack result = recipe.get().getCraftingResult(inventory);
            this.outputSlot.putStack(result);
        } else {
            this.outputSlot.putStack(ItemStack.EMPTY);
        }



    }

    public IItemHandler getInventoryByName(String name) {
        return (IItemHandler)(name.equals("player") ? new PlayerInvWrapper(this.getPlayerInventory()) : this.craftingInventoryHost.getInventoryByName(name));
    }

    public boolean useRealItems() {
        return true;
    }

    public IRecipe<CraftingInventory> getCurrentRecipe() {
        return this.currentRecipe;
    }

    public void clearCraftingGrid() {
        Preconditions.checkState(this.isClient());
        CraftingMatrixSlot slot = this.craftingSlots[0];
        InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slot.slotNumber, 0L);
        NetworkHandler.instance().sendToServer(p);
    }

    public boolean hasItemType(ItemStack itemStack, int amount) {
        Iterator var3 = this.getSlots(SlotSemantic.CRAFTING_GRID).iterator();

        while(var3.hasNext()) {
            Slot slot = (Slot)var3.next();
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

    static {
        TYPE = ContainerTypeBuilder.create(ExtendedContainer::new, ITerminalHost.class).requirePermission(SecurityPermissions.CRAFT).build("advancedcraftingterm");
    }
}
