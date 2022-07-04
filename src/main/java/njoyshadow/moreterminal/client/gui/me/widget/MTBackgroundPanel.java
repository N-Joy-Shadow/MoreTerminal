package njoyshadow.moreterminal.client.gui.me.widget;

import appeng.client.Point;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.Rect2i;
import njoyshadow.moreterminal.api.client.gui.IMTCompositeWidget;
import njoyshadow.moreterminal.client.gui.style.MTBlitter;

public class MTBackgroundPanel implements IMTCompositeWidget {
    private final MTBlitter background;

    // Relative to current screen origin (not window)
    private int x;
    private int y;

    public MTBackgroundPanel(MTBlitter background) {
        this.background = background;
    }
    @Override
    public void setPosition(Point position) {
        x = position.getX();
        y = position.getY();
    }
    @Override
    public void setSize(int width, int height) {
        // Size of panels is implied by the background
    }
    @Override
    public Rect2i getBounds() {
        return new Rect2i(x, y, background.getSrcWidth(), background.getSrcHeight());
    }
    @Override
    public void drawBackgroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {
        background.dest(bounds.getX() + x, bounds.getY() + y).blit(poseStack, zIndex);
    }
}