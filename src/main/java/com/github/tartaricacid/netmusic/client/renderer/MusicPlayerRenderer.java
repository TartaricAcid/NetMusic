package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class MusicPlayerRenderer extends TileEntitySpecialRenderer<TileEntityMusicPlayer> {
    public static final ModelMusicPlayer MODEL = new ModelMusicPlayer();
    public static final ResourceLocation TEXTURE = new ResourceLocation(NetMusic.MOD_ID, "textures/block/music_player.png");
    public static MusicPlayerRenderer instance;

    public MusicPlayerRenderer() {
        instance = this;
    }

    @Override
    public void render(TileEntityMusicPlayer te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(te.getBlockMetadata());
        ItemStack cd = te.getPlayerInv().getStackInSlot(0);
        ModelRenderer disc = MODEL.getDiscBone();
        disc.isHidden = cd.isEmpty();
        if (!cd.isEmpty() && te.isPlay()) {
            disc.rotateAngleY = (float) ((2 * Math.PI / 40) * ((Minecraft.getSystemTime() / 50) % 40));
        }

        renderMusicPlayer(x, y, z, alpha, facing, MODEL);
    }

    public void renderMusicPlayer(double x, double y, double z, float alpha, EnumFacing facing, ModelMusicPlayer modelMusicPlayer) {
        this.bindTexture(TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        final boolean lightmapEnabled = GL11.glGetBoolean(GL11.GL_TEXTURE_2D);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.translate(x + 0.5, y + 1.5, z + 0.5);
        switch (facing) {
            case NORTH:
            default:
                break;
            case SOUTH:
                GlStateManager.rotate(180, 0, 1, 0);
                break;
            case EAST:
                GlStateManager.rotate(270, 0, 1, 0);
                break;
            case WEST:
                GlStateManager.rotate(90, 0, 1, 0);
                break;
        }
        GlStateManager.rotate(180, 0, 0, 1);
        modelMusicPlayer.render(null, 0, 0, 0, 0, 0, 0.0625f);
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        if (lightmapEnabled) {
            GlStateManager.enableTexture2D();
        } else {
            GlStateManager.disableTexture2D();
        }
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}
