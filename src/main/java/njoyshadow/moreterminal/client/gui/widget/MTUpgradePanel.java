package njoyshadow.moreterminal.client.gui.widget;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Rects;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.container.slot.AppEngSlot;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import njoyshadow.moreterminal.api.client.gui.widget.IMTCompositeWidget;
import njoyshadow.moreterminal.client.gui.me.items.MTBaseScreen;
import njoyshadow.moreterminal.client.gui.me.style.utils.MTBlitter;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MTUpgradePanel implements IMTCompositeWidget {
    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 7;
    private static final int MAX_ROWS = 8;
    private static final MTBlitter BACKGROUND = MTBlitter.texture("guis/extra_panels.png", 128, 128);
    private static final MTBlitter INNER_CORNER;
    private final List<Slot> slots;
    private Point screenOrigin;
    private int x;
    private int y;
    private final Supplier<List<ITextComponent>> tooltipSupplier;

    public MTUpgradePanel(List<Slot> slots) {
        this(slots, Collections::emptyList);
    }

    public MTUpgradePanel(List<Slot> slots, Supplier<List<ITextComponent>> tooltipSupplier) {
        this.screenOrigin = Point.ZERO;
        this.slots = slots;
        this.tooltipSupplier = tooltipSupplier;
    }

    public void setPosition(Point position) {
        this.x = position.getX();
        this.y = position.getY();
    }

    public void setSize(int width, int height) {
    }

    public Rectangle2d getBounds() {
        int slotCount = this.getUpgradeSlotCount();
        int height = 14 + Math.min(8, slotCount) * 18;
        int width = 14 + (slotCount + 8 - 1) / 8 * 18;
        return new Rectangle2d(this.x, this.y, width, height);
    }

    public void populateScreen(Consumer<Widget> addWidget, Rectangle2d bounds, MTBaseScreen<?> screen) {
        this.screenOrigin = Point.fromTopLeft(bounds);
    }

    public void updateBeforeRender() {
        int slotOriginX = this.x + 7;
        int slotOriginY = this.y + 7;
        Iterator var3 = this.slots.iterator();

        while(var3.hasNext()) {
            Slot slot = (Slot) var3.next();
            if (slot.isEnabled()) {
                slot.xPos = slotOriginX + 1;
                slot.yPos = slotOriginY + 1;
                slotOriginY += 18;
            }
        }

    }

    public void drawBackgroundLayer(MatrixStack matrices, int zIndex, Rectangle2d bounds, Point mouse) {
        int slotCount = this.getUpgradeSlotCount();
        if (slotCount > 0) {
            int slotOriginX = this.screenOrigin.getX() + this.x + 7;
            int slotOriginY = this.screenOrigin.getY() + this.y + 7;

            for(int i = 0; i < slotCount; ++i) {
                int row = i % 8;
                int col = i / 8;
                int x = slotOriginX + col * 18;
                int y = slotOriginY + row * 18;
                boolean borderLeft = col == 0;
                boolean borderTop = row == 0;
                boolean lastSlot = i + 1 >= slotCount;
                boolean lastRow = row + 1 >= 8;
                boolean borderBottom = lastRow || lastSlot;
                boolean borderRight = i >= slotCount - 8;
                drawSlot(matrices, zIndex, x, y, borderLeft, borderTop, borderRight, borderBottom);
                if (col > 0 && lastSlot && !lastRow) {
                    INNER_CORNER.dest(x, y + 18).blit(matrices, zIndex);
                }
            }

        }
    }

    public void addExclusionZones(List<Rectangle2d> exclusionZones, Rectangle2d screenBounds) {
        int offsetX = screenBounds.getX();
        int offsetY = screenBounds.getY();
        int slotCount = this.getUpgradeSlotCount();
        boolean margin = true;
        int fullCols = slotCount / 8;
        int rightEdge = offsetX + this.x;
        int remaining;
        if (fullCols > 0) {
            remaining = 14 + fullCols * 18;
            exclusionZones.add(Rects.expand(new Rectangle2d(rightEdge, offsetY + this.y, remaining, 158), 2));
            rightEdge += remaining;
        }

        remaining = slotCount - fullCols * 8;
        if (remaining > 0) {
            exclusionZones.add(Rects.expand(new Rectangle2d(rightEdge, offsetY + this.y, 18 + (fullCols > 0 ? 0 : 14), 14 + remaining * 18), 2));
        }

    }

    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        if (this.getUpgradeSlotCount() == 0) {
            return null;
        } else {
            List<ITextComponent> tooltip = (List)this.tooltipSupplier.get();
            return tooltip.isEmpty() ? null : new Tooltip(tooltip);
        }
    }

    private static void drawSlot(MatrixStack matrices, int zIndex, int x, int y, boolean borderLeft, boolean borderTop, boolean borderRight, boolean borderBottom) {
        int srcX = 7;
        int srcY = 7;
        int srcWidth = 18;
        int srcHeight = 18;
        if (borderLeft) {
            x -= 7;
            srcX = 0;
            srcWidth += 7;
        }

        if (borderRight) {
            srcWidth += 7;
        }

        if (borderTop) {
            y -= 7;
            srcY = 0;
            srcHeight += 7;
        }

        if (borderBottom) {
            srcHeight += 7;
        }

        BACKGROUND.src(srcX, srcY, srcWidth, srcHeight).dest(x, y).blit(matrices, zIndex);
    }

    private int getUpgradeSlotCount() {
        int count = 0;
        Iterator var2 = this.slots.iterator();

        while(var2.hasNext()) {
            Slot slot = (Slot)var2.next();
            if (slot instanceof AppEngSlot && ((AppEngSlot)slot).isSlotEnabled()) {
                ++count;
            }
        }

        return count;
    }

    static {
        INNER_CORNER = BACKGROUND.copy().src(12, 33, 18, 18);
    }
}
