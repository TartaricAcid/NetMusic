package com.github.tartaricacid.netmusic.init;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.renderer.MusicPlayerItemRenderer;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class InitItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, NetMusic.MOD_ID);

    public static RegistryObject<Item> MUSIC_CD = ITEMS.register("music_cd", ItemMusicCD::new);
    public static ItemGroup TAB = new ItemGroup("netmusic") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(InitBlocks.MUSIC_PLAYER.get());
        }
    };

    public static RegistryObject<Item> MUSIC_PLAYER = ITEMS.register("music_player", () -> new BlockItem(InitBlocks.MUSIC_PLAYER.get(),
            (new Item.Properties()).tab(TAB).setISTER(() -> MusicPlayerItemRenderer::new)));
    public static RegistryObject<Item> CD_BURNER = ITEMS.register("cd_burner", () -> new BlockItem(InitBlocks.CD_BURNER.get(), new Item.Properties().stacksTo(1).tab(TAB)));
}
