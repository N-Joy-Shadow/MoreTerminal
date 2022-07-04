package njoyshadow.moreterminal.client.gui.style;


import com.google.gson.*;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

public enum MTBlitterDeserializer implements JsonDeserializer<MTBlitter> {

    INSTANCE;

    @Override
    public MTBlitter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (!json.isJsonObject()) {
            throw new JsonParseException("MTBlitters must be objects");
        }

        JsonObject root = json.getAsJsonObject();

        String texture = GsonHelper.getAsString(root, "texture");
        int textureWidth = GsonHelper.getAsInt(root, "textureWidth", MTBlitter.DEFAULT_TEXTURE_WIDTH);
        int textureHeight = GsonHelper.getAsInt(root, "textureHeight", MTBlitter.DEFAULT_TEXTURE_HEIGHT);

        MTBlitter Blitter;
        if (texture.contains(":")) {
            Blitter = new MTBlitter(new ResourceLocation(texture), textureWidth, textureHeight);
        } else {
            Blitter = MTBlitter.texture(texture, textureWidth, textureHeight);
        }

        if (root.has("srcRect")) {
            Rect2i srcRect = context.deserialize(root.get("srcRect"), Rect2i.class);
            Blitter = Blitter.src(srcRect);
        }

        return Blitter;
    }
}