package com.github.tartaricacid.netmusic.client.command;

import com.github.tartaricacid.netmusic.client.config.MusicListManage;
import com.github.tartaricacid.netmusic.network.GiveDiscMessage;
import com.github.tartaricacid.netmusic.proxy.CommonProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class NetMusicCommand extends CommandBase {
    @Override
    public String getName() {
        return "netmusic";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/netmusic <reload|get163|get163cd>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equals("reload")) {
            try {
                MusicListManage.loadConfigSongs();
                sender.sendMessage(new TextComponentTranslation("command.netmusic.music_cd.reload.success"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if (args.length == 2) {
            if (args[0].equals("get163")) {
                try {
                    long listId = parseLong(args[1]);
                    MusicListManage.add163List(listId);
                    sender.sendMessage(new TextComponentTranslation("command.netmusic.music_cd.add163.success"));
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(new TextComponentTranslation("command.netmusic.music_cd.add163.fail"));
                }
                return;
            }

            if (args[0].equals("get163cd")) {
                try {
                    long songId = parseLong(args[1]);
                    CommonProxy.INSTANCE.sendToServer(new GiveDiscMessage(MusicListManage.get163Song(songId)));
                    sender.sendMessage(new TextComponentTranslation("command.netmusic.music_cd.add163cd.success"));
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(new TextComponentTranslation("command.netmusic.music_cd.add163cd.fail"));
                }
                return;
            }
        }

        sender.sendMessage((new TextComponentTranslation("command.netmusic.music_cd.reload"))
                .setStyle(new Style().setColor(TextFormatting.RED)));
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reload", "get163", "get163cd");
        }
        return Collections.emptyList();
    }
}
