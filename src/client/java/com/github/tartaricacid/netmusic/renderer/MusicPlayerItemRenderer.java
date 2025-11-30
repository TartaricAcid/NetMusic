package com.github.tartaricacid.netmusic.renderer;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;

/**
 * @author : IMG
 * @create : 2024/10/6
 */
public class MusicPlayerItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    @Override
    public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.scale(4 / 3.0f, 4 / 3.0f, 4 / 3.0f);
        matrices.translate(0.5 - 0.5 / 0.75, 0, 0.5 - 0.5 / 0.75);
        MusicPlayerRenderer.instance.renderMusicPlayer(matrices, vertexConsumers, light, Direction.WEST);
    }

}
