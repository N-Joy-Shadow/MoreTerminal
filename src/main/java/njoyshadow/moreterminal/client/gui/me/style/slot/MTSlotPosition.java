package njoyshadow.moreterminal.client.gui.me.style.slot;

import appeng.client.gui.layout.SlotGridLayout;
import appeng.client.gui.style.Position;
import njoyshadow.moreterminal.client.gui.me.style.grid.MTSlotGridLayout;

import javax.annotation.Nullable;

public class MTSlotPosition  extends Position {
    @Nullable
    private MTSlotGridLayout grid;

    public MTSlotPosition() {
    }

    @Nullable
    public MTSlotGridLayout getGrid() {
        return this.grid;
    }

    public void setGrid(@Nullable MTSlotGridLayout grid) {
        this.grid = grid;
    }

    public String toString() {
        String result = super.toString();
        return this.grid != null ? result + "grid=" + this.grid : result;
    }
}