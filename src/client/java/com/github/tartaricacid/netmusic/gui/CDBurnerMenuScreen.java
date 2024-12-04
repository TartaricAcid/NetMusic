package com.github.tartaricacid.netmusic.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.config.MusicListManage;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
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
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author : IMG
 * @create : 2024/10/7
 */
public class CDBurnerMenuScreen extends HandledScreen<CDBurnerMenu> {
    private static final Identifier BG = Identifier.of(NetMusic.MOD_ID, "textures/gui/cd_burner.png");
    private static final Pattern ID_REG = Pattern.compile("^\\d{4,}$");
    private static final Pattern URL_1_REG = Pattern.compile("^https://music\\.163\\.com/song\\?id=(\\d+).*$");
    private static final Pattern URL_2_REG = Pattern.compile("^https://music\\.163\\.com/#/song\\?id=(\\d+).*$");
    private TextFieldWidget textField;
    private CheckboxWidget readOnlyButton;
    private Text tips = Text.empty();

    public CDBurnerMenuScreen(CDBurnerMenu handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundHeight = 176;
        playerInventoryTitleY = 82;
    }

    @Override
    protected void init() {
        super.init();

        String perText = "";
        boolean focus = false;
        if (textField != null) {
            perText = textField.getText();
            focus = textField.isFocused();
        }
        textField = new TextFieldWidget(client.textRenderer, x + 12, y + 18, 132, 16, Text.empty()) {
            @Override
            public void write(String text) {
                Matcher matcher1 = URL_1_REG.matcher(text);
                if (matcher1.find()) {
                    String group = matcher1.group(1);
                    this.setText(group);
                    return;
                }

                Matcher matcher2 = URL_2_REG.matcher(text);
                if (matcher2.find()) {
                    String group = matcher2.group(1);
                    this.setText(group);
                    return;
                }

                super.write(text);
            }
        };

        textField.setText(perText);
        textField.setDrawsBackground(false);
        textField.setMaxLength(19);
        textField.setEditableColor(0xF3EFE0);
        textField.setFocused(focus);
        textField.setCursorToEnd(false);
        this.addSelectableChild(textField);

        this.readOnlyButton = CheckboxWidget.builder(Text.translatable("gui.netmusic.cd_burner.read_only"), textRenderer)
                .pos(x + 66, y + 34)
                .maxWidth(80)
                .checked(false)
                .build();
        this.addDrawableChild(readOnlyButton);
        this.addDrawableChild(
                ButtonWidget.builder(Text.translatable("gui.netmusic.cd_burner.craft"), button -> handleCraftButton())
                        .position(x + 7, y + 35)
                        .size(55, 18)
                        .build()
        );
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
        textField.render(context, mouseX, mouseY, delta);
        if (StringUtils.isBlank(textField.getText()) && !textField.isFocused()) {
            context.drawText(textRenderer, Text.translatable("gui.netmusic.cd_burner.id.tips").formatted(Formatting.ITALIC), this.x + 12, this.y + 18, Formatting.GRAY.getColorValue(), false);
        }
        context.drawTextWrapped(textRenderer, tips, this.x + 8, this.y + 57, 135, 0xCF0000);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String value = this.textField.getText();
        super.resize(client, width, height);
        this.textField.setText(value);
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
        // Prevent the E key from closing the interface
        if (keyCode == 256) {
            this.client.player.closeHandledScreen();
        }
        if (client.options.inventoryKey.matchesKey(keyCode, scanCode) && textField.isFocused()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void insertText(String text, boolean override) {
        if (override) {
            this.textField.setText(text);
        } else {
            this.textField.write(text);
        }
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
        if (StringUtils.isBlank(textField.getText())) {
            this.tips = Text.translatable("gui.netmusic.cd_burner.no_music_id");
            return;
        }
        if (ID_REG.matcher(textField.getText()).matches()) {
            long id = Long.parseLong(textField.getText());
            try {
                ItemMusicCD.SongInfo song = MusicListManage.get163Song(id);
                if (StringUtils.isBlank(song.songUrl) || StringUtils.isBlank(song.songName)) {
                    this.tips = Text.translatable("gui.netmusic.cd_burner.get_info_error");
                    return;
                }
                song.readOnly = readOnlyButton.isChecked();
                ClientNetWorkHandler.sendToServer(new SetMusicIDMessage(song));
            } catch (Exception e) {
                this.tips = Text.translatable("gui.netmusic.cd_burner.get_info_error");
                e.printStackTrace();
            }
        } else {
            this.tips = Text.translatable("gui.netmusic.cd_burner.music_id_error");
        }
    }
}
