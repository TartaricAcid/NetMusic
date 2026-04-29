package com.github.tartaricacid.netmusic.client.gui;

import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/**
 * 防止客户端类加载崩溃，故需要专门开辟一个 class 用于打开 GUI
 */
public class BigMegaphoneScreenProxy {
    public static void open(BlockPos blockPos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.getBlockEntity(blockPos) instanceof TileEntityBigMegaphone) {
            minecraft.setScreen(new BigMegaphoneScreen(blockPos));
        }
    }
}
