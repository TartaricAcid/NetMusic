package com.github.tartaricacid.netmusic.compat.cloth;

import com.github.tartaricacid.netmusic.config.GeneralConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.net.Proxy;


public class MenuIntegration {
    public static Screen getModsConfigScreen(Screen parent) {
        return getConfigBuilder().setParentScreen(parent)
                .setSavingRunnable(GeneralConfig.INSTANCE::save).build();
    }

    public static ConfigBuilder getConfigBuilder() {
        ConfigBuilder root = ConfigBuilder.create().setTitle(Text.translatable("itemGroup.netmusic"));
        root.setGlobalized(true);
        root.setGlobalizedExpanded(false);
        ConfigEntryBuilder entryBuilder = root.entryBuilder();
        generalConfig(root, entryBuilder);
        return root;
    }

    @SuppressWarnings("all")
    private static void generalConfig(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory general = root.getOrCreateCategory(Text.translatable("config.netmusic.general"));

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netmusic.general.enable_stereo"), GeneralConfig.ENABLE_STEREO)
                .setTooltip(Text.translatable("config.netmusic.general.enable_stereo.tooltip"))
                .setDefaultValue(true)
                .setSaveConsumer(value -> GeneralConfig.ENABLE_STEREO = value)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(Text.translatable("config.netmusic.general.proxy_type"), Proxy.Type.class, GeneralConfig.PROXY_TYPE)
                .setTooltip(Text.translatable("config.netmusic.general.proxy_type.tooltip"))
                .setDefaultValue(Proxy.Type.DIRECT)
                .setSaveConsumer(value -> GeneralConfig.PROXY_TYPE = value)
                .build());

        general.addEntry(entryBuilder.startTextField(Text.translatable("config.netmusic.general.proxy_address"), GeneralConfig.PROXY_ADDRESS)
                .setTooltip(Text.translatable("config.netmusic.general.proxy_address.tooltip"))
                .setDefaultValue("")
                .setSaveConsumer(value -> GeneralConfig.PROXY_ADDRESS = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("config.netmusic.general.enable_player_lyrics"), GeneralConfig.ENABLE_PLAYER_LYRICS)
                .setTooltip(Text.translatable("config.netmusic.general.enable_player_lyrics.tooltip"))
                .setDefaultValue(true)
                .setSaveConsumer(value -> GeneralConfig.ENABLE_PLAYER_LYRICS = value)
                .build());
    }
}

