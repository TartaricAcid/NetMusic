package com.github.tartaricacid.netmusic.client.config;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.api.ExtraMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicList;
import com.github.tartaricacid.netmusic.api.pojo.NetEaseMusicSong;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MusicListManage {
    private static final int MAX_NUM = 100;
    private static final Gson GSON = new Gson();
    private static final Path CONFIG_DIR = Paths.get("config").resolve("net_music");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("music.json");
    public static List<ItemMusicCD.SongInfo> SONGS = Lists.newArrayList();

    public static void loadConfigSongs() throws IOException {
        if (!Files.isDirectory(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }

        File file = CONFIG_FILE.toFile();
        InputStream stream = null;
        if (Files.exists(CONFIG_FILE)) {
            stream = Files.newInputStream(file.toPath());
        } else {
            ResourceLocation res = new ResourceLocation(NetMusic.MOD_ID, "music.json");
            stream = Minecraft.getInstance().getResourceManager().getResource(res).getInputStream();
        }

        SONGS = GSON.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8),
                new TypeToken<List<ItemMusicCD.SongInfo>>() {
                }.getType());
    }

    public static ItemMusicCD.SongInfo get163Song(long id) throws Exception {
        NetEaseMusicSong pojo = GSON.fromJson(NetMusic.NET_EASE_WEB_API.song(id), NetEaseMusicSong.class);
        return new ItemMusicCD.SongInfo(pojo);
    }

    public static void add163List(long id) throws Exception {
        if (!Files.isDirectory(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }

        NetEaseMusicList pojo = GSON.fromJson(NetMusic.NET_EASE_WEB_API.list(id), NetEaseMusicList.class);

        int count = pojo.getPlayList().getTracks().size();
        int size = Math.min(pojo.getPlayList().getTrackIds().size(), MAX_NUM);
        // 获取额外歌曲
        if (count < size) {
            long[] ids = new long[size - count];
            for (int i = count; i < size; i++) {
                ids[i - count] = pojo.getPlayList().getTrackIds().get(i).getId();
            }
            String extraTrackInfo = NetMusic.NET_EASE_WEB_API.songs(ids);
            ExtraMusicList extra = GSON.fromJson(extraTrackInfo, ExtraMusicList.class);
            pojo.getPlayList().getTracks().addAll(extra.getTracks());
        }

        SONGS.clear();
        for (NetEaseMusicList.Track track : pojo.getPlayList().getTracks()) {
            SONGS.add(new ItemMusicCD.SongInfo(track));
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        FileUtils.write(CONFIG_FILE.toFile(), gson.toJson(SONGS), StandardCharsets.UTF_8);
    }
}
