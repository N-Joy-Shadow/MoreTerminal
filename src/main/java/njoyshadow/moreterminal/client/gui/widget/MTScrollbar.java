package njoyshadow.moreterminal.client.gui.widget;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.EventRepeater;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.Scrollbar;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import njoyshadow.moreterminal.api.client.gui.widget.IMTCompositeWidget;
import njoyshadow.moreterminal.client.gui.me.style.utils.MTBlitter;

import java.time.Duration;

public class MTScrollbar implements IScrollSource, IMTCompositeWidget {
    private static final int HANDLE_WIDTH = 12;
    private static final int HANDLE_HEIGHT = 15;
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tabs.png");
    private static final Blitter ENABLED;
    private static final Blitter DISABLED;
    private int displayX = 0;
    private int displayY = 0;
    private int width = 12;
    private int height = 16;
    private int pageSize = 1;
    private int maxScroll = 0;
    private int minScroll = 0;
    private int currentScroll = 0;
    private boolean dragging;
    private int dragYOffset;
    private final EventRepeater eventRepeater = new EventRepeater(Duration.ofMillis(250L), Duration.ofMillis(150L));

    public MTScrollbar() {
    }

    public Rectangle2d getBounds() {
        return new Rectangle2d(this.displayX, this.displayY, this.width, this.height);
    }

    public void drawForegroundLayer(MatrixStack matrices, int zIndex, Rectangle2d bounds, Point mouse) {
        int yOffset;
        Blitter image;
        if (this.getRange() == 0) {
            yOffset = 0;
            image = DISABLED;
        } else {
            yOffset = this.getHandleYOffset();
            image = ENABLED;
        }

        image.dest(this.displayX, this.displayY + yOffset).blit(matrices, zIndex);
    }

    private int getHandleYOffset() {
        if (this.getRange() == 0) {
            return 0;
        } else {
            int availableHeight = this.height - 15;
            return (this.currentScroll - this.minScroll) * availableHeight / this.getRange();
        }
    }

    private int getRange() {
        return this.maxScroll - this.minScroll;
    }

    public MTScrollbar setHeight(int v) {
        this.height = v;
        return this;
    }

    public void setPosition(Point position) {
        this.displayX = position.getX();
        this.displayY = position.getY();
    }

    public void setSize(int width, int height) {
        if (width != 0) {
            this.width = width;
        }

        if (height != 0) {
            this.height = height;
        }

    }

    public void setRange(int min, int max, int pageSize) {
        this.minScroll = min;
        this.maxScroll = max;
        this.pageSize = pageSize;
        if (this.minScroll > this.maxScroll) {
            this.maxScroll = this.minScroll;
        }

        this.applyRange();
    }

    private void applyRange() {
        this.currentScroll = Math.max(Math.min(this.currentScroll, this.maxScroll), this.minScroll);
    }

    public int getCurrentScroll() {
        return this.currentScroll;
    }

    public boolean onMouseDown(Point mousePos, int button) {
        if (button != 0) {
            return false;
        } else {
            this.dragging = false;
            if (this.getRange() == 0) {
                return true;
            } else {
                int relY = mousePos.getY() - this.displayY;
                int handleYOffset = this.getHandleYOffset();
                if (relY < handleYOffset) {
                    this.pageUp();
                    this.eventRepeater.repeat(this::pageUp);
                } else if (relY < handleYOffset + 15) {
                    this.dragging = true;
                    this.dragYOffset = relY - handleYOffset;
                } else {
                    this.pageDown();
                    this.eventRepeater.repeat(this::pageDown);
                }

                return true;
            }
        }
    }

    public boolean onMouseUp(Point mousePos, int button) {
        if (button == 0) {
            this.dragging = false;
            this.eventRepeater.stop();
        }

        return false;
    }

    public boolean wantsAllMouseUpEvents() {
        return true;
    }

    public boolean onMouseDrag(Point mousePos, int button) {
        if (this.getRange() != 0 && this.dragging && !this.eventRepeater.isRepeating()) {
            double handleUpperEdgeY = (double)(mousePos.getY() - this.displayY - this.dragYOffset);
            double availableHeight = (double)(this.height - 15);
            double position = MathHelper.clamp(handleUpperEdgeY / availableHeight, 0.0D, 1.0D);
            this.currentScroll = this.minScroll + (int)Math.round(position * (double)this.getRange());
            this.applyRange();
            return true;
        } else {
            return false;
        }
    }

    public boolean onMouseWheel(Point mousePos, double delta) {
        if (this.getRange() == 0) {
            return false;
        } else {
            delta = Math.max(Math.min(-delta, 1.0D), -1.0D);
            this.currentScroll = (int)((double)this.currentScroll + delta * (double)this.pageSize);
            this.applyRange();
            return true;
        }
    }

    public boolean wantsAllMouseWheelEvents() {
        return true;
    }

    public void tick() {
        this.eventRepeater.tick();
    }

    private void pageUp() {
        this.currentScroll -= this.pageSize;
        this.applyRange();
    }

    private void pageDown() {
        this.currentScroll += this.pageSize;
        this.applyRange();
    }

    static {
        ENABLED = Blitter.texture(TEXTURE).src(232, 0, 12, 15);
        DISABLED = Blitter.texture(TEXTURE).src(244, 0, 12, 15);
    }
}