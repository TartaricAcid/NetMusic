package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.SetMusicIDMessage;
import com.github.tartaricacid.netmusic.proxy.CommonProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CDBurnerMenuScreen extends GuiContainer {
    private static final ResourceLocation BG = new ResourceLocation(NetMusic.MOD_ID, "textures/gui/cd_burner.png");
    private static final Pattern ID_REG = Pattern.compile("^\\d{4,}$");
    private static final Pattern URL_1_REG = Pattern.compile("^https://music\\.163\\.com/song\\?id=(\\d+).*$");
    private static final Pattern URL_2_REG = Pattern.compile("^https://music\\.163\\.com/#/song\\?id=(\\d+).*$");
    private GuiTextField textField;
    private String tips = "";

    public CDBurnerMenuScreen(IInventory playerInventory) {
        super(new CDBurnerMenu(playerInventory));
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        String perText = "";
        boolean focus = false;
        if (textField != null) {
            perText = textField.getText();
            focus = textField.isFocused();
        }
        textField = new GuiTextField(0, mc.fontRenderer, guiLeft + 8, guiTop + 14, 133, 16) {
            @Override
            public void writeText(String text) {
                Matcher matcher1 = URL_1_REG.matcher(text);
                if (matcher1.find()) {
                    String group = matcher1.group(1);
                    super.writeText(group);
                    return;
                }

                Matcher matcher2 = URL_2_REG.matcher(text);
                if (matcher2.find()) {
                    String group = matcher2.group(1);
                    super.writeText(group);
                    return;
                }
                super.writeText(text);
            }
        };
        textField.setText(perText);
        textField.setMaxStringLength(19);
        textField.setTextColor(0xF3EFE0);
        textField.setFocused(focus);
        textField.setCursorPositionEnd();
        this.addButton(new GuiButton(1, guiLeft + 7, guiTop + 33, 135, 20,
                I18n.format("gui.netmusic.cd_burner.craft")));
    }

    @Override
    public void updateScreen() {
        textField.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.color(255, 255, 255);
        if (StringUtils.isBlank(textField.getText()) && !textField.isFocused()) {
            drawString(fontRenderer, "§o" + I18n.format("gui.netmusic.cd_burner.id.tips"), this.guiLeft + 12, this.guiTop + 18, 0xaaaaaa);
        }
        fontRenderer.drawSplitString(tips, this.guiLeft + 8, this.guiTop + 55, 135, 0xCF0000);
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
        mc.getTextureManager().bindTexture(BG);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 166);
        textField.drawTextBox();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            handleCraftButton();
        }
    }

    private void handleCraftButton() {
        if (StringUtils.isBlank(textField.getText())) {
            this.tips = I18n.format("gui.netmusic.cd_burner.no_music_id");
            return;
        }
        if (ID_REG.matcher(textField.getText()).matches()) {
            long id = Long.parseLong(textField.getText());
            try {
                ItemMusicCD.SongInfo song = MusicListManage.get163Song(id);
                CommonProxy.INSTANCE.sendToServer(new SetMusicIDMessage(song));
            } catch (Exception e) {
                this.tips = I18n.format("gui.netmusic.cd_burner.get_info_error");
                e.printStackTrace();
            }
        } else {
            this.tips = I18n.format("gui.netmusic.cd_burner.music_id_error");
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        textField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char c, int keyCode) throws IOException {
        super.keyTyped(c, keyCode);
        textField.textboxKeyTyped(c, keyCode);
    }
}
