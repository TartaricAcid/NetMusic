package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.client.MegaphoneLinkManager;
import com.github.tartaricacid.netmusic.config.GeneralConfig;
import com.github.tartaricacid.netmusic.network.message.BigMegaphoneControlMessage;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.StringUtils;

public class BigMegaphoneScreen extends Screen {
    private static final int WIDTH = 240;

    private final BlockPos blockPos;

    private int leftPos;
    private int topPos;

    private EditBox urlTextField;
    private EditBox nameTextField;
    private RangeSlider rangeSlider;

    /** 当前选择的广播模式 */
    private TileEntityBigMegaphone.BroadcastMode broadcastMode = TileEntityBigMegaphone.BroadcastMode.STREAM;

    private Component tips = Component.empty();
    private boolean loadedFromBlockEntity = false;

    public BigMegaphoneScreen(BlockPos blockPos) {
        super(Component.translatable("block.netmusic.big_megaphone"));
        this.blockPos = blockPos;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - 200) / 2;

        // 模式切换按钮
        this.addRenderableWidget(Button.builder(this.getModeButtonText(),
                        b -> this.toggleBroadcastMode())
                .pos(this.leftPos, this.topPos).size(WIDTH, 20).build());

        // 根据模式初始化不同的控件
        this.initModeSpecificWidgets();

        // 范围滑条
        this.initRangeSlider(this.rangeSlider == null ? 32 : this.rangeSlider.getCurrentRange());

        // 预设电台按钮（仅流媒体模式）
        if (broadcastMode == TileEntityBigMegaphone.BroadcastMode.STREAM) {
            this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.big_megaphone.presets"),
                            b -> this.openPresetPicker())
                    .pos(this.leftPos, this.topPos + 139).size(WIDTH, 20).build());
        }

        // 底部操作按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.big_megaphone.save"),
                        b -> this.sendAction(BigMegaphoneControlMessage.Action.SAVE))
                .pos(this.leftPos, this.topPos + 164).size(76, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.big_megaphone.start"),
                        b -> this.sendAction(BigMegaphoneControlMessage.Action.START))
                .pos(this.leftPos + 82, this.topPos + 164).size(76, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.big_megaphone.stop"),
                        b -> this.sendAction(BigMegaphoneControlMessage.Action.STOP))
                .pos(this.leftPos + 164, this.topPos + 164).size(76, 20).build());

        this.initFromBlockEntity();
    }

    private void initModeSpecificWidgets() {
        if (broadcastMode == TileEntityBigMegaphone.BroadcastMode.STREAM) {
            // 流媒体模式：URL和名称输入框
            this.initUrlEditBox();
            this.initNameEditBox();
        } else {
            // 唱片机源模式：选择唱片机按钮 + 状态显示
            this.addRenderableWidget(Button.builder(
                            Component.translatable("gui.netmusic.big_megaphone.select_player"),
                            b -> this.startSelectingPlayer())
                    .pos(this.leftPos, this.topPos + 24).size(WIDTH, 20).build());

            // 显示当前链接状态
            this.addRenderableWidget(Button.builder(this.getPlayerLinkText(), b -> {})
                    .pos(this.leftPos, this.topPos + 48).size(WIDTH, 20).build());
        }
    }

    private Component getModeButtonText() {
        if (broadcastMode == TileEntityBigMegaphone.BroadcastMode.STREAM) {
            return Component.translatable("gui.netmusic.big_megaphone.mode.stream");
        } else {
            return Component.translatable("gui.netmusic.big_megaphone.mode.player_source");
        }
    }

    private Component getPlayerLinkText() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return Component.translatable("gui.netmusic.big_megaphone.player.not_linked");
        }
        BlockEntity be = level.getBlockEntity(this.blockPos);
        if (be instanceof TileEntityBigMegaphone megaphone && megaphone.getSourcePlayerId() != null) {
            return Component.translatable("gui.netmusic.big_megaphone.player.linked").withStyle(ChatFormatting.GREEN);
        }
        return Component.translatable("gui.netmusic.big_megaphone.player.not_linked").withStyle(ChatFormatting.RED);
    }

    private void toggleBroadcastMode() {
        if (broadcastMode == TileEntityBigMegaphone.BroadcastMode.STREAM) {
            broadcastMode = TileEntityBigMegaphone.BroadcastMode.PLAYER_SOURCE;
        } else {
            broadcastMode = TileEntityBigMegaphone.BroadcastMode.STREAM;
        }
        // 重建界面
        this.rebuildWidgets();
    }

    private void startSelectingPlayer() {
        MegaphoneLinkManager.startSelecting(this.blockPos);
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
            // 显示提示
            this.minecraft.player.displayClientMessage(
                    Component.translatable("gui.netmusic.big_megaphone.select_player.hint").withStyle(ChatFormatting.YELLOW), true);
        }
    }

    private void initUrlEditBox() {
        String previousText = this.urlTextField == null ? "" : this.urlTextField.getValue();
        boolean focused = this.urlTextField != null && this.urlTextField.isFocused();
        this.urlTextField = new EditBox(this.font, this.leftPos, this.topPos + 24, WIDTH, 18,
                Component.literal("Megaphone URL Box"));
        this.urlTextField.setMaxLength(1024);
        this.urlTextField.setTextColor(0xF3EFE0);
        this.urlTextField.setFocused(focused);
        this.urlTextField.setValue(previousText);
        this.addRenderableWidget(this.urlTextField);
    }

    private void initNameEditBox() {
        String previousText = this.nameTextField == null ? "" : this.nameTextField.getValue();
        boolean focused = this.nameTextField != null && this.nameTextField.isFocused();
        this.nameTextField = new EditBox(this.font, this.leftPos, this.topPos + 47, WIDTH, 18,
                Component.literal("Megaphone Name Box"));
        this.nameTextField.setMaxLength(256);
        this.nameTextField.setTextColor(0xF3EFE0);
        this.nameTextField.setFocused(focused);
        this.nameTextField.setValue(previousText);
        this.addRenderableWidget(this.nameTextField);
    }

    private void initRangeSlider(int range) {
        int maxRange = Math.max(1, GeneralConfig.BIG_MEGAPHONE_MAX_RANGE.get());
        double value = maxRange == 1 ? 0 : (double) (Mth.clamp(range, 1, maxRange) - 1) / (maxRange - 1);
        this.rangeSlider = new RangeSlider(this.leftPos, this.topPos + 114, WIDTH, 20, value, maxRange);
        this.addRenderableWidget(this.rangeSlider);
    }

    private void openPresetPicker() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new BigMegaphonePresetPickerScreen(this));
        }
    }

    @Override
    public void tick() {
        super.tick();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null || !(level.getBlockEntity(this.blockPos) instanceof TileEntityBigMegaphone)) {
            this.onClose();
        }
    }

    private void initFromBlockEntity() {
        if (this.loadedFromBlockEntity) {
            return;
        }
        Minecraft minecraft = this.getMinecraft();
        if (minecraft.level == null) {
            return;
        }
        BlockEntity blockEntity = minecraft.level.getBlockEntity(this.blockPos);
        if (blockEntity instanceof TileEntityBigMegaphone megaphone) {
            this.urlTextField.setValue(megaphone.getStreamUrl());
            this.nameTextField.setValue(megaphone.getDisplayName());
            this.rangeSlider.setRange(megaphone.getMaxRange());
            this.broadcastMode = megaphone.getBroadcastMode();
            this.loadedFromBlockEntity = true;
            // 需要重建以显示正确的模式界面
            this.rebuildWidgets();
            return;
        }
        this.onClose();
    }

    public void applyPresetStation(String name, String url) {
        this.tips = Component.empty();
        if (this.urlTextField != null) {
            this.urlTextField.setValue(url);
        }
        if (this.nameTextField != null) {
            this.nameTextField.setValue(name);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);

        graphics.drawCenteredString(this.font, this.tips, this.width / 2, this.topPos + 150, 0xCF0000);

        for (Renderable renderable : this.renderables) {
            renderable.render(graphics, mouseX, mouseY, partialTicks);
        }

        // 流媒体模式的占位文字
        if (broadcastMode == TileEntityBigMegaphone.BroadcastMode.STREAM) {
            if (StringUtils.isBlank(this.urlTextField.getValue()) && !this.urlTextField.isFocused()) {
                MutableComponent placeHolder = Component.translatable("gui.netmusic.big_megaphone.url.tips").withStyle(ChatFormatting.ITALIC);
                graphics.drawString(this.font, placeHolder, this.leftPos + 5, this.topPos + 29, 0xaaaaaa, false);
            }
            if (StringUtils.isBlank(this.nameTextField.getValue()) && !this.nameTextField.isFocused()) {
                MutableComponent placeHolder = Component.translatable("gui.netmusic.big_megaphone.name.tips").withStyle(ChatFormatting.ITALIC);
                graphics.drawString(this.font, placeHolder, this.leftPos + 5, this.topPos + 52, 0xaaaaaa, false);
            }
        }
    }

    private void sendAction(BigMegaphoneControlMessage.Action action) {
        this.tips = Component.empty();
        String url = this.urlTextField == null ? "" : this.urlTextField.getValue().trim();
        String name = this.nameTextField == null ? "" : this.nameTextField.getValue().trim();
        int range = this.rangeSlider.getCurrentRange();

        if (action != BigMegaphoneControlMessage.Action.STOP) {
            if (broadcastMode == TileEntityBigMegaphone.BroadcastMode.STREAM) {
                // 流媒体模式验证
                if (StringUtils.isBlank(url)) {
                    this.tips = Component.translatable("gui.netmusic.big_megaphone.url.empty");
                    return;
                }
                if (!BigMegaphoneUtil.isValidStreamUrl(url)) {
                    this.tips = Component.translatable("gui.netmusic.big_megaphone.url.invalid");
                    return;
                }
                if (StringUtils.isBlank(name)) {
                    this.tips = Component.translatable("gui.netmusic.big_megaphone.name.empty");
                    return;
                }
            } else {
                // 唱片机源模式验证
                ClientLevel level = Minecraft.getInstance().level;
                if (level != null && level.getBlockEntity(this.blockPos) instanceof TileEntityBigMegaphone megaphone) {
                    if (megaphone.getSourcePlayerId() == null) {
                        this.tips = Component.translatable("gui.netmusic.big_megaphone.player.not_linked");
                        return;
                    }
                }
            }
        }

        PacketDistributor.sendToServer(new BigMegaphoneControlMessage(
                this.blockPos, url, name, range, broadcastMode, action));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (broadcastMode == TileEntityBigMegaphone.BroadcastMode.STREAM) {
            if (this.urlTextField != null && this.urlTextField.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(this.urlTextField);
                return true;
            }
            if (this.nameTextField != null && this.nameTextField.mouseClicked(mouseX, mouseY, button)) {
                this.setFocused(this.nameTextField);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
        if (this.getMinecraft().options.keyInventory.isActiveAndMatches(key)) {
            if (broadcastMode == TileEntityBigMegaphone.BroadcastMode.STREAM) {
                if ((this.urlTextField != null && this.urlTextField.isFocused()) ||
                    (this.nameTextField != null && this.nameTextField.isFocused())) {
                    return true;
                }
            }
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class RangeSlider extends AbstractSliderButton {
        private final int maxRange;

        protected RangeSlider(int x, int y, int width, int height, double value, int maxRange) {
            super(x, y, width, height, Component.empty(), value);
            this.maxRange = maxRange;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(Component.translatable("gui.netmusic.big_megaphone.range", this.getCurrentRange()));
        }

        @Override
        protected void applyValue() {
            this.updateMessage();
        }

        public int getCurrentRange() {
            if (this.maxRange <= 1) {
                return 1;
            }
            return Mth.clamp((int) Math.round(1 + this.value * (this.maxRange - 1)), 1, this.maxRange);
        }

        public void setRange(int range) {
            if (this.maxRange <= 1) {
                this.value = 0;
            } else {
                this.value = (double) (Mth.clamp(range, 1, this.maxRange) - 1) / (double) (this.maxRange - 1);
            }
            this.updateMessage();
        }
    }
}