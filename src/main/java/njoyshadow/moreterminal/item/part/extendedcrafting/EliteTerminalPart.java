package njoyshadow.moreterminal.item.part.extendedcrafting;

import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractTerminalPart;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import njoyshadow.moreterminal.container.extendedcrafting.BasicCraftingTerminalContainer;
import njoyshadow.moreterminal.container.extendedcrafting.EliteCraftingTerminalContainer;

import java.util.List;

public class EliteTerminalPart extends AbstractTerminalPart {
    @PartModels
    //public static final ResourceLocation MODEL_OFF = new ResourceLocation(Moreterminal.MOD_ID, "part/crafting_terminal_off");
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(AppEng.MOD_ID, "part/crafting_terminal_off");
    @PartModels
    //public static final ResourceLocation MODEL_ON = new ResourceLocation(Moreterminal.MOD_ID, "part/crafting_terminal_on");
    public static final ResourceLocation MODEL_ON = new ResourceLocation(AppEng.MOD_ID, "part/crafting_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 49);

    public EliteTerminalPart(final ItemStack is) {
        super(is);
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        super.getDrops(drops, wrenched);

        for (final ItemStack is : this.craftingGrid) {
            if (!is.isEmpty()) {
                drops.add(is);
            }
        }
    }

    @Override
    public void readFromNBT(final CompoundNBT data) {
        super.readFromNBT(data);

        this.craftingGrid.readFromNBT(data, "elite_crafting_grid");
    }

    @Override
    public void writeToNBT(final CompoundNBT data) {
        super.writeToNBT(data);
        this.craftingGrid.writeToNBT(data, "elite_crafting_grid");
    }

    @Override
    public ContainerType<?> getContainerType(final PlayerEntity p) {
        if (Platform.checkPermissions(p, this, SecurityPermissions.CRAFT, false)) {
            return EliteCraftingTerminalContainer.TYPE;
        }
        return EliteCraftingTerminalContainer.TYPE;
    }


    //todo fix
    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("crafting")) {
            //return this.craftingGrid;
            return this.craftingGrid;
        }
        return super.getInventoryByName(name);
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}

