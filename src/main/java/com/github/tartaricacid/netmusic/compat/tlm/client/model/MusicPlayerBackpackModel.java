package com.github.tartaricacid.netmusic.compat.tlm.client.model;

import com.github.tartaricacid.netmusic.NetMusic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class MusicPlayerBackpackModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(NetMusic.MOD_ID, "main"), "music_player_backpack");
    private final ModelPart main;

    public MusicPlayerBackpackModel(ModelPart root) {
        this.main = root.getChild("main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create(), PartPose.offset(0.0F, 13.25F, 1.0F));

        PartDefinition bone466 = main.addOrReplaceChild("bone466", CubeListBuilder.create().texOffs(0, 26).addBox(-5.0F, -27.0F, -2.0F, 1.0F, 16.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(4, 26).addBox(4.0F, -27.0F, -2.0F, 1.0F, 16.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 24).addBox(-6.0F, -20.0F, -2.0F, 12.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition bone36 = bone466.addOrReplaceChild("bone36", CubeListBuilder.create().texOffs(0, 20).addBox(-6.0F, 3.5F, -0.5F, 12.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 16).addBox(-6.0F, 5.5F, -0.5F, 12.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.0F, -28.5F, -1.5F));

        PartDefinition bone = bone466.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 6).addBox(6.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.15F))
                .texOffs(0, 6).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.15F)), PartPose.offsetAndRotation(-3.5F, -19.5F, -1.5F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone2 = bone466.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 6).addBox(6.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.15F))
                .texOffs(0, 6).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.15F)), PartPose.offsetAndRotation(-3.5F, -13.5F, -1.5F, 0.7854F, 0.0F, 0.0F));

        PartDefinition bone22 = bone466.addOrReplaceChild("bone22", CubeListBuilder.create(), PartPose.offset(4.0F, -18.9017F, -3.8731F));

        PartDefinition bone23 = bone22.addOrReplaceChild("bone23", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone30 = bone22.addOrReplaceChild("bone30", CubeListBuilder.create().texOffs(0, 8).mirror().addBox(-1.0F, -1.0F, -3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone31 = bone22.addOrReplaceChild("bone31", CubeListBuilder.create().texOffs(10, 0).mirror().addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.025F)).mirror(false), PartPose.offsetAndRotation(-0.5F, 5.5F, 2.0F, -0.2443F, 0.0F, 0.0F));

        PartDefinition bone24 = bone466.addOrReplaceChild("bone24", CubeListBuilder.create(), PartPose.offset(-4.0F, -18.9017F, -3.8731F));

        PartDefinition bone25 = bone24.addOrReplaceChild("bone25", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.025F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone26 = bone24.addOrReplaceChild("bone26", CubeListBuilder.create().texOffs(0, 8).addBox(0.0F, -1.0F, -3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone27 = bone24.addOrReplaceChild("bone27", CubeListBuilder.create().texOffs(10, 0).addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.025F)), PartPose.offsetAndRotation(0.5F, 5.5F, 2.0F, -0.2443F, 0.0F, 0.0F));

        PartDefinition bone4 = bone466.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(24, 0).addBox(-5.5F, -2.3361F, -2.487F, 11.0F, 1.0F, 8.0F, new CubeDeformation(-0.25F))
                .texOffs(0, 22).addBox(-6.0F, -1.6861F, -3.237F, 12.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 18).addBox(-6.0F, -1.6861F, 3.263F, 12.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
                .texOffs(13, 0).addBox(-5.0F, -1.6861F, -3.737F, 1.0F, 1.0F, 9.0F, new CubeDeformation(-0.1F))
                .texOffs(0, 6).addBox(4.0F, -1.6861F, -3.737F, 1.0F, 1.0F, 9.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.0F, -12.3139F, 1.237F));

        PartDefinition bone5 = bone4.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(8, 33).addBox(4.0F, -0.5F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.2F))
                .texOffs(8, 26).addBox(-5.0F, -0.5F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition bone6 = main.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(35, 0).addBox(-15.0F, -13.1861F, -13.237F, 30.0F, 32.0F, 27.0F, new CubeDeformation(-10.5F))
                .texOffs(0, 59).addBox(-11.5F, -7.9861F, -6.187F, 24.0F, 24.0F, 18.0F, new CubeDeformation(-8.5F))
                .texOffs(86, 59).addBox(-8.0F, -6.1861F, -2.187F, 13.0F, 13.0F, 10.0F, new CubeDeformation(-4.5F)), PartPose.offset(0.0F, -2.6139F, 2.237F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
