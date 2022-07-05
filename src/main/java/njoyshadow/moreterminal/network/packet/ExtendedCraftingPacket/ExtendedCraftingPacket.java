package njoyshadow.moreterminal.network.packet.ExtendedCraftingPacket;

import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.StorageHelper;
import appeng.core.AELog;
import appeng.helpers.IMenuCraftingPacket;
import appeng.items.storage.ViewCellItem;
import appeng.util.prioritylist.IPartitionList;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;
import io.netty.buffer.Unpooled;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import njoyshadow.moreterminal.network.packet.MTBasePacket;
import njoyshadow.moreterminal.utils.crafting.ExtendedCraftingRecipeUtil;

import javax.annotation.Nullable;
import java.util.*;

public class ExtendedCraftingPacket extends MTBasePacket {

    private ResourceLocation recipeId;

    private NonNullList<ItemStack> ingredientTemplates;

    private boolean craftMissing;

    private int GRID_SIZE;
    private int GRID_MATRIX;


    public ExtendedCraftingPacket(FriendlyByteBuf stream) {
        if (stream.readBoolean()) {
            this.recipeId = stream.readResourceLocation();
        } else {
            this.recipeId = null;
        }
        int[] streamInts = stream.readVarIntArray();

        this.GRID_SIZE = streamInts[1];
        this.GRID_MATRIX = this.GRID_SIZE * this.GRID_SIZE;
        ingredientTemplates = NonNullList.withSize(streamInts[0], ItemStack.EMPTY);
        for (int i = 0; i < ingredientTemplates.size(); i++) {
            ingredientTemplates.set(i, stream.readItem());
        }
        craftMissing = stream.readBoolean();
    }

    public ExtendedCraftingPacket(@Nullable ResourceLocation recipeId,
                                            NonNullList<ItemStack> ingredientTemplates, boolean craftMissing, int Grid_Size) {
        var data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        if (recipeId != null) {
            data.writeBoolean(true);
            data.writeResourceLocation(recipeId);
        } else {
            data.writeBoolean(false);
        }
        data.writeVarIntArray(new int[] {ingredientTemplates.size(), Grid_Size});
        for (var stack : ingredientTemplates) {
            data.writeItem(stack);
        }
        data.writeBoolean(craftMissing);

        configureWrite(data);
    }

    /**
     * Serverside handler for this packet.
     */
    @Override
    public void serverPacketData(ServerPlayer player) {
        // Setup and verification
        var menu = player.containerMenu;
        if (!(menu instanceof IMenuCraftingPacket cct)) {
            // Server might have closed the menu before the client-packet is processed. This is not an error.
            return;
        }

        if (!cct.useRealItems()) {
            AELog.warn("Trying to use real items for crafting in a pattern encoding terminal");
            return;
        }

        // We can only fill the crafting grid if the menu is actually online
        var node = cct.getNetworkNode();
        if (node == null) {
            return;
        }

        var grid = node.getGrid();

        var security = grid.getSecurityService();
        var energy = grid.getEnergyService();
        var craftMatrix = cct.getCraftingMatrix();

        // We'll try to use the best possible ingredients based on what's available in the network

        var storage = grid.getStorageService().getInventory();
        var cachedStorage = grid.getStorageService().getCachedInventory();
        var filter = ViewCellItem.createItemFilter(cct.getViewCells());
        var ingredients = getDesiredIngredients(player);

        // Prepare to autocraft some stuff
        var craftingService = grid.getCraftingService();
        var toAutoCraft = new LinkedHashMap<AEKey, Long>();

        // Handle each slot
        for (var x = 0; x < craftMatrix.size(); x++) {
            var currentItem = craftMatrix.getStackInSlot(x);
            var ingredient = ingredients.get(x);

            // Move out items blocking the grid
            if (!currentItem.isEmpty()) {
                // Put away old item, if not correct
                if (ingredient.test(currentItem)) {
                    // Grid already has an item that matches the ingredient
                    continue;
                } else {
                    if (security.hasPermission(player, SecurityPermissions.INJECT)) {
                        var in = AEItemKey.of(currentItem);
                        var inserted = StorageHelper.poweredInsert(energy, storage, in, currentItem.getCount(),
                                cct.getActionSource());
                        if (inserted < currentItem.getCount()) {
                            currentItem = currentItem.copy();
                            currentItem.shrink((int) inserted);
                        } else {
                            currentItem = ItemStack.EMPTY;
                        }
                    }
                    // If more is remaining, try moving it to the player inventory
                    player.getInventory().add(currentItem);

                    craftMatrix.setItemDirect(x, currentItem.isEmpty() ? ItemStack.EMPTY : currentItem);
                }
            }

            if (ingredient.isEmpty()) {
                continue;
            }

            // Try to find the best item for this slot. Sort by the amount available in the last tick,
            // then try to extract from most to least available item until 1 can be extracted.
            if (currentItem.isEmpty() && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
                var request = findBestMatchingItemStack(ingredient, filter, cachedStorage);
                for (var what : request) {
                    var extracted = StorageHelper.poweredExtraction(energy, storage, what, 1, cct.getActionSource());
                    if (extracted > 0) {
                        currentItem = what.toStack(Ints.saturatedCast(extracted));
                        break;
                    }
                }
            }

            // If still nothing, try taking it from the player inventory
            if (currentItem.isEmpty()) {
                currentItem = takeIngredientFromPlayer(player, ingredient);
            }

            craftMatrix.setItemDirect(x, currentItem);

            // If we couldn't find the item, schedule its autocrafting
            if (currentItem.isEmpty() && craftMissing) {
                findCraftableKey(ingredient, craftingService).ifPresent(key -> {
                    toAutoCraft.merge(key, 1L, Long::sum);
                });
            }
        }
        menu.slotsChanged(craftMatrix.toContainer());
        if (!toAutoCraft.isEmpty()) {
            // This must be the last call since it changes the menu!
            var stacks = toAutoCraft.entrySet().stream().map(e -> new GenericStack(e.getKey(), e.getValue())).toList();
            cct.startAutoCrafting(stacks);
        }
    }

    private ItemStack takeIngredientFromPlayer(ServerPlayer player, Ingredient ingredient) {
        var playerInv = player.getInventory();
        for (int i = 0; i < playerInv.items.size(); i++) {
            var item = playerInv.getItem(i);
            if (ingredient.test(item)) {
                var result = item.split(1);
                if (!result.isEmpty()) {
                    return result;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private NonNullList<Ingredient> getDesiredIngredients(Player player) {
        // Try to retrieve the real recipe on the server-side
        if (this.recipeId != null) {
            var recipe = player.getLevel().getRecipeManager().byKey(this.recipeId).orElse(null);
            if (recipe != null) {
                return ExtendedCraftingRecipeUtil.ensureExtendedCraftingMatrix(recipe,GRID_SIZE);
            }
        }

        // If the recipe is unavailable for any reason, use the templates provided by the client
        var ingredients = NonNullList.withSize(GRID_MATRIX, Ingredient.EMPTY);
        Preconditions.checkArgument(ingredients.size() == this.ingredientTemplates.size(),
                "Got %d ingredient templates from client, expected %d",
                ingredientTemplates.size(), ingredients.size());
        for (int i = 0; i < ingredients.size(); i++) {
            var template = ingredientTemplates.get(i);
            if (!template.isEmpty()) {
                ingredients.set(i, Ingredient.of(template));
            }
        }

        return ingredients;
    }

    /**
     * From a stream of AE item stacks, pick the one with the highest available amount in the network. Returns null if
     * the stream is empty.
     * <p/>
     * We normalize the stored amount vs. the amount needed for the recipe. While this is irrelevant for crafting
     * recipes, it can be relevant for processing patterns where an ingredient doesn't always have amount=1.
     *
     * <pre>
     * Example:
     * Recipe asks for 16xItem A or 1xItem B.
     * Storage has 16xItem A and 15xItem B.
     * Item B should have priority because the recipe could be crafted 15x, while with item A it could only be crafted
     * once.
     * </pre>
     */
    private List<AEItemKey> findBestMatchingItemStack(Ingredient ingredient, IPartitionList filter,
                                                      KeyCounter storage) {
        return Arrays.stream(ingredient.getItems())//
                .map(AEItemKey::of) //
                .filter(r -> r != null && (filter == null || filter.isListed(r)))
                .flatMap(s -> storage.findFuzzy(s, FuzzyMode.IGNORE_ALL).stream())//
                // While FuzzyMode.IGNORE_ALL will retrieve all stacks of the same Item which matches
                // standard Vanilla Ingredient matching, there are NBT-matching Ingredient subclasses on Forge,
                // and Mods might actually have mixed into Ingredient
                .filter(e -> ingredient.test(((AEItemKey) e.getKey()).toStack()))
                // Sort in descending order of availability
                .sorted((a, b) -> Long.compare(b.getLongValue(), a.getLongValue()))//
                .map(e -> (AEItemKey) e.getKey())//
                .toList();
    }

    private Optional<AEItemKey> findCraftableKey(Ingredient ingredient, ICraftingService craftingService) {
        return Arrays.stream(ingredient.getItems())//
                .map(AEItemKey::of)//
                .map(s -> (AEItemKey) craftingService.getFuzzyCraftable(s,
                        key -> ingredient.test(((AEItemKey) key).toStack())))//
                .filter(Objects::nonNull)//
                .findAny();//
    }
}
