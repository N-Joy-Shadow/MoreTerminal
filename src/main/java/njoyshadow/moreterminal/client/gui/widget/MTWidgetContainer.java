package njoyshadow.moreterminal.client.gui.widget;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.WidgetStyle;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.TabButton;
import appeng.container.implementations.PriorityContainer;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.SwitchGuisPacket;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;
import njoyshadow.moreterminal.api.client.gui.widget.IMTCompositeWidget;
import njoyshadow.moreterminal.client.gui.me.items.MTBaseScreen;
import njoyshadow.moreterminal.client.gui.me.style.MTScreenStyle;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MTWidgetContainer {
    private final MTScreenStyle style;
    private final Map<String, Widget> widgets = new HashMap();
    private final Map<String, IMTCompositeWidget> compositeWidgets = new HashMap();

    public MTWidgetContainer(MTScreenStyle style) {
        this.style = style;
    }

    public void add(String id, Widget widget) {
        Preconditions.checkState(!this.compositeWidgets.containsKey(id), "%s already used for composite widget", id);
        WidgetStyle widgetStyle = this.style.getWidget(id);
        if (widgetStyle.getWidth() != 0) {
            widget.setWidth(widgetStyle.getWidth());
        }

        if (widgetStyle.getHeight() != 0) {
            widget.setHeight(widgetStyle.getHeight());
        }

        if (widget instanceof TabButton) {
            ((TabButton)widget).setHideEdge(widgetStyle.isHideEdge());
        }

        if (this.widgets.put(id, widget) != null) {
            throw new IllegalStateException("Duplicate id: " + id);
        }
    }

    public void add(String id, IMTCompositeWidget widget) {
        Preconditions.checkState(!this.widgets.containsKey(id), "%s already used for widget", id);
        WidgetStyle widgetStyle = this.style.getWidget(id);
        widget.setSize(widgetStyle.getWidth(), widgetStyle.getHeight());
        if (this.compositeWidgets.put(id, widget) != null) {
            throw new IllegalStateException("Duplicate id: " + id);
        }
    }

    public Button addButton(String id, ITextComponent text, Button.IPressable action, Button.ITooltip tooltip) {
        Button button = new Button(0, 0, 0, 0, text, action, tooltip);
        this.add(id, (Widget)button);
        return button;
    }

    public Button addButton(String id, ITextComponent text, Button.IPressable action) {
        return this.addButton(id, text, action, Button.EMPTY_TOOLTIP);
    }

    public Button addButton(String id, ITextComponent text, Runnable action, Button.ITooltip tooltip) {
        return this.addButton(id, text, (btn) -> {
            action.run();
        }, tooltip);
    }

    public Button addButton(String id, ITextComponent text, Runnable action) {
        return this.addButton(id, text, action, Button.EMPTY_TOOLTIP);
    }

    public MTScrollbar addScrollBar(String id) {
        MTScrollbar scrollbar = new MTScrollbar();
        this.add(id, (IMTCompositeWidget) scrollbar);
        return scrollbar;
    }

    public void populateScreen(Consumer<Widget> addWidget, Rectangle2d bounds, MTBaseScreen<?> screen) {
        Iterator var4 = this.widgets.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<String, Widget> entry = (Map.Entry)var4.next();
            Widget widget = (Widget)entry.getValue();
            WidgetStyle widgetStyle = this.style.getWidget((String)entry.getKey());
            Point pos = widgetStyle.resolve(bounds);
            widget.x = pos.getX();
            widget.y = pos.getY();
            addWidget.accept(widget);
        }

        Rectangle2d relativeBounds = new Rectangle2d(0, 0, bounds.getWidth(), bounds.getHeight());
        Iterator var10 = this.compositeWidgets.entrySet().iterator();

        while(var10.hasNext()) {
            Map.Entry<String, IMTCompositeWidget> entry = (Map.Entry)var10.next();
            IMTCompositeWidget widget = (IMTCompositeWidget)entry.getValue();
            WidgetStyle widgetStyle = this.style.getWidget((String)entry.getKey());
            widget.setPosition(widgetStyle.resolve(relativeBounds));
            widget.populateScreen(addWidget, bounds, screen);
        }

    }

    public void tick() {
        Iterator var1 = this.compositeWidgets.values().iterator();

        while(var1.hasNext()) {
            IMTCompositeWidget widget = (IMTCompositeWidget)var1.next();
            widget.tick();
        }

    }

    public void updateBeforeRender() {
        Iterator var1 = this.compositeWidgets.values().iterator();

        while(var1.hasNext()) {
            IMTCompositeWidget widget = (IMTCompositeWidget)var1.next();
            widget.updateBeforeRender();
        }

    }

    public void drawBackgroundLayer(MatrixStack matrices, int zIndex, Rectangle2d bounds, Point mouse) {
        Iterator var5 = this.compositeWidgets.values().iterator();

        while(var5.hasNext()) {
            IMTCompositeWidget widget = (IMTCompositeWidget)var5.next();
            widget.drawBackgroundLayer(matrices, zIndex, bounds, mouse);
        }

    }

    public void drawForegroundLayer(MatrixStack matrices, int zIndex, Rectangle2d bounds, Point mouse) {
        Iterator var5 = this.compositeWidgets.values().iterator();

        while(var5.hasNext()) {
            IMTCompositeWidget widget = (IMTCompositeWidget)var5.next();
            widget.drawForegroundLayer(matrices, zIndex, bounds, mouse);
        }

    }

    public boolean onMouseDown(Point mousePos, int btn) {
        Iterator var3 = this.compositeWidgets.values().iterator();

        IMTCompositeWidget widget;
        do {
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                widget = (IMTCompositeWidget)var3.next();
            } while(!widget.wantsAllMouseDownEvents() && !mousePos.isIn(widget.getBounds()));
        } while(!widget.onMouseDown(mousePos, btn));

        return true;
    }

    public boolean onMouseUp(Point mousePos, int btn) {
        Iterator var3 = this.compositeWidgets.values().iterator();

        IMTCompositeWidget widget;
        do {
            do {
                if (!var3.hasNext()) {
                    return false;
                }

                widget = (IMTCompositeWidget)var3.next();
            } while(!widget.wantsAllMouseUpEvents() && !mousePos.isIn(widget.getBounds()));
        } while(!widget.onMouseUp(mousePos, btn));

        return true;
    }

    public boolean onMouseDrag(Point mousePos, int btn) {
        Iterator var3 = this.compositeWidgets.values().iterator();

        IMTCompositeWidget widget;
        do {
            if (!var3.hasNext()) {
                return false;
            }

            widget = (IMTCompositeWidget)var3.next();
        } while(!widget.onMouseDrag(mousePos, btn));

        return true;
    }

    public boolean onMouseWheel(Point mousePos, double wheelDelta) {
        Iterator var4 = this.compositeWidgets.values().iterator();

        IMTCompositeWidget widget;
        do {
            do {
                if (!var4.hasNext()) {
                    return false;
                }

                widget = (IMTCompositeWidget)var4.next();
            } while(!widget.wantsAllMouseWheelEvents() && !mousePos.isIn(widget.getBounds()));
        } while(!widget.onMouseWheel(mousePos, wheelDelta));

        return true;
    }

    public void addExclusionZones(List<Rectangle2d> exclusionZones, Rectangle2d bounds) {
        Iterator var3 = this.compositeWidgets.values().iterator();

        while(var3.hasNext()) {
            IMTCompositeWidget widget = (IMTCompositeWidget)var3.next();
            widget.addExclusionZones(exclusionZones, bounds);
        }

    }

    public void addOpenPriorityButton() {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.add("openPriority", (Widget)(new TabButton(Icon.WRENCH, GuiText.Priority.text(), itemRenderer, (btn) -> {
            this.openPriorityGui();
        })));
    }

    private void openPriorityGui() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(PriorityContainer.TYPE));
    }

    @Nullable
    public Tooltip getTooltip(int mouseX, int mouseY) {
        Iterator var3 = this.compositeWidgets.values().iterator();

        while(var3.hasNext()) {
            IMTCompositeWidget c = (IMTCompositeWidget)var3.next();
            Rectangle2d bounds = c.getBounds();
            if (mouseX >= bounds.getX() && mouseX < bounds.getX() + bounds.getWidth() && mouseY >= bounds.getY() && mouseY < bounds.getY() + bounds.getHeight()) {
                Tooltip tooltip = c.getTooltip(mouseX, mouseY);
                if (tooltip != null) {
                    return tooltip;
                }
            }
        }

        return null;
    }
}