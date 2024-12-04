package com.github.tartaricacid.netmusic.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.inventory.ComputerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.ClientNetWorkHandler;
import com.github.tartaricacid.netmusic.networking.message.SetMusicIDMessage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import oshi.util.Util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * @author : IMG
 * @create : 2024/10/11
 */
public class ComputerMenuScreen extends HandledScreen<ComputerMenu> {
    private static final Identifier BG = Identifier.of(NetMusic.MOD_ID, "textures/gui/computer.png");
    private static final Pattern URL_HTTP_REG = Pattern.compile("(http|ftp|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?");
    private static final Pattern URL_FILE_REG = Pattern.compile("^[a-zA-Z]:\\\\(?:[^\\\\/:*?\"<>|\\r\\n]+\\\\)*[^\\\\/:*?\"<>|\\r\\n]*$");
    private static final Pattern TIME_REG = Pattern.compile("^\\d+$");
    private TextFieldWidget urlTextField;
    private TextFieldWidget nameTextField;
    private TextFieldWidget timeTextField;
    private CheckboxWidget readOnlyButton;
    private Text tips = Text.empty();

    public ComputerMenuScreen(ComputerMenu handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 216;
        this.playerInventoryTitleY = backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.initUrlEditBox();
        this.initNameEditBox();
        this.initTimeEditBox();
        this.readOnlyButton = CheckboxWidget.builder(Text.translatable("gui.netmusic.cd_burner.read_only"), textRenderer)
                .pos(x + 58, y + 55)
                .maxWidth(80)
                .checked(false)
                .build();
        this.addDrawableChild(readOnlyButton);
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.netmusic.cd_burner.craft"), button -> handleCraftButton())
                .position(x + 7, y + 78)
                .size(135, 18)
                .build());
    }

    private void initUrlEditBox() {
        String perText = "";
        boolean focus = false;
        if (urlTextField != null) {
            perText = urlTextField.getText();
            focus = urlTextField.isFocused();
        }
        urlTextField = new TextFieldWidget(textRenderer, x + 10, y + 18, 120, 16, Text.literal("Music URL Box"));
        urlTextField.setText(perText);
        urlTextField.setDrawsBackground(false);
        urlTextField.setMaxLength(32500);
        urlTextField.setEditableColor(0xF3EFE0);
        urlTextField.setFocused(focus);
        urlTextField.setCursorToEnd(false);
        this.addSelectableChild(urlTextField);
    }

    private void initNameEditBox() {
        String preText = "";
        boolean focus = false;
        if (nameTextField != null) {
            preText = nameTextField.getText();
            focus = nameTextField.isFocused();
        }
        nameTextField = new TextFieldWidget(textRenderer, x + 10, y + 39, 120, 16, Text.literal("Music Name Box"));
        nameTextField.setText(preText);
        nameTextField.setDrawsBackground(false);
        nameTextField.setMaxLength(256);
        nameTextField.setEditableColor(0xF3EFE0);
        nameTextField.setFocused(focus);
        nameTextField.setCursorToEnd(false);
        this.addSelectableChild(nameTextField);
    }

    private void initTimeEditBox() {
        String preText = "";
        boolean focus = false;
        if (timeTextField != null) {
            preText = timeTextField.getText();
            focus = timeTextField.isFocused();
        }
        timeTextField = new TextFieldWidget(textRenderer, x + 10, y + 61, 40, 16, Text.literal("Music Time Box"));
        timeTextField.setText(preText);
        timeTextField.setDrawsBackground(false);
        timeTextField.setMaxLength(5);
        timeTextField.setEditableColor(0xF3EFE0);
        timeTextField.setFocused(focus);
        timeTextField.setCursorToEnd(false);
        this.addSelectableChild(timeTextField);
    }

    private void handleCraftButton() {
        ItemStack cd = getScreenHandler().getInput().getStack();
        if (cd.isEmpty()) {
            this.tips = Text.translatable("gui.netmusic.cd_burner.cd_is_empty");
            return;
        }

        ItemMusicCD.SongInfo songInfo = ItemMusicCD.getSongInfo(cd);
        if (songInfo != null && songInfo.readOnly) {
            this.tips = Text.translatable("gui.netmusic.cd_burner.cd_read_only");
            return;
        }
        // 参数判空与合法性检查
        String urlText = urlTextField.getText();
        if (Util.isBlank(urlText)) {
            this.tips = Text.translatable("gui.netmusic.computer.url.empty");
            return;
        }
        String nameText = nameTextField.getText();
        if (Util.isBlank(nameText)) {
            this.tips = Text.translatable("gui.netmusic.computer.name.empty");
            return;
        }
        String timeText = timeTextField.getText();
        if (Util.isBlank(timeText)) {
            this.tips = Text.translatable("gui.netmusic.computer.time.empty");
            return;
        }

        if (!TIME_REG.matcher(timeText).matches()) {
            this.tips = Text.translatable("gui.netmusic.computer.time.not_number");
            return;
        }
        int time = Integer.parseInt(timeText);
        if (URL_HTTP_REG.matcher(urlText).matches()) {
            ItemMusicCD.SongInfo song = new ItemMusicCD.SongInfo(urlText, nameText, time, readOnlyButton.isChecked());
            ClientNetWorkHandler.sendToServer(new SetMusicIDMessage(song));
            return;
        }
        if (URL_FILE_REG.matcher(urlText).matches()) {
            File file = Paths.get(urlText).toFile();
            if (!file.isFile()) {
                this.tips = Text.translatable("gui.netmusic.computer.url.local_file_error");
                return;
            }
            try {
                URL url = file.toURI().toURL();
                ItemMusicCD.SongInfo song = new ItemMusicCD.SongInfo(url.toString(), nameText, time, readOnlyButton.isChecked());
                ClientNetWorkHandler.sendToServer(new SetMusicIDMessage(song));
                return;
            } catch (MalformedURLException e) {
                e.fillInStackTrace();
            }
        }
        this.tips = Text.translatable("gui.netmusic.computer.url.error");
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int posX = this.x;
        int posY = this.y;
        context.drawTexture(BG, posX, posY, 0, 0, this.backgroundWidth, this.backgroundHeight);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        urlTextField.render(context, mouseX, mouseY, delta);
        nameTextField.render(context, mouseX, mouseY, delta);
        timeTextField.render(context, mouseX, mouseY, delta);
        if (Util.isBlank(urlTextField.getText()) && !urlTextField.isFocused()) {
            context.drawText(textRenderer, Text.translatable("gui.netmusic.computer.url.tips").formatted(Formatting.ITALIC), this.x + 12, this.y + 18, Formatting.GRAY.getColorValue(), false);
        }
        if (Util.isBlank(nameTextField.getText()) && !nameTextField.isFocused()) {
            context.drawText(textRenderer, Text.translatable("gui.netmusic.computer.name.tips").formatted(Formatting.ITALIC), this.x + 12, this.y + 39, Formatting.GRAY.getColorValue(), false);
        }
        if (Util.isBlank(timeTextField.getText()) && !timeTextField.isFocused()) {
            context.drawText(textRenderer, Text.translatable("gui.netmusic.computer.time.tips").formatted(Formatting.ITALIC), this.x + 12, this.y + 61, Formatting.GRAY.getColorValue(), false);
        }
        context.drawTextWrapped(textRenderer, tips, this.x + 8, this.y + 100, 162, 0xCF0000);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String urlValue = this.urlTextField.getText();
        String nameValue = this.nameTextField.getText();
        String timeValue = this.timeTextField.getText();
        super.resize(client, width, height);
        this.urlTextField.setText(urlValue);
        this.nameTextField.setText(nameValue);
        this.timeTextField.setText(timeValue);
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
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.client.player.closeHandledScreen();
        }
        if (client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            if (urlTextField.isFocused() || nameTextField.isFocused() || timeTextField.isFocused()) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void insertText(String text, boolean override) {
        if (override) {
            this.urlTextField.setText(text);
            this.nameTextField.setText(text);
            this.timeTextField.setText(text);
        } else {
            this.urlTextField.write(text);
            this.nameTextField.write(text);
            this.timeTextField.write(text);
        }
    }
}
