package com.github.tartaricacid.netmusic.compat.tlm;

import com.github.tartaricacid.netmusic.compat.tlm.ai.PlayMusicTool;
import com.github.tartaricacid.netmusic.compat.tlm.ai.StopMusicTool;
import com.github.tartaricacid.netmusic.compat.tlm.backpack.MusicPlayerBackpack;
import com.github.tartaricacid.netmusic.compat.tlm.chatbubble.LyricChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.ai.agent.tool.ToolRegister;
import com.github.tartaricacid.touhoulittlemaid.api.ILittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.LittleMaidExtension;
import com.github.tartaricacid.touhoulittlemaid.entity.backpack.BackpackManager;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.ChatBubbleRegister;

@LittleMaidExtension
public class MaidPlugin implements ILittleMaid {
    @Override
    public void addMaidBackpack(BackpackManager manager) {
        manager.add(new MusicPlayerBackpack());
    }

    @Override
    public void registerAITool(ToolRegister register) {
        register.register(new PlayMusicTool());
        register.register(new StopMusicTool());
    }

    @Override
    public void registerChatBubble(ChatBubbleRegister register) {
        register.register(LyricChatBubbleData.ID, new LyricChatBubbleData.LyricChatSerializer());
    }
}