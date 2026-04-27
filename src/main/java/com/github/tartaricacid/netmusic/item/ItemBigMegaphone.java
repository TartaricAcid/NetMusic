package com.github.tartaricacid.netmusic.item;

import com.github.tartaricacid.netmusic.client.renderer.BigMegaphoneItemRenderer;
import com.github.tartaricacid.netmusic.init.InitBlocks;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import java.util.function.Function;

public class ItemBigMegaphone extends BlockItem {
    public ItemBigMegaphone() {
        super(InitBlocks.BIG_MEGAPHONE.get(), (new Properties()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private final Function<Minecraft, BlockEntityWithoutLevelRenderer> rendererProvider =
                    Util.memoize(minecraft -> new BigMegaphoneItemRenderer(
                            minecraft.getBlockEntityRenderDispatcher(),
                            minecraft.getEntityModels())
                    );

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                Minecraft minecraft = Minecraft.getInstance();
                return rendererProvider.apply(minecraft);
            }
        });
    }
}
