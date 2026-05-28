package com.github.tartaricacid.netmusic.client.audio.raytrace;

import net.minecraft.world.level.block.SoundType;

import java.util.IdentityHashMap;

import static net.minecraft.world.level.block.SoundType.*;

public record BlockSoundProperty(
        float absorption,
        float roughness,
        float hfGain
) {
    private static final BlockSoundProperty DEFAULT = new BlockSoundProperty(0.05f, 0.5f, 0.8f);
    private static final IdentityHashMap<SoundType, BlockSoundProperty> PROPERTIES = new IdentityHashMap<>() {{
        put(EMPTY, DEFAULT);
        put(WOOD, new BlockSoundProperty(0.2f, 0.5f, 0.4f));
        put(GRAVEL, new BlockSoundProperty(0.6f, 0.85f, 0.2f));
        put(GRASS, new BlockSoundProperty(0.5f, 0.8f, 0.1f));
        put(LILY_PAD, new BlockSoundProperty(0.5f, 0.3f, 0.4f));
        put(STONE, new BlockSoundProperty(0.05f, 0.3f, 0.8f));
        put(METAL, new BlockSoundProperty(0.02f, 0.1f, 0.95f));
        put(GLASS, new BlockSoundProperty(0.01f, 0, 1));
        put(WOOL, new BlockSoundProperty(0.9f, 0.9f, 0.05f));
        put(SAND, new BlockSoundProperty(0.6f, 0.9f, 0.1f));
        put(SNOW, new BlockSoundProperty(0.92f, 0.8f, 0.01f));
        put(POWDER_SNOW, new BlockSoundProperty(0.98f, 1, 0));
        put(LADDER, new BlockSoundProperty(0.3f, 0.6f, 0.4f));
        put(ANVIL, new BlockSoundProperty(0.01f, 0.15f, 0.9f));
        put(SLIME_BLOCK, new BlockSoundProperty(0.4f, 0.05f, 0.2f));
        put(HONEY_BLOCK, new BlockSoundProperty(0.5f, 0.1f, 0.1f));
        put(WET_GRASS, new BlockSoundProperty(0.65f, 0.7f, 0.1f));
        put(CORAL_BLOCK, new BlockSoundProperty(0.3f, 0.8f, 0.5f));
        put(BAMBOO, new BlockSoundProperty(0.15f, 0.2f, 0.7f));
        put(BAMBOO_SAPLING, new BlockSoundProperty(0.3f, 0.5f, 0.4f));
        put(SCAFFOLDING, new BlockSoundProperty(0.3f, 0.7f, 0.3f));
        put(SWEET_BERRY_BUSH, new BlockSoundProperty(0.7f, 0.9f, 0.1f));
        put(CROP, new BlockSoundProperty(0.8f, 0.9f, 0.1f));
        put(HARD_CROP, new BlockSoundProperty(0.4f, 0.6f, 0.3f));
        put(VINE, new BlockSoundProperty(0.75f, 0.9f, 0.1f));
        put(NETHER_WART, new BlockSoundProperty(0.6f, 0.7f, 0.2f));
        put(LANTERN, new BlockSoundProperty(0.05f, 0.2f, 0.9f));
        put(STEM, new BlockSoundProperty(0.25f, 0.6f, 0.3f));
        put(NYLIUM, new BlockSoundProperty(0.5f, 0.8f, 0.2f));
        put(FUNGUS, new BlockSoundProperty(0.4f, 0.7f, 0.1f));
        put(ROOTS, new BlockSoundProperty(0.7f, 0.9f, 0.1f));
        put(SHROOMLIGHT, new BlockSoundProperty(0.2f, 0.75f, 0.2f));
        put(WEEPING_VINES, new BlockSoundProperty(0.7f, 0.8f, 0.1f));
        put(TWISTING_VINES, new BlockSoundProperty(0.7f, 0.8f, 0.1f));
        put(SOUL_SAND, new BlockSoundProperty(0.75f, 0.95f, 0.05f));
        put(SOUL_SOIL, new BlockSoundProperty(0.5f, 0.7f, 0.2f));
        put(BASALT, new BlockSoundProperty(0.05f, 0.4f, 0.7f));
        put(WART_BLOCK, new BlockSoundProperty(0.5f, 0.6f, 0.1f));
        put(NETHERRACK, new BlockSoundProperty(0.45f, 0.85f, 0.2f));
        put(NETHER_BRICKS, new BlockSoundProperty(0.03f, 0.2f, 0.85f));
        put(NETHER_SPROUTS, new BlockSoundProperty(0.6f, 0.9f, 0.1f));
        put(NETHER_ORE, new BlockSoundProperty(0.4f, 0.8f, 0.25f));
        put(BONE_BLOCK, new BlockSoundProperty(0.2f, 0.5f, 0.5f));
        put(NETHERITE_BLOCK, new BlockSoundProperty(0.01f, 0.1f, 0.95f));
        put(ANCIENT_DEBRIS, new BlockSoundProperty(0.15f, 0.7f, 0.5f));
        put(LODESTONE, new BlockSoundProperty(0.05f, 0.2f, 0.75f));
        put(CHAIN, new BlockSoundProperty(0.1f, 0.7f, 0.8f));
        put(NETHER_GOLD_ORE, new BlockSoundProperty(0.4f, 0.8f, 0.25f));
        put(GILDED_BLACKSTONE, new BlockSoundProperty(0.04f, 0.3f, 0.8f));
        put(CANDLE, new BlockSoundProperty(0.3f, 0.4f, 0.2f));
        put(AMETHYST, new BlockSoundProperty(0.02f, 0.1f, 2));
        put(AMETHYST_CLUSTER, new BlockSoundProperty(0.05f, 0.5f, 2));
        put(SMALL_AMETHYST_BUD, new BlockSoundProperty(0.05f, 0.5f, 2));
        put(MEDIUM_AMETHYST_BUD, new BlockSoundProperty(0.05f, 0.5f, 2));
        put(LARGE_AMETHYST_BUD, new BlockSoundProperty(0.05f, 0.5f, 2));
        put(TUFF, new BlockSoundProperty(0.15f, 0.6f, 0.5f));
        put(TUFF_BRICKS, new BlockSoundProperty(0.1f, 0.3f, 0.6f));
        put(POLISHED_TUFF, new BlockSoundProperty(0.08f, 0.15f, 0.7f));
        put(CALCITE, new BlockSoundProperty(0.15f, 0.4f, 0.6f));
        put(DRIPSTONE_BLOCK, new BlockSoundProperty(0.1f, 0.5f, 0.6f));
        put(POINTED_DRIPSTONE, new BlockSoundProperty(0.1f, 0.7f, 0.6f));
        put(COPPER, new BlockSoundProperty(0.03f, 0.15f, 0.9f));
        put(COPPER_BULB, new BlockSoundProperty(0.05f, 0.2f, 0.85f));
        put(COPPER_GRATE, new BlockSoundProperty(0.2f, 0.6f, 0.7f));
        put(CAVE_VINES, new BlockSoundProperty(0.7f, 0.8f, 0.1f));
        put(SPORE_BLOSSOM, new BlockSoundProperty(0.8f, 0.9f, 0.05f));
        put(CACTUS_FLOWER, new BlockSoundProperty(0.6f, 0.7f, 0.2f));
        put(AZALEA, new BlockSoundProperty(0.6f, 0.9f, 0.1f));
        put(FLOWERING_AZALEA, new BlockSoundProperty(0.6f, 0.9f, 0.1f));
        put(MOSS_CARPET, new BlockSoundProperty(0.9f, 0.95f, 0));
        put(PINK_PETALS, new BlockSoundProperty(0.8f, 0.9f, 0.1f));
        put(LEAF_LITTER, new BlockSoundProperty(0.75f, 0.95f, 0.05f));
        put(MOSS, new BlockSoundProperty(0.85f, 0.85f, 0.05f));
        put(BIG_DRIPLEAF, new BlockSoundProperty(0.6f, 0.4f, 0.2f));
        put(SMALL_DRIPLEAF, new BlockSoundProperty(0.6f, 0.4f, 0.2f));
        put(ROOTED_DIRT, new BlockSoundProperty(0.55f, 0.85f, 0.15f));
        put(HANGING_ROOTS, new BlockSoundProperty(0.7f, 0.9f, 0.1f));
        put(AZALEA_LEAVES, new BlockSoundProperty(0.75f, 0.95f, 0.1f));
        put(SCULK_SENSOR, new BlockSoundProperty(0.8f, 0.5f, 0.2f));
        put(SCULK_CATALYST, new BlockSoundProperty(0.7f, 0.4f, 0.3f));
        put(SCULK, new BlockSoundProperty(0.95f, 0.7f, 0.05f));
        put(SCULK_VEIN, new BlockSoundProperty(0.8f, 0.8f, 0.1f));
        put(SCULK_SHRIEKER, new BlockSoundProperty(0.4f, 0.5f, 0.4f));
        put(GLOW_LICHEN, new BlockSoundProperty(0.2f, 0.6f, 0.4f));
        put(DEEPSLATE, new BlockSoundProperty(0.04f, 0.3f, 0.75f));
        put(DEEPSLATE_BRICKS, new BlockSoundProperty(0.03f, 0.2f, 0.8f));
        put(DEEPSLATE_TILES, new BlockSoundProperty(0.03f, 0.15f, 0.85f));
        put(POLISHED_DEEPSLATE, new BlockSoundProperty(0.03f, 0.1f, 0.85f));
        put(FROGLIGHT, new BlockSoundProperty(0.2f, 0.2f, 0.6f));
        put(FROGSPAWN, new BlockSoundProperty(0.8f, 0.3f, 0.1f));
        put(MANGROVE_ROOTS, new BlockSoundProperty(0.35f, 0.8f, 0.3f));
        put(MUDDY_MANGROVE_ROOTS, new BlockSoundProperty(0.5f, 0.8f, 0.2f));
        put(MUD, new BlockSoundProperty(0.8f, 0.6f, 0.05f));
        put(MUD_BRICKS, new BlockSoundProperty(0.25f, 0.4f, 0.4f));
        put(PACKED_MUD, new BlockSoundProperty(0.3f, 0.5f, 0.3f));
        put(HANGING_SIGN, new BlockSoundProperty(0.2f, 0.5f, 0.4f));
        put(NETHER_WOOD_HANGING_SIGN, new BlockSoundProperty(0.25f, 0.55f, 0.35f));
        put(BAMBOO_WOOD_HANGING_SIGN, new BlockSoundProperty(0.15f, 0.2f, 0.6f));
        put(BAMBOO_WOOD, new BlockSoundProperty(0.15f, 0.25f, 0.8f));
        put(NETHER_WOOD, new BlockSoundProperty(0.25f, 0.55f, 0.35f));
        put(CHERRY_WOOD, new BlockSoundProperty(0.2f, 0.5f, 0.4f));
        put(CHERRY_SAPLING, new BlockSoundProperty(0.6f, 0.8f, 0.2f));
        put(CHERRY_LEAVES, new BlockSoundProperty(0.75f, 0.95f, 0.1f));
        put(CHERRY_WOOD_HANGING_SIGN, new BlockSoundProperty(0.2f, 0.5f, 0.4f));
        put(CHISELED_BOOKSHELF, new BlockSoundProperty(0.4f, 0.7f, 0.2f));
        put(SUSPICIOUS_SAND, new BlockSoundProperty(0.6f, 0.9f, 0.1f));
        put(SUSPICIOUS_GRAVEL, new BlockSoundProperty(0.6f, 0.9f, 0.2f));
        put(DECORATED_POT, new BlockSoundProperty(0.1f, 0.3f, 0.8f));
        put(DECORATED_POT_CRACKED, new BlockSoundProperty(0.1f, 0.8f, 0.8f));
        put(TRIAL_SPAWNER, new BlockSoundProperty(0.05f, 0.4f, 0.7f));
        put(SPONGE, new BlockSoundProperty(0.7f, 0.95f, 0.1f));
        put(WET_SPONGE, new BlockSoundProperty(0.5f, 0.8f, 0.2f));
        put(VAULT, new BlockSoundProperty(0.05f, 0.3f, 0.8f));
        put(CREAKING_HEART, new BlockSoundProperty(0.3f, 0.6f, 0.3f));
        put(HEAVY_CORE, new BlockSoundProperty(0.01f, 0.1f, 0.9f));
        put(COBWEB, new BlockSoundProperty(0.8f, 1, 0.1f));
        put(SPAWNER, new BlockSoundProperty(0.2f, 0.7f, 0.6f));
        put(RESIN, new BlockSoundProperty(0.1f, 0.2f, 0.7f));
        put(RESIN_BRICKS, new BlockSoundProperty(0.08f, 0.2f, 0.75f));
        put(IRON, new BlockSoundProperty(0.02f, 0.05f, 0.95f));
        put(DRIED_GHAST, new BlockSoundProperty(0.3f, 0.6f, 0.3f));
    }};

    public static BlockSoundProperty get(SoundType soundType) {
        return PROPERTIES.getOrDefault(soundType, DEFAULT);
    }
}
