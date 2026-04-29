package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.event.ConfigEvent;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import static com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer.TEXTURE;

public class MusicPlayerRenderer implements BlockEntityRenderer<TileEntityMusicPlayer, MusicPlayerRenderState> {
    private final ModelMusicPlayer.Block model;
    private final Font font;

    public MusicPlayerRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new ModelMusicPlayer.Block(context.bakeLayer(ModelMusicPlayer.LAYER));
        this.font = context.font();
    }

    public static AABB getAABB(BlockPos pStart, BlockPos pEnd) {
        return new AABB(pStart.getX(), pStart.getY(), pStart.getZ(), pEnd.getX(), pEnd.getY(), pEnd.getZ());
    }

    @Override
    public MusicPlayerRenderState createRenderState() {
        return new MusicPlayerRenderState();
    }

    @Override
    public void extractRenderState(TileEntityMusicPlayer te, MusicPlayerRenderState state, float partialTicks, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, camera, breakProgress);

        state.facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        state.hasDisc = !te.getPlayerInv().getResource(0).isEmpty();
        if (state.hasDisc && te.isPlay()) {
            state.discRotation = (float) ((2 * Math.PI / 40) * (((double) System.currentTimeMillis() / 50) % 40));
        }

        if (!GeneralConfig.ENABLE_PLAYER_LYRICS.get()) {
            return;
        }
        LyricRecord lyricRecord = te.lyricRecord;
        if (lyricRecord == null) {
            return;
        }
        Int2ObjectSortedMap<String> lyrics = lyricRecord.getLyrics();
        if (lyrics == null || lyrics.isEmpty()) {
            return;
        }

        // 如果已经停止播放了，直接清空
        if (!te.isPlay()) {
            te.lyricRecord = null;
            return;
        }

        state.currentLyricColor = ConfigEvent.PLAYER_ORIGINAL_COLOR;
        state.transLyricColor = ConfigEvent.PLAYER_TRANSLATED_COLOR;
        state.y = 0.5f;

        String lyric = lyrics.get(lyrics.firstIntKey());
        if (StringUtils.isNotBlank(lyric)) {
            state.currentLine = Component.literal(lyric);
        } else {
            state.currentLine = Component.empty();
        }
        state.translatedLine = null;

        Int2ObjectSortedMap<String> transLyrics = lyricRecord.getTransLyrics();
        if (transLyrics != null && !transLyrics.isEmpty()) {
            String transLyric = transLyrics.get(transLyrics.firstIntKey());
            if (StringUtils.isNotBlank(transLyric)) {
                state.translatedLine = Component.literal(transLyric);
            }
            state.y += 0.5f;
        } else {
            state.currentLyricColor = ConfigEvent.PLAYER_TRANSLATED_COLOR;
        }
    }

    @Override
    public void submit(MusicPlayerRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        submitMusicPlayer(state, poseStack, submitNodeCollector);
        submitLyric(state, poseStack, submitNodeCollector, camera);
    }

    public void submitMusicPlayer(MusicPlayerRenderState state, PoseStack matrixStack, SubmitNodeCollector submitNode) {
        matrixStack.pushPose();
        matrixStack.scale(0.75f, 0.75f, 0.75f);
        matrixStack.translate(0.5 / 0.75, 1.5, 0.5 / 0.75);
        matrixStack.mulPose(Axis.YP.rotationDegrees(180 - state.facing.get2DDataValue() * 90));
        matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
        submitNode.submitModel(model, state, matrixStack, TEXTURE, state.lightCoords,
                OverlayTexture.NO_OVERLAY, 0, state.breakProgress);
        matrixStack.popPose();
    }

    private void submitLyric(MusicPlayerRenderState state, PoseStack poseStack,
                             SubmitNodeCollector submitNode, CameraRenderState camera) {
        MutableComponent currentLine = state.currentLine;
        MutableComponent translatedLine = state.translatedLine;

        if (currentLine.equals(Component.empty()) && translatedLine == null) {
            return;
        }

        int currentLyricColor = state.currentLyricColor;
        int transLyricColor = state.transLyricColor;
        float y = state.y;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.625, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(camera.yRot));
        poseStack.mulPose(Axis.XN.rotationDegrees(-camera.xRot));
        poseStack.scale(-0.025F, -0.025F, -0.025F);

        float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int bgColor = (int) (opacity * 255.0F) << 24;

        if (!currentLine.getContents().equals(PlainTextContents.EMPTY)) {
            float currentLineWidth = (float) (-this.font.width(currentLine) / 2);
            FormattedCharSequence text = currentLine.getVisualOrderText();
            submitNode.submitText(poseStack, currentLineWidth, -y, text,
                    false, Font.DisplayMode.NORMAL, state.lightCoords,
                    currentLyricColor, bgColor, 0);
        }

        if (translatedLine != null) {
            float translatedLineWidth = (float) (-this.font.width(translatedLine) / 2);
            FormattedCharSequence text = translatedLine.getVisualOrderText();
            submitNode.submitText(poseStack, translatedLineWidth, -y - 12, text,
                    false, Font.DisplayMode.NORMAL, state.lightCoords,
                    transLyricColor, bgColor, 0);
        }

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(TileEntityMusicPlayer blockEntity) {
        BlockPos worldPosition = blockEntity.getBlockPos();
        return getAABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(1, 2, 1));
    }
}
