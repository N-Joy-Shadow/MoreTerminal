package njoyshadow.moreterminal.api.client.gui;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Tooltip;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import njoyshadow.moreterminal.client.gui.me.common.MTBaseScreen;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public interface IMTCompositeWidget {

    default boolean isVisible() {
        return true;
    }

    void setPosition(Point position);

    void setSize(int width, int height);

    /**
     * @return The area occupied by this widget relative to the dialogs origin.
     */
    Rect2i getBounds();

    /**
     * Allows the widget to add exclusion zones, which are used for managing space with other overlay mods such as JEI.
     *
     * @param exclusionZones The list to add additional exclusion zones to. Exclusion zones should be in window
     *                       coordinates.
     * @param screenBounds   The bounds of the current screen in window coordinates.
     */
    default void addExclusionZones(List<Rect2i> exclusionZones, Rect2i screenBounds) {
        Rect2i bounds = getBounds();
        if (bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
            return;
        }

        // Automatically add the bounds if they exceed the screen bounds
        if (bounds.getX() < 0
                || bounds.getY() < 0
                || bounds.getX() + bounds.getWidth() > screenBounds.getWidth()
                || bounds.getY() + bounds.getHeight() > screenBounds.getHeight()) {
            exclusionZones.add(new Rect2i(
                    screenBounds.getX() + bounds.getX(),
                    screenBounds.getY() + bounds.getY(),
                    bounds.getWidth(),
                    bounds.getHeight()));
        }
    }

    /**
     * Reinitializes a Vanilla screen and populates it with additional vanilla widgets.
     * <p/>
     * This is called initially when the screen is first shown, and called again everytime the screen is resized, as
     * Vanilla does it's positioning logic entirely in this method.
     *
     * @param bounds The bounding box of the screen in window coordinates.
     */
    default void populateScreen(Consumer<AbstractWidget> addWidget, Rect2i bounds, MTBaseScreen<?> screen) {
    }

    /**
     * Drive animations. This is called alongside each client tick, via {@link Screen#tick()}. This is called less often
     * than {@link #updateBeforeRender()}.
     */
    default void tick() {
    }

    /**
     * Perform layout directly before any rendering methods are called.
     */
    default void updateBeforeRender() {
    }

    /**
     * Draw this composite widget on the background layer of the screen.
     *
     * @param poseStack The current matrix stack. Is NOT transformed to the screen, but rather is at the origin of the
     *                  window.
     * @param zIndex    The z-index to draw at.
     * @param bounds    The bounds of the current dialog in window coordinates.
     * @param mouse     The current mouse position relative to the dialogs origin.
     */
    default void drawBackgroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {
    }

    /**
     * Draw this composite widget on the foreground layer of the screen.
     *
     * @param poseStack The current matrix stack. Is transformed such that 0,0 is at the dialogs origin.
     * @param zIndex    The z-index to draw at.
     * @param bounds    The bounds of the current dialog in dialog coordinates (x,y are 0).
     * @param mouse     The current mouse position relative to the dialogs origin.
     */
    default void drawForegroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {
    }

    /**
     * Called when the player presses a mouse button on the screen.
     *
     * @param mousePos The current coordinate of the mouse cursor relative to the current dialogs origin.
     * @param button   The pressed button (0=left)
     * @return True to handle the event, false to pass it to other widgets.
     */
    default boolean onMouseDown(Point mousePos, int button) {
        return false;
    }

    /**
     * Override and return true to capture all mouse up events, even if the mouse is not over the widget.
     */
    default boolean wantsAllMouseDownEvents() {
        return false;
    }

    /**
     * Called when the player releases a mouse button on the screen.
     *
     * @param mousePos The current coordinate of the mouse cursor relative to the current dialogs origin.
     * @param button   The released button (0=left)
     * @return True to handle the event, false to pass it to other widgets.
     */
    default boolean onMouseUp(Point mousePos, int button) {
        return false;
    }

    /**
     * Override and return true to capture all mouse up events, even if the mouse is not over the widget.
     */
    default boolean wantsAllMouseUpEvents() {
        return false;
    }

    /**
     * Called when the player moves the mouse on the screen while holding a mouse button.
     *
     * @param mousePos The current coordinate of the mouse cursor relative to the current dialogs origin.
     * @param button   The held button (0=left)
     * @return True to handle the event, false to pass it to other widgets.
     */
    default boolean onMouseDrag(Point mousePos, int button) {
        return false;
    }

    /**
     * Called when the player moves the mousewheel.
     *
     * @param mousePos The current coordinate of the mouse cursor relative to the current dialogs origin.
     * @param delta    The mouse wheel movement.
     * @return True to handle the event, false to pass it to other widgets.
     */
    default boolean onMouseWheel(Point mousePos, double delta) {
        return false;
    }

    /**
     * Override and return true to capture all mouse wheel events, even if the mouse is not over the widget.
     */
    default boolean wantsAllMouseWheelEvents() {
        return false;
    }

    /**
     * Gets a tooltip at the given mouse position for this widget.
     *
     * @param mouseX The mouse x-coordinate relative to the screen.
     * @param mouseY The mouse y-coordinate relative to the screen.
     * @return Null if no tooltip is present, the tooltip otherwise.
     */
    @Nullable
    default Tooltip getTooltip(int mouseX, int mouseY) {
        return null;
    }

}
