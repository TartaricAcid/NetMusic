package com.github.tartaricacid.netmusic.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

public class MusicPlayerRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean hasDisc = false;
    public float discRotation = 0.0f;

    public MutableComponent currentLine = Component.empty();
    public @Nullable MutableComponent translatedLine = null;

    public int currentLyricColor;
    public int transLyricColor;
    public float y;
}
