package njoyshadow.moreterminal.client.gui.me.items.extendedcrafting;

import appeng.api.config.SecurityPermissions;
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
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.slot.CraftingMatrixSlot;
import appeng.menu.slot.CraftingTermSlot;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.util.inv.PlayerInternalInventory;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class BaseExtendCraftingTermMenu extends MEStorageMenu implements IMenuCraftingPacket {

    public static final MenuType<CraftingTermMenu> TYPE = MenuTypeBuilder
            .create(CraftingTermMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("craftingterm");

    private static final String ACTION_CLEAR_TO_PLAYER = "clearToPlayer";

    private final ISegmentedInventory craftingInventoryHost;
    private final CraftingMatrixSlot[] craftingSlots = new CraftingMatrixSlot[9];
    private final CraftingContainer recipeTestContainer = new CraftingContainer(this, 3, 3);

    private final CraftingTermSlot outputSlot;
    private Recipe<CraftingContainer> currentRecipe;

    public BaseExtendCraftingTermMenu(int id, Inventory ip, ITerminalHost host) {
        this(TYPE, id, ip, host, true);
    }

    public BaseExtendCraftingTermMenu(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host,
                            boolean bindInventory) {
        super(menuType, id, ip, host, bindInventory);
        this.craftingInventoryHost = (ISegmentedInventory) host;

        var craftingGridInv = this.craftingInventoryHost
                .getSubInventory(CraftingTerminalPart.INV_CRAFTING);

        for (int i = 0; i < 9; i++) {
            this.addSlot(this.craftingSlots[i] = new CraftingMatrixSlot(this, craftingGridInv, i),
                    SlotSemantics.CRAFTING_GRID);
        }

        this.addSlot(this.outputSlot = new CraftingTermSlot(this.getPlayerInventory().player, this.getActionSource(),
                        this.powerSource, host.getInventory(), craftingGridInv, craftingGridInv, this),
                SlotSemantics.CRAFTING_RESULT);

        updateCurrentRecipeAndOutput(true);

        registerClientAction(ACTION_CLEAR_TO_PLAYER, this::clearToPlayerInventory);
    }

    /**
     * Callback for when the crafting matrix is changed.
     */

    @Override
    public void slotsChanged(Container inventory) {
        updateCurrentRecipeAndOutput(false);
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return true;
    }

    private void updateCurrentRecipeAndOutput(boolean forceUpdate) {
        boolean hasChanged = forceUpdate;
        for (int x = 0; x < 9; x++) {
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
        this.currentRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, recipeTestContainer, level)
                .orElse(null);

        if (this.currentRecipe == null) {
            this.outputSlot.set(ItemStack.EMPTY);
        } else {
            this.outputSlot.set(this.currentRecipe.assemble(recipeTestContainer));
        }
    }

    @Override
    public InternalInventory getCraftingMatrix() {
        return this.craftingInventoryHost.getSubInventory(CraftingTerminalPart.INV_CRAFTING);
    }

    @Override
    public boolean useRealItems() {
        return true;
    }

    @Override
    public void startAutoCrafting(List<GenericStack> toCraft) {
        CraftConfirmMenu.openWithCraftingList(getActionHost(), (ServerPlayer) getPlayer(), getLocator(), toCraft);
    }

    public Recipe<CraftingContainer> getCurrentRecipe() {
        return this.currentRecipe;
    }

    /**
     * Clears the crafting grid and moves everything back into the network inventory.
     */
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

    /**
     * Determines which slots of the given slot-to-item map cannot be filled with items based on the contents of this
     * terminal or player inventory.
     *
     * @return The keys of the given slot-map for which no stored ingredients could be found, separated in craftable and
     *         missing items.
     */
    public CraftingTermMenu.MissingIngredientSlots findMissingIngredients(Map<Integer, Ingredient> ingredients) {

        // Try to figure out if any slots have missing ingredients
        // Find every "slot" (in JEI parlance) that has no equivalent item in the item repo or player inventory
        Set<Integer> missingSlots = new HashSet<>(); // missing but not craftable
        Set<Integer> craftableSlots = new HashSet<>(); // missing but craftable

        // We need to track how many of a given item stack we've already used for other slots in the recipe.
        // Otherwise recipes that need 4x<item> will not correctly show missing items if at least 1 of <item> is in
        // the grid.
        var reservedGridAmounts = new Object2IntOpenHashMap<>();
        var playerItems = getPlayerInventory().items;
        var reservedPlayerItems = new int[playerItems.size()];

        for (var entry : ingredients.entrySet()) {
            var ingredient = entry.getValue();

            boolean found = false;
            // Player inventory is cheaper to check
            for (int i = 0; i < playerItems.size(); i++) {
                var stack = playerItems.get(i);
                if (stack.getCount() - reservedPlayerItems[i] > 0 && ingredient.test(stack)) {
                    reservedPlayerItems[i]++;
                    found = true;
                    break;
                }
            }

            // Then check the terminal screen's repository of network items
            if (!found) {
                // We use AE stacks to get an easily comparable item type key that ignores stack size
                int neededAmount = reservedGridAmounts.getOrDefault(ingredient, 0) + 1;
                if (hasIngredient(ingredient, neededAmount)) {
                    reservedGridAmounts.put(ingredient, neededAmount);
                    found = true;
                }
            }

            // Check the terminal once again, but this time for craftable items
            if (!found) {
                for (var stack : ingredient.getItems()) {
                    if (isCraftable(stack)) {
                        craftableSlots.add(entry.getKey());
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                missingSlots.add(entry.getKey());
            }
        }

        return new CraftingTermMenu.MissingIngredientSlots(missingSlots, craftableSlots);
    }

    public record MissingIngredientSlots(Set<Integer> missingSlots, Set<Integer> craftableSlots) {
        public int totalSize() {
            return missingSlots.size() + craftableSlots.size();
        }

        public boolean anyMissing() {
            return missingSlots.size() > 0;
        }

        public boolean anyCraftable() {
            return craftableSlots.size() > 0;
        }
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

    public void clearToPlayerInventory() {
        if (isClientSide()) {
            sendClientAction(ACTION_CLEAR_TO_PLAYER);
            return;
        }

        var craftingGridInv = this.craftingInventoryHost.getSubInventory(CraftingTerminalPart.INV_CRAFTING);
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
