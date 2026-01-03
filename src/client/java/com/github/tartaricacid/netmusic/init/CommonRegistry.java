package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.gui.CDBurnerMenuScreen;
import com.github.tartaricacid.netmusic.gui.ComputerMenuScreen;
import com.github.tartaricacid.netmusic.networking.message.GetMusicListMessage;
import com.github.tartaricacid.netmusic.networking.message.MusicToClientMessage;
import com.github.tartaricacid.netmusic.networking.message.PlayProgressMessage;
import com.github.tartaricacid.netmusic.networking.message.StopMusicMessage;
import com.github.tartaricacid.netmusic.receiver.GetMusicListMessageReceiver;
import com.github.tartaricacid.netmusic.receiver.MusicToClientMessageReceiver;
import com.github.tartaricacid.netmusic.receiver.PlayProgressMessageReceiver;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;

/**
 * @author : IMG
 * @create : 2024/10/4
 */
public class CommonRegistry {

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(MusicToClientMessage.TYPE, new MusicToClientMessageReceiver());
        ClientPlayNetworking.registerGlobalReceiver(GetMusicListMessage.TYPE, new GetMusicListMessageReceiver());
        ClientPlayNetworking.registerGlobalReceiver(PlayProgressMessage.TYPE, new PlayProgressMessageReceiver());
        ClientPlayNetworking.registerGlobalReceiver(StopMusicMessage.TYPE, new com.github.tartaricacid.netmusic.receiver.StopMusicMessageReceiver());
        BlockRenderLayerMap.INSTANCE.putBlock(InitBlocks.CD_BURNER, RenderLayer.getCutout());
        HandledScreens.register(NetMusic.CD_BURNER_MENU_SCREEN_HANDLER_TYPE, CDBurnerMenuScreen::new);
        HandledScreens.register(NetMusic.COMPUTER_MENU_SCREEN_HANDLER_TYPE, ComputerMenuScreen::new);
    }
}
