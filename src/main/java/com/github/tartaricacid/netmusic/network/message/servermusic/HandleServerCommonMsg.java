package com.github.tartaricacid.netmusic.network.message.servermusic;

import com.github.tartaricacid.netmusic.network.NetworkHandler;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;

public class HandleServerCommonMsg {
    public static void handleServer(CommonMessage message, ServerPlayer player) {
        if (message.msgType == CommonMessage.MsgType.ASK) {
            // 处理获取文件大小
            if (message.msg.startsWith("getFileSize/")) {
                File file = new File("saved_music/" + message.msg.split("/")[1]);
                if (!file.exists() || !file.isFile()) {
                    NetworkHandler.sendToClientPlayer(new CommonMessage(
                            message.streamId,
                            CommonMessage.MsgType.ERR,
                            "Not find file",
                            -1
                    ), player);
                }
                NetworkHandler.sendToClientPlayer(new CommonMessage(
                        message.streamId,
                        CommonMessage.MsgType.INFO,
                        "file size",
                        file.length()
                ), player);
            }
        }
    }
}
