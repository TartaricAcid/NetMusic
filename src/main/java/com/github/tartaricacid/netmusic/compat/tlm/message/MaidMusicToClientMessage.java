package com.github.tartaricacid.netmusic.compat.tlm.message;

import com.github.tartaricacid.netmusic.client.audio.MusicPlayManager;
import com.github.tartaricacid.netmusic.compat.tlm.chatbubble.LyricChatBubbleData;
import com.github.tartaricacid.netmusic.compat.tlm.client.audio.MaidNetMusicSound;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.tartaricacid.netmusic.client.audio.MusicPlayManager.MUSIC_163_URL;

public class MaidMusicToClientMessage {
    private static final Pattern PATTERN = Pattern.compile("^.*?\\?id=(\\d+)\\.mp3$");

    private final int entityId;
    private final String url;
    private final int timeSecond;
    private final String songName;

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

    public static void encode(MaidMusicToClientMessage message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
        buf.writeUtf(message.url);
        buf.writeInt(message.timeSecond);
        buf.writeUtf(message.songName);
    }

    public static void handle(MaidMusicToClientMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> CompletableFuture.runAsync(() -> onHandle(message), Util.backgroundExecutor()));
        }
        context.setPacketHandled(true);
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
}
