package njoyshadow.moreterminal.client.gui.me.style;

import appeng.client.Point;
import appeng.client.gui.me.common.StackSizeRenderer;
import appeng.client.gui.me.fluids.FluidStackSizeRenderer;
import appeng.client.gui.style.StackSizeStyle;
import net.minecraft.client.renderer.Rectangle2d;
import njoyshadow.moreterminal.client.gui.me.style.utils.MTBlitter;

import javax.annotation.Nullable;

public class MTTerminalStyle {
    private MTBlitter header;
    private MTBlitter firstRow;
    private MTBlitter row;
    private MTBlitter lastRow;
    private MTBlitter bottom;
    private Integer maxRows;
    private int slotsPerRow;
    private Rectangle2d searchFieldRect;
    private boolean sortable = true;
    private boolean supportsAutoCrafting = false;
    private StackSizeStyle stackSizeStyle;
    private boolean showTooltipsWithItemInHand;

    public MTTerminalStyle() {
        this.stackSizeStyle = StackSizeStyle.ITEMS;
    }

    public MTBlitter getHeader() {
        return this.header;
    }

    public void setHeader(MTBlitter header) {
        this.header = header;
    }

    public MTBlitter getFirstRow() {
        return this.firstRow;
    }

    public void setFirstRow(MTBlitter firstRow) {
        this.firstRow = firstRow;
    }

    public MTBlitter getRow() {
        return this.row;
    }

    public void setRow(MTBlitter row) {
        this.row = row;
    }

    public MTBlitter getLastRow() {
        return this.lastRow;
    }

    public void setLastRow(MTBlitter lastRow) {
        this.lastRow = lastRow;
    }

    public MTBlitter getBottom() {
        return this.bottom;
    }

    public void setBottom(MTBlitter bottom) {
        this.bottom = bottom;
    }

    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }

    public int getSlotsPerRow() {
        return this.slotsPerRow;
    }

    public void setSlotsPerRow(int slotsPerRow) {
        this.slotsPerRow = slotsPerRow;
    }

    public void setSearchFieldRect(Rectangle2d searchFieldRect) {
        this.searchFieldRect = searchFieldRect;
    }

    public boolean isSortable() {
        return this.sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public int getScreenWidth() {
        int screenWidth = this.header.getSrcWidth();
        screenWidth = Math.max(screenWidth, this.firstRow.getSrcWidth());
        screenWidth = Math.max(screenWidth, this.row.getSrcWidth());
        screenWidth = Math.max(screenWidth, this.lastRow.getSrcWidth());
        screenWidth = Math.max(screenWidth, this.bottom.getSrcWidth());
        return screenWidth;
    }

    public int getPossibleRows(int availableHeight) {
        return (availableHeight - this.header.getSrcHeight() - this.bottom.getSrcHeight()) / this.row.getSrcHeight();
    }

    public Point getSlotPos(int row, int col) {
        int x = 8 + col * 18;
        int y = this.header.getSrcHeight();
        if (row > 0) {
            y += this.firstRow.getSrcHeight();
            y += (row - 1) * this.row.getSrcHeight();
        }

        return (new Point(x, y)).move(1, 1);
    }

    public Rectangle2d getSearchFieldRect() {
        return this.searchFieldRect;
    }

    @Nullable
    public Integer getMaxRows() {
        return this.maxRows;
    }

    public int getScreenHeight(int rows) {
        int result = this.header.getSrcHeight();
        result += this.firstRow.getSrcHeight();
        result += Math.max(0, rows - 2) * this.row.getSrcHeight();
        result += this.lastRow.getSrcHeight();
        result += this.bottom.getSrcHeight();
        return result;
    }

    public boolean isSupportsAutoCrafting() {
        return this.supportsAutoCrafting;
    }

    public void setSupportsAutoCrafting(boolean supportsAutoCrafting) {
        this.supportsAutoCrafting = supportsAutoCrafting;
    }

    public boolean isShowTooltipsWithItemInHand() {
        return this.showTooltipsWithItemInHand;
    }

    public void setShowTooltipsWithItemInHand(boolean showTooltipsWithItemInHand) {
        this.showTooltipsWithItemInHand = showTooltipsWithItemInHand;
    }

    public StackSizeStyle getStackSizeStyle() {
        return this.stackSizeStyle;
    }

    public void setStackSizeStyle(StackSizeStyle stackSizeStyle) {
        this.stackSizeStyle = stackSizeStyle;
    }

    public StackSizeRenderer getStackSizeRenderer() {
        switch(this.stackSizeStyle) {
            case ITEMS:
            default:
                return new StackSizeRenderer();
            case FLUIDS:
                return new FluidStackSizeRenderer();
        }
    }

    public void validate() {
        if (this.header == null) {
            throw new RuntimeException("terminalStyle.header is missing");
        } else if (this.firstRow == null) {
            throw new RuntimeException("terminalStyle.firstRow is missing");
        } else if (this.row == null) {
            throw new RuntimeException("terminalStyle.row is missing");
        } else if (this.lastRow == null) {
            throw new RuntimeException("terminalStyle.lastRow is missing");
        } else if (this.bottom == null) {
            throw new RuntimeException("terminalStyle.bottom is missing");
        } else if (this.searchFieldRect == null) {
            throw new RuntimeException("terminalStyle.searchFieldRect is missing");
        } else if (this.stackSizeStyle == null) {
            throw new RuntimeException("terminalStyle.stackSizeStyle is missing");
        }
    }
}