package njoyshadow.moreterminal.item.part.terminalPart;

import appeng.items.parts.PartModels;
import appeng.parts.reporting.AbstractReportingPart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import njoyshadow.moreterminal.Moreterminal;

public class MTAbstractDisplayPart extends AbstractReportingPart {
    @PartModels
    protected static final ResourceLocation MODEL_BASE = new ResourceLocation(Moreterminal.MOD_ID, "part/display_base");
    @PartModels
    protected static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation(Moreterminal.MOD_ID, "part/display_status_off");
    @PartModels
    protected static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation(Moreterminal.MOD_ID, "part/display_status_on");
    @PartModels
    protected static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation(Moreterminal.MOD_ID, "part/display_status_has_channel");

    public MTAbstractDisplayPart(ItemStack is) {
        super(is, true);
    }

    public boolean isLightSource() {
        return false;
    }
}
