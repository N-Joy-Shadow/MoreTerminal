package njoyshadow.moreterminal.client.gui.style;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import njoyshadow.moreterminal.Moreterminal;
import org.lwjgl.opengl.GL11;

public class MTBlitter {
    // This assumption is obviously bogus, but currently all textures are this size,
    // and it's impossible to get the texture size from an already loaded texture.
    // The coordinates will still be correct when a resource pack provides bigger textures as long
    // as each texture element is still positioned at the same relative position
    public static final int DEFAULT_TEXTURE_WIDTH = 512;
    public static final int DEFAULT_TEXTURE_HEIGHT = 512;

    private final ResourceLocation texture;
    // This texture size is only used to convert the source rectangle into uv coordinates (which are [0,1] and work
    // with textures of any size at runtime).
    private final int referenceWidth;
    private final int referenceHeight;
    private int r = 255;
    private int g = 255;
    private int b = 255;
    private int a = 255;
    private Rect2i srcRect;
    private Rect2i destRect = new Rect2i(0, 0, 0, 0);
    private boolean blending = true;

    MTBlitter(ResourceLocation texture, int referenceWidth, int referenceHeight) {
        this.texture = texture;
        this.referenceWidth = referenceWidth;
        this.referenceHeight = referenceHeight;
    }

    /**
     * Creates a MTBlitter where the source rectangle is in relation to a 256x256 pixel texture.
     */
    public static MTBlitter texture(ResourceLocation file) {
        return texture(file, DEFAULT_TEXTURE_WIDTH, DEFAULT_TEXTURE_HEIGHT);
    }

    /**
     * Creates a MTBlitter where the source rectangle is in relation to a 256x256 pixel texture.
     */
    public static MTBlitter texture(String file) {
        return texture(file, DEFAULT_TEXTURE_WIDTH, DEFAULT_TEXTURE_HEIGHT);
    }

    /**
     * Creates a MTBlitter where the source rectangle is in relation to a texture of the given size.
     */
    public static MTBlitter texture(ResourceLocation file, int referenceWidth, int referenceHeight) {
        return new MTBlitter(file, referenceWidth, referenceHeight);
    }

    /**
     * Creates a MTBlitter where the source rectangle is in relation to a texture of the given size.
     */
    public static MTBlitter texture(String file, int referenceWidth, int referenceHeight) {
        return new MTBlitter(new ResourceLocation(Moreterminal.MOD_ID, "textures/" + file), referenceWidth, referenceHeight);
    }

    /**
     * Creates a MTBlitter from a texture atlas sprite.
     */
    public static MTBlitter sprite(TextureAtlasSprite sprite) {
        // We use this convoluted method to convert from UV in the range of [0,1] back to pixel values with a
        // fictitious reference size of Integer.MAX_VALUE. This is converted back to UV later when we actually blit.
        final int refSize = Integer.MAX_VALUE;
        TextureAtlas atlas = sprite.atlas();

        return new MTBlitter(atlas.location(), refSize, refSize)
                .src(
                        (int) (sprite.getU0() * refSize),
                        (int) (sprite.getV0() * refSize),
                        (int) ((sprite.getU1() - sprite.getU0()) * refSize),
                        (int) ((sprite.getV1() - sprite.getV0()) * refSize));
    }

    public MTBlitter copy() {
        MTBlitter result = new MTBlitter(texture, referenceWidth, referenceHeight);
        result.srcRect = srcRect;
        result.destRect = destRect;
        result.r = r;
        result.g = g;
        result.b = b;
        result.a = a;
        return result;
    }

    public int getSrcX() {
        return srcRect == null ? 0 : srcRect.getX();
    }

    public int getSrcY() {
        return srcRect == null ? 0 : srcRect.getY();
    }

    public int getSrcWidth() {
        return srcRect == null ? destRect.getWidth() : srcRect.getWidth();
    }

    public int getSrcHeight() {
        return srcRect == null ? destRect.getHeight() : srcRect.getHeight();
    }

    /**
     * Use the given rectangle from the texture (in pixels assuming a 256x256 texture size).
     */
    public MTBlitter src(int x, int y, int w, int h) {
        this.srcRect = new Rect2i(x, y, w, h);
        return this;
    }

    /**
     * Use the given rectangle from the texture (in pixels assuming a 256x256 texture size).
     */
    public MTBlitter src(Rect2i rect) {
        return src(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Draw into the rectangle defined by the given coordinates.
     */
    public MTBlitter dest(int x, int y, int w, int h) {
        this.destRect = new Rect2i(x, y, w, h);
        return this;
    }

    /**
     * Draw at the given x,y coordinate and use the source rectangle size as the destination rectangle size.
     */
    public MTBlitter dest(int x, int y) {
        return dest(x, y, 0, 0);
    }

    /**
     * Draw into the given rectangle.
     */
    public MTBlitter dest(Rect2i rect) {
        return dest(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public Rect2i getDestRect() {
        int x = destRect.getX();
        int y = destRect.getY();
        int w = 0, h = 0;
        if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
            w = destRect.getWidth();
            h = destRect.getHeight();
        } else if (srcRect != null) {
            w = srcRect.getWidth();
            h = srcRect.getHeight();
        }
        return new Rect2i(x, y, w, h);
    }

    public MTBlitter color(float r, float g, float b) {
        this.r = (int) (Mth.clamp(r, 0, 1) * 255);
        this.g = (int) (Mth.clamp(g, 0, 1) * 255);
        this.b = (int) (Mth.clamp(b, 0, 1) * 255);
        return this;
    }

    public MTBlitter opacity(float a) {
        this.a = (int) (Mth.clamp(a, 0, 1) * 255);
        return this;
    }

    public MTBlitter color(float r, float g, float b, float a) {
        return color(r, g, b).opacity(a);
    }

    /**
     * Enables or disables alpha-blending. If disabled, all pixels of the texture will be drawn as opaque, and the alpha
     * value set using {@link #opacity(float)} will be ignored.
     */
    public MTBlitter blending(boolean enable) {
        this.blending = enable;
        return this;
    }

    /**
     * Sets the color to the R,G,B values encoded in the lower 24-bit of the given integer.
     */
    public MTBlitter colorRgb(int packedRgb) {
        float r = (packedRgb >> 16 & 255) / 255.0F;
        float g = (packedRgb >> 8 & 255) / 255.0F;
        float b = (packedRgb & 255) / 255.0F;

        return color(r, g, b);
    }

    public void blit(int zIndex) {
        // If we're not using a specific pose stack for transforms, we pass an empty
        // one to just get an identity transform
        blit(new PoseStack(), zIndex);
    }

    public void blit(PoseStack poseStack, int zIndex) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, this.texture);

        // With no source rectangle, we'll use the entirety of the texture. This happens rarely though.
        float minU, minV, maxU, maxV;
        if (srcRect == null) {
            minU = minV = 0;
            maxU = maxV = 1;
        } else {
            minU = srcRect.getX() / (float) referenceWidth;
            minV = srcRect.getY() / (float) referenceHeight;
            maxU = (srcRect.getX() + srcRect.getWidth()) / (float) referenceWidth;
            maxV = (srcRect.getY() + srcRect.getHeight()) / (float) referenceHeight;
        }

        // It's possible to not set a destination rectangle size, in which case the
        // source rectangle size will be used
        float x1 = destRect.getX();
        float y1 = destRect.getY();
        float x2 = x1, y2 = y1;
        if (destRect.getWidth() != 0 && destRect.getHeight() != 0) {
            x2 += destRect.getWidth();
            y2 += destRect.getHeight();
        } else if (srcRect != null) {
            x2 += srcRect.getWidth();
            y2 += srcRect.getHeight();
        }

        Matrix4f matrix = poseStack.last().pose();

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.vertex(matrix, x1, y2, zIndex)
                .uv(minU, maxV)
                .color(r, g, b, a)
                .endVertex();
        bufferbuilder.vertex(matrix, x2, y2, zIndex)
                .uv(maxU, maxV)
                .color(r, g, b, a)
                .endVertex();
        bufferbuilder.vertex(matrix, x2, y1, zIndex)
                .uv(maxU, minV)
                .color(r, g, b, a)
                .endVertex();
        bufferbuilder.vertex(matrix, x1, y1, zIndex)
                .uv(minU, minV)
                .color(r, g, b, a)
                .endVertex();
        bufferbuilder.end();

        if (blending) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            RenderSystem.disableBlend();
        }
        RenderSystem.enableTexture();
        BufferUploader.end(bufferbuilder);
    }

}
