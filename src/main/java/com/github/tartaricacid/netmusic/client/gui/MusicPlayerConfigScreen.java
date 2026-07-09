package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

/**
 * 唱片机配置界面：MUSIC_U Cookie 和音质设置
 * <p>
 * 通过 Shift + 空手右键唱片机打开。
 */
public class MusicPlayerConfigScreen extends Screen {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/gui/cd_burner.png");

    private EditBox cookieField;
    private int selectedQualityIndex = 2; // 默认320k

    private static final int[] QUALITY_VALUES = {128000, 192000, 320000, 999000};
    private static final String[] QUALITY_NAMES = {"128k", "192k", "320k", "无损"};

    public MusicPlayerConfigScreen() {
        super(Component.translatable("gui.netmusic.config.title"));
    }

    @Override
    protected void init() {
        super.init();

        // 初始化音质选择索引
        int currentQuality = GeneralConfig.MUSIC_QUALITY.get();
        selectedQualityIndex = 2; // 默认320k
        for (int i = 0; i < QUALITY_VALUES.length; i++) {
            if (QUALITY_VALUES[i] == currentQuality) {
                selectedQualityIndex = i;
                break;
            }
        }

        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;

        // MUSIC_U Cookie 输入框
        this.cookieField = new EditBox(Minecraft.getInstance().font, centerX - 120, startY + 20, 240, 16, Component.literal("MUSIC_U Cookie"));
        this.cookieField.setMaxLength(1024);
        this.cookieField.setValue(GeneralConfig.MUSIC_U.get());
        this.cookieField.setHint(Component.translatable("gui.netmusic.config.cookie_hint"));
        this.addRenderableWidget(this.cookieField);

        // 音质选择按钮
        this.addRenderableWidget(Button.builder(getQualityButtonText(), (b) -> {
            selectedQualityIndex = (selectedQualityIndex + 1) % QUALITY_VALUES.length;
            b.setMessage(getQualityButtonText());
        }).pos(centerX - 60, startY + 50).size(120, 20).build());

        // 保存按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.config.save"), (b) -> {
            saveConfig();
            this.onClose();
        }).pos(centerX - 65, startY + 85).size(60, 20).build());

        // 取消按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.config.cancel"), (b) -> {
            this.onClose();
        }).pos(centerX + 5, startY + 85).size(60, 20).build());
    }

    private Component getQualityButtonText() {
        return Component.translatable("gui.netmusic.config.quality", QUALITY_NAMES[selectedQualityIndex]);
    }

    private void saveConfig() {
        String cookie = this.cookieField.getValue().trim();
        GeneralConfig.MUSIC_U.set(cookie);
        GeneralConfig.MUSIC_QUALITY.set(QUALITY_VALUES[selectedQualityIndex]);

        // 保存配置到文件
        GeneralConfig.MUSIC_U.save();
        GeneralConfig.MUSIC_QUALITY.save();

        NetMusic.LOGGER.info("NetMusic config saved: MUSIC_U={}, Quality={}", 
                StringUtils.isBlank(cookie) ? "(empty)" : "***", QUALITY_VALUES[selectedQualityIndex]);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 渲染半透明背景
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;

        // 标题
        graphics.drawCenteredString(this.font, Component.translatable("gui.netmusic.config.title"), centerX, startY - 10, 0xFFFFFF);

        // Cookie 标签
        graphics.drawString(this.font, Component.translatable("gui.netmusic.config.cookie_label"), centerX - 120, startY + 8, 0xAAAAAA);

        // 音质标签
        graphics.drawString(this.font, Component.translatable("gui.netmusic.config.quality_label"), centerX - 120, startY + 54, 0xAAAAAA);

        // 说明文字
        graphics.drawWordWrap(this.font, Component.translatable("gui.netmusic.config.cookie_desc"), centerX - 120, startY + 115, 240, 0x888888);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.cookieField.isFocused()) {
            return this.cookieField.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (this.cookieField.isFocused()) {
            return this.cookieField.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}