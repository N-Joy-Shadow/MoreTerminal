package njoyshadow.moreterminal.menu.extendedcrafting;

import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ITerminalHost;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.util.inv.PlayerInternalInventory;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import com.blakebr0.extendedcrafting.container.inventory.ExtendedCraftingInventory;
import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import njoyshadow.moreterminal.item.part.extendedcrafting.BaseExtendCraftingTermPart;

import java.util.List;
import java.util.function.Predicate;

public class BaseCraftingTermMenu extends MEStorageMenu implements IMenuCraftingPacket {
    private static final String ACTION_CLEAR_TO_PLAYER = "clearToPlayer";
    private final int GRID_SIZE;
    private final int GRID_MATRIX;

    private final ISegmentedInventory craftingInventoryHost;
    private final CraftingMatrixSlot[] craftingSlots;

    private final ExtendedCraftingInventory recipeTestContainer;
    private final ResourceLocation INV_CRAFTING;
    //TODO Replace this
    private final CraftingTermSlot outputSlot;
    private ITableRecipe currentRecipe;



    public BaseCraftingTermMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host, int GridSize, ResourceLocation INV) {
        super(menuType, id, ip, host,true);

        this.INV_CRAFTING =INV;
        System.out.println(this.INV_CRAFTING);
        this.GRID_SIZE = GridSize;
        this.GRID_MATRIX = GRID_SIZE * GRID_SIZE;

        this.craftingInventoryHost = (ISegmentedInventory) host;
        this.craftingSlots = new CraftingMatrixSlot[GRID_MATRIX];
        this.recipeTestContainer  = new ExtendedCraftingInventory(this,new BaseItemStackHandler(GRID_MATRIX), GRID_SIZE);

        var craftingGridInv = this.craftingInventoryHost.getSubInventory(this.INV_CRAFTING);

        for (int i = 0; i < GRID_MATRIX; i++) {
            this.addSlot(this.craftingSlots[i] = new CraftingMatrixSlot(this, craftingGridInv, i),
                    SlotSemantics.CRAFTING_GRID);
        }

        this.addSlot(this.outputSlot = new CraftingTermSlot(this.getPlayerInventory().player, this.getActionSource(),
                        this.powerSource, host.getInventory(), craftingGridInv, craftingGridInv, this),
                SlotSemantics.CRAFTING_RESULT);

        updateCurrentRecipeAndOutput(true);


        registerClientAction(ACTION_CLEAR_TO_PLAYER, this::clearToPlayerInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void slotsChanged(Container inventory) {
        updateCurrentRecipeAndOutput(false);
    }

    @Override
    public InternalInventory getCraftingMatrix() {
        //TODO FIX THIS
        return this.craftingInventoryHost.getSubInventory(this.INV_CRAFTING);
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    public void clearCraftingGrid() {
        Preconditions.checkState(isClientSide());
        CraftingMatrixSlot slot = craftingSlots[0];
        final InventoryActionPacket p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slot.index, 0);
        NetworkHandler.instance().sendToServer(p);
    }

    @Override
    public boolean hasIngredient(Predicate<ItemStack> predicate, int amount) {
        // In addition to the base item repo, also check the crafting grid if it
        // already contains some of the needed items
        for (var slot : getSlots(SlotSemantics.CRAFTING_GRID)) {
            var stackInSlot = slot.getItem();
            if (!stackInSlot.isEmpty() && predicate.test(stackInSlot)) {
                if (stackInSlot.getCount() >= amount) {
                    return true;
                }
                amount -= stackInSlot.getCount();
            }

        }

        return super.hasIngredient(predicate, amount);
    }

    @Override
    public void startAutoCrafting(List<GenericStack> toCraft) {
        CraftConfirmMenu.openWithCraftingList(getActionHost(), (ServerPlayer) getPlayer(), getLocator(), toCraft);
    }

    public ITableRecipe getCurrentRecipe() {
        return this.currentRecipe;
    }

    protected boolean isCraftable(ItemStack itemStack) {
        var clientRepo = getClientRepo();

        if (clientRepo != null) {
            for (var stack : clientRepo.getAllEntries()) {
                if (AEItemKey.matches(stack.getWhat(), itemStack) && stack.isCraftable()) {
                    return true;
                }
            }
        }

        return false;
    }

    private void updateCurrentRecipeAndOutput(boolean forceUpdate) {
        boolean hasChanged = forceUpdate;
        for (int x = 0; x < GRID_MATRIX; x++) {
            var stack = this.craftingSlots[x].getItem();
            if (!ItemStack.isSameItemSameTags(stack, recipeTestContainer.getItem(x))) {
                hasChanged = true;
                recipeTestContainer.setItem(x, stack.copy());
            }
        }

        if (!hasChanged) {
            return;
        }

        Level level = this.getPlayerInventory().player.level;
        this.currentRecipe = level.getRecipeManager().getRecipeFor(RecipeTypes.TABLE, recipeTestContainer, level)
                .orElse(null);

        if (this.currentRecipe == null) {
            this.outputSlot.set(ItemStack.EMPTY);
        } else {
            this.outputSlot.set(this.currentRecipe.assemble(recipeTestContainer));
        }
    }

    public void clearToPlayerInventory() {
        if (isClientSide()) {
            sendClientAction(ACTION_CLEAR_TO_PLAYER);
            return;
        }
        //TODO FIX THIS
        var craftingGridInv = this.craftingInventoryHost.getSubInventory(this.INV_CRAFTING);
        var playerInv = new PlayerInternalInventory(getPlayerInventory());

        for (int i = 0; i < craftingGridInv.size(); ++i) {
            for (int emptyLoop = 0; emptyLoop < 2; ++emptyLoop) {
                boolean allowEmpty = emptyLoop == 1;

                // Hotbar first
                final int HOTBAR_SIZE = 9;
                for (int j = HOTBAR_SIZE; j-- > 0;) {
                    if (playerInv.getStackInSlot(j).isEmpty() == allowEmpty) {
                        craftingGridInv.setItemDirect(i,
                                playerInv.getSlotInv(j).addItems(craftingGridInv.getStackInSlot(i)));
                    }
                }
                // Rest of inventory
                for (int j = HOTBAR_SIZE; j < Inventory.INVENTORY_SIZE; ++j) {
                    if (playerInv.getStackInSlot(j).isEmpty() == allowEmpty) {
                        craftingGridInv.setItemDirect(i,
                                playerInv.getSlotInv(j).addItems(craftingGridInv.getStackInSlot(i)));
                    }
                }
            }
        }
    }
}
