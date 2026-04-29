package com.github.tartaricacid.netmusic.client.model;

import com.github.tartaricacid.netmusic.NetMusic;
import com.github.tartaricacid.netmusic.client.renderer.BigMegaphoneRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

public class ModelBigMegaphone {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(NetMusic.MOD_ID, "main"), "big_megaphone");

    public static class Block extends Model<BigMegaphoneRenderState> {
        public Block(ModelPart root) {
            super(root, RenderTypes::entityCutout);
        }
    }

    public static class Item extends Model<Unit> {
        public Item(ModelPart root) {
            super(root, RenderTypes::entityCutout);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(92, 85).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.005F))
                .texOffs(0, 47).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 47).addBox(-2.0F, -32.0F, -2.0F, 4.0F, 18.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bone = root.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(56, 77).addBox(-1.0F, -6.0F, 9.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(28, 88).addBox(3.0F, -8.0F, 12.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(56, 86).addBox(-6.0F, -8.0F, 12.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(28, 80).addBox(-1.0F, 0.0F, 2.4F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(40, 93).addBox(-1.0F, -2.0F, 8.4F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, -9.0F, -2.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(60, 30).addBox(-3.0F, -8.0F, 5.4F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(46, 53).addBox(-6.0F, -11.0F, 12.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 53).addBox(-6.0F, -2.0F, 12.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 47).addBox(-6.0F, -2.0F, -11.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(46, 47).addBox(-6.0F, -11.0F, -11.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(56, 59).addBox(-3.0F, -8.0F, -5.4F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(92, 65).addBox(-1.0F, -2.0F, -6.4F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(80, 24).addBox(-1.0F, 0.0F, -4.4F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(44, 80).addBox(-6.0F, -8.0F, -11.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 86).addBox(3.0F, -8.0F, -11.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(38, 71).addBox(-1.0F, -6.0F, -12.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, -2.0F));

        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 71).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.2242F, -4.8457F, 0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r2 = bone.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(74, 77).addBox(-3.0F, -3.0F, -3.0F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0803F, -5.0F, -6.3764F, 0.0F, 0.3927F, 0.0F));

        PartDefinition cube_r3 = bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 75).addBox(1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0803F, -5.0F, -6.3764F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube_r4 = bone.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(60, 40).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0803F, -6.3764F, -0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(76, 47).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0803F, 10.3764F, 0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r6 = bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(14, 78).addBox(1.0F, -3.0F, -2.0F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0803F, -5.0F, 10.3764F, 0.0F, 0.3927F, 0.0F));

        PartDefinition cube_r7 = bone.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(78, 61).addBox(-3.0F, -3.0F, -2.0F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0803F, -5.0F, 10.3764F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube_r8 = bone.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(76, 54).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.2242F, 8.8457F, -0.3927F, 0.0F, 0.0F));

        PartDefinition bone2 = root.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(56, 77).addBox(-1.0F, -1.88F, 7.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(28, 88).addBox(3.0F, -3.88F, 10.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(56, 86).addBox(-6.0F, -3.88F, 10.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(28, 80).addBox(-1.0F, 4.12F, 0.4F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(40, 93).addBox(-1.0F, 2.12F, 6.4F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.0F, -4.88F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(60, 30).addBox(-3.0F, -3.88F, 3.4F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(46, 53).addBox(-6.0F, -6.88F, 10.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 53).addBox(-6.0F, 2.12F, 10.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(16, 47).addBox(-6.0F, 2.12F, -13.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(46, 47).addBox(-6.0F, -6.88F, -13.0F, 12.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(56, 59).addBox(-3.0F, -3.88F, -7.4F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(92, 65).addBox(-1.0F, 2.12F, -8.4F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(80, 24).addBox(-1.0F, 4.12F, -6.4F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(44, 80).addBox(-6.0F, -3.88F, -13.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 86).addBox(3.0F, -3.88F, -13.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(38, 71).addBox(-1.0F, -1.88F, -14.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -23.12F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition cube_r9 = bone2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(16, 71).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.8958F, -6.8457F, 0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r10 = bone2.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(74, 77).addBox(-3.0F, -3.0F, -3.0F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0803F, -0.88F, -8.3764F, 0.0F, 0.3927F, 0.0F));

        PartDefinition cube_r11 = bone2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 75).addBox(1.0F, -3.0F, -3.0F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0803F, -0.88F, -8.3764F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube_r12 = bone2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(60, 40).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.9603F, -8.3764F, -0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r13 = bone2.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(76, 47).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.9603F, 8.3764F, 0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r14 = bone2.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(14, 78).addBox(1.0F, -3.0F, -2.0F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0803F, -0.88F, 8.3764F, 0.0F, 0.3927F, 0.0F));

        PartDefinition cube_r15 = bone2.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(78, 61).addBox(-3.0F, -3.0F, -2.0F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0803F, -0.88F, 8.3764F, 0.0F, -0.3927F, 0.0F));

        PartDefinition cube_r16 = bone2.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(76, 54).addBox(-3.0F, -3.0F, -2.0F, 6.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.8958F, 6.8457F, -0.3927F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }
}
