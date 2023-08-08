package com.github.tartaricacid.netmusic.network;

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class NetMusicGuiHandler implements IGuiHandler {
    public static final int CD_BURNER_ID = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == CD_BURNER_ID) {
            return new CDBurnerMenu(player.inventory);
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == CD_BURNER_ID) {
            return new CDBurnerMenuScreen(player.inventory);
        }
        return null;
    }
}
