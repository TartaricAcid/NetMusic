package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.event.ConfigEvent;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

public class MusicPlayerRenderer implements BlockEntityRenderer<TileEntityMusicPlayer, MusicPlayerRenderer.RenderState> {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/block/music_player.png");

    public static ModelMusicPlayer MODEL;
    public static MusicPlayerRenderer INSTANCE;

    private final Font font;

    public MusicPlayerRenderer(BlockEntityRendererProvider.Context context) {
        MODEL = new ModelMusicPlayer(context.bakeLayer(ModelMusicPlayer.LAYER));
        INSTANCE = this;
        this.font = context.font();
    }

    @Override
    public RenderState createRenderState() {
        return new RenderState();
    }

    @Override
    public void extractRenderState(TileEntityMusicPlayer te, RenderState renderState, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(te, renderState, partialTicks, cameraPos, crumblingOverlay);

        Direction facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        ItemStack cd = te.getItem(0);
        renderState.facing = facing;
        renderState.discVisible = !cd.isEmpty();
        renderState.playing = te.isPlay();
        if (renderState.discVisible && renderState.playing) {
            renderState.discRotation = (float) ((2 * Math.PI / 40) * (((double) System.currentTimeMillis() / 50) % 40));
        } else {
            renderState.discRotation = 0;
        }

        renderState.renderLyrics = false;
        renderState.currentLine = Component.empty();
        renderState.translatedLine = null;
        renderState.currentLyricColor = ConfigEvent.PLAYER_ORIGINAL_COLOR;
        renderState.transLyricColor = ConfigEvent.PLAYER_TRANSLATED_COLOR;
        renderState.textYOffset = 0.5f;

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

        if (!te.isPlay()) {
            te.lyricRecord = null;
            return;
        }

        String lyric = lyrics.get(lyrics.firstIntKey());
        if (StringUtils.isNotBlank(lyric)) {
            renderState.currentLine = Component.literal(lyric);
        }

        Int2ObjectSortedMap<String> transLyrics = lyricRecord.getTransLyrics();
        if (transLyrics != null && !transLyrics.isEmpty()) {
            String transLyric = transLyrics.get(transLyrics.firstIntKey());
            if (StringUtils.isNotBlank(transLyric)) {
                renderState.translatedLine = Component.literal(transLyric);
            }
            renderState.textYOffset += 0.5f;
        } else {
            renderState.currentLyricColor = ConfigEvent.PLAYER_TRANSLATED_COLOR;
        }

        renderState.renderLyrics = true;
    }

    @Override
    public void submit(RenderState renderState, @NonNull PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        ModelPart disc = MODEL.getDiscBone();
        disc.visible = renderState.discVisible;
        disc.yRot = renderState.discRotation;

        renderMusicPlayer(renderState, poseStack, submitNodeCollector.order(0));
        renderLyric(renderState, poseStack, submitNodeCollector.order(0));
    }

    private void renderMusicPlayer(RenderState renderState, PoseStack poseStack, OrderedSubmitNodeCollector submitNodeCollector) {
        poseStack.pushPose();
        poseStack.scale(0.75f, 0.75f, 0.75f);
        poseStack.translate(0.5 / 0.75, 1.5, 0.5 / 0.75);
        switch (renderState.facing) {
            case SOUTH:
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                break;
            case EAST:
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
                break;
            case WEST:
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                break;
            case NORTH:
            default:
                break;
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        submitNodeCollector.submitModelPart(
                MODEL.root(),
                poseStack,
                MODEL.renderType(TEXTURE),
                renderState.lightCoords,
                OverlayTexture.NO_OVERLAY,
                null,
                0xffffffff,
                renderState.breakProgress
        );
        poseStack.popPose();
    }

    private void renderLyric(RenderState renderState, PoseStack poseStack, OrderedSubmitNodeCollector submitNodeCollector) {
        if (!renderState.renderLyrics) {
            return;
        }

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (camera == null || !camera.isInitialized()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 1.625, 0.5);
        poseStack.mulPose(Axis.YN.rotationDegrees(camera.yRot()));
        poseStack.mulPose(Axis.XN.rotationDegrees(-camera.xRot()));
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int bgColor = (int) (opacity * 255.0F) << 24;

        if (!renderState.currentLine.getContents().equals(PlainTextContents.EMPTY)) {
            float currentLineWidth = (float) (-this.font.width(renderState.currentLine) / 2);
            submitNodeCollector.submitText(
                    poseStack,
                    currentLineWidth,
                    -renderState.textYOffset,
                    renderState.currentLine.getVisualOrderText(),
                    false,
                    Font.DisplayMode.NORMAL,
                    renderState.currentLyricColor,
                    bgColor,
                    renderState.lightCoords,
                    0
            );
        }

        if (renderState.translatedLine != null) {
            float translatedLineWidth = (float) (-this.font.width(renderState.translatedLine) / 2);
            submitNodeCollector.submitText(
                    poseStack,
                    translatedLineWidth,
                    -renderState.textYOffset - 12,
                    renderState.translatedLine.getVisualOrderText(),
                    false,
                    Font.DisplayMode.NORMAL,
                    renderState.transLyricColor,
                    bgColor,
                    renderState.lightCoords,
                    0
            );
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class RenderState extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public boolean discVisible = false;
        public boolean playing = false;
        public float discRotation = 0;
        public boolean renderLyrics = false;
        public MutableComponent currentLine = Component.empty();
        public MutableComponent translatedLine = null;
        public int currentLyricColor = ConfigEvent.PLAYER_ORIGINAL_COLOR;
        public int transLyricColor = ConfigEvent.PLAYER_TRANSLATED_COLOR;
        public float textYOffset = 0.5f;
    }
}
