package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Unit;
import org.joml.Vector3fc;

import java.util.function.Consumer;

import static com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer.TEXTURE;

public class MusicPlayerItemRenderer implements NoDataSpecialModelRenderer {
    private final ModelMusicPlayer.Item model;

    public MusicPlayerItemRenderer(SpecialModelRenderer.BakingContext context) {
        this.model = new ModelMusicPlayer.Item(context.entityModelSet().bakeLayer(ModelMusicPlayer.LAYER));
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.root().getExtentsForGui(poseStack, output);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNode, int light, int overlay, boolean hasFoil, int outlineColor) {
        //poseStack.scale(4 / 3.0f, 4 / 3.0f, 4 / 3.0f);
        //poseStack.translate(0.5 - 0.5 / 0.75, 0, 0.5 - 0.5 / 0.75);
        submitNode.submitModel(this.model, Unit.INSTANCE, poseStack, TEXTURE, light, overlay, 0, null);
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<MusicPlayerItemRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new MusicPlayerItemRenderer.Unbaked());

        @Override
        public MapCodec<MusicPlayerItemRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public MusicPlayerItemRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new MusicPlayerItemRenderer(context);
        }
    }
}