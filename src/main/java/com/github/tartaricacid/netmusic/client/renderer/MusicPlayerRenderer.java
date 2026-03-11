package com.github.tartaricacid.netmusic.client.renderer;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.event.ConfigEvent;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.apache.commons.lang3.StringUtils;

public class MusicPlayerRenderer implements BlockEntityRenderer<TileEntityMusicPlayer> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(NetMusic.MOD_ID, "textures/block/music_player.png");

    public static ModelMusicPlayer<?> MODEL;
    public static MusicPlayerRenderer INSTANCE;

    private final Font font;
    private final BlockEntityRenderDispatcher dispatcher;

    public MusicPlayerRenderer(BlockEntityRendererProvider.Context context) {
        MODEL = new ModelMusicPlayer<>(context.bakeLayer(ModelMusicPlayer.LAYER));
        INSTANCE = this;
        this.font = context.getFont();
        this.dispatcher = context.getBlockEntityRenderDispatcher();
    }

    @Override
    public void render(TileEntityMusicPlayer te, float pPartialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Direction facing = te.getBlockState().getValue(HorizontalDirectionalBlock.FACING);
        ItemStack cd = te.getItem(0);
        ModelPart disc = MODEL.getDiscBone();
        disc.visible = !cd.isEmpty();
        if (!cd.isEmpty() && te.isPlay()) {
            disc.yRot = (float) ((2 * Math.PI / 40) * (((double) System.currentTimeMillis() / 50) % 40));
        }
        renderMusicPlayer(matrixStack, buffer, combinedLight, facing);
        renderLyric(te, matrixStack, buffer, combinedLight);
    }

    public void renderMusicPlayer(PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, Direction facing) {
        matrixStack.pushPose();
        matrixStack.scale(0.75f, 0.75f, 0.75f);
        matrixStack.translate(0.5 / 0.75, 1.5, 0.5 / 0.75);
        switch (facing) {
            case SOUTH:
                matrixStack.mulPose(Axis.YP.rotationDegrees(180));
                break;
            case EAST:
                matrixStack.mulPose(Axis.YP.rotationDegrees(270));
                break;
            case WEST:
                matrixStack.mulPose(Axis.YP.rotationDegrees(90));
                break;
            case NORTH:
            default:
                break;
        }
        matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        MODEL.renderToBuffer(matrixStack, vertexBuilder, combinedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        matrixStack.popPose();
    }

    private void renderLyric(TileEntityMusicPlayer te, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn) {
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

        Camera camera = this.dispatcher.camera;
        int currentLyricColor = ConfigEvent.PLAYER_ORIGINAL_COLOR;
        int transLyricColor = ConfigEvent.PLAYER_TRANSLATED_COLOR;
        float y = 0.5f;

        String lyric = lyrics.get(lyrics.firstIntKey());
        MutableComponent currentLine;
        if (StringUtils.isNotBlank(lyric)) {
            currentLine = Component.literal(lyric);
        } else {
            currentLine = Component.empty();
        }
        MutableComponent translatedLine = null;

        Int2ObjectSortedMap<String> transLyrics = lyricRecord.getTransLyrics();
        if (transLyrics != null && !transLyrics.isEmpty()) {
            String transLyric = transLyrics.get(transLyrics.firstIntKey());
            if (StringUtils.isNotBlank(transLyric)) {
                translatedLine = Component.literal(transLyric);
            }
            y += 0.5f;
        } else {
            currentLyricColor = ConfigEvent.PLAYER_TRANSLATED_COLOR;
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 1.625, 0.5);
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        float opacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int bgColor = (int) (opacity * 255.0F) << 24;

        if (!currentLine.getContents().equals(ComponentContents.EMPTY)) {
            float currentLineWidth = (float) (-this.font.width(currentLine) / 2);
            this.font.drawInBatch(currentLine, currentLineWidth, -y, currentLyricColor, false,
                    poseStack.last().pose(), bufferIn, Font.DisplayMode.NORMAL,
                    bgColor, combinedLightIn);
        }

        if (translatedLine != null) {
            float translatedLineWidth = (float) (-this.font.width(translatedLine) / 2);
            this.font.drawInBatch(translatedLine, translatedLineWidth, -y - 12, transLyricColor, false,
                    poseStack.last().pose(), bufferIn, Font.DisplayMode.NORMAL,
                    bgColor, combinedLightIn);
        }

        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityMusicPlayer musicPlayer) {
        return true;
    }
}
