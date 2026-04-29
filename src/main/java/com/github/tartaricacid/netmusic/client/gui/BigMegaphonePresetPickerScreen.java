package com.github.tartaricacid.netmusic.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class BigMegaphonePresetPickerScreen extends Screen {
    private static final int PAGE_SIZE = 5;

    private final BigMegaphoneScreen parent;
    private int leftPos;
    private int topPos;
    private int page;

    public BigMegaphonePresetPickerScreen(BigMegaphoneScreen parent) {
        super(Component.translatable("gui.netmusic.big_megaphone.preset_picker"));
        this.parent = parent;
    }

    @Override
    public void init() {
        this.leftPos = (this.width - 240) / 2;
        this.topPos = (this.height - 170) / 2;
        this.rebuildPresetButtons();
    }

    private void rebuildPresetButtons() {
        this.clearWidgets();

        int start = this.page * PAGE_SIZE;
        var stations = BigMegaphonePresetManager.getStations();
        int end = Math.min(start + PAGE_SIZE, stations.size());

        for (int i = start; i < end; i++) {
            int index = i - start;
            var station = stations.get(i);
            Component text = Component.literal(station.name());
            this.addRenderableWidget(Button.builder(text, b -> this.selectStation(station))
                    .pos(this.leftPos, this.topPos + 20 + index * 22).size(240, 20)
                    .build());
        }

        Button previous = Button.builder(Component.translatable("gui.netmusic.big_megaphone.page.previous"), b -> doPrevious())
                .pos(this.leftPos, this.topPos + 156)
                .size(76, 20)
                .build();
        previous.active = this.page > 0;
        this.addRenderableWidget(previous);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.netmusic.big_megaphone.back"), b -> this.onClose())
                .pos(this.leftPos + 82, this.topPos + 156)
                .size(76, 20)
                .build());

        int maxPage = this.getMaxPage();
        Button next = Button.builder(Component.translatable("gui.netmusic.big_megaphone.page.next"), b -> doNext(maxPage))
                .pos(this.leftPos + 164, this.topPos + 156)
                .size(76, 20)
                .build();
        next.active = this.page < maxPage;
        this.addRenderableWidget(next);
    }

    private void doNext(int maxPage) {
        if (this.page < maxPage) {
            this.page++;
            this.rebuildPresetButtons();
        }
    }

    private void doPrevious() {
        if (this.page > 0) {
            this.page--;
            this.rebuildPresetButtons();
        }
    }

    private int getMaxPage() {
        int size = BigMegaphonePresetManager.getStations().size();
        return size == 0 ? 0 : (size - 1) / PAGE_SIZE;
    }

    private void selectStation(BigMegaphonePresetManager.PresetStation station) {
        this.parent.applyPresetStation(station.name(), station.url());
        this.minecraft.setScreen(this.parent);

    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        graphics.centeredText(this.font, this.title, this.width / 2, this.topPos + 6, 0xFFFFFFFF);
        String pageText = "%d / %d".formatted(this.page + 1, this.getMaxPage() + 1);
        graphics.centeredText(this.font, pageText, this.width / 2, this.topPos + 138, 0xFFAAAAAA);
        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
