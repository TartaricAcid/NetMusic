package com.github.tartaricacid.netmusic.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MusicPlayerItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    @Override
    public void render(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        poseStack.scale(4 / 3.0f, 4 / 3.0f, 4 / 3.0f);
        poseStack.translate(0.5 - 0.5 / 0.75, 0, 0.5 - 0.5 / 0.75);
        MusicPlayerRenderer.INSTANCE.renderMusicPlayer(poseStack, bufferIn, combinedLightIn, Direction.WEST);
    }
}