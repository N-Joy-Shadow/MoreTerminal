package njoyshadow.moreterminal.integration.AppEng.sync;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.container.me.items.PatternTermContainer;
import appeng.core.Api;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.packets.JEIRecipePacket;
import appeng.helpers.IContainerCraftingPacket;
import appeng.items.storage.ViewCellItem;
import appeng.me.Grid;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.WrapperInvItemHandler;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;
import com.blakebr0.cucumber.crafting.ISpecialRecipe;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapedTableRecipe;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


public class JEIExtendedRecipePacket extends MTBasePacket {
    private static final int INLINE_RECIPE_NONE = 1;
    private static final int INLINE_RECIPE_SHAPED = 2;
    private ResourceLocation recipeId;
    @Nullable
    private IRecipe<?> recipe;
    private boolean crafting;
    public int GridSize;
    public JEIExtendedRecipePacket(PacketBuffer stream) {
        this.crafting = stream.readBoolean();
        String id = stream.readString(32767);
        this.recipeId = new ResourceLocation(id);
        int[] intData = stream.readVarIntArray();
        int inlineRecipeType = intData[0];
        this.GridSize = intData[1];
        switch (inlineRecipeType) {
            case INLINE_RECIPE_SHAPED:
                this.recipe = ShapedTableRecipe.Serializer.CRAFTING_SHAPED.read(this.recipeId, stream);
            case INLINE_RECIPE_NONE:
                return;
            default:
                throw new IllegalArgumentException("Invalid inline recipe type.");
        }
    }

    public JEIExtendedRecipePacket(ResourceLocation recipeId, boolean crafting, int GridSize) {
        this.GridSize = GridSize;
        PacketBuffer data = this.createCommonHeader(recipeId, crafting, INLINE_RECIPE_NONE, GridSize);
        this.configureWrite(data);
    }

    public JEIExtendedRecipePacket(ShapedRecipe recipe, boolean crafting, int GridSize) {
        this.GridSize = GridSize;
        PacketBuffer data = this.createCommonHeader(recipe.getId(), crafting, INLINE_RECIPE_SHAPED, GridSize);
        ShapedTableRecipe.Serializer.CRAFTING_SHAPED.write(data, recipe);
        this.configureWrite(data);
    }

    private PacketBuffer createCommonHeader(ResourceLocation recipeId, boolean crafting, int inlineRecipeType, int GridSize) {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        int IntData[] = {inlineRecipeType, GridSize};
        data.writeInt(this.getPacketID());
        data.writeBoolean(crafting);
        data.writeResourceLocation(recipeId);
        data.writeVarIntArray(IntData);
        return data;
    }

    public void serverPacketData(INetworkInfo manager, PlayerEntity player) {
        ServerPlayerEntity pmp = (ServerPlayerEntity) player;
        Container con = pmp.openContainer;
        Preconditions.checkArgument(con instanceof IContainerCraftingPacket);
        IRecipe<?> recipe = (IRecipe<?>) player.getEntityWorld().getRecipeManager().getRecipe(this.recipeId).orElse(null);
        if (recipe == null && this.recipe != null) {
            recipe = (ShapedTableRecipe) this.recipe;
        }

        Preconditions.checkArgument(recipe != null);

        IContainerCraftingPacket cct = (IContainerCraftingPacket) con;
        IGridNode node = cct.getNetworkNode();
        Preconditions.checkArgument(node != null);

        IGrid grid = node.getGrid();
        Preconditions.checkArgument(grid != null);

        IStorageGrid inv = (IStorageGrid) grid.getCache(IStorageGrid.class);

        Preconditions.checkArgument(inv != null);
        ISecurityGrid security = (ISecurityGrid) grid.getCache(ISecurityGrid.class);

        Preconditions.checkArgument(security != null);
        IEnergyGrid energy = (IEnergyGrid) grid.getCache(IEnergyGrid.class);
        ICraftingGrid crafting = (ICraftingGrid) grid.getCache(ICraftingGrid.class);
        IItemHandler craftMatrix = cct.getInventoryByName("crafting");
        IItemHandler playerInventory = cct.getInventoryByName("player");
        IMEMonitor<IAEItemStack> storage = inv.getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        IPartitionList<IAEItemStack> filter = ViewCellItem.createFilter(cct.getViewCells());
        NonNullList<Ingredient> ingredients = this.ensure3by3CraftingMatrix(recipe);



        for (int x = 0; x < craftMatrix.getSlots(); ++x) {
            ItemStack currentItem = craftMatrix.getStackInSlot(x);
            Ingredient ingredient = (Ingredient) ingredients.get(x);
            if (!currentItem.isEmpty()) {
                ItemStack newItem = this.canUseInSlot(ingredient, currentItem);
                if (newItem != currentItem && security.hasPermission(player, SecurityPermissions.INJECT)) {
                    IAEItemStack in = AEItemStack.fromItemStack(currentItem);
                    IAEItemStack out = cct.useRealItems() ? (IAEItemStack) Platform.poweredInsert(energy, storage, in, cct.getActionSource()) : null;
                    if (out != null) {
                        currentItem = out.createItemStack();
                    } else {
                        currentItem = ItemStack.EMPTY;
                    }
                }
            }

            if (currentItem.isEmpty() && security.hasPermission(player, SecurityPermissions.EXTRACT)) {
                Object out;
                if (cct.useRealItems()) {
                    IAEItemStack request = this.findBestMatchingItemStack(ingredient, filter, storage, cct);
                    out = request != null ? (IAEItemStack) Platform.poweredExtraction(energy, storage, request.setStackSize(1L), cct.getActionSource()) : null;
                } else {
                    out = this.findBestMatchingPattern(ingredient, filter, crafting, storage, cct);
                    if (out == null) {
                        out = this.findBestMatchingItemStack(ingredient, filter, storage, cct);
                    }

                    if (out == null && ingredient.getMatchingStacks().length > 0) {
                        out = AEItemStack.fromItemStack(ingredient.getMatchingStacks()[0]);
                    }
                }
                if (out != null) {
                    currentItem = ((IAEItemStack) out).createItemStack();
                }
            }
            if (currentItem.isEmpty()) {
                ItemStack[] matchingStacks = ingredient.getMatchingStacks();
                int matchingStackSize = matchingStacks.length;

                for (int SizeNum = 0; SizeNum < matchingStackSize; ++SizeNum) {
                    ItemStack matchingStack = matchingStacks[SizeNum];
                    if (currentItem.isEmpty()) {
                        AdaptorItemHandler ad = new AdaptorItemHandler(playerInventory);

                        if (cct.useRealItems()) {
                            currentItem = ad.removeItems(1, matchingStack, (IInventoryDestination) null);
                        } else {
                            currentItem = ad.simulateRemove(1, matchingStack, (IInventoryDestination) null);
                        }
                    }
                }
            }
            ItemHandlerUtil.setStackInSlot(craftMatrix, x, currentItem);
        }
        if (!this.crafting) {
            this.handleProcessing(con, cct, recipe);
        }
        con.onCraftMatrixChanged(new WrapperInvItemHandler(craftMatrix));
    }

    private NonNullList<Ingredient> ensure3by3CraftingMatrix(IRecipe<?> recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        NonNullList<Ingredient> expandedIngredients = NonNullList.withSize(GridSize * GridSize, Ingredient.EMPTY);
        Preconditions.checkArgument(ingredients.size() <= GridSize * GridSize);
        if (recipe instanceof ShapedTableRecipe) {
            ShapedTableRecipe shapedRecipe = (ShapedTableRecipe) recipe;
            int width = shapedRecipe.getWidth();
            int height = shapedRecipe.getHeight();
            Preconditions.checkArgument(width <= GridSize && height <= GridSize);
            for (int h = 0; h < height; ++h) {
                for (int w = 0; w < width; ++w) {
                    int source = w + h * width;
                    int target = w + h * GridSize;
                    Ingredient i = (Ingredient) ingredients.get(source);
                    expandedIngredients.set(target, i);
                }
            }
        } else {
            for (int i = 0; i < ingredients.size(); ++i) {
                expandedIngredients.set(i, ingredients.get(i));
            }
        }

        return expandedIngredients;
    }


    private ItemStack canUseInSlot(Ingredient ingredient, ItemStack is) {
        return (ItemStack) Arrays.stream(ingredient.getMatchingStacks()).filter((p) -> {
            return p.isItemEqual(is);
        }).findFirst().orElse(ItemStack.EMPTY);
    }

    private IAEItemStack findBestMatchingItemStack(Ingredient ingredients, IPartitionList<IAEItemStack> filter, IMEMonitor<IAEItemStack> storage, IContainerCraftingPacket cct) {
        Stream<AEItemStack> stacks = Arrays.stream(ingredients.getMatchingStacks()).map(AEItemStack::fromItemStack).filter((r) -> {
            if(r != null){
            }
            return r != null && (filter == null || filter.isListed(r));
        });
        return getMostStored(stacks, storage, cct);
    }

    private IAEItemStack findBestMatchingPattern(Ingredient ingredients, IPartitionList<IAEItemStack> filter, ICraftingGrid crafting, IMEMonitor<IAEItemStack> storage, IContainerCraftingPacket cct) {
        Stream<IAEItemStack> stacks = Arrays.stream(ingredients.getMatchingStacks()).map(AEItemStack::fromItemStack).filter((r) -> {
            return r != null && (filter == null || filter.isListed(r));
        }).map((s) -> {
            return (IAEItemStack) s.setCraftable(!crafting.getCraftingFor(s, (ICraftingPatternDetails) null, 0, (World) null).isEmpty());
        }).filter(IAEStack::isCraftable);
        return getMostStored(stacks, storage, cct);
    }

    private static IAEItemStack getMostStored(Stream<? extends IAEItemStack> stacks, IMEMonitor<IAEItemStack> storage, IContainerCraftingPacket cct) {
        return (IAEItemStack) stacks.map((s) -> {

            IAEItemStack stored = (IAEItemStack) storage.extractItems(s.copy().setStackSize(9223372036854775807L), Actionable.SIMULATE, cct.getActionSource());
            return Pair.of(s, stored != null ? stored.getStackSize() : 0L);
        }).min((left, right) -> {
            return Long.compare((Long) right.getSecond(), (Long) left.getSecond());
        }).map(Pair::getFirst).orElse(null);
    }

    private void handleProcessing(Container con, IContainerCraftingPacket cct, IRecipe<?> recipe) {
        if (con instanceof PatternTermContainer) {
            PatternTermContainer patternTerm = (PatternTermContainer) con;
            if (!patternTerm.craftingMode) {
                IItemHandler output = cct.getInventoryByName("output");
                ItemHandlerUtil.setStackInSlot(output, 0, recipe.getRecipeOutput());
                ItemHandlerUtil.setStackInSlot(output, 1, ItemStack.EMPTY);
                ItemHandlerUtil.setStackInSlot(output, 2, ItemStack.EMPTY);
            }
        }

    }
}