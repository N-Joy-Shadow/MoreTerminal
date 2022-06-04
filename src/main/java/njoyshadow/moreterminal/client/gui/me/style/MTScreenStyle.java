package njoyshadow.moreterminal.client.gui.me.style;

import appeng.client.gui.style.*;
import appeng.container.SlotSemantic;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import njoyshadow.moreterminal.client.gui.me.style.slot.MTSlotPosition;
import njoyshadow.moreterminal.client.gui.me.style.utils.MTBlitter;
import njoyshadow.moreterminal.client.gui.me.style.utils.MTBlitterDeserializer;
import njoyshadow.moreterminal.client.gui.me.style.utils.MTColorDeserializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class MTScreenStyle {
    public static final Gson GSON;
    private final Map<SlotSemantic, MTSlotPosition> slots = new EnumMap(SlotSemantic.class);
    private final Map<String, Text> text = new HashMap();
    private final Map<PaletteColor, Color> palette = new EnumMap(PaletteColor.class);
    private final Map<String, MTBlitter> images = new HashMap();
    @Nullable
    private MTBlitter background;
    @Nullable
    private MTTerminalStyle terminalStyle;
    private final Map<String, WidgetStyle> widgets = new HashMap();

    public MTScreenStyle() {
    }

    public Color getColor(PaletteColor color) {
        return (Color)this.palette.get(color);
    }

    public Map<SlotSemantic, MTSlotPosition> getSlots() {
        return this.slots;
    }

    public Map<String, Text> getText() {
        return this.text;
    }

    public MTBlitter getBackground() {
        return this.background != null ? this.background.copy() : null;
    }

    public WidgetStyle getWidget(String id) {
        WidgetStyle widget = (WidgetStyle)this.widgets.get(id);
        if (widget == null) {
            throw new IllegalStateException("Screen is missing required widget: " + id);
        } else {
            return widget;
        }
    }

    @Nonnull
    public MTBlitter getImage(String id) {
        MTBlitter blitter = (MTBlitter)this.images.get(id);
        if (blitter == null) {
            throw new IllegalStateException("Screen is missing required image: " + id);
        } else {
            return blitter;
        }
    }

    @Nullable
    public MTTerminalStyle getTerminalStyle() {
        return this.terminalStyle;
    }

    public void validate() {
        PaletteColor[] var1 = PaletteColor.values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            PaletteColor value = var1[var3];
            if (!this.palette.containsKey(value)) {
                throw new RuntimeException("Palette is missing color " + value);
            }
        }

        if (this.terminalStyle != null) {
            this.terminalStyle.validate();
        }

    }

    static {
        GSON = (new GsonBuilder()).disableHtmlEscaping().registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer())
                .registerTypeHierarchyAdapter(Style.class, new net.minecraft.util.text.Style.Serializer())
                .registerTypeAdapter(MTBlitter.class, MTBlitterDeserializer.INSTANCE)
                .registerTypeAdapter(Rectangle2d.class, Rectangle2dDeserializer.INSTANCE)
                .registerTypeAdapter(Color.class, MTColorDeserializer.INSTANCE).create();
    }
}