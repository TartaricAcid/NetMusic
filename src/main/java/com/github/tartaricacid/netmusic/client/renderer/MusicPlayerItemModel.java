package com.github.tartaricacid.netmusic.client.renderer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class MusicPlayerItemModel implements ItemModel {
    private static final ItemTransform GUI_TRANSFORM = transform(22.5f, 22.5f, 0, 0, -2.5f, 0, 0.5f, 0.5f, 0.5f);
    private static final ItemTransform GROUND_TRANSFORM = transform(0, 0, 0, -2.75f, 2, -1.75f, 0.375f, 0.375f, 0.375f);
    private static final ItemTransform HEAD_TRANSFORM = transform(0, 180, 0, 2.75f, 8, 1.75f, 0.5f, 0.5f, 0.5f);
    private static final ItemTransform THIRD_PERSON_TRANSFORM = transform(45, -115, 0, 0, -0.5f, -0.5f, 0.375f, 0.375f, 0.375f);
    private static final ItemTransform FIRST_PERSON_TRANSFORM = transform(0, -45, 30, 1.75f, 1.75f, 1.75f, 0.25f, 0.25f, 0.25f);
    private static final ItemTransform FIXED_TRANSFORM = transform(0, 180, 0, 2, -2.25f, 0, 0.4f, 0.4f, 0.4f);

    private final MusicPlayerItemRenderer renderer;
    private final TextureAtlasSprite particleIcon;
    private final Supplier<Vector3fc[]> extents;

    public MusicPlayerItemModel(MusicPlayerItemRenderer renderer, TextureAtlasSprite particleIcon) {
        this.renderer = renderer;
        this.particleIcon = particleIcon;
        this.extents = this::createExtents;
    }

    @Override
    public void update(ItemStackRenderState renderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext displayContext, ClientLevel clientLevel, ItemOwner itemOwner, int seed) {
        renderState.appendModelIdentityElement(this);

        ItemStackRenderState.LayerRenderState layer = renderState.newLayer();
        layer.setUsesBlockLight(false);
        layer.setParticleIcon(this.particleIcon);
        layer.setTransform(getTransform(displayContext));
        layer.setExtents(this.extents);
        layer.setupSpecialModel(this.renderer, null);

        if (itemStack.hasFoil()) {
            ItemStackRenderState.FoilType foil = ItemStackRenderState.FoilType.STANDARD;
            layer.setFoilType(foil);
            renderState.setAnimated();
            renderState.appendModelIdentityElement(foil);
        }
    }

    private Vector3fc[] createExtents() {
        Set<Vector3fc> extentsSet = new HashSet<>();
        this.renderer.getExtents(extentsSet::add);
        return extentsSet.toArray(new Vector3fc[0]);
    }

    private static ItemTransform getTransform(ItemDisplayContext displayContext) {
        return switch (displayContext) {
            case GUI -> GUI_TRANSFORM;
            case GROUND -> GROUND_TRANSFORM;
            case HEAD -> HEAD_TRANSFORM;
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> THIRD_PERSON_TRANSFORM;
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> FIRST_PERSON_TRANSFORM;
            case FIXED -> FIXED_TRANSFORM;
            default -> ItemTransform.NO_TRANSFORM;
        };
    }

    private static ItemTransform transform(float rotX, float rotY, float rotZ, float tx, float ty, float tz, float sx, float sy, float sz) {
        return new ItemTransform(
                new Vector3f(rotX, rotY, rotZ),
                new Vector3f(tx / 16.0f, ty / 16.0f, tz / 16.0f),
                new Vector3f(sx, sy, sz)
        );
    }
}
