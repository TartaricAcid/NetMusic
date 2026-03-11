package com.github.tartaricacid.netmusic.compat.tlm.init;

import com.github.tartaricacid.netmusic.compat.tlm.client.model.MusicPlayerBackpackModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class ModelInit {
    public static void init() {
        EntityModelLayerRegistry.registerModelLayer(MusicPlayerBackpackModel.LAYER, MusicPlayerBackpackModel::createBodyLayer);
    }
}
