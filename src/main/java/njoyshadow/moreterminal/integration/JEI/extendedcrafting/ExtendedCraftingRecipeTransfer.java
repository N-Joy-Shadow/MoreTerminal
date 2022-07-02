package njoyshadow.moreterminal.integration.JEI.extendedcrafting;

import appeng.api.stacks.AEItemKey;
import appeng.core.AELog;
import appeng.core.localization.ItemModText;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.util.CraftingRecipeUtil;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.blakebr0.extendedcrafting.api.crafting.RecipeTypes;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapedTableRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapelessTableRecipe;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import njoyshadow.moreterminal.integration.JEI.ExtendedCraftingRecipeHandler;
import njoyshadow.moreterminal.menu.extendedcrafting.BaseCraftingTermMenu;
import njoyshadow.moreterminal.network.handler.MTNetworkHandler;
import njoyshadow.moreterminal.network.packet.ExtendedCraftingPacket.ExtendedCraftingPacket;
import njoyshadow.moreterminal.utils.crafting.ExtendedCraftingRecipeUtil;

import java.util.*;


public class ExtendedCraftingRecipeTransfer<T extends BaseCraftingTermMenu>
        extends ExtendedCraftingRecipeHandler
        implements IRecipeTransferHandler<T, ITableRecipe> {

    private final Class<T> containerClass;
    private final IRecipeTransferHandlerHelper helper;
    
    // Colors for the slot highlights
    private static final int BLUE_SLOT_HIGHLIGHT_COLOR = 0x400000ff;
    private static final int RED_SLOT_HIGHLIGHT_COLOR = 0x66ff0000;
    // Colors for the buttons
    private static final int BLUE_PLUS_BUTTON_COLOR = 0x804545FF;
    private static final int ORANGE_PLUS_BUTTON_COLOR = 0x80FFA500;

    private IDrawable req;

    private static final Comparator<GridInventoryEntry> ENTRY_COMPARATOR = Comparator
            .comparing(GridInventoryEntry::getStoredAmount);

    public ExtendedCraftingRecipeTransfer(Class<T> containerClass, IRecipeTransferHandlerHelper helper,int GridSize) {
        super(GridSize);
        this.containerClass = containerClass;
        this.helper = helper;
    }
    @Override
    public IRecipeTransferError transferRecipe(T menu, ITableRecipe recipe, IRecipeLayout display, Player player,
                                               boolean maxTransfer, boolean doTransfer) {
        if (recipe.getType() != RecipeTypes.TABLE) {
            return helper.createInternalError();
        }

        if (recipe.getIngredients().isEmpty()) {
            return helper.createUserErrorWithTooltip(ItemModText.INCOMPATIBLE_RECIPE.text());
        }

        if (!recipe.canCraftInDimensions(GRID_WIDTH, GRID_HEIGHT)) {
            return helper.createUserErrorWithTooltip(ItemModText.RECIPE_TOO_LARGE.text());
        }

        // Thank you RS for pioneering this amazing feature! :)
        boolean craftMissing = AbstractContainerScreen.hasControlDown();
        // Find missing ingredient
        var slotToIngredientMap = getGuiSlotToIngredientMap(recipe);
        var missingSlots = menu.findMissingIngredients(getGuiSlotToIngredientMap(recipe));

        if (missingSlots.missingSlots().size() == slotToIngredientMap.size()) {
            // All missing, can't do much...
            return helper.createUserErrorForSlots(ItemModText.NO_ITEMS.text(), missingSlots.missingSlots());
        }

        // Find missing ingredients and highlight the slots which have these
        if (!doTransfer) {
            if (missingSlots.totalSize() != 0) {
                // Highlight the slots with missing ingredients.
                return new ErrorRenderer(menu, recipe);
            }
        } else {
            System.out.println("PerformStart");
            performTransfer(menu, recipe, craftMissing);
        }
        // No error
        return null;
    }

    protected void performTransfer(T menu, Recipe<?> recipe, boolean craftMissing) {

        // We send the items in the recipe in any case to serve as a fallback in case the recipe is transient
        var templateItems = findGoodTemplateItems(recipe, menu);

        var recipeId = recipe.getId();
        // Don't transmit a recipe id to the server in case the recipe is not actually resolvable
        // this is the case for recipes synthetically generated for JEI
        if (menu.getPlayer().level.getRecipeManager().byKey(recipe.getId()).isEmpty()) {
            AELog.debug("Cannot send recipe id %s to server because it's transient", recipeId);
            recipeId = null;
        }
        System.out.println("PerformComplete and sending Packet");
        MTNetworkHandler.instance()
                .sendToServer(new ExtendedCraftingPacket(recipeId, templateItems, craftMissing,GRID_HEIGHT));
    }

    private NonNullList<ItemStack> findGoodTemplateItems(Recipe<?> recipe, MEStorageMenu menu) {
        var ingredientPriorities = getIngredientPriorities(menu, ENTRY_COMPARATOR);

        var templateItems = NonNullList.withSize(GRID_MATRIX, ItemStack.EMPTY);
        var ingredients = ExtendedCraftingRecipeUtil.ensureExtendedCraftingMatrix(recipe,this.GRID_HEIGHT);
        for (int i = 0; i < ingredients.size(); i++) {
            var ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                // Try to find the best item. In case the ingredient is a tag, it might contain versions the
                // player doesn't actually have
                var stack = ingredientPriorities.entrySet()
                        .stream()
                        .filter(e -> e.getKey() instanceof AEItemKey itemKey && ingredient.test(itemKey.toStack()))
                        .max(Comparator.comparingInt(Map.Entry::getValue))
                        .map(e -> ((AEItemKey) e.getKey()).toStack())
                        .orElse(ingredient.getItems()[0]);

                templateItems.set(i, stack);
            }
        }
        return templateItems;
    }

    private static Map<Integer, Ingredient> getGuiSlotToIngredientMap(Recipe<?> recipe) {
        var ingredients = recipe.getIngredients();

        // JEI will align non-shaped recipes smaller than 3x3 in the grid. It'll center them horizontally, and
        // some will be aligned to the bottom. (i.e. slab recipes).
        int width , height;
        if (recipe instanceof ShapedTableRecipe shapedRecipe) {
            width = shapedRecipe.getWidth();
            height = shapedRecipe.getHeight();
        }
        else {
            if(recipe instanceof ShapelessTableRecipe shapelessRecipe)
            {
                int Tier = shapelessRecipe.getTier();
                System.out.println("Tier : " + Tier);
                switch (Tier){
                    case 1:

                        break;
                }
            }
            int Size = ingredients.size();
            if(Size > 49){
                width = 9;
                height = 9;
            }else if(Size > 25){
                width = 7;
                height = 7;
            }else if(Size > 9){
                width = 5;
                height =5;
            }
            else{
                width = 3;
                height = 3;
            }
        }

        //TODO FIX LATER
        /*else {
            if (ingredients.size() > 4) {
                width = height = 3;
            } else if (ingredients.size() > 1) {
                width = height = 2;
            } else {
                width = height = 1;
            }
        }*/

        var result = new HashMap<Integer, Ingredient>(ingredients.size());
        for (int i = 0; i < ingredients.size(); i++) {
            // JEI uses slot 0 for the output by default, shifting all input slots by 1
            var guiSlot = 1 + getCraftingIndex(i, width, height);
            var ingredient = ingredients.get(i);
            if (!ingredient.isEmpty()) {
                result.put(guiSlot, ingredient);
            }
        }
        return result;
    }

    private static int getCraftingIndex(int i, int width, int height) {
        int index;
        if (width == 1) {
            if (height == 3) {
                index = (i * 3) + 1;
            } else if (height == 2) {
                index = (i * 3) + 1;
            } else {
                index = 4;
            }
        } else if (height == 1) {
            index = i + 3;
        } else if (width == 2) {
            index = i;
            if (i > 1) {
                index++;
                if (i > 3) {
                    index++;
                }
            }
        } else if (height == 2) {
            index = i + 3;
        } else {
            index = i;
        }
        return index;
    }

    @Override
    public Class<T> getContainerClass() {
        return containerClass;
    }

    @Override
    public Class<ITableRecipe> getRecipeClass() {
        return ITableRecipe.class;
    }

    private record ErrorRenderer(BaseCraftingTermMenu menu, Recipe<?> recipe) implements IRecipeTransferError {
        @Override
        public Type getType() {
            return Type.COSMETIC;
        }

        @Override
        public void showError(PoseStack poseStack, int mouseX, int mouseY, IRecipeLayout recipeLayout, int recipeX,
                              int recipeY) {
            // This needs to be recomputed every time since JEI reuses the error renderer.
            boolean craftMissing = AbstractContainerScreen.hasControlDown();
            var missingSlots = menu.findMissingIngredients(getGuiSlotToIngredientMap(recipe));

            List<Component> extraTooltip = new ArrayList<>();
            if (missingSlots.anyCraftable()) {
                if (craftMissing) {
                    extraTooltip.add(ItemModText.WILL_CRAFT.text().withStyle(ChatFormatting.BLUE));
                } else {
                    extraTooltip.add(ItemModText.CTRL_CLICK_TO_CRAFT.text().withStyle(ChatFormatting.BLUE));
                }
            }
            if (missingSlots.anyMissing()) {
                extraTooltip.add(ItemModText.MISSING_ITEMS.text().withStyle(ChatFormatting.RED));
            }

            // 1) draw slot highlights
            IGuiItemStackGroup itemStackGroup = recipeLayout.getItemStacks();
            var ingredients = itemStackGroup.getGuiIngredients();
            for (var entry : ingredients.entrySet()) {
                int i = entry.getKey();
                boolean missing = missingSlots.missingSlots().contains(i);
                boolean craftable = missingSlots.craftableSlots().contains(i);
                if (missing || craftable) {
                    entry.getValue().drawHighlight(poseStack,
                            missing ? RED_SLOT_HIGHLIGHT_COLOR : BLUE_SLOT_HIGHLIGHT_COLOR, recipeX, recipeY);
                }
            }
            // 2) draw tooltip
            drawHoveringText(poseStack, extraTooltip, mouseX, mouseY);
        }

        // Copy-pasted from JEI since it doesn't seem to expose these
        public static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y) {
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;
            drawHoveringText(poseStack, textLines, x, y, ItemStack.EMPTY, font);
        }

        private static void drawHoveringText(PoseStack poseStack, List<Component> textLines, int x, int y,
                                             ItemStack itemStack, Font font) {
            Minecraft minecraft = Minecraft.getInstance();
            Screen screen = minecraft.screen;
            if (screen == null) {
                return;
            }

            Optional<TooltipComponent> tooltipImage = itemStack.getTooltipImage();
            screen.renderTooltip(poseStack, textLines, tooltipImage, x, y, font, itemStack);
        }
    }
}