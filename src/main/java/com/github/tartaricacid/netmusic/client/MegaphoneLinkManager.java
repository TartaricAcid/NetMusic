package com.github.tartaricacid.netmusic.client;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

/**
 * 客户端选择模式管理器
 * 当玩家在大喇叭GUI中点击"选择唱片机"按钮后，
 * 关闭GUI并进入选择模式，等待玩家右键唱片机完成链接
 */
public class MegaphoneLinkManager {
    /** 当前正在等待选择唱片机的大喇叭位置，null表示不在选择模式 */
    private static BlockPos pendingMegaphonePos = null;

    /** 进入选择模式 */
    public static void startSelecting(BlockPos megaphonePos) {
        pendingMegaphonePos = megaphonePos;
    }

    /** 是否处于选择模式 */
    public static boolean isSelecting() {
        return pendingMegaphonePos != null;
    }

    /** 获取等待链接的大喇叭位置 */
    @Nullable
    public static BlockPos getPendingMegaphonePos() {
        return pendingMegaphonePos;
    }

    /** 退出选择模式（完成链接或取消） */
    public static void stopSelecting() {
        pendingMegaphonePos = null;
    }
}