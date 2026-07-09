package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.compat.tlm.client.model.MusicPlayerBackpackModel;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ModelInit {
    public static void init(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MusicPlayerBackpackModel.LAYER, MusicPlayerBackpackModel::createBodyLayer);
    }
}
