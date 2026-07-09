package com.github.tartaricacid.netmusic.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 播放列表的播放模式
 */
public enum PlayMode {
    SEQUENTIAL("sequential"),
    RANDOM("random"),
    SINGLE_LOOP("single_loop");

    public static final Codec<PlayMode> CODEC = Codec.STRING.xmap(
            PlayMode::byName,
            PlayMode::getName
    );
    public static final StreamCodec<ByteBuf, PlayMode> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
            PlayMode::byName,
            PlayMode::getName
    );

    private final String name;

    PlayMode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PlayMode byName(String name) {
        for (PlayMode mode : values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }
        return SEQUENTIAL;
    }

    /**
     * 循环切换到下一个播放模式
     */
    public PlayMode next() {
        return switch (this) {
            case SEQUENTIAL -> RANDOM;
            case RANDOM -> SINGLE_LOOP;
            case SINGLE_LOOP -> SEQUENTIAL;
        };
    }
}