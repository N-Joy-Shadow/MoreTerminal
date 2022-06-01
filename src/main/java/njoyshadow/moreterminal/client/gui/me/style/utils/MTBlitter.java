package njoyshadow.moreterminal.client.gui.me.style.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class MTBlitter {
    public static final int DEFAULT_TEXTURE_WIDTH = 256;
    public static final int DEFAULT_TEXTURE_HEIGHT = 256;
    private final ResourceLocation texture;
    private final int referenceWidth;
    private final int referenceHeight;
    private int r = 255;
    private int g = 255;
    private int b = 255;
    private int a = 255;
    private Rectangle2d srcRect;
    private Rectangle2d destRect = new Rectangle2d(0, 0, 0, 0);
    private boolean blending = true;

    MTBlitter(ResourceLocation texture, int referenceWidth, int referenceHeight) {
        this.texture = texture;
        this.referenceWidth = referenceWidth;
        this.referenceHeight = referenceHeight;
    }

    public static MTBlitter texture(ResourceLocation file) {
        return texture((ResourceLocation)file, 256, 256);
    }

    public static MTBlitter texture(String file) {
        return texture((String)file, 256, 256);
    }

    public static MTBlitter texture(ResourceLocation file, int referenceWidth, int referenceHeight) {
        return new MTBlitter(file, referenceWidth, referenceHeight);
    }

    public static MTBlitter texture(String file, int referenceWidth, int referenceHeight) {
        return new MTBlitter(new ResourceLocation("moreterminal", "textures/" + file), referenceWidth, referenceHeight);
    }

    public static MTBlitter sprite(TextureAtlasSprite sprite) {
        int refSize = 2147483647;
        AtlasTexture atlas = sprite.getAtlasTexture();
        return (new MTBlitter(atlas.getTextureLocation(), 2147483647, 2147483647)).src((int)(sprite.getMinU() * 2.14748365E9F), (int)(sprite.getMinV() * 2.14748365E9F), (int)((sprite.getMaxU() - sprite.getMinU()) * 2.14748365E9F), (int)((sprite.getMaxV() - sprite.getMinV()) * 2.14748365E9F));
    }

    public MTBlitter copy() {
        MTBlitter result = new MTBlitter(this.texture, this.referenceWidth, this.referenceHeight);
        result.srcRect = this.srcRect;
        result.destRect = this.destRect;
        result.r = this.r;
        result.g = this.g;
        result.b = this.b;
        result.a = this.a;
        return result;
    }

    public int getSrcX() {
        return this.srcRect == null ? 0 : this.srcRect.getX();
    }

    public int getSrcY() {
        return this.srcRect == null ? 0 : this.srcRect.getY();
    }

    public int getSrcWidth() {
        return this.srcRect == null ? this.destRect.getWidth() : this.srcRect.getWidth();
    }

    public int getSrcHeight() {
        return this.srcRect == null ? this.destRect.getHeight() : this.srcRect.getHeight();
    }

    public MTBlitter src(int x, int y, int w, int h) {
        this.srcRect = new Rectangle2d(x, y, w, h);
        return this;
    }

    public MTBlitter src(Rectangle2d rect) {
        return this.src(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public MTBlitter dest(int x, int y, int w, int h) {
        this.destRect = new Rectangle2d(x, y, w, h);
        return this;
    }

    public MTBlitter dest(int x, int y) {
        return this.dest(x, y, 0, 0);
    }

    public MTBlitter dest(Rectangle2d rect) {
        return this.dest(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public Rectangle2d getDestRect() {
        int x = this.destRect.getX();
        int y = this.destRect.getY();
        int w = 0;
        int h = 0;
        if (this.destRect.getWidth() != 0 && this.destRect.getHeight() != 0) {
            w = this.destRect.getWidth();
            h = this.destRect.getHeight();
        } else if (this.srcRect != null) {
            w = this.srcRect.getWidth();
            h = this.srcRect.getHeight();
        }

        return new Rectangle2d(x, y, w, h);
    }

    public MTBlitter color(float r, float g, float b) {
        this.r = (int)(MathHelper.clamp(r, 0.0F, 1.0F) * 255.0F);
        this.g = (int)(MathHelper.clamp(g, 0.0F, 1.0F) * 255.0F);
        this.b = (int)(MathHelper.clamp(b, 0.0F, 1.0F) * 255.0F);
        return this;
    }

    public MTBlitter opacity(float a) {
        this.a = (int)(MathHelper.clamp(a, 0.0F, 1.0F) * 255.0F);
        return this;
    }

    public MTBlitter color(float r, float g, float b, float a) {
        return this.color(r, g, b).opacity(a);
    }

    public MTBlitter blending(boolean enable) {
        this.blending = enable;
        return this;
    }

    public MTBlitter colorRgb(int packedRgb) {
        float r = (float)(packedRgb >> 16 & 255) / 255.0F;
        float g = (float)(packedRgb >> 8 & 255) / 255.0F;
        float b = (float)(packedRgb & 255) / 255.0F;
        return this.color(r, g, b);
    }

    public void blit(MatrixStack matrices, int zIndex) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        textureManager.bindTexture(this.texture);
        float minU;
        float minV;
        float maxU;
        float maxV;
        if (this.srcRect == null) {
            minV = 0.0F;
            minU = 0.0F;
            maxV = 1.0F;
            maxU = 1.0F;
        } else {
            minU = (float)this.srcRect.getX() / (float)this.referenceWidth;
            minV = (float)this.srcRect.getY() / (float)this.referenceHeight;
            maxU = (float)(this.srcRect.getX() + this.srcRect.getWidth()) / (float)this.referenceWidth;
            maxV = (float)(this.srcRect.getY() + this.srcRect.getHeight()) / (float)this.referenceHeight;
        }

        float x1 = (float)this.destRect.getX();
        float y1 = (float)this.destRect.getY();
        float x2 = x1;
        float y2 = y1;
        if (this.destRect.getWidth() != 0 && this.destRect.getHeight() != 0) {
            x2 = x1 + (float)this.destRect.getWidth();
            y2 = y1 + (float)this.destRect.getHeight();
        } else if (this.srcRect != null) {
            x2 = x1 + (float)this.srcRect.getWidth();
            y2 = y1 + (float)this.srcRect.getHeight();
        }

        Matrix4f matrix = matrices.getLast().getMatrix();
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR_TEX);
        bufferbuilder.pos(matrix, x1, y2, (float)zIndex).color(this.r, this.g, this.b, this.a).tex(minU, maxV).endVertex();
        bufferbuilder.pos(matrix, x2, y2, (float)zIndex).color(this.r, this.g, this.b, this.a).tex(maxU, maxV).endVertex();
        bufferbuilder.pos(matrix, x2, y1, (float)zIndex).color(this.r, this.g, this.b, this.a).tex(maxU, minV).endVertex();
        bufferbuilder.pos(matrix, x1, y1, (float)zIndex).color(this.r, this.g, this.b, this.a).tex(minU, minV).endVertex();
        bufferbuilder.finishDrawing();
        if (this.blending) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(770, 771);
        } else {
            RenderSystem.disableBlend();
        }

        RenderSystem.enableTexture();
        WorldVertexBufferUploader.draw(bufferbuilder);
    }
}
