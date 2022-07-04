package njoyshadow.moreterminal.client.gui.style;

import appeng.client.gui.layout.SlotGridLayout;
import appeng.client.gui.style.Position;
import njoyshadow.moreterminal.client.gui.layout.MTSlotGridLayout;

import javax.annotation.Nullable;

public class MTSlotPosition extends Position {
    @Nullable
    private MTSlotGridLayout grid;

    @Nullable
    public MTSlotGridLayout getGrid() {
        return grid;
    }

    public void setGrid(@Nullable MTSlotGridLayout grid) {
        this.grid = grid;
    }

    @Override
    public String toString() {
        String result = super.toString();
        return grid != null ? result + "grid=" + grid : result;
    }
}
