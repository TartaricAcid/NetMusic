package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MusicPlayerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final MusicPlayerRenderer renderer;

    public MusicPlayerItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        ModelMusicPlayer model = new ModelMusicPlayer(modelSet.bakeLayer(ModelMusicPlayer.LAYER));
        ModelPart disc = model.getDiscBone();
        disc.visible = false;
        this.renderer = new MusicPlayerRenderer(model, Minecraft.getInstance().font, dispatcher);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        poseStack.scale(4 / 3.0f, 4 / 3.0f, 4 / 3.0f);
        poseStack.translate(0.5 - 0.5 / 0.75, 0, 0.5 - 0.5 / 0.75);
        renderer.renderMusicPlayer(poseStack, bufferIn, combinedLightIn, Direction.WEST);
    }
}