package njoyshadow.moreterminal.client.gui.me.style;


import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import njoyshadow.moreterminal.Moreterminal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.function.Predicate;

public class MTStyleManager {
    private static final Map<String, MTScreenStyle> styleCache = new HashMap();
    public static final String PROP_INCLUDES = "includes";
    private static IResourceManager resourceManager;

    public MTStyleManager() {
    }

    private static String getBasePath(String path) {
        int lastSep = path.lastIndexOf(47);
        return lastSep == -1 ? "" : path.substring(0, lastSep + 1);
    }

    public static MTScreenStyle loadStyleDoc(String path) throws IOException {
        MTScreenStyle style = loadStyleDocInternal(path);
        style.validate();
        return style;
    }

    private static JsonObject loadMergedJsonTree(String path, Set<String> loadedFiles, Set<String> resourcePacks) throws IOException {
        Preconditions.checkArgument(path.startsWith("/"), "Path needs to start with slash");
        if (path.contains("..")) {
            path = URI.create(path).normalize().toString();
        }

        if (!loadedFiles.add(path)) {
            throw new IllegalStateException("Recursive style includes: " + loadedFiles);
        } else if (resourceManager == null) {
            throw new IllegalStateException("ResourceManager was not set. Was initialize called?");
        } else {
            String basePath = getBasePath(path);
            IResource resource = resourceManager.getResource(Moreterminal.MakeID(path.substring(1)));
            Throwable var6 = null;

            JsonObject document;
            try {
                resourcePacks.add(resource.getPackName());
                document = (JsonObject)MTScreenStyle.GSON.fromJson(new InputStreamReader(resource.getInputStream()), JsonObject.class);
            } catch (Throwable var16) {
                var6 = var16;
                throw var16;
            } finally {
                if (resource != null) {
                    if (var6 != null) {
                        try {
                            resource.close();
                        } catch (Throwable var15) {
                            var6.addSuppressed(var15);
                        }
                    } else {
                        resource.close();
                    }
                }

            }

            if (document.has("includes")) {
                String[] includes = (String[])MTScreenStyle.GSON.fromJson(document.get("includes"), String[].class);
                List<JsonObject> layers = new ArrayList();
                String[] var7 = includes;
                int var8 = includes.length;

                for(int var9 = 0; var9 < var8; ++var9) {
                    String include = var7[var9];
                    layers.add(loadMergedJsonTree(basePath + include, loadedFiles, resourcePacks));
                }

                layers.add(document);
                document = combineLayers(layers);
            }

            return document;
        }
    }

    private static JsonObject combineLayers(List<JsonObject> layers) {
        JsonObject result = new JsonObject();
        Iterator var2 = layers.iterator();

        while(var2.hasNext()) {
            JsonObject layer = (JsonObject)var2.next();
            Iterator var4 = layer.entrySet().iterator();

            while(var4.hasNext()) {
                Map.Entry<String, JsonElement> entry = (Map.Entry)var4.next();
                result.add((String)entry.getKey(), (JsonElement)entry.getValue());
            }
        }

        mergeObjectKeys("slots", layers, result);
        mergeObjectKeys("text", layers, result);
        mergeObjectKeys("palette", layers, result);
        mergeObjectKeys("images", layers, result);
        mergeObjectKeys("terminalStyle", layers, result);
        mergeObjectKeys("widgets", layers, result);
        return result;
    }

    private static void mergeObjectKeys(String propertyName, List<JsonObject> layers, JsonObject target) throws JsonParseException {
        JsonObject mergedObject = null;
        Iterator var4 = layers.iterator();

        while(true) {
            JsonElement layerEl;
            do {
                if (!var4.hasNext()) {
                    if (mergedObject != null) {
                        target.add(propertyName, mergedObject);
                    }

                    return;
                }

                JsonObject layer = (JsonObject)var4.next();
                layerEl = layer.get(propertyName);
            } while(layerEl == null);

            if (!layerEl.isJsonObject()) {
                throw new JsonParseException("Expected " + propertyName + " to be an object, but was: " + layerEl);
            }

            JsonObject layerObj = layerEl.getAsJsonObject();
            if (mergedObject == null) {
                mergedObject = new JsonObject();
            }

            Iterator var8 = layerObj.entrySet().iterator();

            while(var8.hasNext()) {
                Map.Entry<String, JsonElement> entry = (Map.Entry)var8.next();
                mergedObject.add((String)entry.getKey(), (JsonElement)entry.getValue());
            }
        }
    }

    private static MTScreenStyle loadStyleDocInternal(String path) {
        MTScreenStyle style = (MTScreenStyle)styleCache.get(path);
        if (style != null) {
            return style;
        } else {
            HashSet resourcePacks = new HashSet();

            try {
                JsonObject document = loadMergedJsonTree(path, new HashSet(), resourcePacks);
                style = (MTScreenStyle)MTScreenStyle.GSON.fromJson(document, MTScreenStyle.class);
                style.validate();
            } catch (Exception var4) {
                throw new JsonParseException("Failed to load style from " + path + " (packs: " + resourcePacks + ")", var4);
            }

            styleCache.put(path, style);
            return style;
        }
    }

    public static void initialize(IResourceManager resourceManager) {
        if (resourceManager instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager)resourceManager).addReloadListener(new MTStyleManager.ReloadListener());
        }

        setResourceManager(resourceManager);
    }

    private static void setResourceManager(IResourceManager resourceManager) {
        MTStyleManager.resourceManager = resourceManager;
        styleCache.clear();
    }

    private static class ReloadListener implements ISelectiveResourceReloadListener {
        private ReloadListener() {
        }

        public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
            MTStyleManager.setResourceManager(resourceManager);
        }
    }
}