package com.github.tartaricacid.netmusic.compat.tlm.client.gui;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.tlm.inventory.MusicPlayerBackpackContainer;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.backpack.IBackpackContainerScreen;
import com.github.tartaricacid.touhoulittlemaid.client.gui.widget.button.BaubleButton;
import com.github.tartaricacid.touhoulittlemaid.compat.trinkets.TrinketsCompat;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.anti_ad.mc.ipn.api.IPNButton;
import org.anti_ad.mc.ipn.api.IPNGuiHint;
import org.anti_ad.mc.ipn.api.IPNPlayerSideOnly;

@IPNPlayerSideOnly
@IPNGuiHint(button = IPNButton.SORT, horizontalOffset = -36, bottom = -12)
@IPNGuiHint(button = IPNButton.SORT_COLUMNS, horizontalOffset = -24, bottom = -24)
@IPNGuiHint(button = IPNButton.SORT_ROWS, horizontalOffset = -12, bottom = -36)
@IPNGuiHint(button = IPNButton.SHOW_EDITOR, horizontalOffset = -5)
@IPNGuiHint(button = IPNButton.SETTINGS, horizontalOffset = -5)
public class MusicPlayerBackpackContainerScreen extends AbstractMaidContainerGui<MusicPlayerBackpackContainer> implements IBackpackContainerScreen {
    private static final ResourceLocation BACKPACK = new ResourceLocation(NetMusic.MOD_ID, "textures/gui/maid_music_player.png");
    private final EntityMaid maid;

    public MusicPlayerBackpackContainerScreen(MusicPlayerBackpackContainer container, Inventory inv, Component titleIn) {
        super(container, inv, titleIn);
        this.imageHeight = 256;
        this.imageWidth = 256;
        this.maid = menu.getMaid();
    }

    @Override
    protected void initAdditionWidgets() {
        BaubleButton button = this.getBaubleButton(maid, leftPos, topPos);
        this.addRenderableWidget(button);
        if (TrinketsCompat.isLoaded()) {
            this.addRenderableWidget(this.getCuriosButton(maid, leftPos, topPos));
        }
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> clickButton(0))
                .size(15, 20).pos(this.leftPos + 142, this.topPos + 135)
                .tooltip(Tooltip.create(Component.translatable("gui.netmusic.maid.music_player_backpack.previous")))
                .build());
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> clickButton(1))
                .size(15, 20).pos(this.leftPos + 235, this.topPos + 135)
                .tooltip(Tooltip.create(Component.translatable("gui.netmusic.maid.music_player_backpack.next")))
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.maid.music_player_backpack.stop"), b -> this.clickButton(2))
                .size(36, 20).pos(this.leftPos + 159, this.topPos + 135).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.maid.music_player_backpack.play"), b -> this.clickButton(3))
                .size(36, 20).pos(this.leftPos + 197, this.topPos + 135).build());
    }

    private void clickButton(int id) {
        if (minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        super.renderBg(graphics, partialTicks, x, y);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BACKPACK);
        graphics.blit(BACKPACK, leftPos + 85, topPos + 36, 0, 0, 165, 128);
        int selectSlotId = this.menu.getSelectSlotId();
        int xIndex = selectSlotId % 6;
        int yIndex = selectSlotId / 6;
        graphics.blit(BACKPACK, leftPos + 142 + 18 * xIndex, topPos + 56 + 18 * yIndex, 165, 0, 18, 18);
    }
}