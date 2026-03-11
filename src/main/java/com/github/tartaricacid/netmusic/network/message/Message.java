package com.github.tartaricacid.netmusic.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface Message {
    FriendlyByteBuf toBuffer();

    ResourceLocation getPacketId();
}