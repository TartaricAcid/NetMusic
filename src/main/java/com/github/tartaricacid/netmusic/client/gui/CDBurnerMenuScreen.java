package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CDBurnerMenuScreen extends AbstractContainerScreen<CDBurnerMenu> {
    private static final ResourceLocation BG = new ResourceLocation(NetMusic.MOD_ID, "textures/gui/cd_burner.png");
    private EditBox textField;

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
        textField = new EditBox(getMinecraft().font, leftPos + 12, topPos + 18, 132, 16, Component.literal("Music ID Box"));
        textField.setValue(perText);
        textField.setBordered(false);
        textField.setMaxLength(16);
        textField.setTextColor(0xF3EFE0);
        textField.setFocused(focus);
        textField.moveCursorToEnd();
        this.addWidget(this.textField);

        this.addRenderableWidget(Button.builder(Component.literal("Craft"), (b) -> {
        }).pos(leftPos + 7, topPos + 33).size(135, 18).build());
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

        textField.render(graphics, x, y, partialTicks);
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
