package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.network.ClientNetWorkHandler;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.anti_ad.mc.ipn.api.IPNIgnore;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@IPNIgnore
public class ComputerMenuScreen extends AbstractContainerScreen<ComputerMenu> {
    private static final Identifier BG = Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/gui/computer.png");
    private static final int INPUT_TEXT_COLOR = 0xFFF3EFE0;
    private static final int HINT_TEXT_COLOR = 0xFF808080;
    private static final int ERROR_TEXT_COLOR = 0xFFCF0000;
    private static final Pattern URL_HTTP_REG = Pattern.compile("(http|ftp|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?");
    private static final Pattern URL_FILE_REG = Pattern.compile("^[a-zA-Z]:\\\\(?:[^\\\\/:*?\"<>|\\r\\n]+\\\\)*[^\\\\/:*?\"<>|\\r\\n]*$");
    private static final Pattern TIME_REG = Pattern.compile("^\\d+$");
    private EditBox urlTextField;
    private EditBox nameTextField;
    private EditBox timeTextField;
    private Checkbox readOnlyButton;
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
        this.readOnlyButton = Checkbox.builder(Component.translatable("gui.netmusic.cd_burner.read_only"), font).pos(leftPos + 58, topPos + 55).maxWidth(80).selected(false).build();
        this.addRenderableWidget(this.readOnlyButton);
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.cd_burner.craft"), (b) -> handleCraftButton())
                .pos(leftPos + 7, topPos + 78).size(135, 18).build());
    }

    private void initUrlEditBox() {
        String perText = "";
        boolean focus = false;
        if (urlTextField != null) {
            perText = urlTextField.getValue();
            focus = urlTextField.isFocused();
        }
        urlTextField = new EditBox(minecraft.font, leftPos + 10, topPos + 18, 120, 16, Component.literal("Music URL Box"));
        urlTextField.setValue(perText);
        urlTextField.setBordered(false);
        urlTextField.setMaxLength(32500);
        urlTextField.setTextColor(INPUT_TEXT_COLOR);
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
        nameTextField = new EditBox(minecraft.font, leftPos + 10, topPos + 39, 120, 16, Component.literal("Music Name Box"));
        nameTextField.setValue(perText);
        nameTextField.setBordered(false);
        nameTextField.setMaxLength(256);
        nameTextField.setTextColor(INPUT_TEXT_COLOR);
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
        timeTextField = new EditBox(minecraft.font, leftPos + 10, topPos + 61, 40, 16, Component.literal("Music Time Box"));
        timeTextField.setValue(perText);
        timeTextField.setBordered(false);
        timeTextField.setMaxLength(5);
        timeTextField.setTextColor(INPUT_TEXT_COLOR);
        timeTextField.setFocused(focus);
        timeTextField.moveCursorToEnd(false);
        this.addWidget(this.timeTextField);
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
            ClientNetWorkHandler.sendToServer(new SetMusicIDMessage(song));
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
                ClientNetWorkHandler.sendToServer(new SetMusicIDMessage(song));
                return;
            } catch (MalformedURLException e) {
                NetMusic.LOGGER.error("Failed to convert file path to URL: {}", urlText, e);
            }
        }
        this.tips = Component.translatable("gui.netmusic.computer.url.error");
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int x, int y) {
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        int posX = this.leftPos;
        int posY = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BG, posX, posY, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
        super.render(graphics, x, y, partialTicks);
        urlTextField.render(graphics, x, y, partialTicks);
        nameTextField.render(graphics, x, y, partialTicks);
        timeTextField.render(graphics, x, y, partialTicks);
        if (StringUtils.isBlank(urlTextField.getValue()) && !urlTextField.isFocused()) {
            graphics.drawString(font, Component.translatable("gui.netmusic.computer.url.tips").withStyle(ChatFormatting.ITALIC), this.leftPos + 12, this.topPos + 18, HINT_TEXT_COLOR, false);
        }
        if (StringUtils.isBlank(nameTextField.getValue()) && !nameTextField.isFocused()) {
            graphics.drawString(font, Component.translatable("gui.netmusic.computer.name.tips").withStyle(ChatFormatting.ITALIC), this.leftPos + 12, this.topPos + 39, HINT_TEXT_COLOR, false);
        }
        if (StringUtils.isBlank(timeTextField.getValue()) && !timeTextField.isFocused()) {
            graphics.drawString(font, Component.translatable("gui.netmusic.computer.time.tips").withStyle(ChatFormatting.ITALIC), this.leftPos + 11, this.topPos + 61, HINT_TEXT_COLOR, false);
        }
        graphics.drawWordWrap(font, tips, this.leftPos + 8, this.topPos + 100, 162, ERROR_TEXT_COLOR);
        renderTooltip(graphics, x, y);
    }

    @Override
    public void resize(int width, int height) {
        String urlValue = this.urlTextField.getValue();
        String nameValue = this.nameTextField.getValue();
        String timeValue = this.timeTextField.getValue();
        super.resize(width, height);
        this.urlTextField.setValue(urlValue);
        this.nameTextField.setValue(nameValue);
        this.timeTextField.setValue(timeValue);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.urlTextField.mouseClicked(event, doubleClick)) {
            this.setFocused(this.urlTextField);
            return true;
        }
        if (this.nameTextField.mouseClicked(event, doubleClick)) {
            this.setFocused(this.nameTextField);
            return true;
        }
        if (this.timeTextField.mouseClicked(event, doubleClick)) {
            this.setFocused(this.timeTextField);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256 && this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
            return true;
        }
        if (this.minecraft != null && this.minecraft.options.keyInventory.matches(event)) {
            if (urlTextField.isFocused() || nameTextField.isFocused() || timeTextField.isFocused()) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            this.urlTextField.setValue(text);
            this.nameTextField.setValue(text);
            this.timeTextField.setValue(text);
        } else {
            this.urlTextField.insertText(text);
            this.nameTextField.insertText(text);
            this.timeTextField.insertText(text);
        }
    }
}

