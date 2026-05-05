package com.github.tartaricacid.netmusic.compat.tlm.ai;

import com.github.tartaricacid.netmusic.api.resolver.MusicPlayResolverManager;
import com.github.tartaricacid.netmusic.api.search.NeteaseMusicSearch;
import com.github.tartaricacid.netmusic.api.search.SearchResponse;
import com.github.tartaricacid.netmusic.compat.tlm.backpack.MusicPlayerBackpack;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidStopMusicMessage;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.ai.agent.tool.ITool;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.LLMClient;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.gson.Gson;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayMusicTool implements ITool<String> {
    public static final String ID = "play_music";
    private static final String DESCRIPTION = "Identify music playback intent and extract keywords to search and play";

    private static final int TOTAL_TIMEOUT_SECONDS = 5;
    private static final String KEYWORDS_ID = "keywords";

    private static final Codec<String> CODEC = Codec.STRING.fieldOf(KEYWORDS_ID).codec();
    private static final Gson GSON = new Gson();

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String summary(EntityMaid maid) {
        return DESCRIPTION;
    }

    @Override
    public Parameter parameters(ObjectParameter root, EntityMaid maid) {
        root.addProperties(KEYWORDS_ID, StringParameter.create());
        return root;
    }

    @Override
    public Codec<String> codec() {
        return CODEC;
    }

    @Override
    public LLMCallback onCall(String toolCallId, String result, LLMCallback callback) {
        // 这个工具是异步触发的，不会调用此方法
        return callback;
    }

    @Override
    public Component invocationSummaryComponent(String result) {
        return Component.translatable("ai.netmusic.tool.play_music.search", result);
    }

    @Override
    public CompletableFuture<LLMCallback> onCallAsync(String toolCallId, String result, LLMCallback callback, LLMClient client) {
        // 女仆必须穿戴唱片背包才可以播放
        if (!(callback.getMaid().getMaidBackpackType() instanceof MusicPlayerBackpack)) {
            LLMCallback toolResult = callback.addToolResult("Must wear a music player backpack to play music", toolCallId);
            return CompletableFuture.completedFuture(toolResult);
        }

        String keywords = StringUtils.trimToEmpty(result);
        if (StringUtils.isBlank(keywords)) {
            LLMCallback toolResult = callback.addToolResult("Error: keywords is blank", toolCallId);
            return CompletableFuture.completedFuture(toolResult);
        }

        return NeteaseMusicSearch.searchSongs(keywords)
                .orTimeout(TOTAL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .handleAsync((response, throwable) ->
                        playMusic(response, throwable, toolCallId, callback), Runnable::run)
                .thenCompose(f -> f);
    }

    private CompletableFuture<LLMCallback> playMusic(HttpResponse<String> response, Throwable throwable, String toolCallId, LLMCallback callback) {
        CompletableFuture<LLMCallback> finalResult = new CompletableFuture<>();
        callback.runOnServerThread(() -> {
            if (throwable != null) {
                String error = "Error: " + throwable.getMessage();
                finalResult.complete(callback.addToolResult(error, toolCallId));
                return;
            }

            if (response.statusCode() != 200) {
                String error = "Network error, Http error code: " + response.statusCode();
                finalResult.complete(callback.addToolResult(error, toolCallId));
                return;
            }

            SearchResponse searchResponse = GSON.fromJson(response.body(), SearchResponse.class);
            SearchResponse.Song songResult = searchResponse.getFirstSong();
            if (songResult == null) {
                String error = "Error: could not find any song";
                finalResult.complete(callback.addToolResult(error, toolCallId));
                return;
            }

            EntityMaid maid = callback.getMaid();
            BlockPos blockPos = maid.blockPosition();
            String name = songResult.getName();
            String artists = songResult.getArtistNames();

            // 先发送停止信息，避免先前的曲子没有关闭
            MaidStopMusicMessage stopMsg = MaidStopMusicMessage.create(maid);
            NetworkHandler.sendToNearby(maid.level(), blockPos, stopMsg);

            // 音乐播放的，延迟 3 秒播放，让 LLM 先说完话，再播放音乐
            var delayedExecutor = CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS);

            String url = songResult.getUrl();
            int timeSecond = songResult.getTimeSecond();
            ItemMusicCD.SongInfo info = new ItemMusicCD.SongInfo(url, name, timeSecond, false);

            CompletableFuture.runAsync(() -> {
                        // 留空仅作为延迟触发点
                    }, delayedExecutor)
                    .thenCompose(v -> MusicPlayResolverManager.resolve(info.clone()))
                    .thenAccept(resolved -> callback.runOnServerThread(() -> {
                        MaidMusicToClientMessage msg = new MaidMusicToClientMessage(
                                maid.getId(), resolved.songUrl, info.songUrl,
                                resolved.songTime, resolved.songName
                        );
                        MaidMusicToClientMessage.showLyric(maid, info.songUrl, resolved.songName, resolved.songTime);
                        NetworkHandler.sendToNearby(maid.level(), blockPos, msg);
                    }));

            if (StringUtils.isNotBlank(artists)) {
                String success = "Successfully playing: %s, Artist: %s".formatted(name, artists);
                finalResult.complete(callback.addToolResult(success, toolCallId));
            } else {
                String success = "Successfully playing: " + name;
                finalResult.complete(callback.addToolResult(success, toolCallId));
            }
        });
        return finalResult;
    }
}
