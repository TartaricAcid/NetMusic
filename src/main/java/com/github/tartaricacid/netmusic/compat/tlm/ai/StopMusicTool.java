package com.github.tartaricacid.netmusic.compat.tlm.ai;

import com.github.tartaricacid.netmusic.compat.tlm.message.MaidStopMusicMessage;
import com.github.tartaricacid.netmusic.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.ai.agent.tool.ITool;
import com.github.tartaricacid.touhoulittlemaid.ai.manager.entity.LLMCallback;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.ObjectParameter;
import com.github.tartaricacid.touhoulittlemaid.ai.service.function.schema.parameter.Parameter;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class StopMusicTool implements ITool<String> {
    public static final String ID = "stop_music";
    private static final String DESCRIPTION = "Call this tool when the user wants to stop the currently playing music.";

    // 因为 LLM 调用此工具时不需要传参数（JSON 为 {}），此 Codec 会直接将其解析为一个空字符串，避免报错。
    private static final Codec<String> CODEC = Codec.unit("");

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String summary(EntityMaid maid) {
        return DESCRIPTION;
    }

    @Override
    public Parameter parameters(ObjectParameter root, EntityMaid maid) {
        // 不需要参数，所以不调用 root.addProperties()，直接返回空的 root 节点
        return root;
    }

    @Override
    public Codec<String> codec() {
        return CODEC;
    }

    @Override
    public LLMCallback onCall(String toolCallId, String result, LLMCallback callback) {
        EntityMaid maid = callback.getMaid();
        BlockPos blockPos = maid.blockPosition();

        MaidStopMusicMessage stopMsg = MaidStopMusicMessage.create(maid);
        NetworkHandler.sendToNearBy(maid.level(), blockPos, stopMsg);

        return callback.addToolResult("Successfully stopped the music.", toolCallId);
    }

    @Override
    public Component invocationSummaryComponent(String result) {
        return Component.translatable("ai.netmusic.tool.stop_music");
    }
}