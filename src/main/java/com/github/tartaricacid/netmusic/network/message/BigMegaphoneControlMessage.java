package com.github.tartaricacid.netmusic.network.message;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.tileentity.TileEntityBigMegaphone;
import com.github.tartaricacid.netmusic.util.BigMegaphoneUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

public class BigMegaphoneControlMessage implements Message {
    public static final ResourceLocation ID = new ResourceLocation(NetMusic.MOD_ID, "big_megaphone_control");

    private final BlockPos pos;
    private final String url;
    private final String name;
    private final int range;
    private final Action action;

    public BigMegaphoneControlMessage(BlockPos pos, String url, String name, int range, Action action) {
        this.pos = pos;
        this.url = url;
        this.name = name;
        this.range = range;
        this.action = action;
    }

    public static BigMegaphoneControlMessage decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        String url = buf.readUtf();
        String name = buf.readUtf();
        int range = buf.readVarInt();
        int actionIndex = buf.readVarInt();
        Action action = Action.byIndex(actionIndex);
        return new BigMegaphoneControlMessage(pos, url, name, range, action);
    }

    public static void encode(BigMegaphoneControlMessage message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeUtf(message.url);
        buf.writeUtf(message.name);
        buf.writeVarInt(message.range);
        buf.writeVarInt(message.action.ordinal());
    }

    public static void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        BigMegaphoneControlMessage message = decode(buf);
        server.execute(() -> onHandle(message, player));
    }

    private static void onHandle(BigMegaphoneControlMessage message, ServerPlayer sender) {
        if (sender == null || sender.distanceToSqr(Vec3.atCenterOf(message.pos)) > 64) {
            return;
        }
        if (!(sender.level().getBlockEntity(message.pos) instanceof TileEntityBigMegaphone megaphone)) {
            return;
        }

        if (message.action == Action.STOP) {
            megaphone.stopBroadcast();
            return;
        }

        if (!BigMegaphoneUtil.isValidStreamUrl(message.url) || Util.isBlank(message.name)) {
            return;
        }

        boolean changed = megaphone.applyConfig(message.url, message.name, message.range);
        if (message.action == Action.START) {
            megaphone.startBroadcast();
        } else if (changed && megaphone.isBroadcasting()) {
            megaphone.startBroadcast();
        }
    }

    @Override
    public FriendlyByteBuf toBuffer() {
        FriendlyByteBuf buf = PacketByteBufs.create();
        encode(this, buf);
        return buf;
    }

    @Override
    public ResourceLocation getPacketId() {
        return ID;
    }

    public enum Action {
        SAVE,
        START,
        STOP;

        public static Action byIndex(int index) {
            if (index < 0 || index >= values().length) {
                return SAVE;
            }
            return values()[index];
        }
    }
}
