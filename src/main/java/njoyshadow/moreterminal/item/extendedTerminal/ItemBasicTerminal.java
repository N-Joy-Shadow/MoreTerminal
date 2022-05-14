package njoyshadow.moreterminal.item.extendedTerminal;


import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPartModel;
import appeng.container.me.items.CraftingTermContainer;
import appeng.container.me.items.ItemTerminalContainer;
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
import njoyshadow.moreterminal.Moreterminal;

import java.util.List;

public class ItemBasicTerminal extends AbstractTerminalPart {
    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(Moreterminal.MOD_ID, "part/crafting_terminal_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(Moreterminal.MOD_ID, "part/crafting_terminal_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    private final AppEngInternalInventory craftingGrid = new AppEngInternalInventory(this, 9);

    public ItemBasicTerminal(final ItemStack is) {
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
        this.craftingGrid.readFromNBT(data, "craftingGrid");
    }

    @Override
    public void writeToNBT(final CompoundNBT data) {
        super.writeToNBT(data);
        this.craftingGrid.writeToNBT(data, "craftingGrid");
    }

    @Override
    public ContainerType<?> getContainerType(final PlayerEntity p) {
        if (Platform.checkPermissions(p, this, SecurityPermissions.CRAFT, false)) {
            return CraftingTermContainer.TYPE;
        }
        return ItemTerminalContainer.TYPE;
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("crafting")) {
            return this.craftingGrid;
        }
        return super.getInventoryByName(name);
    }

    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }




}