package com.github.tartaricacid.netmusic.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class LyricOverlay implements IGuiOverlay {
    public static final LyricOverlay instance = new LyricOverlay();

    private static String line = "";

    private static int lyricFadeOutTime = 0;
    private static int lyricFadeInTime = 0;
    private static int lyricStayTime = 0;
    private static int lyricTime = 0;

    int tick = 0;

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        var font = gui.getFont();
        Minecraft minecraft = gui.getMinecraft();
        Options options = minecraft.options;
        if (!options.hideGui && lyricTime > 0 && !line.isEmpty()) {
            float f4 = (float) lyricTime - partialTick;
            int k1 = 255;
            if (lyricTime > lyricFadeOutTime + lyricStayTime) {
                float f6 = (float)(lyricFadeInTime + lyricStayTime + lyricFadeOutTime) - f4;
                k1 = (int)(f6 * 255.0F / (float)lyricFadeInTime);
            }

            if (lyricTime <= lyricFadeOutTime) {
                k1 = (int)(f4 * 255.0F / (float)lyricFadeOutTime);
            }

            k1 = Mth.clamp(k1, 0, 255);
            if (k1 > 8) {
                int i2 = k1 << 24 & -16777216;
                guiGraphics.drawString(font, Component.literal(line).withStyle(ChatFormatting.ITALIC), screenWidth / 2 - font.width(line) / 2, screenHeight / 2 + 60, 16777215 | i2);
            }
        }

        if (tick != gui.getGuiTicks() && lyricTime > 0) {
            tick = gui.getGuiTicks();
            lyricTime--;
        }
    }

    public static void setTimes(int thisLyricTime, int nextLyricTime) {
        lyricFadeInTime = 0;
        lyricStayTime = nextLyricTime == -1 ? 40 : (nextLyricTime - thisLyricTime) + 20;
        lyricFadeOutTime = 20;

        lyricTime = lyricFadeInTime + lyricStayTime + lyricFadeOutTime;
    }

    public static void setLine(String line) {
        LyricOverlay.line = line;
    }
}
