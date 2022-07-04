package njoyshadow.moreterminal.client.gui.style;

import appeng.client.gui.style.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class MTScreenStyle{
    public static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
        .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
        .registerTypeAdapter(MTBlitter.class, MTBlitterDeserializer.INSTANCE)
        .registerTypeAdapter(Rect2i.class, Rectangle2dDeserializer.INSTANCE)
        .registerTypeAdapter(Color.class, MTColorDeserializer.INSTANCE)
        .create();

        /**
         * Positioning information for groups of slots.
         */
        private final Map<String, MTSlotPosition> slots = new HashMap<>();

        /**
         * Various text-labels positioned on the screen.
         */
        private final Map<String, Text> text = new HashMap<>();

        /**
         * Color-Palette for the screen.
         */
        private final Map<PaletteColor, Color> palette = new EnumMap<>(PaletteColor.class);

        /**
         * Additional images that are screen-specific.
         */
        private final Map<String, MTBlitter> images = new HashMap<>();

        /**
         * The screen background, which is optional. If defined, it is also used to size the dialog.
         */
        @Nullable
        private MTBlitter background;

        @Nullable
        private MTTerminalStyle terminalStyle;

        private final Map<String, WidgetStyle> widgets = new HashMap<>();

        private final Map<String, TooltipArea> tooltips = new HashMap<>();

        public Color getColor(PaletteColor color) {
            return palette.get(color);
        }

        public Map<String, MTSlotPosition> getSlots() {
            return slots;
        }

        public Map<String, Text> getText() {
            return text;
        }

        public Map<String, TooltipArea> getTooltips() {
            return tooltips;
        }

        public MTBlitter getBackground() {
            return background != null ? background.copy() : null;
        }

        public WidgetStyle getWidget(String id) {
            WidgetStyle widget = widgets.get(id);
            if (widget == null) {
                throw new IllegalStateException("Screen is missing required widget: " + id);
            }
            return widget;
        }

        public MTBlitter getImage(String id) {
            MTBlitter blitter = images.get(id);
            if (blitter == null) {
                throw new IllegalStateException("Screen is missing required image: " + id);
            }
            return blitter;
        }

        @Nullable
        public MTTerminalStyle getTerminalStyle() {
            return terminalStyle;
        }

        public void validate() {
            for (PaletteColor value : PaletteColor.values()) {
                if (!palette.containsKey(value)) {
                    throw new RuntimeException("Palette is missing color " + value);
                }
            }

            if (terminalStyle != null) {
                terminalStyle.validate();
            }
        }

}
