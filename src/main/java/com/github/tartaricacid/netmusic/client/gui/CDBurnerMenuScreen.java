package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.client.network.ClientNetWorkHandler;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@IPNIgnore
public class CDBurnerMenuScreen extends AbstractContainerScreen<CDBurnerMenu> {
    private static final ResourceLocation BG = new ResourceLocation(NetMusic.MOD_ID, "textures/gui/cd_burner.png");
    private static final Pattern ID_REG = Pattern.compile("^\\d{4,}$");
    private static final Pattern DJ_ID_REG = Pattern.compile("^dj/(\\d+)$");
    private static final Pattern URL_1_REG = Pattern.compile("^https://music\\.163\\.com/song\\?id=(\\d+).*$");
    private static final Pattern URL_2_REG = Pattern.compile("^https://music\\.163\\.com/#/song\\?id=(\\d+).*$");
    private static final Pattern DJ_URL_1_REG = Pattern.compile("^https://music\\.163\\.com/dj\\?id=(\\d+).*$");
    private static final Pattern DJ_URL_2_REG = Pattern.compile("^https://music\\.163\\.com/#/dj\\?id=(\\d+).*$");
    private EditBox textField;
    private Checkbox readOnlyButton;
    private Component tips = Component.empty();

    public CDBurnerMenuScreen(CDBurnerMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageHeight = 176;
    }

    @Override
    protected void init() {
        super.init();

        String perText = "";
        boolean focus = false;
        if (textField != null) {
            perText = textField.getValue();
            focus = textField.isFocused();
        }
        textField = new EditBox(minecraft.font, leftPos + 12, topPos + 18, 132, 16, Component.literal("Music ID Box")) {
            @Override
            public void insertText(String text) {
                Matcher matcher1 = URL_1_REG.matcher(text);
                if (matcher1.find()) {
                    String group = matcher1.group(1);
                    super.insertText(group);
                    return;
                }

                Matcher matcher2 = URL_2_REG.matcher(text);
                if (matcher2.find()) {
                    String group = matcher2.group(1);
                    super.insertText(group);
                    return;
                }

                Matcher matcher3 = DJ_URL_1_REG.matcher(text);
                if (matcher3.find()) {
                    String group = matcher3.group(1);
                    super.insertText("dj/" + group);
                    return;
                }

                Matcher matcher4 = DJ_URL_2_REG.matcher(text);
                if (matcher4.find()) {
                    String group = matcher4.group(1);
                    super.insertText("dj/" + group);
                    return;
                }

                super.insertText(text);
            }
        };
        textField.setValue(perText);
        textField.setBordered(false);
        textField.setMaxLength(19);
        textField.setTextColor(0xF3EFE0);
        textField.setFocused(focus);
        textField.moveCursorToEnd();
        this.addWidget(this.textField);

        this.readOnlyButton = new Checkbox(leftPos + 66, topPos + 34, 80, 20, Component.translatable("gui.netmusic.cd_burner.read_only"), false);
        this.addRenderableWidget(this.readOnlyButton);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.cd_burner.craft"), (b) -> handleCraftButton())
                .pos(leftPos + 7, topPos + 35).size(55, 18).build());
    }

    private void handleCraftButton() {
        ItemStack cd = this.getMenu().getInput().getItem();
        if (cd.isEmpty()) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_is_empty");
            return;
        }
        ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(cd);
        if (songInfo != null && songInfo.readOnly) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_read_only");
            return;
        }
        if (Util.isBlank(textField.getValue())) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.no_music_id");
            return;
        }
        Matcher djMatcher = DJ_ID_REG.matcher(textField.getValue());
        if (djMatcher.find()) {
            long djId = Long.parseLong(djMatcher.group(1));
            try {
                ItemMusicCD.SongInfo djSong = MusicListManage.getDjSong(djId);
                if (StringUtils.isBlank(djSong.songUrl) || StringUtils.isBlank(djSong.songName)) {
                    this.tips = Component.translatable("gui.netmusic.cd_burner.get_info_error");
                    return;
                }
                djSong.readOnly = this.readOnlyButton.selected();
                ClientNetWorkHandler.sendToServer(new SetMusicIDMessage(djSong));
                return;
            } catch (Exception e) {
                this.tips = Component.translatable("gui.netmusic.cd_burner.get_info_error");
                NetMusic.LOGGER.error(e);
                return;
            }
        }
        if (ID_REG.matcher(textField.getValue()).matches()) {
            long id = Long.parseLong(textField.getValue());
            try {
                ItemMusicCD.SongInfo song = MusicListManage.get163Song(id);
                if (StringUtils.isBlank(song.songUrl) || StringUtils.isBlank(song.songName)) {
                    this.tips = Component.translatable("gui.netmusic.cd_burner.get_info_error");
                    return;
                }
                song.readOnly = this.readOnlyButton.selected();
                ClientNetWorkHandler.sendToServer(new SetMusicIDMessage(song));
            } catch (Exception e) {
                this.tips = Component.translatable("gui.netmusic.cd_burner.get_info_error");
                NetMusic.LOGGER.error(e);
            }
        } else {
            this.tips = Component.translatable("gui.netmusic.cd_burner.music_id_error");
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        renderBackground(graphics);
        int posX = this.leftPos;
        int posY = (this.height - this.imageHeight) / 2;
        graphics.blit(BG, posX, posY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
        super.render(graphics, x, y, partialTicks);
        textField.render(graphics, x, y, partialTicks);
        if (Util.isBlank(textField.getValue()) && !textField.isFocused()) {
            graphics.drawString(font, Component.translatable("gui.netmusic.cd_burner.id.tips").withStyle(ChatFormatting.ITALIC), this.leftPos + 12, this.topPos + 18, ChatFormatting.GRAY.getColor(), false);
        }
        graphics.drawWordWrap(font, tips, this.leftPos + 8, this.topPos + 57, 135, 0xCF0000);
        renderTooltip(graphics, x, y);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String value = this.textField.getValue();
        super.resize(minecraft, width, height);
        this.textField.setValue(value);
    }

    @Override
    protected void containerTick() {
        this.textField.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.textField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.textField);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 防止 E 键关闭界面
        if (keyCode == InputConstants.KEY_ESCAPE) {
            this.minecraft.player.closeContainer();
        }
        if (minecraft.options.keyInventory.matches(keyCode, scanCode) && textField.isFocused()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            this.textField.setValue(text);
        } else {
            this.textField.insertText(text);
        }
    }
}
