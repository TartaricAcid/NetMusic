package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.item.PlayMode;
import com.github.tartaricacid.netmusic.item.PlaylistData;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import com.github.tartaricacid.netmusic.network.message.SetPlaylistMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@IPNIgnore
public class ComputerMenuScreen extends AbstractContainerScreen<ComputerMenu> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/gui/computer.png");
    private static final Pattern URL_HTTP_REG = Pattern.compile("(http|ftp|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?");
    private static final Pattern URL_FILE_REG = Pattern.compile("^[a-zA-Z]:\\\\(?:[^\\\\/:*?\"<>|\\r\\n]+\\\\)*[^\\\\/:*?\"<>|\\r\\n]*$");
    private static final Pattern TIME_REG = Pattern.compile("^\\d+$");
    private static final Pattern LIST_ID_REG = Pattern.compile("^list/(\\d+)$");
    private static final Pattern LIST_URL_1_REG = Pattern.compile("^https://music\\.163\\.com/playlist\\?id=(\\d+).*$");
    private static final Pattern LIST_URL_2_REG = Pattern.compile("^https://music\\.163\\.com/#/playlist\\?id=(\\d+).*$");
    private EditBox urlTextField;
    private EditBox nameTextField;
    private EditBox timeTextField;
    private EditBox playlistNameField;
    private Checkbox readOnlyButton;
    private PlayMode selectedPlayMode = PlayMode.SEQUENTIAL;
    private Component tips = Component.empty();

    public ComputerMenuScreen(ComputerMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.imageHeight = 216;
    }

    @Override
    protected void init() {
        super.init();
        this.initUrlEditBox();
        this.initNameEditBox();
        this.initTimeEditBox();
        this.initPlaylistNameEditBox();
        this.readOnlyButton = Checkbox.builder(Component.translatable("gui.netmusic.cd_burner.read_only"), font).pos(leftPos + 58, topPos + 55).maxWidth(80).selected(false).build();
        this.addRenderableWidget(this.readOnlyButton);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.cd_burner.craft"), (b) -> handleCraftButton())
                .pos(leftPos + 7, topPos + 78).size(60, 18).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.computer.import_playlist"), (b) -> handleImportPlaylistButton())
                .pos(leftPos + 72, topPos + 78).size(70, 18).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.computer.parse_playlist"), (b) -> handleParsePlaylistButton())
                .pos(leftPos + 7, topPos + 98).size(70, 18).build());
        this.addRenderableWidget(Button.builder(getPlayModeButtonText(), (b) -> {
            selectedPlayMode = selectedPlayMode.next();
            b.setMessage(getPlayModeButtonText());
        }).pos(leftPos + 82, topPos + 98).size(80, 18).build());
    }

    private Component getPlayModeButtonText() {
        return Component.translatable("gui.netmusic.cd_burner.play_mode", Component.translatable("tooltips.netmusic.cd.playlist.play_mode." + selectedPlayMode.getName()));
    }

    private void initUrlEditBox() {
        String perText = "";
        boolean focus = false;
        if (urlTextField != null) {
            perText = urlTextField.getValue();
            focus = urlTextField.isFocused();
        }
        urlTextField = new EditBox(getMinecraft().font, leftPos + 10, topPos + 18, 120, 16, Component.literal("Music URL Box"));
        urlTextField.setValue(perText);
        urlTextField.setBordered(false);
        urlTextField.setMaxLength(32500);
        urlTextField.setTextColor(0xF3EFE0);
        urlTextField.setFocused(focus);
        urlTextField.moveCursorToEnd(false);
        this.addWidget(this.urlTextField);
    }

    private void initNameEditBox() {
        String perText = "";
        boolean focus = false;
        if (nameTextField != null) {
            perText = nameTextField.getValue();
            focus = nameTextField.isFocused();
        }
        nameTextField = new EditBox(getMinecraft().font, leftPos + 10, topPos + 39, 120, 16, Component.literal("Music Name Box"));
        nameTextField.setValue(perText);
        nameTextField.setBordered(false);
        nameTextField.setMaxLength(256);
        nameTextField.setTextColor(0xF3EFE0);
        nameTextField.setFocused(focus);
        nameTextField.moveCursorToEnd(false);
        this.addWidget(this.nameTextField);
    }

    private void initTimeEditBox() {
        String perText = "";
        boolean focus = false;
        if (timeTextField != null) {
            perText = timeTextField.getValue();
            focus = timeTextField.isFocused();
        }
        timeTextField = new EditBox(getMinecraft().font, leftPos + 10, topPos + 61, 40, 16, Component.literal("Music Time Box"));
        timeTextField.setValue(perText);
        timeTextField.setBordered(false);
        timeTextField.setMaxLength(5);
        timeTextField.setTextColor(0xF3EFE0);
        timeTextField.setFocused(focus);
        timeTextField.moveCursorToEnd(false);
        this.addWidget(this.timeTextField);
    }

    private void initPlaylistNameEditBox() {
        String perText = "";
        boolean focus = false;
        if (playlistNameField != null) {
            perText = playlistNameField.getValue();
            focus = playlistNameField.isFocused();
        }
        playlistNameField = new EditBox(getMinecraft().font, leftPos + 10, topPos + 96, 120, 16, Component.literal("Playlist Name Box"));
        playlistNameField.setValue(perText);
        playlistNameField.setBordered(false);
        playlistNameField.setMaxLength(128);
        playlistNameField.setTextColor(0xF3EFE0);
        playlistNameField.setFocused(focus);
        playlistNameField.moveCursorToEnd(false);
        this.addWidget(this.playlistNameField);
    }

    private void handleCraftButton() {
        ItemStack cd = this.getMenu().getInput().getStackInSlot(0);
        if (cd.isEmpty()) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_is_empty");
            return;
        }
        ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(cd);
        if (songInfo != null && songInfo.readOnly) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_read_only");
            return;
        }
        String urlText = urlTextField.getValue();
        if (StringUtils.isBlank(urlText)) {
            this.tips = Component.translatable("gui.netmusic.computer.url.empty");
            return;
        }
        String nameText = nameTextField.getValue();
        if (StringUtils.isBlank(nameText)) {
            this.tips = Component.translatable("gui.netmusic.computer.name.empty");
            return;
        }
        String timeText = timeTextField.getValue();
        if (StringUtils.isBlank(timeText)) {
            this.tips = Component.translatable("gui.netmusic.computer.time.empty");
            return;
        }
        if (!TIME_REG.matcher(timeText).matches()) {
            this.tips = Component.translatable("gui.netmusic.computer.time.not_number");
            return;
        }
        int time = Integer.parseInt(timeText);
        if (URL_HTTP_REG.matcher(urlText).matches()) {
            ItemMusicCD.SongInfo song = new ItemMusicCD.SongInfo(urlText, nameText, time, this.readOnlyButton.selected());
            NetworkHandler.sendToServer(new SetMusicIDMessage(song));
            return;
        }
        if (URL_FILE_REG.matcher(urlText).matches()) {
            File file = Paths.get(urlText).toFile();
            if (!file.isFile()) {
                this.tips = Component.translatable("gui.netmusic.computer.url.local_file_error");
                return;
            }
            try {
                URL url = file.toURI().toURL();
                ItemMusicCD.SongInfo song = new ItemMusicCD.SongInfo(url.toString(), nameText, time, this.readOnlyButton.selected());
                NetworkHandler.sendToServer(new SetMusicIDMessage(song));
                return;
            } catch (MalformedURLException e) {
                NetMusic.LOGGER.error("Failed to convert file path to URL: {}", urlText, e);
            }
        }
        this.tips = Component.translatable("gui.netmusic.computer.url.error");
    }

    /**
     * 从剪贴板导入播放列表JSON并写入唱片
     * JSON格式: [{"url":"...","name":"...","time_second":180}, ...]
     * 或: {"playlist_name":"...","songs":[...]}
     */
    private void handleImportPlaylistButton() {
        ItemStack cd = this.getMenu().getInput().getStackInSlot(0);
        if (cd.isEmpty()) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_is_empty");
            return;
        }
        ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(cd);
        if (songInfo != null && songInfo.readOnly) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_read_only");
            return;
        }
        PlaylistData existingPlaylist = ItemMusicCD.getPlaylistData(cd);
        if (existingPlaylist != null && !existingPlaylist.isEmpty() && existingPlaylist.isReadOnly()) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_read_only");
            return;
        }

        String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
        if (StringUtils.isBlank(clipboard)) {
            this.tips = Component.translatable("gui.netmusic.computer.playlist.clipboard_empty");
            return;
        }

        try {
            Gson gson = new Gson();
            String playlistName = StringUtils.isBlank(playlistNameField.getValue()) ? "" : playlistNameField.getValue();
            List<ItemMusicCD.SongInfo> songs;

            // 尝试解析为播放列表对象格式
            try {
                PlaylistData parsed = gson.fromJson(clipboard, PlaylistData.class);
                if (parsed != null && !parsed.isEmpty()) {
                    // 使用解析到的播放列表名称（如果用户没有指定）
                    if (StringUtils.isBlank(playlistName) && StringUtils.isNotBlank(parsed.getPlaylistName())) {
                        playlistName = parsed.getPlaylistName();
                    }
                    PlaylistData result = new PlaylistData(playlistName, parsed.getSongs(), parsed.getPlayMode(), this.readOnlyButton.selected());
                    NetworkHandler.sendToServer(new SetPlaylistMessage(result));
                    this.tips = Component.translatable("gui.netmusic.computer.playlist.import_success", result.getSongCount());
                    return;
                }
            } catch (Exception ignored) {
                // 不是播放列表对象格式，尝试歌曲数组格式
            }

            // 尝试解析为歌曲数组格式
            songs = gson.fromJson(clipboard, new TypeToken<List<ItemMusicCD.SongInfo>>() {}.getType());
            if (songs != null && !songs.isEmpty()) {
                PlaylistData result = new PlaylistData(playlistName, songs, selectedPlayMode, this.readOnlyButton.selected());
                NetworkHandler.sendToServer(new SetPlaylistMessage(result));
                this.tips = Component.translatable("gui.netmusic.computer.playlist.import_success", result.getSongCount());
                return;
            }

            this.tips = Component.translatable("gui.netmusic.computer.playlist.parse_error");
        } catch (Exception e) {
            this.tips = Component.translatable("gui.netmusic.computer.playlist.parse_error");
            NetMusic.LOGGER.error("Failed to parse playlist JSON from clipboard", e);
        }
    }

    /**
     * 从输入框解析网易云歌单ID并写入唱片
     * 支持格式: list/12345 或直接粘贴歌单URL
     */
    private void handleParsePlaylistButton() {
        ItemStack cd = this.getMenu().getInput().getStackInSlot(0);
        if (cd.isEmpty()) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_is_empty");
            return;
        }
        ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(cd);
        if (songInfo != null && songInfo.readOnly) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_read_only");
            return;
        }
        PlaylistData existingPlaylist = ItemMusicCD.getPlaylistData(cd);
        if (existingPlaylist != null && !existingPlaylist.isEmpty() && existingPlaylist.isReadOnly()) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.cd_read_only");
            return;
        }

        String text = urlTextField.getValue();
        if (StringUtils.isBlank(text)) {
            this.tips = Component.translatable("gui.netmusic.computer.playlist.id_empty");
            return;
        }

        // 尝试从URL中提取歌单ID
        Matcher listUrlMatcher1 = LIST_URL_1_REG.matcher(text);
        if (listUrlMatcher1.find()) {
            text = "list/" + listUrlMatcher1.group(1);
        } else {
            Matcher listUrlMatcher2 = LIST_URL_2_REG.matcher(text);
            if (listUrlMatcher2.find()) {
                text = "list/" + listUrlMatcher2.group(1);
            }
        }

        Matcher listMatcher = LIST_ID_REG.matcher(text);
        if (!listMatcher.find()) {
            this.tips = Component.translatable("gui.netmusic.computer.playlist.id_error");
            return;
        }

        long listId = Long.parseLong(listMatcher.group(1));
        try {
            PlaylistData playlist = MusicListManage.get163Playlist(listId);
            if (playlist.isEmpty()) {
                this.tips = Component.translatable("gui.netmusic.cd_burner.get_info_error");
                return;
            }
            String playlistName = StringUtils.isBlank(playlistNameField.getValue()) ? playlist.getPlaylistName() : playlistNameField.getValue();
            PlaylistData result = new PlaylistData(playlistName, playlist.getSongs(), selectedPlayMode, this.readOnlyButton.selected());
            NetworkHandler.sendToServer(new SetPlaylistMessage(result));
            this.tips = Component.translatable("gui.netmusic.computer.playlist.import_success", result.getSongCount());
        } catch (Exception e) {
            this.tips = Component.translatable("gui.netmusic.cd_burner.get_info_error");
            NetMusic.LOGGER.error("Failed to parse NetEase playlist for computer, playlist id: {}", listId, e);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        int posX = this.leftPos;
        int posY = (this.height - this.imageHeight) / 2;
        graphics.blit(BG, posX, posY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
        super.render(graphics, x, y, partialTicks);
        urlTextField.render(graphics, x, y, partialTicks);
        nameTextField.render(graphics, x, y, partialTicks);
        timeTextField.render(graphics, x, y, partialTicks);
        playlistNameField.render(graphics, x, y, partialTicks);
        if (StringUtils.isBlank(urlTextField.getValue()) && !urlTextField.isFocused()) {
            graphics.drawString(font, Component.translatable("gui.netmusic.computer.url.tips").withStyle(ChatFormatting.ITALIC), this.leftPos + 12, this.topPos + 18, ChatFormatting.GRAY.getColor(), false);
        }
        if (StringUtils.isBlank(nameTextField.getValue()) && !nameTextField.isFocused()) {
            graphics.drawString(font, Component.translatable("gui.netmusic.computer.name.tips").withStyle(ChatFormatting.ITALIC), this.leftPos + 12, this.topPos + 39, ChatFormatting.GRAY.getColor(), false);
        }
        if (StringUtils.isBlank(timeTextField.getValue()) && !timeTextField.isFocused()) {
            graphics.drawString(font, Component.translatable("gui.netmusic.computer.time.tips").withStyle(ChatFormatting.ITALIC), this.leftPos + 11, this.topPos + 61, ChatFormatting.GRAY.getColor(), false);
        }
        if (StringUtils.isBlank(playlistNameField.getValue()) && !playlistNameField.isFocused()) {
            graphics.drawString(font, Component.translatable("gui.netmusic.computer.playlist.name.tips").withStyle(ChatFormatting.ITALIC), this.leftPos + 12, this.topPos + 96, ChatFormatting.GRAY.getColor(), false);
        }
        graphics.drawWordWrap(font, tips, this.leftPos + 8, this.topPos + 120, 162, 0xCF0000);
        renderTooltip(graphics, x, y);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String urlValue = this.urlTextField.getValue();
        String nameValue = this.nameTextField.getValue();
        String timeValue = this.timeTextField.getValue();
        String playlistNameValue = this.playlistNameField.getValue();
        super.resize(minecraft, width, height);
        this.urlTextField.setValue(urlValue);
        this.nameTextField.setValue(nameValue);
        this.timeTextField.setValue(timeValue);
        this.playlistNameField.setValue(playlistNameValue);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.urlTextField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.urlTextField);
            return true;
        }
        if (this.nameTextField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.nameTextField);
            return true;
        }
        if (this.timeTextField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.timeTextField);
            return true;
        }
        if (this.playlistNameField.mouseClicked(mouseX, mouseY, button)) {
            this.setFocused(this.playlistNameField);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        // 防止 E 键关闭界面
        if (this.getMinecraft().options.keyInventory.isActiveAndMatches(mouseKey)) {
            if (urlTextField.isFocused() || nameTextField.isFocused() || timeTextField.isFocused() || playlistNameField.isFocused()) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            this.urlTextField.setValue(text);
            this.nameTextField.setValue(text);
            this.timeTextField.setValue(text);
            this.playlistNameField.setValue(text);
        } else {
            this.urlTextField.insertText(text);
            this.nameTextField.insertText(text);
            this.timeTextField.insertText(text);
            this.playlistNameField.insertText(text);
        }
    }
}
