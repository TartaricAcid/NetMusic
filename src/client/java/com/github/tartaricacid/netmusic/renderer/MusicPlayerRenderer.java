package com.github.tartaricacid.netmusic.renderer;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.model.ModelMusicPlayer;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.apache.commons.lang3.StringUtils;

/**
 * @author : IMG
 * @create : 2024/10/4
 */
public class MusicPlayerRenderer implements BlockEntityRenderer<TileEntityMusicPlayer> {
    public static ModelMusicPlayer<?> MODEL;
    public static final Identifier TEXTURE = Identifier.of(NetMusic.MOD_ID, "textures/block/music_player.png");
    public static MusicPlayerRenderer instance;

    private final TextRenderer font;
    private final BlockEntityRenderDispatcher dispatcher;

    public MusicPlayerRenderer(BlockEntityRendererFactory.Context context) {
        MODEL = new ModelMusicPlayer<>(context.getLayerModelPart(ModelMusicPlayer.LAYER));
        instance = this;
        this.font = context.getTextRenderer();
        this.dispatcher = context.getRenderDispatcher();
    }

    @Override
    public void render(TileEntityMusicPlayer entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        Direction facing = entity.getCachedState().get(HorizontalFacingBlock.FACING);
        ItemStack cd = entity.getStack(0);
        ModelPart disc = MODEL.getDiscBone();
        disc.visible = !entity.isEmpty();
        if (!cd.isEmpty() && entity.isPlay()) {
            disc.yaw = (float) ((2 * Math.PI / 40) * (((double) System.currentTimeMillis() / 50) % 40));
        }
        renderMusicPlayer(matrices, vertexConsumers, light, facing);
        renderLyric(entity, matrices, vertexConsumers, light);
    }

    public void renderMusicPlayer(MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int combinedLight, Direction facing) {
        matrixStack.push();
        matrixStack.scale(0.75f, 0.75f, 0.75f);
        matrixStack.translate(0.5 / 0.75, 1.5, 0.5 / 0.75);
        switch (facing) {
            case SOUTH:
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                break;
            case EAST:
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270));
                break;
            case WEST:
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                break;
            case NORTH:
            default:
                break;
        }
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(TEXTURE));
        MODEL.render(matrixStack, buffer, combinedLight, OverlayTexture.DEFAULT_UV, 0xffffffff);
        matrixStack.pop();
    }

    private void renderLyric(TileEntityMusicPlayer te, MatrixStack matrixStack, VertexConsumerProvider vertexConsumers, int combinedLight) {
        if (!GeneralConfig.ENABLE_PLAYER_LYRICS) {
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
        Formatting currentLyricColor = Formatting.GRAY;
        Formatting transLyricColor = Formatting.WHITE;
        float y = 0.5f;

        String lyric = lyrics.get(lyrics.firstIntKey());
        MutableText currentLine;
        if (StringUtils.isNotBlank(lyric)) {
            currentLine = Text.literal(lyric);
        } else {
            currentLine = Text.empty();
        }
        MutableText translatedLine = null;

        Int2ObjectSortedMap<String> transLyrics = lyricRecord.getTransLyrics();
        if (transLyrics != null && !transLyrics.isEmpty()) {
            String transLyric = transLyrics.get(transLyrics.firstIntKey());
            if (StringUtils.isNotBlank(transLyric)) {
                translatedLine = Text.literal(transLyric);
            }
            y += 0.5f;
        } else {
            currentLyricColor = Formatting.WHITE;
        }
        currentLine = currentLine.formatted(currentLyricColor);

        matrixStack.push();
        matrixStack.translate(0.5, 1.625, 0.5);
        matrixStack.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(camera.getYaw()));
        matrixStack.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(-camera.getPitch()));
        matrixStack.scale(-0.025F, -0.025F, 0.025F);

        float opacity = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
        int bgColor = (int) (opacity * 255.0F) << 24;

        if (!currentLine.getContent().equals(PlainTextContent.EMPTY)) {
            float currentLineWidth = (float) (-this.font.getWidth(currentLine) / 2);
            this.font.draw(currentLine, currentLineWidth, -y, 0xffffffff, false,
                    matrixStack.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL,
                    bgColor, combinedLight);
        }

        if (translatedLine != null) {
            float translatedLineWidth = (float) (-this.font.getWidth(translatedLine) / 2);
            translatedLine = translatedLine.formatted(transLyricColor);
            this.font.draw(translatedLine, translatedLineWidth, -y - 12, 0xffffffff, false,
                    matrixStack.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.NORMAL,
                    bgColor, combinedLight);
        }

        matrixStack.pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(TileEntityMusicPlayer blockEntity) {
        return true;
    }
}
