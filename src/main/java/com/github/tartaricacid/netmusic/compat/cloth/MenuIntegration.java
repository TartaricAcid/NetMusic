package com.github.tartaricacid.netmusic.compat.cloth;

import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.event.ConfigEvent;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.Proxy;

public class MenuIntegration {
    public static Screen getModsConfigScreen(Screen parent) {
        return getConfigBuilder().setParentScreen(parent).build();
    }

    public static ConfigBuilder getConfigBuilder() {
        ConfigBuilder root = ConfigBuilder.create().setTitle(Component.translatable("itemGroup.netmusic"));
        root.setGlobalized(true);
        root.setGlobalizedExpanded(false);
        ConfigEntryBuilder entryBuilder = root.entryBuilder();
        generalConfig(root, entryBuilder);
        root.setSavingRunnable(ConfigEvent::reloadColors);
        return root;
    }

    @SuppressWarnings("all")
    private static void generalConfig(ConfigBuilder root, ConfigEntryBuilder entryBuilder) {
        ConfigCategory general = root.getOrCreateCategory(Component.translatable("config.netmusic.general"));

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.netmusic.general.enable_stereo"), GeneralConfig.ENABLE_STEREO.get())
                .setTooltip(Component.translatable("config.netmusic.general.enable_stereo.tooltip"))
                .setDefaultValue(GeneralConfig.ENABLE_STEREO.getDefault())
                .setSaveConsumer(GeneralConfig.ENABLE_STEREO::set)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(Component.translatable("config.netmusic.general.proxy_type"), Proxy.Type.class, GeneralConfig.PROXY_TYPE.get())
                .setTooltip(Component.translatable("config.netmusic.general.proxy_type.tooltip"))
                .setDefaultValue(GeneralConfig.PROXY_TYPE.getDefault())
                .setSaveConsumer(GeneralConfig.PROXY_TYPE::set)
                .build());

        general.addEntry(entryBuilder.startTextField(Component.translatable("config.netmusic.general.proxy_address"), GeneralConfig.PROXY_ADDRESS.get())
                .setTooltip(Component.translatable("config.netmusic.general.proxy_address.tooltip"))
                .setDefaultValue(GeneralConfig.PROXY_ADDRESS.getDefault())
                .setSaveConsumer(GeneralConfig.PROXY_ADDRESS::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.netmusic.general.enable_player_lyrics"), GeneralConfig.ENABLE_PLAYER_LYRICS.get())
                .setTooltip(Component.translatable("config.netmusic.general.enable_player_lyrics.tooltip"))
                .setDefaultValue(GeneralConfig.ENABLE_PLAYER_LYRICS.getDefault())
                .setSaveConsumer(GeneralConfig.ENABLE_PLAYER_LYRICS::set)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.netmusic.general.enable_maid_lyrics"), GeneralConfig.ENABLE_MAID_LYRICS.get())
                .setTooltip(Component.translatable("config.netmusic.general.enable_maid_lyrics.tooltip"))
                .setDefaultValue(GeneralConfig.ENABLE_MAID_LYRICS.getDefault())
                .setSaveConsumer(GeneralConfig.ENABLE_MAID_LYRICS::set)
                .build());

        general.addEntry(entryBuilder.startAlphaColorField(
                        Component.translatable("config.netmusic.general.original_player_lyrics_color"),
                        ConfigEvent.parseColor(GeneralConfig.ORIGINAL_PLAYER_LYRICS_COLOR.get()))
                .setTooltip(Component.translatable("config.netmusic.general.original_player_lyrics_color.tooltip"))
                .setDefaultValue(ConfigEvent.parseColor(GeneralConfig.ORIGINAL_PLAYER_LYRICS_COLOR.getDefault()))
                .setSaveConsumer(color -> GeneralConfig.ORIGINAL_PLAYER_LYRICS_COLOR.set(String.format("#%08X", color)))
                .build());

        general.addEntry(entryBuilder.startAlphaColorField(
                        Component.translatable("config.netmusic.general.translated_player_lyrics_color"),
                        ConfigEvent.parseColor(GeneralConfig.TRANSLATED_PLAYER_LYRICS_COLOR.get()))
                .setTooltip(Component.translatable("config.netmusic.general.translated_player_lyrics_color.tooltip"))
                .setDefaultValue(ConfigEvent.parseColor(GeneralConfig.TRANSLATED_PLAYER_LYRICS_COLOR.getDefault()))
                .setSaveConsumer(color -> GeneralConfig.TRANSLATED_PLAYER_LYRICS_COLOR.set(String.format("#%08X", color)))
                .build());

        general.addEntry(entryBuilder.startAlphaColorField(
                        Component.translatable("config.netmusic.general.original_maid_lyrics_color"),
                        ConfigEvent.parseColor(GeneralConfig.ORIGINAL_MAID_LYRICS_COLOR.get()))
                .setTooltip(Component.translatable("config.netmusic.general.original_maid_lyrics_color.tooltip"))
                .setDefaultValue(ConfigEvent.parseColor(GeneralConfig.ORIGINAL_MAID_LYRICS_COLOR.getDefault()))
                .setSaveConsumer(color -> GeneralConfig.ORIGINAL_MAID_LYRICS_COLOR.set(String.format("#%08X", color)))
                .build());

        general.addEntry(entryBuilder.startAlphaColorField(
                        Component.translatable("config.netmusic.general.translated_maid_lyrics_color"),
                        ConfigEvent.parseColor(GeneralConfig.TRANSLATED_MAID_LYRICS_COLOR.get()))
                .setTooltip(Component.translatable("config.netmusic.general.translated_maid_lyrics_color.tooltip"))
                .setDefaultValue(ConfigEvent.parseColor(GeneralConfig.TRANSLATED_MAID_LYRICS_COLOR.getDefault()))
                .setSaveConsumer(color -> GeneralConfig.TRANSLATED_MAID_LYRICS_COLOR.set(String.format("#%08X", color)))
                .build());
    }
}
