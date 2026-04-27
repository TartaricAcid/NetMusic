package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class BigMegaphonePresetManager {
    private static final ResourceLocation PRESET_FILE = new ResourceLocation(NetMusic.MOD_ID, "broadcasting_presets.json");
    private static final Gson GSON = new Gson();

    private static List<PresetStation> STATIONS = Lists.newArrayList();

    private BigMegaphonePresetManager() {
    }

    @OnlyIn(Dist.CLIENT)
    public static void loadBundledStations() throws IOException {
        loadBundledStations(Minecraft.getInstance().getResourceManager());
    }

    public static void loadBundledStations(ResourceManager manager) throws IOException {
        STATIONS = Lists.newArrayList();
        Optional<Resource> optional = manager.getResource(PRESET_FILE);
        if (optional.isEmpty()) {
            return;
        }
        try (InputStreamReader reader = new InputStreamReader(optional.get().open(), StandardCharsets.UTF_8)) {
            List<PresetStation> loaded = GSON.fromJson(reader, new TypeToken<List<PresetStation>>() {
            }.getType());
            if (loaded != null) {
                List<PresetStation> sanitized = Lists.newArrayList();
                for (PresetStation station : loaded) {
                    if (station == null || station.name() == null || station.url() == null) {
                        continue;
                    }
                    String name = station.name().trim();
                    String url = station.url().trim();
                    if (Util.isBlank(name) || !BigMegaphoneUtil.isValidStreamUrl(url)) {
                        continue;
                    }
                    sanitized.add(new PresetStation(name, url));
                }
                STATIONS = Collections.unmodifiableList(sanitized);
            }
        }
    }

    public static List<PresetStation> getStations() {
        return STATIONS;
    }

    public record PresetStation(String name, String url) {
    }
}
