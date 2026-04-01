package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;

import java.util.function.Consumer;

public class MusicPlayerItemRenderer implements NoDataSpecialModelRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/block/music_player.png");
    private final ModelMusicPlayer model;

    public MusicPlayerItemRenderer(ModelMusicPlayer model) {
        this.model = model;
    }

    @Override
    public void submit(ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean hasFoil, int seed) {
        poseStack.pushPose();
        applyBasePose(poseStack);
        submitNodeCollector.submitModelPart(
                this.model.root(),
                poseStack,
                this.model.renderType(TEXTURE),
                light,
                OverlayTexture.NO_OVERLAY,
                null,
                false,
                hasFoil,
                0xffffffff,
                null,
                seed
        );
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> extents) {
        PoseStack poseStack = new PoseStack();
        applyBasePose(poseStack);
        this.model.root().getExtentsForGui(poseStack, extents);
    }

    private static void applyBasePose(PoseStack poseStack) {
        poseStack.scale(4 / 3.0f, 4 / 3.0f, 4 / 3.0f);
        poseStack.translate(0.5 - 0.5 / 0.75, 0, 0.5 - 0.5 / 0.75);
        poseStack.scale(0.75f, 0.75f, 0.75f);
        poseStack.translate(0.5 / 0.75, 1.5, 0.5 / 0.75);
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
    }
}
