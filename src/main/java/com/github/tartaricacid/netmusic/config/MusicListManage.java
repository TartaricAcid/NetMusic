package com.github.tartaricacid.netmusic.config;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.ExtraMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.PlayMode;
import com.github.tartaricacid.netmusic.item.PlaylistData;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MusicListManage {
    /** 未登录时歌单最大歌曲数 */
    private static final int MAX_NUM_NO_LOGIN = 100;
    /** 登录后歌单最大歌曲数 */
    private static final int MAX_NUM_LOGGED_IN = 1000;
    private static final Gson GSON = new Gson();
    private static final Path CONFIG_DIR = Paths.get("config").resolve("net_music");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("music.json");
    public static List<ItemMusicCD.SongInfo> SONGS = Lists.newArrayList();

    @OnlyIn(Dist.CLIENT)
    public static void loadConfigSongs() throws IOException {
        loadConfigSongs(Minecraft.getInstance().getResourceManager());
    }

    public static void loadConfigSongs(ResourceManager manager) throws IOException {
        if (!Files.isDirectory(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }

        File file = CONFIG_FILE.toFile();
        InputStream stream = null;
        if (Files.exists(CONFIG_FILE)) {
            stream = Files.newInputStream(file.toPath());
        } else {
            ResourceLocation res = ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "music.json");
            Optional<Resource> optional = manager.getResource(res);
            if (optional.isPresent()) {
                stream = optional.get().open();
            }
        }
        if (stream != null) {
            SONGS = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
                    new TypeToken<List<ItemMusicCD.SongInfo>>() {
                    }.getType());
        }
    }

    public static ItemMusicCD.SongInfo get163Song(long id) throws Exception {
        NetEaseMusicSong pojo = GSON.fromJson(NetMusic.NET_EASE_WEB_API.song(id), NetEaseMusicSong.class);
        return new ItemMusicCD.SongInfo(pojo);
    }

    public static ItemMusicCD.SongInfo getDjSong(long id) throws Exception {
        String result = NetMusic.NET_EASE_WEB_API.dj(id);
        JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
        JsonObject program = jsonObject.getAsJsonObject("program");
        if (program == null) {
            NetMusic.LOGGER.error("Failed to get DJ song info, program is null for id: {}", id);
            return new ItemMusicCD.SongInfo();
        }
        String mainSong = program.getAsJsonObject("mainSong").toString();
        if (mainSong == null) {
            NetMusic.LOGGER.error("Failed to get DJ song info, mainSong is null for id: {}", id);
            return new ItemMusicCD.SongInfo();
        }
        NetEaseMusicSong.Song netEaseMusicSong = new Gson().fromJson(mainSong, NetEaseMusicSong.Song.class);
        return new ItemMusicCD.SongInfo(netEaseMusicSong);
    }

    public static void add163List(long id) throws Exception {
        if (!Files.isDirectory(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }

        NetEaseMusicList pojo = GSON.fromJson(NetMusic.NET_EASE_WEB_API.list(id), NetEaseMusicList.class);

        int count = pojo.getPlayList().getTracks().size();
        int maxNum = GeneralConfig.MUSIC_U.get().isEmpty() ? MAX_NUM_NO_LOGIN : MAX_NUM_LOGGED_IN;
        int size = Math.min(pojo.getPlayList().getTrackIds().size(), maxNum);
        // 分批获取额外歌曲（每批最多50首，避免URL过长导致API截断）
        if (count < size) {
            List<Long> extraIds = new ArrayList<>();
            for (int i = count; i < size; i++) {
                extraIds.add(pojo.getPlayList().getTrackIds().get(i).getId());
            }
            fetchExtraTracks(extraIds, pojo);
        }

        SONGS.clear();
        for (NetEaseMusicList.Track track : pojo.getPlayList().getTracks()) {
            SONGS.add(new ItemMusicCD.SongInfo(track));
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        FileUtils.write(CONFIG_FILE.toFile(), gson.toJson(SONGS), StandardCharsets.UTF_8);
    }

    /**
     * 解析网易云歌单，返回 PlaylistData 对象
     * <p>
     * 此方法用于唱片刻录机解析歌单ID，生成播放列表唱片。
     * 歌曲URL使用语义URL（https://music.163.com/song/media/outer/url?id=xxx.mp3），
     * 播放时由 NetEaseSongUrlResolver 懒加载解析为真实直链。
     *
     * @param id 歌单ID
     * @return PlaylistData 播放列表数据
     * @throws Exception 网络请求或解析异常
     */
    public static PlaylistData get163Playlist(long id) throws Exception {
        NetEaseMusicList pojo = GSON.fromJson(NetMusic.NET_EASE_WEB_API.list(id), NetEaseMusicList.class);

        int count = pojo.getPlayList().getTracks().size();
        int maxNum = GeneralConfig.MUSIC_U.get().isEmpty() ? MAX_NUM_NO_LOGIN : MAX_NUM_LOGGED_IN;
        int size = Math.min(pojo.getPlayList().getTrackIds().size(), maxNum);
        // 分批获取额外歌曲（每批最多50首，避免URL过长导致API截断）
        if (count < size) {
            List<Long> extraIds = new ArrayList<>();
            for (int i = count; i < size; i++) {
                extraIds.add(pojo.getPlayList().getTrackIds().get(i).getId());
            }
            fetchExtraTracks(extraIds, pojo);
        }

        // 构建播放列表
        String playlistName = pojo.getPlayList().getName();
        PlaylistData.Builder builder = new PlaylistData.Builder().playlistName(playlistName);
        for (NetEaseMusicList.Track track : pojo.getPlayList().getTracks()) {
            builder.addSong(new ItemMusicCD.SongInfo(track));
        }
        return builder.build();
    }

    /**
     * 分批获取额外歌曲信息，每批最多BATCH_SIZE首
     * <p>
     * 网易云API的songs()接口通过URL传参，ID过多会导致URL超长被截断，
     * 因此需要分批请求并合并结果。
     *
     * @param extraIds 需要额外获取的歌曲ID列表
     * @param pojo     歌单数据对象，获取的歌曲将添加到其tracks中
     */
    private static void fetchExtraTracks(List<Long> extraIds, NetEaseMusicList pojo) {
        int batchSize = 50;
        for (int i = 0; i < extraIds.size(); i += batchSize) {
            int end = Math.min(i + batchSize, extraIds.size());
            long[] batchIds = new long[end - i];
            for (int j = 0; j < batchIds.length; j++) {
                batchIds[j] = extraIds.get(i + j);
            }
            try {
                String extraTrackInfo = NetMusic.NET_EASE_WEB_API.songs(batchIds);
                ExtraMusicList extra = GSON.fromJson(extraTrackInfo, ExtraMusicList.class);
                if (extra.getTracks() != null) {
                    pojo.getPlayList().getTracks().addAll(extra.getTracks());
                    NetMusic.LOGGER.debug("Fetched batch {}/{} extra tracks: {} songs",
                            (i / batchSize + 1), (extraIds.size() + batchSize - 1) / batchSize, extra.getTracks().size());
                }
            } catch (Exception e) {
                NetMusic.LOGGER.error("Failed to fetch extra tracks batch starting at index {}: {}", i, e.getMessage());
            }
        }
    }
}
