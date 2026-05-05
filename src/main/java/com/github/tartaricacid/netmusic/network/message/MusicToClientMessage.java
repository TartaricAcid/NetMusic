package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.lyric.LyricParser;
import com.github.tartaricacid.netmusic.api.lyric.LyricRecord;
import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.client.audio.NetMusicSound;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.MUSIC_163_URL;

public class MusicToClientMessage {
    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");

    private final BlockPos pos;
    private final String url;
    private final String rawUrl;
    private final int timeSecond;
    private final String songName;

    public MusicToClientMessage(BlockPos pos, String url, String rawUrl, int timeSecond, String songName) {
        this.pos = pos;
        this.url = url;
        this.rawUrl = StringUtils.defaultIfBlank(rawUrl, url);
        this.timeSecond = timeSecond;
        this.songName = songName;
    }

    public static MusicToClientMessage decode(FriendlyByteBuf buf) {
        return new MusicToClientMessage(BlockPos.of(buf.readLong()), buf.readUtf(), buf.readUtf(), buf.readInt(), buf.readUtf());
    }

    public static void encode(MusicToClientMessage message, FriendlyByteBuf buf) {
        buf.writeLong(message.pos.asLong());
        buf.writeUtf(message.url);
        buf.writeUtf(message.rawUrl);
        buf.writeInt(message.timeSecond);
        buf.writeUtf(message.songName);
    }

    public static void handle(MusicToClientMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor()));
        }
        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void onHandle(MusicToClientMessage message) {
        // 使用数组方便在 lambda 表达式中修改
        LyricRecord[] record = new LyricRecord[1];

        // 如果是网易云的音乐，那么尝试添加歌词
        if (GeneralConfig.ENABLE_PLAYER_LYRICS.get() && message.url.startsWith(MUSIC_163_URL)) {
            Matcher matcher = PATTERN.matcher(message.url);
            if (matcher.find()) {
                long musicId = Long.parseLong(matcher.group(1));
                try {
                    String lyric = NetMusic.NET_EASE_WEB_API.lyric(musicId);
                    record[0] = LyricParser.parseLyric(lyric, message.songName);
                } catch (IOException e) {
                    NetMusic.LOGGER.error("Failed to load lyric for music id: {}", musicId, e);
                }
            }
        }

        MusicPlayManager.play(message.url, message.songName, url -> new NetMusicSound(message.pos, url, message.timeSecond, record[0]));
    }
}
