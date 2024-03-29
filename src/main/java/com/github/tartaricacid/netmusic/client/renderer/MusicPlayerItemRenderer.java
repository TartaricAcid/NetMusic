package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public class MusicPlayerItemRenderer extends ItemStackTileEntityRenderer {
    public static final ModelMusicPlayer MODEL = new ModelMusicPlayer();

    @Override
    public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStack.scale(4 / 3.0f, 4 / 3.0f, 4 / 3.0f);
        matrixStack.translate(0.5 - 0.5 / 0.75, 0, 0.5 - 0.5 / 0.75);
        MusicPlayerRenderer.instance.renderMusicPlayer(matrixStack, bufferIn, combinedLightIn, Direction.WEST, MODEL);
    }
}
