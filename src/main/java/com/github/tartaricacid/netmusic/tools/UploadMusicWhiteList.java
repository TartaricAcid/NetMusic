package com.github.tartaricacid.netmusic.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.mojang.text2speech.Narrator.LOGGER;

public class UploadMusicWhiteList {
    private static Gson gson = new GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    /**
     * 加载配置文件
     * @return 配置对象
     */
    public static WhiteList loadWhiteList() {
        final String filePath = "upload_music_whitelist.json";
        Path path = Paths.get(filePath);

        // 如果文件不存在，创建默认配置并保存
        if (!Files.exists(path)) {
            WhiteList defaultWhiteList = new WhiteList();
            saveWhiteList(defaultWhiteList);
            return defaultWhiteList;
        }

        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(filePath), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, WhiteList.class);
        } catch (IOException e) {
            LOGGER.error("Error loading whiteList file: {}", e.getMessage());
            return new WhiteList(); // 返回默认配置
        }
    }

    /**
     * 保存配置文件
     * @param whiteList 配置对象
     */
    public static void saveWhiteList(WhiteList whiteList) {
        final String filePath = "upload_music_whitelist.json";
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(whiteList, writer);
        } catch (IOException e) {
            LOGGER.error("Error saving whiteList file: {}", e.getMessage());
        }
    }

    public static class WhiteList {
        @SerializedName("whitelist")
        private List<String> whitelist = List.of();

        public List<String> getWhitelist() {
            return whitelist;
        }
    }
}
