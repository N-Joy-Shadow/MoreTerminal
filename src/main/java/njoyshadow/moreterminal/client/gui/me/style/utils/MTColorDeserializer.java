package njoyshadow.moreterminal.client.gui.me.style.utils;

import appeng.client.gui.style.Color;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public enum MTColorDeserializer implements JsonDeserializer<Color> {
    INSTANCE;

    MTColorDeserializer() {
    }

    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Color.parse(json.getAsString());
    }
}
