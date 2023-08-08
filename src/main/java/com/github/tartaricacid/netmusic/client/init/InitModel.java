package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerItemRenderer;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerRenderer;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = NetMusic.MOD_ID, value = Side.CLIENT)
public final class InitModel {
    @SubscribeEvent
    public static void register(ModelRegistryEvent event) {
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMusicPlayer.class, new MusicPlayerRenderer());
        Item.getItemFromBlock(InitBlocks.MUSIC_PLAYER).setTileEntityItemStackRenderer(MusicPlayerItemRenderer.INSTANCE);
        registerRender(InitItems.MUSIC_CD);
        registerRender(InitBlocks.MUSIC_PLAYER);
        registerRender(InitBlocks.CD_BURNER);
        registerRender(Item.getItemFromBlock(InitBlocks.CD_BURNER));
    }

    public static ModelResourceLocation getModelRl(Item item, String variant) {
        return new ModelResourceLocation(Objects.requireNonNull(item.getRegistryName()), variant);
    }

    public static void registerRender(Block block) {
        registerRender(Item.getItemFromBlock(block));
    }

    public static void registerRender(Item item) {
        registerRender(item, 0);
    }

    public static void registerRender(Item item, int meta) {
        registerRender(item, meta, "inventory");
    }

    public static void registerRender(Item item, int meta, String variant) {
        ModelLoader.setCustomModelResourceLocation(item, meta, getModelRl(item, variant));
    }
}
