package njoyshadow.moreterminal.item.part.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.InternalInventory;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.Platform;
import appeng.util.inv.AppEngInternalInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import njoyshadow.moreterminal.Moreterminal;


import java.sql.Array;
import java.util.List;

public class BaseExtendCraftingTermPart extends AbstractTerminalPart {
    public final AppEngInternalInventory craftingGrid;
    public BaseExtendCraftingTermPart(IPartItem<?> partItem, int GridSize) {
        super(partItem);

        this.craftingGrid = new AppEngInternalInventory(this,GridSize * GridSize);
    }

    /**
     * A sub-inventory that contains crafting ingredients used in the crafting grid.
     */
    public static final ResourceLocation BASIC_INV_CRAFTING = Moreterminal.MakeID("basic_crafting_terminal");
    public static final ResourceLocation ADVANCED_INV_CRAFTING = Moreterminal.MakeID("advanced_crafting_terminal");
    public static final ResourceLocation ELITE_INV_CRAFTING = Moreterminal.MakeID("elite_crafting_terminal");
    public static final ResourceLocation ULTIMATE_INV_CRAFTING = Moreterminal.MakeID("ultimate_crafting_terminal");

    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(AppEng.MOD_ID, "part/crafting_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(AppEng.MOD_ID, "part/crafting_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);




    @Override
    public void addAdditionalDrops(List<ItemStack> drops, boolean wrenched) {
        for (ItemStack is : this.craftingGrid) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.craftingGrid.readFromNBT(data, "craftingGrid");
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        this.craftingGrid.writeToNBT(data, "craftingGrid");
    }

    @Override
    public MenuType<?> getMenuType(Player p) {
        if (Platform.checkPermissions(p, this, SecurityPermissions.CRAFT, false, false)) {
            return MEStorageMenu.TYPE;
        }
        return MEStorageMenu.TYPE;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(BASIC_INV_CRAFTING)) {
            return craftingGrid;
        }
        else if(id.equals(ADVANCED_INV_CRAFTING)){
            return craftingGrid;
        }
        else if(id.equals(ELITE_INV_CRAFTING)){
            return craftingGrid;
        }
        else if(id.equals(ULTIMATE_INV_CRAFTING)){
            return craftingGrid;
        }
        else {
            return super.getSubInventory(id);
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

}
