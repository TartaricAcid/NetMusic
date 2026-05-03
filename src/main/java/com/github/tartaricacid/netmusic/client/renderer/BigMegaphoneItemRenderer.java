package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.client.model.ModelBigMegaphone;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.util.Unit;
import org.joml.Vector3fc;

import java.util.function.Consumer;

import static com.github.tartaricacid.netmusic.client.renderer.BigMegaphoneRenderer.TEXTURE_OFF;

public class BigMegaphoneItemRenderer implements NoDataSpecialModelRenderer {
    private final ModelBigMegaphone.Item model;

    public BigMegaphoneItemRenderer(SpecialModelRenderer.BakingContext context) {
        this.model = new ModelBigMegaphone.Item(context.entityModelSet().bakeLayer(ModelBigMegaphone.LAYER));
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.root().getExtentsForGui(poseStack, output);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNode, int light, int overlay, boolean hasFoil, int outlineColor) {
        submitNode.submitModel(this.model, Unit.INSTANCE, poseStack, TEXTURE_OFF, light, overlay, 0, null);
    }

    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
        public static final MapCodec<BigMegaphoneItemRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new BigMegaphoneItemRenderer.Unbaked());

        @Override
        public MapCodec<BigMegaphoneItemRenderer.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public BigMegaphoneItemRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new BigMegaphoneItemRenderer(context);
        }
    }
}