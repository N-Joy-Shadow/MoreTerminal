package njoyshadow.moreterminal.client.gui.me.style.utils;

import com.google.gson.*;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import njoyshadow.moreterminal.utils.MTApi;

import java.lang.reflect.Type;

public enum MTBlitterDeserializer implements JsonDeserializer<MTBlitter>{
     INSTANCE;

    MTBlitterDeserializer() {
    }
    public MTBlitter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException { if (!json.isJsonObject()) {
        throw new JsonParseException("Blitters must be objects");
    } else {
        JsonObject root = json.getAsJsonObject();
        String texture = JSONUtils.getString(root, "texture");
        int textureWidth = JSONUtils.getInt(root, "textureWidth", 512);
        int textureHeight = JSONUtils.getInt(root, "textureHeight", 512);


        MTBlitter blitter;
        if (texture.contains(":")) {
            blitter = new MTBlitter(new ResourceLocation(texture), textureWidth, textureHeight);
        } else {
            blitter = MTBlitter.texture(texture, textureWidth, textureHeight);
        }

        if (root.has("srcRect")) {
            Rectangle2d srcRect = (Rectangle2d)context.deserialize(root.get("srcRect"), Rectangle2d.class);
            blitter = blitter.src(srcRect);
        }
        return blitter;
        }
    }
}
