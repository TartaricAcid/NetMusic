package com.github.tartaricacid.netmusic.compat.tlm.ai;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.search.NeteaseMusicSearch;
import com.github.tartaricacid.netmusic.api.search.SearchResponse;
import com.github.tartaricacid.netmusic.compat.tlm.backpack.MusicPlayerBackpack;
import com.github.tartaricacid.netmusic.compat.tlm.message.MaidMusicToClientMessage;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.IFunctionCall;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.response.ToolResponse;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.StringParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.llm.openai.request.ChatCompletion;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.google.gson.Gson;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PlaySoundFunction implements IFunctionCall<PlaySoundFunction.Result> {
    public static final String ID = "netmusic:play_sound";
    private static final String DESCRIPTION = """
            When I mention something related to playing music.
            Please extract the song keywords from my words and call this function to search and play the music.""";
    private static final String SEARCH_TEXT_TITLE = "Song Keywords";
    private static final String SEARCH_TEXT = "search_text";
    private static final String ERROR_TIMEOUT = "Search music timed out";
    private static final String ERROR_INTERRUPTED = "Search music was interrupted";
    private static final String ERROR_NETWORK = "A network error occurred while searching for music";
    private static final String ERROR_MUSIC_NOT_FOUND = "Music not found for the given keywords: ";
    private static final String ERROR_PARSE_JSON = "Failed to parse search response";
    private static final String PLAY_SUCCESS = "Successfully started playing music: ";
    private static final Gson GSON = new Gson();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription(EntityMaid maid) {
        return DESCRIPTION;
    }

    @Override
    public boolean addToChatCompletion(EntityMaid maid, ChatCompletion chatCompletion) {
        return maid.getMaidBackpackType().getId().equals(MusicPlayerBackpack.ID);
    }

    @Override
    public Parameter addParameters(ObjectParameter root, EntityMaid maid) {
        StringParameter searchText = StringParameter.create().setTitle(SEARCH_TEXT_TITLE);
        root.addProperties(SEARCH_TEXT, searchText);
        return root;
    }

    @Override
    public Codec<Result> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf(SEARCH_TEXT).forGetter(Result::searchText)
        ).apply(instance, Result::new));
    }

    @Override
    public ToolResponse onToolCall(Result result, EntityMaid maid) {
        CountDownLatch latch = new CountDownLatch(1);
        String searchText = result.searchText().trim();
        String[] toolResponseText = new String[1];
        NeteaseMusicSearch.searchFirstSong(searchText, (response, throwable) ->
                tryToPlayMusic(maid, response, throwable, toolResponseText, searchText, latch));
        try {
            // 最多等待 5 秒
            if (!latch.await(5, TimeUnit.SECONDS)) {
                return new ToolResponse(ERROR_TIMEOUT);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ToolResponse(ERROR_INTERRUPTED);
        }
        return new ToolResponse(toolResponseText[0]);
    }

    private void tryToPlayMusic(EntityMaid maid, HttpResponse<String> response, Throwable throwable,
                                String[] toolResponseText, String searchText, CountDownLatch latch) {
        try {
            if (throwable != null) {
                toolResponseText[0] = ERROR_NETWORK;
                NetMusic.LOGGER.error(throwable);
                return;
            }
            if (response.statusCode() != 200) {
                toolResponseText[0] = ERROR_NETWORK;
                NetMusic.LOGGER.error("Search request failed with status code: {}", response.statusCode());
                return;
            }
            SearchResponse searchResponse = GSON.fromJson(response.body(), SearchResponse.class);
            SearchResponse.Song songResult = searchResponse.getFirstSong();
            if (songResult == null) {
                toolResponseText[0] = ERROR_MUSIC_NOT_FOUND + searchText;
                return;
            }
            MaidMusicToClientMessage msg = new MaidMusicToClientMessage(maid.getId(), songResult.getUrl(),
                    songResult.getTimeSecond(), songResult.getName());
            MaidMusicToClientMessage.showLyric(maid, songResult.getUrl(), songResult.getName(), songResult.getTimeSecond());
            NetworkHandler.sendToNearBy(maid.level(), maid.blockPosition(), msg);
            toolResponseText[0] = PLAY_SUCCESS + songResult.getName();
        } catch (Exception e) {
            toolResponseText[0] = ERROR_PARSE_JSON;
            NetMusic.LOGGER.error("Failed to parse search response", e);
        } finally {
            latch.countDown();
        }
    }

    public record Result(String searchText) {
    }
}
