package njoyshadow.moreterminal.api.client.gui.widget;

import appeng.client.Point;
import appeng.client.gui.Tooltip;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import njoyshadow.moreterminal.client.gui.me.items.MTBaseScreen;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public interface IMTCompositeWidget {
    int BUTTON_LEFT = 0;

    void setPosition(Point var1);

    void setSize(int var1, int var2);

    Rectangle2d getBounds();

    default void addExclusionZones(List<Rectangle2d> exclusionZones, Rectangle2d screenBounds) {
        Rectangle2d bounds = this.getBounds();
        if (bounds.getWidth() > 0 && bounds.getHeight() > 0) {
            if (bounds.getX() < 0 || bounds.getY() < 0 || bounds.getX() + bounds.getWidth() > screenBounds.getWidth() || bounds.getY() + bounds.getHeight() > screenBounds.getHeight()) {
                exclusionZones.add(new Rectangle2d(screenBounds.getX() + bounds.getX(), screenBounds.getY() + bounds.getY(), bounds.getWidth(), bounds.getHeight()));
            }

        }
    }

    default void populateScreen(Consumer< Widget > addWidget, Rectangle2d bounds, MTBaseScreen<?> screen) {
    }

    default void tick() {
    }

    default void updateBeforeRender() {
    }

    default void drawBackgroundLayer(MatrixStack matrices, int zIndex, Rectangle2d bounds, Point mouse) {
    }

    default void drawForegroundLayer(MatrixStack matrices, int zIndex, Rectangle2d bounds, Point mouse) {
    }

    default boolean onMouseDown(Point mousePos, int button) {
        return false;
    }

    default boolean wantsAllMouseDownEvents() {
        return false;
    }

    default boolean onMouseUp(Point mousePos, int button) {
        return false;
    }

    default boolean wantsAllMouseUpEvents() {
        return false;
    }

    default boolean onMouseDrag(Point mousePos, int button) {
        return false;
    }

    default boolean onMouseWheel(Point mousePos, double delta) {
        return false;
    }

    default boolean wantsAllMouseWheelEvents() {
        return false;
    }

    @Nullable
    default Tooltip getTooltip(int mouseX, int mouseY) {
        return null;
    }
}
