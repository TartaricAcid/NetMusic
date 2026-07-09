package com.github.tartaricacid.netmusic.compat.tlm.backpack;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.compat.tlm.backpack.data.MusicPlayerBackpackData;
import com.github.tartaricacid.netmusic.compat.tlm.client.model.MusicPlayerBackpackModel;
import com.github.tartaricacid.netmusic.compat.tlm.inventory.MusicPlayerBackpackContainer;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.touhoulittlemaid.api.backpack.IBackpackData;
import com.github.tartaricacid.touhoulittlemaid.api.backpack.IMaidBackpack;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityTombstone;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.AbstractMaidContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class MusicPlayerBackpack extends IMaidBackpack {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "music_player_backpack");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "textures/entity/music_player_backpack.png");
    private static final int MAX_AVAILABLE = 30;

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Item getItem() {
        return InitItems.MUSIC_PLAYER_BACKPACK.get();
    }

    @Override
    public void onPutOn(ItemStack itemStack, Player player, EntityMaid entityMaid) {
    }

    @Override
    public void onTakeOff(ItemStack stack, Player player, EntityMaid maid) {
        this.dropAllItems(maid);
    }

    @Override
    public void onSpawnTombstone(EntityMaid entityMaid, EntityTombstone entityTombstone) {
    }

    @Override
    public MenuProvider getGuiProvider(final int entityId) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("Maid Music Player Container");
            }

            @Override
            public AbstractMaidContainer createMenu(int index, Inventory playerInventory, Player player) {
                return new MusicPlayerBackpackContainer(index, playerInventory, entityId);
            }

            @Override
            public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                return false;
            }
        };
    }

    @Override
    public boolean hasBackpackData() {
        return true;
    }

    @Nullable
    @Override
    public IBackpackData getBackpackData(EntityMaid maid) {
        return new MusicPlayerBackpackData();
    }

    @Override
    public int getAvailableMaxContainerIndex() {
        return MAX_AVAILABLE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void offsetBackpackItem(PoseStack poseStack) {
        poseStack.translate(0.0, 0.625, -0.05);
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public EntityModel<EntityMaid> getBackpackModel(EntityModelSet entityModelSet) {
        return new MusicPlayerBackpackModel<>(entityModelSet.bakeLayer(MusicPlayerBackpackModel.LAYER));
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getBackpackTexture() {
        return TEXTURE;
    }
}
