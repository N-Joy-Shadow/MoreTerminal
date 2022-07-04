package njoyshadow.moreterminal.client.gui.me.widget;

import appeng.client.Point;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.EventRepeater;
import appeng.client.gui.widgets.IScrollSource;
import appeng.core.AppEng;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import njoyshadow.moreterminal.api.client.gui.IMTCompositeWidget;

import java.time.Duration;

public class MTScrollbar implements IScrollSource, IMTCompositeWidget {

    private boolean visible = true;

    /**
     * The screen x-coordinate of the scrollbar's inner track.
     */
    private int displayX = 0;

    /**
     * The screen y-coordinate of the scrollbar's inner track.
     */
    private int displayY = 0;

    private final Style style;

    /**
     * The inner height of the scrollbar track.
     */
    private int height = 16;
    private int pageSize = 1;

    private int maxScroll = 0;
    private int minScroll = 0;
    private int currentScroll = 0;

    /**
     * True if the scrollbar's handle is currently being dragged.
     */
    private boolean dragging;
    /**
     * The y-coordinate relative to the upper edge of the scrollbar handle, where the user pressed the mouse button to
     * drag. While dragging, this is applied as an offset to the effective scrollbar position.
     */
    private int dragYOffset;

    /**
     * Capture all mouse wheel events to make it scroll when the mouse wheel is used anywhere on the screen.
     */
    private boolean captureMouseWheel = true;

    private final EventRepeater eventRepeater = new EventRepeater(Duration.ofMillis(250), Duration.ofMillis(150));

    public MTScrollbar(Style style) {
        this.style = style;
    }

    public MTScrollbar() {
        this(DEFAULT);
    }

    @Override
    public Rect2i getBounds() {
        return new Rect2i(displayX, displayY, style.handleWidth(), height);
    }

    /**
     * Draws the handle of the scrollbar.
     * <p>
     * The GUI is assumed to already contain a prebaked scrollbar track in its background.
     */
    @Override
    public void drawForegroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {
        // Draw the track (nice for debugging)
        // fill(poseStack, displayX, displayY, this.displayX + width, this.displayY +
        // height, 0xffff0000);

        int yOffset;
        Blitter image;
        if (this.getRange() == 0) {
            yOffset = 0;
            image = style.disabledBlitter();
        } else {
            yOffset = getHandleYOffset();
            image = style.enabledBlitter();
        }

        image.dest(this.displayX, this.displayY + yOffset).blit(poseStack, zIndex);
    }

    /**
     * Returns the y-position of the scrollbar handle in relation to the upper edge of the scrollbar's track.
     */
    private int getHandleYOffset() {
        if (getRange() == 0) {
            return 0;
        }
        int availableHeight = this.height - style.handleHeight();
        return (this.currentScroll - this.minScroll) * availableHeight / this.getRange();
    }

    private int getRange() {
        return this.maxScroll - this.minScroll;
    }

    public MTScrollbar setHeight(int v) {
        this.height = v;
        return this;
    }

    @Override
    public void setPosition(Point position) {
        this.displayX = position.getX();
        this.displayY = position.getY();
    }

    @Override
    public void setSize(int width, int height) {
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

    @Override
    public int getCurrentScroll() {
        return this.currentScroll;
    }

    @Override
    public boolean onMouseDown(Point mousePos, int button) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false; // Only handle left mouse button
        }

        this.dragging = false;

        // Do nothing when there's no range, but swallow the event
        if (getRange() == 0) {
            return true;
        }

        int relY = mousePos.getY() - displayY;

        int handleYOffset = getHandleYOffset();

        if (relY < handleYOffset) {
            // Clicks above the handle will page up, repeatedly
            pageUp();
            eventRepeater.repeat(this::pageUp);

        } else if (relY < handleYOffset + style.handleHeight()) {
            // Clicks on the handle will initiate dragging it
            this.dragging = true;
            this.dragYOffset = relY - handleYOffset;
        } else {
            // Clicks below the handle will page down, repeatedly
            pageDown();
            eventRepeater.repeat(this::pageDown);
        }

        return true;
    }

    @Override
    public boolean onMouseUp(Point mousePos, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            this.dragging = false;
            this.eventRepeater.stop();
        }
        return false;
    }

    @Override
    public boolean wantsAllMouseUpEvents() {
        // We need all mouse up events to properly stop dragging, since we don't have "real" mouse capture
        return true;
    }

    @Override
    public boolean onMouseDrag(Point mousePos, int button) {
        if (this.getRange() == 0 || !this.dragging || this.eventRepeater.isRepeating()) {
            return false;
        }

        // Compute the position of the mouse (adjusted for where it grabbed the handle,
        // so as if it grabbed
        // the upper edge of it) within the scrollable area of the track (minus the
        // handle height).
        double handleUpperEdgeY = mousePos.getY() - this.displayY - this.dragYOffset;
        double availableHeight = this.height - style.handleHeight();
        double position = Mth.clamp(handleUpperEdgeY / availableHeight, 0.0, 1.0);

        this.currentScroll = this.minScroll + (int) Math.round(position * this.getRange());
        this.applyRange();
        return true;
    }

    @Override
    public boolean onMouseWheel(Point mousePos, double delta) {
        // Do nothing when there's no range
        if (getRange() == 0) {
            return false;
        }

        delta = Math.max(Math.min(-delta, 1), -1);
        this.currentScroll += delta * this.pageSize;
        this.applyRange();
        return true;
    }

    @Override
    public boolean wantsAllMouseWheelEvents() {
        // Capture all mouse wheel events since we want to scroll even when over the item grid
        return captureMouseWheel;
    }

    public boolean isCaptureMouseWheel() {
        return captureMouseWheel;
    }

    public void setCaptureMouseWheel(boolean captureMouseWheel) {
        this.captureMouseWheel = captureMouseWheel;
    }

    /**
     * Ticks the scrollbar for the purposes of input-repeats (since mouse-downs are not repeat-triggered), used to
     * repeatedly page-up or page-down when the mouse is held in the area above or below the scrollbar handle.
     */
    @Override
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

    public static final Style DEFAULT = Style.create(
            new ResourceLocation("minecraft", "textures/gui/container/creative_inventory/tabs.png"),
            12,
            15,
            232, 0,
            244, 0);

    public static final Style SMALL = Style.create(
            AppEng.makeId("textures/guis/pattern_modes.png"),
            7,
            15,
            242, 0,
            249, 0);

    /**
     * @param handleWidth     Width of the scrollbar handle sprite in the source texture.
     * @param handleHeight    Height of the scrollbar handle sprite in the source texture.
     * @param texture         Texture containing the scrollbar handle sprites.
     * @param enabledBlitter  Rectangle in the source texture that contains the sprite for an enabled handle.
     * @param disabledBlitter Rectangle in the source texture that contains the sprite for a disabled handle.
     */
    public record Style(
            int handleWidth,
            int handleHeight,
            ResourceLocation texture,
            Blitter enabledBlitter,
            Blitter disabledBlitter) {
        public static Style create(
                ResourceLocation texture,
                int handleWidth,
                int handleHeight,
                int enabledSrcX, int enabledSrcY,
                int disabledSrcX, int disabledSrcY) {
            return new Style(
                    handleWidth,
                    handleHeight,
                    texture,
                    Blitter.texture(texture).src(enabledSrcX, enabledSrcY, handleWidth, handleHeight),
                    Blitter.texture(texture).src(disabledSrcX, disabledSrcY, handleWidth, handleHeight));
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
