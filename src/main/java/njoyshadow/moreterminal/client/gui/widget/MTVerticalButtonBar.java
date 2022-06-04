package njoyshadow.moreterminal.client.gui.widget;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import njoyshadow.moreterminal.api.client.gui.widget.IMTCompositeWidget;
import njoyshadow.moreterminal.client.gui.me.items.MTBaseScreen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public class MTVerticalButtonBar implements IMTCompositeWidget {
    private static final int VERTICAL_SPACING = 4;
    private static final int MARGIN = 2;
    private final List<Button> buttons = new ArrayList();
    private Point screenOrigin;
    private Rectangle2d bounds;
    private Point position;

    public MTVerticalButtonBar() {
        this.screenOrigin = Point.ZERO;
        this.bounds = new Rectangle2d(0, 0, 0, 0);
    }

    public void add(Button button) {
        this.buttons.add(button);
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public void setSize(int width, int height) {
    }

    public Rectangle2d getBounds() {
        return this.bounds;
    }

    public void updateBeforeRender() {
        int currentY = this.position.getY() + 2;
        int maxWidth = 0;
        Iterator var3 = this.buttons.iterator();

        while(var3.hasNext()) {
            Button button = (Button)var3.next();
            if (button.visible) {
                button.x = this.screenOrigin.getX() + this.position.getX() - 2 - button.getWidth();
                button.y = this.screenOrigin.getY() + currentY;
                currentY += button.getHeight() + 4;
                maxWidth = Math.max(button.getWidth(), maxWidth);
            }
        }

        if (maxWidth == 0) {
            this.bounds = new Rectangle2d(0, 0, 0, 0);
        } else {
            int boundX = this.position.getX() - maxWidth - 4;
            int boundY = this.position.getY();
            this.bounds = new Rectangle2d(boundX, boundY, maxWidth + 4, currentY - boundY);
        }

    }

    public void populateScreen(Consumer<Widget> addWidget, Rectangle2d bounds, MTBaseScreen<?> screen) {
        this.screenOrigin = Point.fromTopLeft(bounds);
        this.buttons.forEach(addWidget);
    }
}
