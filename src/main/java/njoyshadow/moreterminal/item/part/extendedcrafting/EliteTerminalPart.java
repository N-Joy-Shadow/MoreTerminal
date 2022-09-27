package njoyshadow.moreterminal.item.part.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.inventories.InternalInventory;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.menu.me.common.MEStorageMenu;
import appeng.parts.PartModel;
import appeng.util.Platform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import njoyshadow.moreterminal.Moreterminal;
import njoyshadow.moreterminal.menu.extendedcrafting.EliteCraftingTermMenu;

public class EliteTerminalPart extends BaseExtendCraftingTermPart{
    public EliteTerminalPart(IPartItem<?> partItem) {
        super(partItem, 7);
    }

    @PartModels
    protected static ResourceLocation MT_MODEL_BASE = new ResourceLocation(Moreterminal.MOD_ID,"part/extendedcrafting/elite/display_base");

    public static final IPartModel MT_MODELS_OFF = new PartModel(MT_MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MT_MODELS_ON = new PartModel(MT_MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MT_MODELS_HAS_CHANNEL = new PartModel(MT_MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    @Override
    public MenuType<?> getMenuType(Player p) {
        if (Platform.checkPermissions(p, this, SecurityPermissions.CRAFT, false, false)) {
            return EliteCraftingTermMenu.TYPE;
        }
        return MEStorageMenu.TYPE;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ELITE_INV_CRAFTING)) {
            return craftingGrid;
        } else {
            return super.getSubInventory(id);
        }
    }
    public IPartModel getStaticModels(){
        return this.selectModel(MT_MODELS_OFF,MT_MODELS_ON,MT_MODELS_HAS_CHANNEL);
    }
}