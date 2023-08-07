package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.netmusic.network.message.SetMusicIDMessage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CDBurnerMenuScreen extends AbstractContainerScreen<CDBurnerMenu> {
    private static final ResourceLocation BG = new ResourceLocation(NetMusic.MOD_ID, "textures/gui/cd_burner.png");
    private static final Pattern ID_REG = Pattern.compile("^\\d{4,}$");
    private static final Pattern URL_1_REG = Pattern.compile("^https://music\\.163\\.com/song\\?id=(\\d+).*$");
    private static final Pattern URL_2_REG = Pattern.compile("^https://music\\.163\\.com/#/song\\?id=(\\d+).*$");
    private EditBox textField;
    private Component tips = TextComponent.EMPTY;

    public CDBurnerMenuScreen(CDBurnerMenu screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
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
        textField = new EditBox(getMinecraft().font, leftPos + 12, topPos + 18, 132, 16, new TextComponent("Music ID Box")) {
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

                super.insertText(text);
            }
        };
        textField.setValue(perText);
        textField.setBordered(false);
        textField.setMaxLength(19);
        textField.setTextColor(0xF3EFE0);
        textField.setFocus(focus);
        textField.moveCursorToEnd();
        this.addWidget(this.textField);
        this.addRenderableWidget(new Button(leftPos + 7, topPos + 33, 135, 20,
                new TranslatableComponent("gui.netmusic.cd_burner.craft"), (b) -> handleCraftButton()));
    }

    private void handleCraftButton() {
        if (StringUtils.isBlank(textField.getValue())) {
            this.tips = new TranslatableComponent("gui.netmusic.cd_burner.no_music_id");
            return;
        }
        if (ID_REG.matcher(textField.getValue()).matches()) {
            long id = Long.parseLong(textField.getValue());
            try {
                ItemMusicCD.SongInfo song = MusicListManage.get163Song(id);
                NetworkHandler.CHANNEL.sendToServer(new SetMusicIDMessage(song));
            } catch (Exception e) {
                this.tips = new TranslatableComponent("gui.netmusic.cd_burner.get_info_error");
                e.printStackTrace();
            }
        } else {
            this.tips = new TranslatableComponent("gui.netmusic.cd_burner.music_id_error");
        }
    }

    @Override
    protected void renderLabels(PoseStack matrixStack, int x, int y) {
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTicks, int pX, int pY) {
        renderBackground(pPoseStack);
        int posX = this.leftPos;
        int posY = (this.height - this.imageHeight) / 2;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BG);
        blit(pPoseStack, posX, posY, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTicks) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTicks);
        textField.render(pPoseStack, pMouseX, pMouseY, pPartialTicks);
        if (StringUtils.isBlank(textField.getValue()) && !textField.isFocused()) {
            drawString(pPoseStack, font, new TranslatableComponent("gui.netmusic.cd_burner.id.tips").withStyle(ChatFormatting.ITALIC), this.leftPos + 12, this.topPos + 18, ChatFormatting.GRAY.getColor());
        }
        font.drawWordWrap(tips, this.leftPos + 8, this.topPos + 55, 135, 0xCF0000);
        renderTooltip(pPoseStack, pMouseX, pMouseY);
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
    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            this.textField.setValue(text);
        } else {
            this.textField.insertText(text);
        }
    }
}