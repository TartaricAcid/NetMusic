package com.github.tartaricacid.netmusic.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MusicPlayerItemRenderer extends BlockEntityWithoutLevelRenderer {
    public MusicPlayerItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        poseStack.scale(4 / 3.0f, 4 / 3.0f, 4 / 3.0f);
        poseStack.translate(0.5 - 0.5 / 0.75, 0, 0.5 - 0.5 / 0.75);
        MusicPlayerRenderer.instance.renderMusicPlayer(poseStack, bufferIn, combinedLightIn, Direction.WEST);
    }
}