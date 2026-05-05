package com.github.tartaricacid.netmusic.compat.tlm.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.compat.tlm.chatbubble.LyricChatBubbleData;
import com.github.tartaricacid.netmusic.compat.tlm.client.audio.MaidNetMusicSound;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.MUSIC_163_URL;

public class MaidMusicToClientMessage implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MaidMusicToClientMessage> TYPE = new CustomPacketPayload.Type<>(
            ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "maid_music_to_client"));

    public static final StreamCodec<ByteBuf, MaidMusicToClientMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MaidMusicToClientMessage::getEntityId,
            ByteBufCodecs.STRING_UTF8, MaidMusicToClientMessage::getUrl,
            ByteBufCodecs.STRING_UTF8, MaidMusicToClientMessage::getRawUrl,
            ByteBufCodecs.VAR_INT, MaidMusicToClientMessage::getTimeSecond,
            ByteBufCodecs.STRING_UTF8, MaidMusicToClientMessage::getSongName,
            MaidMusicToClientMessage::new);

    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");

    private final int entityId;
    private final String url;
    private final String rawUrl;
    private final int timeSecond;
    private final String songName;

    public MaidMusicToClientMessage(int entityId, String url, String rawUrl, int timeSecond, String songName) {
        this.entityId = entityId;
        this.url = url;
        this.rawUrl = StringUtils.defaultIfBlank(rawUrl, url);
        this.timeSecond = timeSecond;
        this.songName = songName;
    }

    public static void showLyric(EntityMaid maid, String url, String songName, int timeSecond) {
        // 如果是网易云的音乐，那么尝试添加歌词
        if (GeneralConfig.ENABLE_MAID_LYRICS.get() && url.startsWith(MUSIC_163_URL)) {
            Matcher matcher = PATTERN.matcher(url);
            if (matcher.find()) {
                long musicId = Long.parseLong(matcher.group(1));
                int songTimeTick = timeSecond * 20 + 20;
                long gameTime = maid.level().getGameTime();
                LyricChatBubbleData bubbleData = new LyricChatBubbleData(musicId, songName, songTimeTick, gameTime);
                maid.getChatBubbleManager().addChatBubble(bubbleData);
            }
        }
    }

    public static void handle(MaidMusicToClientMessage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(MaidMusicToClientMessage message) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(message.entityId);
        if (!(entity instanceof EntityMaid maid)) {
            return;
        }
        MusicPlayManager.play(message.url, message.songName, url -> new MaidNetMusicSound(maid, url, message.timeSecond));
    }

    public int getEntityId() {
        return entityId;
    }

    public String getUrl() {
        return url;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public int getTimeSecond() {
        return timeSecond;
    }

    public String getSongName() {
        return songName;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
