package com.github.tartaricacid.netmusic.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class BigMegaphoneRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean broadcasting = false;
}
