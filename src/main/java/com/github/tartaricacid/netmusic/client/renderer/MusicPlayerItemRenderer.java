package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;

import static com.github.tartaricacid.netmusic.client.renderer.MusicPlayerRenderer.TEXTURE;

public class MusicPlayerItemRenderer extends TileEntityItemStackRenderer {
    public static final ModelMusicPlayer MODEL = new ModelMusicPlayer();
    public static final MusicPlayerItemRenderer INSTANCE = new MusicPlayerItemRenderer();

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        if (itemStackIn.getItem() == Item.getItemFromBlock(InitBlocks.MUSIC_PLAYER)) {
            renderMusicPlayer(0, 0, 0, 1.0f, EnumFacing.WEST, MODEL);
        }
    }

    private void renderMusicPlayer(double x, double y, double z, float alpha, EnumFacing facing, ModelMusicPlayer modelMusicPlayer) {
        MusicPlayerRenderer.instance.bindTexture(TEXTURE);
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
