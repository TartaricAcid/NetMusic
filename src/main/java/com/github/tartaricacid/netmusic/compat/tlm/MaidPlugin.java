package com.github.tartaricacid.netmusic.compat.tlm;

import com.github.tartaricacid.netmusic.compat.tlm.ai.PlaySoundFunction;
import com.github.tartaricacid.netmusic.compat.tlm.backpack.MusicPlayerBackpack;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.FunctionCallRegister;
import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.BackpackManager;

@LittleMaidExtension
public class MaidPlugin implements ILittleMaid {
    @Override
    public void addMaidBackpack(BackpackManager manager) {
        manager.add(new MusicPlayerBackpack());
    }

    @Override
    public void registerAIFunctionCall(FunctionCallRegister register) {
        register.register(new PlaySoundFunction());
    }
}