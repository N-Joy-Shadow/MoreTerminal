package njoyshadow.moreterminal.client.gui.me.widget;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Tooltip;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.core.localization.GuiText;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import njoyshadow.moreterminal.api.client.gui.IMTCompositeWidget;
import njoyshadow.moreterminal.client.gui.style.MTBlitter;
import njoyshadow.moreterminal.client.gui.style.MTScreenStyle;

import javax.annotation.Nullable;

public class MTToolboxPanel  implements IMTCompositeWidget {

    // Backdrop for the 3x3 toolbox offered by the network-tool
    private final MTBlitter background;

    private final Component toolbeltName;

    // Relative to the origin of the current screen (not window)
    private Rect2i bounds = new Rect2i(0, 0, 0, 0);

    public MTToolboxPanel(MTScreenStyle style, Component toolbeltName) {
        this.background = style.getImage("toolbox");
        this.toolbeltName = toolbeltName;
    }

    @Override
    public void setPosition(Point position) {
        this.bounds = new Rect2i(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void setSize(int width, int height) {
        this.bounds = new Rect2i(bounds.getX(), bounds.getY(), width, height);
    }

    @Override
    public Rect2i getBounds() {
        return bounds;
    }

    @Override
    public void drawBackgroundLayer(PoseStack poseStack, int zIndex, Rect2i bounds, Point mouse) {
        background.dest(
                bounds.getX() + this.bounds.getX(),
                bounds.getY() + this.bounds.getY(),
                this.bounds.getWidth(),
                this.bounds.getHeight()).blit(poseStack, zIndex);
    }

    @Nullable
    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        return new Tooltip(
                this.toolbeltName,
                GuiText.UpgradeToolbelt.text().plainCopy().withStyle(ChatFormatting.GRAY));
    }

}
