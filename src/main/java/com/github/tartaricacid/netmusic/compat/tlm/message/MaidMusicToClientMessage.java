package com.github.tartaricacid.netmusic.compat.tlm.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.tlm.chatbubble.LyricChatBubbleData;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.network.message.Message;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.MUSIC_163_URL;

public class MaidMusicToClientMessage implements Message {
    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "maid_music_to_client");
    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");

    public final int entityId;
    public final String url;
    public final int timeSecond;
    public final String songName;

    public MaidMusicToClientMessage(int entityId, String url, int timeSecond, String songName) {
        this.entityId = entityId;
        this.url = url;
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

    public static MaidMusicToClientMessage decode(FriendlyByteBuf buf) {
        return new MaidMusicToClientMessage(buf.readInt(), buf.readUtf(), buf.readInt(), buf.readUtf());
    }

    @Override
    public FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(entityId);
        buf.writeUtf(url);
        buf.writeInt(timeSecond);
        buf.writeUtf(songName);
        return buf;
    }

    @Override
    public ResourceLocation getPacketId() {
        return ID;
    }
}
