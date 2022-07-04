package njoyshadow.moreterminal.client.gui.style;

import appeng.client.Point;

import javax.annotation.Nullable;

public class MTTerminalStyle {
    private MTBlitter header;

    /**
     * The area to draw for the first row in the terminal. Usually this includes the top of the scrollbar.
     */
    private MTBlitter firstRow;

    /**
     * The area to repeat for every row in the terminal. Should be 16px for the item + 2px for the border in size.
     */
    private MTBlitter row;

    /**
     * The area to draw for the last row in the terminal. Usually this includes the top of the scrollbar.
     */
    private MTBlitter lastRow;

    /**
     * The area to draw at the bottom of the terminal (i.e. includes the player inventory)
     */
    private MTBlitter bottom;

    /**
     * If specified, limits the terminal to at most this many rows rather than using up available space.
     */
    private Integer maxRows;

    /**
     * Currently only 9 is supported here.
     */
    private int slotsPerRow;

    private boolean sortable = true;

    private boolean supportsAutoCrafting = false;

    /**
     * Should the terminal show item tooltips for the network inventory even if the player has an item in their hand?
     * Useful for showing fluid tooltips when the player has a bucket in hand.
     */
    private boolean showTooltipsWithItemInHand;

    public MTBlitter getHeader() {
        return header;
    }

    public void setHeader(MTBlitter header) {
        this.header = header;
    }

    public MTBlitter getFirstRow() {
        return firstRow;
    }

    public void setFirstRow(MTBlitter firstRow) {
        this.firstRow = firstRow;
    }

    public MTBlitter getRow() {
        return row;
    }

    public void setRow(MTBlitter row) {
        this.row = row;
    }

    public MTBlitter getLastRow() {
        return lastRow;
    }

    public void setLastRow(MTBlitter lastRow) {
        this.lastRow = lastRow;
    }

    public MTBlitter getBottom() {
        return bottom;
    }

    public void setBottom(MTBlitter bottom) {
        this.bottom = bottom;
    }

    public void setMaxRows(Integer maxRows) {
        this.maxRows = maxRows;
    }

    public int getSlotsPerRow() {
        return slotsPerRow;
    }

    public void setSlotsPerRow(int slotsPerRow) {
        this.slotsPerRow = slotsPerRow;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public int getScreenWidth() {
        // Calculate a bounding box for the screen
        int screenWidth = header.getSrcWidth();
        screenWidth = Math.max(screenWidth, firstRow.getSrcWidth());
        screenWidth = Math.max(screenWidth, row.getSrcWidth());
        screenWidth = Math.max(screenWidth, lastRow.getSrcWidth());
        screenWidth = Math.max(screenWidth, bottom.getSrcWidth());
        return screenWidth;
    }

    public int getPossibleRows(int availableHeight) {
        return (availableHeight - header.getSrcHeight()
                - bottom.getSrcHeight()) / row.getSrcHeight();
    }

    /**
     * Gets the position of one of the network grid slots. The returned position is within the slots 1px border.
     */
    public Point getSlotPos(int row, int col) {
        int x = 7 + col * 18;

        int y = header.getSrcHeight();
        if (row > 0) {
            y += firstRow.getSrcHeight();
            y += (row - 1) * this.row.getSrcHeight();
        }

        // +1 is the margin between the bounding box of the slot and the real minecraft slot. this is due to the border
        return new Point(x, y).move(1, 1);
    }

    /**
     * @return The number of rows this terminal should display (at most). If null, the player's chosen terminal style
     *         determines the number of rows.
     */
    @Nullable
    public Integer getMaxRows() {
        return maxRows;
    }

    /**
     * Calculates the height of the screen given a number of rows to display.
     */
    public int getScreenHeight(int rows) {
        int result = header.getSrcHeight();
        result += firstRow.getSrcHeight();
        result += Math.max(0, rows - 2) * row.getSrcHeight();
        result += lastRow.getSrcHeight();
        result += bottom.getSrcHeight();
        return result;
    }

    public boolean isSupportsAutoCrafting() {
        return supportsAutoCrafting;
    }

    public void setSupportsAutoCrafting(boolean supportsAutoCrafting) {
        this.supportsAutoCrafting = supportsAutoCrafting;
    }

    public boolean isShowTooltipsWithItemInHand() {
        return showTooltipsWithItemInHand;
    }

    public void setShowTooltipsWithItemInHand(boolean showTooltipsWithItemInHand) {
        this.showTooltipsWithItemInHand = showTooltipsWithItemInHand;
    }

    public void validate() {
        if (header == null) {
            throw new RuntimeException("terminalStyle.header is missing");
        }
        if (firstRow == null) {
            throw new RuntimeException("terminalStyle.firstRow is missing");
        }
        if (row == null) {
            throw new RuntimeException("terminalStyle.row is missing");
        }
        if (lastRow == null) {
            throw new RuntimeException("terminalStyle.lastRow is missing");
        }
        if (bottom == null) {
            throw new RuntimeException("terminalStyle.bottom is missing");
        }
    }
}
