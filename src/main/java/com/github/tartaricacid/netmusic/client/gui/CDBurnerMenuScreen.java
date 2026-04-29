package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CDBurnerMenuScreen extends AbstractContainerScreen<CDBurnerMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/gui/cd_burner.png");

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
        super(screenContainer, inv, titleIn, 176, 176);
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
        textField = new EditBox(this.font, leftPos + 12, topPos + 18, 132, 16, Component.literal("Music ID Box")) {
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
        textField.setTextColor(0xFFF3EFE0);
        textField.setFocused(focus);
        textField.moveCursorToEnd(false);
        this.addRenderableWidget(this.textField);

        this.readOnlyButton = Checkbox.builder(Component.translatable("gui.netmusic.cd_burner.read_only"), font).pos(leftPos + 66, topPos + 34).maxWidth(80).selected(false).build();
        this.addRenderableWidget(this.readOnlyButton);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.cd_burner.craft"), (b) -> handleCraftButton())
                .pos(leftPos + 7, topPos + 35).size(55, 18).build());
    }

    private void handleCraftButton() {
        ItemStack cd = this.getMenu().getInput().getResource(0).toStack();
        if (cd.isEmpty()) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_is_empty");
            return;
        }
        ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(cd);
        if (songInfo != null && songInfo.readOnly) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_read_only");
            return;
        }
        if (StringUtils.isBlank(textField.getValue())) {
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
                NetworkHandler.sendToServer(new SetMusicIDMessage(djSong));
                return;
            } catch (Exception e) {
                this.tips = Component.translatable("gui.netmusic.cd_burner.get_info_error");
                NetMusic.LOGGER.error("Failed to get DJ song info for CD burner, dj id: {}", djId, e);
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
                NetworkHandler.sendToServer(new SetMusicIDMessage(song));
            } catch (Exception e) {
                this.tips = Component.translatable("gui.netmusic.cd_burner.get_info_error");
                NetMusic.LOGGER.error("Failed to get song info for CD burner, song id: {}", id, e);
            }
        } else {
            this.tips = Component.translatable("gui.netmusic.cd_burner.music_id_error");
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int x, int y) {
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BG, leftPos, topPos, 0, 0,
                imageWidth, imageHeight, 256, 256);
        this.minecraft.gui.extractDeferredSubtitles();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractContents(graphics, mouseX, mouseY, a);
        if (StringUtils.isBlank(textField.getValue()) && !textField.isFocused()) {
            graphics.text(font, Component.translatable("gui.netmusic.cd_burner.id.tips").withStyle(ChatFormatting.ITALIC), this.leftPos + 12,
                    this.topPos + 18, 0xFFaaaaaa, false);
        }
        graphics.textWithWordWrap(font, tips, this.leftPos + 8, this.topPos + 57, 135, 0xFFCF0000, false);
    }

    @Override
    public void resize(int width, int height) {
        String value = this.textField.getValue();
        super.resize(width, height);
        this.textField.setValue(value);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.textField.mouseClicked(event, doubleClick)) {
            this.setFocused(this.textField);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return this.textField.charTyped(event) || super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        InputConstants.Key mouseKey = InputConstants.getKey(event);
        // 防止 E 键关闭界面
        if (this.getMinecraft().options.keyInventory.isActiveAndMatches(mouseKey) && textField.isFocused()) {
            return true;
        }
        return super.keyPressed(event);
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
