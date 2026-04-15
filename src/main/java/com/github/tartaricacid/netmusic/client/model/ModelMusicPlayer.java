package com.github.tartaricacid.netmusic.client.model;

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


public class ModelMusicPlayer<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(NetMusic.MOD_ID, "main"), "musicplayer");
    private final ModelPart laba;
    private final ModelPart tube;
    private final ModelPart wheel;
    private final ModelPart ruler;
    private final ModelPart box;
    private final ModelPart disc;
    private final ModelPart getDiscBone;

    public ModelMusicPlayer(ModelPart root) {
        this.laba = root.getChild("laba");
        this.tube = root.getChild("tube");
        this.wheel = root.getChild("wheel");
        this.ruler = root.getChild("ruler");
        this.box = root.getChild("box");
        this.disc = root.getChild("disc");
        this.getDiscBone = root.getChild("getDiscBone");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition laba = partdefinition.addOrReplaceChild("laba", CubeListBuilder.create().texOffs(6, 19).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.0F, 7.0F, -2.3562F, 0.0F, 0.0F));

        PartDefinition piece = laba.addOrReplaceChild("piece", CubeListBuilder.create(), PartPose.offset(0.0F, 13.0F, 0.0F));

        PartDefinition bone = piece.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.1706F, 0.6479F, 0.0F, 0.0F));

        PartDefinition bone2 = bone.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, -0.245F));

        PartDefinition bone3 = bone.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, 0.245F));

        PartDefinition bone16 = bone.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.7854F));

        PartDefinition bone20 = bone.addOrReplaceChild("bone20", CubeListBuilder.create().texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.7854F));

        PartDefinition bone17 = bone.addOrReplaceChild("bone17", CubeListBuilder.create().texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone19 = bone.addOrReplaceChild("bone19", CubeListBuilder.create().texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.3927F));

        PartDefinition bone18 = bone.addOrReplaceChild("bone18", CubeListBuilder.create(), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone5 = piece.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.3F, 2.3956F, 0.3066F, 0.0F, 0.0F));

        PartDefinition bone6 = bone5.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, -0.1244F));

        PartDefinition bone7 = bone5.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(-1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, 0.1244F));

        PartDefinition piece2 = laba.addOrReplaceChild("piece2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone4 = piece2.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.1706F, 0.6479F, 0.0F, 0.0F));

        PartDefinition bone8 = bone4.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, -0.245F));

        PartDefinition bone10 = bone4.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, 0.245F));

        PartDefinition bone11 = bone4.addOrReplaceChild("bone11", CubeListBuilder.create().texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.7854F));

        PartDefinition bone12 = bone4.addOrReplaceChild("bone12", CubeListBuilder.create().texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.7854F));

        PartDefinition bone13 = bone4.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone14 = bone4.addOrReplaceChild("bone14", CubeListBuilder.create().texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.3927F));

        PartDefinition bone15 = bone4.addOrReplaceChild("bone15", CubeListBuilder.create(), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone21 = piece2.addOrReplaceChild("bone21", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.3F, 2.3956F, 0.3066F, 0.0F, 0.0F));

        PartDefinition bone22 = bone21.addOrReplaceChild("bone22", CubeListBuilder.create().texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, -0.1244F));

        PartDefinition bone23 = bone21.addOrReplaceChild("bone23", CubeListBuilder.create().texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(-1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, 0.1244F));

        PartDefinition piece3 = laba.addOrReplaceChild("piece3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone24 = piece3.addOrReplaceChild("bone24", CubeListBuilder.create().texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.1706F, 0.6479F, 0.0F, 0.0F));

        PartDefinition bone25 = bone24.addOrReplaceChild("bone25", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, -0.245F));

        PartDefinition bone26 = bone24.addOrReplaceChild("bone26", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, 0.245F));

        PartDefinition bone27 = bone24.addOrReplaceChild("bone27", CubeListBuilder.create().texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.7854F));

        PartDefinition bone28 = bone24.addOrReplaceChild("bone28", CubeListBuilder.create().texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.7854F));

        PartDefinition bone29 = bone24.addOrReplaceChild("bone29", CubeListBuilder.create().texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone30 = bone24.addOrReplaceChild("bone30", CubeListBuilder.create().texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.3927F));

        PartDefinition bone31 = bone24.addOrReplaceChild("bone31", CubeListBuilder.create(), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone32 = piece3.addOrReplaceChild("bone32", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.3F, 2.3956F, 0.3066F, 0.0F, 0.0F));

        PartDefinition bone33 = bone32.addOrReplaceChild("bone33", CubeListBuilder.create().texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, -0.1244F));

        PartDefinition bone34 = bone32.addOrReplaceChild("bone34", CubeListBuilder.create().texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(-1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, 0.1244F));

        PartDefinition piece4 = laba.addOrReplaceChild("piece4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition bone35 = piece4.addOrReplaceChild("bone35", CubeListBuilder.create().texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.1706F, 0.6479F, 0.0F, 0.0F));

        PartDefinition bone36 = bone35.addOrReplaceChild("bone36", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, -0.245F));

        PartDefinition bone37 = bone35.addOrReplaceChild("bone37", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, 0.245F));

        PartDefinition bone38 = bone35.addOrReplaceChild("bone38", CubeListBuilder.create().texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.7854F));

        PartDefinition bone39 = bone35.addOrReplaceChild("bone39", CubeListBuilder.create().texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.7854F));

        PartDefinition bone40 = bone35.addOrReplaceChild("bone40", CubeListBuilder.create().texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone41 = bone35.addOrReplaceChild("bone41", CubeListBuilder.create().texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.3927F));

        PartDefinition bone42 = bone35.addOrReplaceChild("bone42", CubeListBuilder.create(), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone43 = piece4.addOrReplaceChild("bone43", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.3F, 2.3956F, 0.3066F, 0.0F, 0.0F));

        PartDefinition bone44 = bone43.addOrReplaceChild("bone44", CubeListBuilder.create().texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, -0.1244F));

        PartDefinition bone45 = bone43.addOrReplaceChild("bone45", CubeListBuilder.create().texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(-1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, 0.1244F));

        PartDefinition piece5 = laba.addOrReplaceChild("piece5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition bone46 = piece5.addOrReplaceChild("bone46", CubeListBuilder.create().texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.1706F, 0.6479F, 0.0F, 0.0F));

        PartDefinition bone47 = bone46.addOrReplaceChild("bone47", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, -0.245F));

        PartDefinition bone48 = bone46.addOrReplaceChild("bone48", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, 0.245F));

        PartDefinition bone49 = bone46.addOrReplaceChild("bone49", CubeListBuilder.create().texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.7854F));

        PartDefinition bone50 = bone46.addOrReplaceChild("bone50", CubeListBuilder.create().texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.7854F));

        PartDefinition bone51 = bone46.addOrReplaceChild("bone51", CubeListBuilder.create().texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone52 = bone46.addOrReplaceChild("bone52", CubeListBuilder.create().texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.3927F));

        PartDefinition bone53 = bone46.addOrReplaceChild("bone53", CubeListBuilder.create(), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone54 = piece5.addOrReplaceChild("bone54", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.3F, 2.3956F, 0.3066F, 0.0F, 0.0F));

        PartDefinition bone55 = bone54.addOrReplaceChild("bone55", CubeListBuilder.create().texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, -0.1244F));

        PartDefinition bone56 = bone54.addOrReplaceChild("bone56", CubeListBuilder.create().texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(-1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, 0.1244F));

        PartDefinition piece6 = laba.addOrReplaceChild("piece6", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

        PartDefinition bone57 = piece6.addOrReplaceChild("bone57", CubeListBuilder.create().texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.1706F, 0.6479F, 0.0F, 0.0F));

        PartDefinition bone58 = bone57.addOrReplaceChild("bone58", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, -0.245F));

        PartDefinition bone59 = bone57.addOrReplaceChild("bone59", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, 0.245F));

        PartDefinition bone60 = bone57.addOrReplaceChild("bone60", CubeListBuilder.create().texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.7854F));

        PartDefinition bone61 = bone57.addOrReplaceChild("bone61", CubeListBuilder.create().texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.7854F));

        PartDefinition bone62 = bone57.addOrReplaceChild("bone62", CubeListBuilder.create().texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone63 = bone57.addOrReplaceChild("bone63", CubeListBuilder.create().texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.3927F));

        PartDefinition bone64 = bone57.addOrReplaceChild("bone64", CubeListBuilder.create(), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone65 = piece6.addOrReplaceChild("bone65", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.3F, 2.3956F, 0.3066F, 0.0F, 0.0F));

        PartDefinition bone66 = bone65.addOrReplaceChild("bone66", CubeListBuilder.create().texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, -0.1244F));

        PartDefinition bone67 = bone65.addOrReplaceChild("bone67", CubeListBuilder.create().texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(-1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, 0.1244F));

        PartDefinition piece7 = laba.addOrReplaceChild("piece7", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition bone68 = piece7.addOrReplaceChild("bone68", CubeListBuilder.create().texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.1706F, 0.6479F, 0.0F, 0.0F));

        PartDefinition bone69 = bone68.addOrReplaceChild("bone69", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, -0.245F));

        PartDefinition bone70 = bone68.addOrReplaceChild("bone70", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, 0.245F));

        PartDefinition bone71 = bone68.addOrReplaceChild("bone71", CubeListBuilder.create().texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.7854F));

        PartDefinition bone72 = bone68.addOrReplaceChild("bone72", CubeListBuilder.create().texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.7854F));

        PartDefinition bone73 = bone68.addOrReplaceChild("bone73", CubeListBuilder.create().texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone74 = bone68.addOrReplaceChild("bone74", CubeListBuilder.create().texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.3927F));

        PartDefinition bone75 = bone68.addOrReplaceChild("bone75", CubeListBuilder.create(), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone76 = piece7.addOrReplaceChild("bone76", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.3F, 2.3956F, 0.3066F, 0.0F, 0.0F));

        PartDefinition bone77 = bone76.addOrReplaceChild("bone77", CubeListBuilder.create().texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, -0.1244F));

        PartDefinition bone78 = bone76.addOrReplaceChild("bone78", CubeListBuilder.create().texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(-1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, 0.1244F));

        PartDefinition piece8 = laba.addOrReplaceChild("piece8", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 13.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone79 = piece8.addOrReplaceChild("bone79", CubeListBuilder.create().texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(0.0F, 0.0F, 7.1706F, 0.6479F, 0.0F, 0.0F));

        PartDefinition bone80 = bone79.addOrReplaceChild("bone80", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, -0.245F));

        PartDefinition bone81 = bone79.addOrReplaceChild("bone81", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.2425F, 0.0F, 0.0F, 0.0F, 0.245F));

        PartDefinition bone82 = bone79.addOrReplaceChild("bone82", CubeListBuilder.create().texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.7854F));

        PartDefinition bone83 = bone79.addOrReplaceChild("bone83", CubeListBuilder.create().texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.7854F));

        PartDefinition bone84 = bone79.addOrReplaceChild("bone84", CubeListBuilder.create().texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone85 = bone79.addOrReplaceChild("bone85", CubeListBuilder.create().texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1969F)), PartPose.offsetAndRotation(-2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, 0.3927F));

        PartDefinition bone86 = bone79.addOrReplaceChild("bone86", CubeListBuilder.create(), PartPose.offsetAndRotation(2.9701F, 0.0F, 0.001F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone87 = piece8.addOrReplaceChild("bone87", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -6.3F, 2.3956F, 0.3066F, 0.0F, 0.0F));

        PartDefinition bone88 = bone87.addOrReplaceChild("bone88", CubeListBuilder.create().texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, -0.1244F));

        PartDefinition bone89 = bone87.addOrReplaceChild("bone89", CubeListBuilder.create().texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
                .texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.26F))
                .texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.31F)), PartPose.offsetAndRotation(-1.0F, 0.124F, 2.4152F, 0.0F, 0.0F, 0.1244F));

        PartDefinition tube = partdefinition.addOrReplaceChild("tube", CubeListBuilder.create().texOffs(6, 19).addBox(-1.25F, -12.4297F, 5.2362F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(0.7637F, -9.416F, 5.2362F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).addBox(5.7773F, -9.416F, 0.2226F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(9, 24).addBox(3.7637F, -9.416F, -1.7911F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.75F, 24.0F, 1.5F));

        PartDefinition bone90 = tube.addOrReplaceChild("bone90", CubeListBuilder.create().texOffs(10, 27).addBox(-0.5F, -0.6533F, -0.2706F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.75F, -12.8536F, 5.3536F, -1.1781F, 0.0F, 0.0F));

        PartDefinition bone97 = tube.addOrReplaceChild("bone97", CubeListBuilder.create().texOffs(0, 25).addBox(-0.7294F, -0.5F, -0.3467F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2773F, -8.916F, -0.2774F, 0.0F, 0.3927F, 0.0F));

        PartDefinition bone98 = tube.addOrReplaceChild("bone98", CubeListBuilder.create().texOffs(0, 25).addBox(-0.6173F, -0.5F, -1.2168F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2773F, -8.916F, -0.2774F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone99 = tube.addOrReplaceChild("bone99", CubeListBuilder.create().texOffs(0, 25).addBox(-0.1808F, -0.5F, -1.9777F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2773F, -8.916F, -0.2774F, 0.0F, 1.1781F, 0.0F));

        PartDefinition bone94 = tube.addOrReplaceChild("bone94", CubeListBuilder.create().texOffs(7, 26).addBox(1.1945F, -0.5F, 0.036F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2637F, -8.916F, 5.7362F, 0.0F, 0.3927F, 0.0F));

        PartDefinition bone95 = tube.addOrReplaceChild("bone95", CubeListBuilder.create().texOffs(7, 26).addBox(1.631F, -0.5F, 0.7969F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2637F, -8.916F, 5.7362F, 0.0F, 0.7854F, 0.0F));

        PartDefinition bone96 = tube.addOrReplaceChild("bone96", CubeListBuilder.create().texOffs(3, 26).addBox(1.7431F, -0.5F, 1.667F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.2637F, -8.916F, 5.7362F, 0.0F, 1.1781F, 0.0F));

        PartDefinition bone91 = tube.addOrReplaceChild("bone91", CubeListBuilder.create().texOffs(0, 27).addBox(-0.2706F, -0.6533F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.75F, -9.9297F, 5.7362F, 0.0F, 0.0F, -0.3927F));

        PartDefinition bone92 = tube.addOrReplaceChild("bone92", CubeListBuilder.create().texOffs(0, 27).addBox(-0.3827F, 0.2168F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.75F, -9.9297F, 5.7362F, 0.0F, 0.0F, -0.7854F));

        PartDefinition bone93 = tube.addOrReplaceChild("bone93", CubeListBuilder.create().texOffs(0, 27).addBox(-0.8192F, 0.9777F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.75F, -9.9297F, 5.7362F, 0.0F, 0.0F, -1.1781F));

        PartDefinition wheel = partdefinition.addOrReplaceChild("wheel", CubeListBuilder.create().texOffs(12, 8).addBox(-1.3536F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1464F)), PartPose.offset(5.0137F, 15.084F, 0.2089F));

        PartDefinition bone9 = wheel.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(12, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1464F)), PartPose.offsetAndRotation(-0.8536F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition bone100 = wheel.addOrReplaceChild("bone100", CubeListBuilder.create().texOffs(12, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1464F)), PartPose.offsetAndRotation(-0.8536F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition bone101 = wheel.addOrReplaceChild("bone101", CubeListBuilder.create().texOffs(12, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1464F)), PartPose.offsetAndRotation(-0.8536F, 0.0F, 0.0F, -2.3562F, 0.0F, 0.0F));

        PartDefinition ruler = partdefinition.addOrReplaceChild("ruler", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, -12.0F, 7.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition box = partdefinition.addOrReplaceChild("box", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -1.0F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, -1.75F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(-0.25F))
                .texOffs(0, 0).addBox(-8.0F, -6.25F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(-0.25F))
                .texOffs(0, 0).addBox(-8.0F, -7.0F, -8.0F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bone102 = box.addOrReplaceChild("bone102", CubeListBuilder.create().texOffs(0, 0).addBox(4.0F, -5.5F, 5.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, -5.5F, 5.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-8.0F, -5.5F, -7.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(4.0F, -5.5F, -7.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(-7.5F, -5.5F, -6.5F, 14.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 7).addBox(-3.0F, -5.0F, -7.0F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.0F, -0.5F));

        PartDefinition disc = partdefinition.addOrReplaceChild("disc", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(-0.0068F))
                .texOffs(12, 40).addBox(-6.5F, -6.501F, -6.5F, 13.0F, 11.0F, 13.0F, new CubeDeformation(-5.5F)), PartPose.offset(0.0F, 17.0F, 0.0F));

        PartDefinition bone103 = disc.addOrReplaceChild("bone103", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(-0.0068F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition bone104 = disc.addOrReplaceChild("bone104", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(-0.0068F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone105 = disc.addOrReplaceChild("bone105", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(-0.0068F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition bone106 = disc.addOrReplaceChild("bone106", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(-0.0068F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone107 = disc.addOrReplaceChild("bone107", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(-0.0068F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, -1.9635F, 0.0F));

        PartDefinition bone108 = disc.addOrReplaceChild("bone108", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(-0.0068F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition bone109 = disc.addOrReplaceChild("bone109", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(-0.0068F)), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, -2.7489F, 0.0F));

        PartDefinition huahen = disc.addOrReplaceChild("huahen", CubeListBuilder.create().texOffs(0, 47).addBox(-2.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1022F))
                .texOffs(0, 47).addBox(1.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1022F))
                .texOffs(0, 52).addBox(-4.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.2044F))
                .texOffs(0, 52).addBox(3.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.2044F)), PartPose.offset(0.0F, 7.0F, 0.0F));

        PartDefinition bone110 = huahen.addOrReplaceChild("bone110", CubeListBuilder.create().texOffs(0, 47).addBox(-2.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1022F))
                .texOffs(0, 47).addBox(1.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1022F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition bone111 = huahen.addOrReplaceChild("bone111", CubeListBuilder.create().texOffs(0, 47).addBox(-2.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1022F))
                .texOffs(0, 47).addBox(1.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1022F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone112 = huahen.addOrReplaceChild("bone112", CubeListBuilder.create().texOffs(0, 47).addBox(-2.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1022F))
                .texOffs(0, 47).addBox(1.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1022F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition bone113 = huahen.addOrReplaceChild("bone113", CubeListBuilder.create().texOffs(0, 47).addBox(-2.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1022F))
                .texOffs(0, 47).addBox(1.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1022F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bone114 = huahen.addOrReplaceChild("bone114", CubeListBuilder.create().texOffs(0, 52).addBox(-4.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.2044F))
                .texOffs(0, 52).addBox(3.2044F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.2044F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        PartDefinition bone115 = huahen.addOrReplaceChild("bone115", CubeListBuilder.create().texOffs(0, 52).addBox(-4.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.2044F))
                .texOffs(0, 52).addBox(3.2044F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.2044F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bone116 = huahen.addOrReplaceChild("bone116", CubeListBuilder.create().texOffs(0, 52).addBox(-4.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.2044F))
                .texOffs(0, 52).addBox(3.2044F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.2044F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.1781F, 0.0F));

        PartDefinition bone117 = huahen.addOrReplaceChild("bone117", CubeListBuilder.create().texOffs(0, 52).addBox(-4.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.2044F))
                .texOffs(0, 52).addBox(3.2044F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.2044F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition getDiscBone = partdefinition.addOrReplaceChild("getDiscBone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        laba.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        tube.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        wheel.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        ruler.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        box.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        disc.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        getDiscBone.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    public ModelPart getDiscBone() {
        return disc;
    }
}