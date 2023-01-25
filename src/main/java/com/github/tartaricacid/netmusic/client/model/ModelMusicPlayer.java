package com.github.tartaricacid.netmusic.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;


public class ModelMusicPlayer extends EntityModel<Entity> {
    private final ModelRenderer laba;
    private final ModelRenderer piece;
    private final ModelRenderer bone;
    private final ModelRenderer bone2;
    private final ModelRenderer bone3;
    private final ModelRenderer bone16;
    private final ModelRenderer bone20;
    private final ModelRenderer bone17;
    private final ModelRenderer bone19;
    private final ModelRenderer bone18;
    private final ModelRenderer bone5;
    private final ModelRenderer bone6;
    private final ModelRenderer bone7;
    private final ModelRenderer piece2;
    private final ModelRenderer bone4;
    private final ModelRenderer bone8;
    private final ModelRenderer bone10;
    private final ModelRenderer bone11;
    private final ModelRenderer bone12;
    private final ModelRenderer bone13;
    private final ModelRenderer bone14;
    private final ModelRenderer bone15;
    private final ModelRenderer bone21;
    private final ModelRenderer bone22;
    private final ModelRenderer bone23;
    private final ModelRenderer piece3;
    private final ModelRenderer bone24;
    private final ModelRenderer bone25;
    private final ModelRenderer bone26;
    private final ModelRenderer bone27;
    private final ModelRenderer bone28;
    private final ModelRenderer bone29;
    private final ModelRenderer bone30;
    private final ModelRenderer bone31;
    private final ModelRenderer bone32;
    private final ModelRenderer bone33;
    private final ModelRenderer bone34;
    private final ModelRenderer piece4;
    private final ModelRenderer bone35;
    private final ModelRenderer bone36;
    private final ModelRenderer bone37;
    private final ModelRenderer bone38;
    private final ModelRenderer bone39;
    private final ModelRenderer bone40;
    private final ModelRenderer bone41;
    private final ModelRenderer bone42;
    private final ModelRenderer bone43;
    private final ModelRenderer bone44;
    private final ModelRenderer bone45;
    private final ModelRenderer piece5;
    private final ModelRenderer bone46;
    private final ModelRenderer bone47;
    private final ModelRenderer bone48;
    private final ModelRenderer bone49;
    private final ModelRenderer bone50;
    private final ModelRenderer bone51;
    private final ModelRenderer bone52;
    private final ModelRenderer bone53;
    private final ModelRenderer bone54;
    private final ModelRenderer bone55;
    private final ModelRenderer bone56;
    private final ModelRenderer piece6;
    private final ModelRenderer bone57;
    private final ModelRenderer bone58;
    private final ModelRenderer bone59;
    private final ModelRenderer bone60;
    private final ModelRenderer bone61;
    private final ModelRenderer bone62;
    private final ModelRenderer bone63;
    private final ModelRenderer bone64;
    private final ModelRenderer bone65;
    private final ModelRenderer bone66;
    private final ModelRenderer bone67;
    private final ModelRenderer piece7;
    private final ModelRenderer bone68;
    private final ModelRenderer bone69;
    private final ModelRenderer bone70;
    private final ModelRenderer bone71;
    private final ModelRenderer bone72;
    private final ModelRenderer bone73;
    private final ModelRenderer bone74;
    private final ModelRenderer bone75;
    private final ModelRenderer bone76;
    private final ModelRenderer bone77;
    private final ModelRenderer bone78;
    private final ModelRenderer piece8;
    private final ModelRenderer bone79;
    private final ModelRenderer bone80;
    private final ModelRenderer bone81;
    private final ModelRenderer bone82;
    private final ModelRenderer bone83;
    private final ModelRenderer bone84;
    private final ModelRenderer bone85;
    private final ModelRenderer bone86;
    private final ModelRenderer bone87;
    private final ModelRenderer bone88;
    private final ModelRenderer bone89;
    private final ModelRenderer tube;
    private final ModelRenderer bone90;
    private final ModelRenderer bone97;
    private final ModelRenderer bone98;
    private final ModelRenderer bone99;
    private final ModelRenderer bone94;
    private final ModelRenderer bone95;
    private final ModelRenderer bone96;
    private final ModelRenderer bone91;
    private final ModelRenderer bone92;
    private final ModelRenderer bone93;
    private final ModelRenderer wheel;
    private final ModelRenderer bone9;
    private final ModelRenderer bone100;
    private final ModelRenderer bone101;
    private final ModelRenderer ruler;
    private final ModelRenderer box;
    private final ModelRenderer bone102;
    private final ModelRenderer disc;
    private final ModelRenderer bone103;
    private final ModelRenderer bone104;
    private final ModelRenderer bone105;
    private final ModelRenderer bone106;
    private final ModelRenderer bone107;
    private final ModelRenderer bone108;
    private final ModelRenderer bone109;
    private final ModelRenderer huahen;
    private final ModelRenderer bone110;
    private final ModelRenderer bone111;
    private final ModelRenderer bone112;
    private final ModelRenderer bone113;
    private final ModelRenderer bone114;
    private final ModelRenderer bone115;
    private final ModelRenderer bone116;
    private final ModelRenderer bone117;
    private final ModelRenderer getDiscBone;

    public ModelMusicPlayer() {
        texWidth = 64;
        texHeight = 64;

        laba = new ModelRenderer(this);
        laba.setPos(0.0F, 11.0F, 7.0F);
        setRotationAngle(laba, -2.3562F, 0.0F, 0.0F);
        laba.texOffs(6, 19).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);

        piece = new ModelRenderer(this);
        piece.setPos(0.0F, 13.0F, 0.0F);
        laba.addChild(piece);


        bone = new ModelRenderer(this);
        bone.setPos(0.0F, 0.0F, 7.1706F);
        piece.addChild(bone);
        setRotationAngle(bone, 0.6479F, 0.0F, 0.0F);
        bone.texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        bone.texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, 0.0F, false);
        bone.texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone2 = new ModelRenderer(this);
        bone2.setPos(2.0F, 0.2425F, 0.0F);
        bone.addChild(bone2);
        setRotationAngle(bone2, 0.0F, 0.0F, -0.245F);
        bone2.texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone3 = new ModelRenderer(this);
        bone3.setPos(-2.0F, 0.2425F, 0.0F);
        bone.addChild(bone3);
        setRotationAngle(bone3, 0.0F, 0.0F, 0.245F);
        bone3.texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone16 = new ModelRenderer(this);
        bone16.setPos(2.9701F, 0.0F, 0.001F);
        bone.addChild(bone16);
        setRotationAngle(bone16, 0.0F, 0.0F, -0.7854F);
        bone16.texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone20 = new ModelRenderer(this);
        bone20.setPos(-2.9701F, 0.0F, 0.001F);
        bone.addChild(bone20);
        setRotationAngle(bone20, 0.0F, 0.0F, 0.7854F);
        bone20.texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone17 = new ModelRenderer(this);
        bone17.setPos(2.9701F, 0.0F, 0.001F);
        bone.addChild(bone17);
        setRotationAngle(bone17, 0.0F, 0.0F, -0.3927F);
        bone17.texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone19 = new ModelRenderer(this);
        bone19.setPos(-2.9701F, 0.0F, 0.001F);
        bone.addChild(bone19);
        setRotationAngle(bone19, 0.0F, 0.0F, 0.3927F);
        bone19.texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone18 = new ModelRenderer(this);
        bone18.setPos(2.9701F, 0.0F, 0.001F);
        bone.addChild(bone18);
        setRotationAngle(bone18, 0.0F, 0.0F, -0.3927F);


        bone5 = new ModelRenderer(this);
        bone5.setPos(0.0F, -6.3F, 2.3956F);
        piece.addChild(bone5);
        setRotationAngle(bone5, 0.3066F, 0.0F, 0.0F);


        bone6 = new ModelRenderer(this);
        bone6.setPos(1.0F, 0.124F, 2.4152F);
        bone5.addChild(bone6);
        setRotationAngle(bone6, 0.0F, 0.0F, -0.1244F);
        bone6.texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone6.texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone6.texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone6.texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        bone7 = new ModelRenderer(this);
        bone7.setPos(-1.0F, 0.124F, 2.4152F);
        bone5.addChild(bone7);
        setRotationAngle(bone7, 0.0F, 0.0F, 0.1244F);
        bone7.texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone7.texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone7.texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone7.texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        piece2 = new ModelRenderer(this);
        piece2.setPos(0.0F, 13.0F, 0.0F);
        laba.addChild(piece2);
        setRotationAngle(piece2, 0.0F, -0.7854F, 0.0F);


        bone4 = new ModelRenderer(this);
        bone4.setPos(0.0F, 0.0F, 7.1706F);
        piece2.addChild(bone4);
        setRotationAngle(bone4, 0.6479F, 0.0F, 0.0F);
        bone4.texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        bone4.texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, 0.0F, false);
        bone4.texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone8 = new ModelRenderer(this);
        bone8.setPos(2.0F, 0.2425F, 0.0F);
        bone4.addChild(bone8);
        setRotationAngle(bone8, 0.0F, 0.0F, -0.245F);
        bone8.texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone10 = new ModelRenderer(this);
        bone10.setPos(-2.0F, 0.2425F, 0.0F);
        bone4.addChild(bone10);
        setRotationAngle(bone10, 0.0F, 0.0F, 0.245F);
        bone10.texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone11 = new ModelRenderer(this);
        bone11.setPos(2.9701F, 0.0F, 0.001F);
        bone4.addChild(bone11);
        setRotationAngle(bone11, 0.0F, 0.0F, -0.7854F);
        bone11.texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone12 = new ModelRenderer(this);
        bone12.setPos(-2.9701F, 0.0F, 0.001F);
        bone4.addChild(bone12);
        setRotationAngle(bone12, 0.0F, 0.0F, 0.7854F);
        bone12.texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone13 = new ModelRenderer(this);
        bone13.setPos(2.9701F, 0.0F, 0.001F);
        bone4.addChild(bone13);
        setRotationAngle(bone13, 0.0F, 0.0F, -0.3927F);
        bone13.texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone14 = new ModelRenderer(this);
        bone14.setPos(-2.9701F, 0.0F, 0.001F);
        bone4.addChild(bone14);
        setRotationAngle(bone14, 0.0F, 0.0F, 0.3927F);
        bone14.texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone15 = new ModelRenderer(this);
        bone15.setPos(2.9701F, 0.0F, 0.001F);
        bone4.addChild(bone15);
        setRotationAngle(bone15, 0.0F, 0.0F, -0.3927F);


        bone21 = new ModelRenderer(this);
        bone21.setPos(0.0F, -6.3F, 2.3956F);
        piece2.addChild(bone21);
        setRotationAngle(bone21, 0.3066F, 0.0F, 0.0F);


        bone22 = new ModelRenderer(this);
        bone22.setPos(1.0F, 0.124F, 2.4152F);
        bone21.addChild(bone22);
        setRotationAngle(bone22, 0.0F, 0.0F, -0.1244F);
        bone22.texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone22.texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone22.texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone22.texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        bone23 = new ModelRenderer(this);
        bone23.setPos(-1.0F, 0.124F, 2.4152F);
        bone21.addChild(bone23);
        setRotationAngle(bone23, 0.0F, 0.0F, 0.1244F);
        bone23.texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone23.texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone23.texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone23.texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        piece3 = new ModelRenderer(this);
        piece3.setPos(0.0F, 13.0F, 0.0F);
        laba.addChild(piece3);
        setRotationAngle(piece3, 0.0F, -1.5708F, 0.0F);


        bone24 = new ModelRenderer(this);
        bone24.setPos(0.0F, 0.0F, 7.1706F);
        piece3.addChild(bone24);
        setRotationAngle(bone24, 0.6479F, 0.0F, 0.0F);
        bone24.texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        bone24.texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, 0.0F, false);
        bone24.texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone25 = new ModelRenderer(this);
        bone25.setPos(2.0F, 0.2425F, 0.0F);
        bone24.addChild(bone25);
        setRotationAngle(bone25, 0.0F, 0.0F, -0.245F);
        bone25.texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone26 = new ModelRenderer(this);
        bone26.setPos(-2.0F, 0.2425F, 0.0F);
        bone24.addChild(bone26);
        setRotationAngle(bone26, 0.0F, 0.0F, 0.245F);
        bone26.texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone27 = new ModelRenderer(this);
        bone27.setPos(2.9701F, 0.0F, 0.001F);
        bone24.addChild(bone27);
        setRotationAngle(bone27, 0.0F, 0.0F, -0.7854F);
        bone27.texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone28 = new ModelRenderer(this);
        bone28.setPos(-2.9701F, 0.0F, 0.001F);
        bone24.addChild(bone28);
        setRotationAngle(bone28, 0.0F, 0.0F, 0.7854F);
        bone28.texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone29 = new ModelRenderer(this);
        bone29.setPos(2.9701F, 0.0F, 0.001F);
        bone24.addChild(bone29);
        setRotationAngle(bone29, 0.0F, 0.0F, -0.3927F);
        bone29.texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone30 = new ModelRenderer(this);
        bone30.setPos(-2.9701F, 0.0F, 0.001F);
        bone24.addChild(bone30);
        setRotationAngle(bone30, 0.0F, 0.0F, 0.3927F);
        bone30.texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone31 = new ModelRenderer(this);
        bone31.setPos(2.9701F, 0.0F, 0.001F);
        bone24.addChild(bone31);
        setRotationAngle(bone31, 0.0F, 0.0F, -0.3927F);


        bone32 = new ModelRenderer(this);
        bone32.setPos(0.0F, -6.3F, 2.3956F);
        piece3.addChild(bone32);
        setRotationAngle(bone32, 0.3066F, 0.0F, 0.0F);


        bone33 = new ModelRenderer(this);
        bone33.setPos(1.0F, 0.124F, 2.4152F);
        bone32.addChild(bone33);
        setRotationAngle(bone33, 0.0F, 0.0F, -0.1244F);
        bone33.texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone33.texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone33.texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone33.texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        bone34 = new ModelRenderer(this);
        bone34.setPos(-1.0F, 0.124F, 2.4152F);
        bone32.addChild(bone34);
        setRotationAngle(bone34, 0.0F, 0.0F, 0.1244F);
        bone34.texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone34.texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone34.texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone34.texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        piece4 = new ModelRenderer(this);
        piece4.setPos(0.0F, 13.0F, 0.0F);
        laba.addChild(piece4);
        setRotationAngle(piece4, 0.0F, -2.3562F, 0.0F);


        bone35 = new ModelRenderer(this);
        bone35.setPos(0.0F, 0.0F, 7.1706F);
        piece4.addChild(bone35);
        setRotationAngle(bone35, 0.6479F, 0.0F, 0.0F);
        bone35.texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        bone35.texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, 0.0F, false);
        bone35.texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone36 = new ModelRenderer(this);
        bone36.setPos(2.0F, 0.2425F, 0.0F);
        bone35.addChild(bone36);
        setRotationAngle(bone36, 0.0F, 0.0F, -0.245F);
        bone36.texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone37 = new ModelRenderer(this);
        bone37.setPos(-2.0F, 0.2425F, 0.0F);
        bone35.addChild(bone37);
        setRotationAngle(bone37, 0.0F, 0.0F, 0.245F);
        bone37.texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone38 = new ModelRenderer(this);
        bone38.setPos(2.9701F, 0.0F, 0.001F);
        bone35.addChild(bone38);
        setRotationAngle(bone38, 0.0F, 0.0F, -0.7854F);
        bone38.texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone39 = new ModelRenderer(this);
        bone39.setPos(-2.9701F, 0.0F, 0.001F);
        bone35.addChild(bone39);
        setRotationAngle(bone39, 0.0F, 0.0F, 0.7854F);
        bone39.texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone40 = new ModelRenderer(this);
        bone40.setPos(2.9701F, 0.0F, 0.001F);
        bone35.addChild(bone40);
        setRotationAngle(bone40, 0.0F, 0.0F, -0.3927F);
        bone40.texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone41 = new ModelRenderer(this);
        bone41.setPos(-2.9701F, 0.0F, 0.001F);
        bone35.addChild(bone41);
        setRotationAngle(bone41, 0.0F, 0.0F, 0.3927F);
        bone41.texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone42 = new ModelRenderer(this);
        bone42.setPos(2.9701F, 0.0F, 0.001F);
        bone35.addChild(bone42);
        setRotationAngle(bone42, 0.0F, 0.0F, -0.3927F);


        bone43 = new ModelRenderer(this);
        bone43.setPos(0.0F, -6.3F, 2.3956F);
        piece4.addChild(bone43);
        setRotationAngle(bone43, 0.3066F, 0.0F, 0.0F);


        bone44 = new ModelRenderer(this);
        bone44.setPos(1.0F, 0.124F, 2.4152F);
        bone43.addChild(bone44);
        setRotationAngle(bone44, 0.0F, 0.0F, -0.1244F);
        bone44.texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone44.texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone44.texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone44.texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        bone45 = new ModelRenderer(this);
        bone45.setPos(-1.0F, 0.124F, 2.4152F);
        bone43.addChild(bone45);
        setRotationAngle(bone45, 0.0F, 0.0F, 0.1244F);
        bone45.texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone45.texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone45.texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone45.texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        piece5 = new ModelRenderer(this);
        piece5.setPos(0.0F, 13.0F, 0.0F);
        laba.addChild(piece5);
        setRotationAngle(piece5, 0.0F, 3.1416F, 0.0F);


        bone46 = new ModelRenderer(this);
        bone46.setPos(0.0F, 0.0F, 7.1706F);
        piece5.addChild(bone46);
        setRotationAngle(bone46, 0.6479F, 0.0F, 0.0F);
        bone46.texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        bone46.texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, 0.0F, false);
        bone46.texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone47 = new ModelRenderer(this);
        bone47.setPos(2.0F, 0.2425F, 0.0F);
        bone46.addChild(bone47);
        setRotationAngle(bone47, 0.0F, 0.0F, -0.245F);
        bone47.texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone48 = new ModelRenderer(this);
        bone48.setPos(-2.0F, 0.2425F, 0.0F);
        bone46.addChild(bone48);
        setRotationAngle(bone48, 0.0F, 0.0F, 0.245F);
        bone48.texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone49 = new ModelRenderer(this);
        bone49.setPos(2.9701F, 0.0F, 0.001F);
        bone46.addChild(bone49);
        setRotationAngle(bone49, 0.0F, 0.0F, -0.7854F);
        bone49.texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone50 = new ModelRenderer(this);
        bone50.setPos(-2.9701F, 0.0F, 0.001F);
        bone46.addChild(bone50);
        setRotationAngle(bone50, 0.0F, 0.0F, 0.7854F);
        bone50.texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone51 = new ModelRenderer(this);
        bone51.setPos(2.9701F, 0.0F, 0.001F);
        bone46.addChild(bone51);
        setRotationAngle(bone51, 0.0F, 0.0F, -0.3927F);
        bone51.texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone52 = new ModelRenderer(this);
        bone52.setPos(-2.9701F, 0.0F, 0.001F);
        bone46.addChild(bone52);
        setRotationAngle(bone52, 0.0F, 0.0F, 0.3927F);
        bone52.texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone53 = new ModelRenderer(this);
        bone53.setPos(2.9701F, 0.0F, 0.001F);
        bone46.addChild(bone53);
        setRotationAngle(bone53, 0.0F, 0.0F, -0.3927F);


        bone54 = new ModelRenderer(this);
        bone54.setPos(0.0F, -6.3F, 2.3956F);
        piece5.addChild(bone54);
        setRotationAngle(bone54, 0.3066F, 0.0F, 0.0F);


        bone55 = new ModelRenderer(this);
        bone55.setPos(1.0F, 0.124F, 2.4152F);
        bone54.addChild(bone55);
        setRotationAngle(bone55, 0.0F, 0.0F, -0.1244F);
        bone55.texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone55.texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone55.texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone55.texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        bone56 = new ModelRenderer(this);
        bone56.setPos(-1.0F, 0.124F, 2.4152F);
        bone54.addChild(bone56);
        setRotationAngle(bone56, 0.0F, 0.0F, 0.1244F);
        bone56.texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone56.texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone56.texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone56.texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        piece6 = new ModelRenderer(this);
        piece6.setPos(0.0F, 13.0F, 0.0F);
        laba.addChild(piece6);
        setRotationAngle(piece6, 0.0F, 2.3562F, 0.0F);


        bone57 = new ModelRenderer(this);
        bone57.setPos(0.0F, 0.0F, 7.1706F);
        piece6.addChild(bone57);
        setRotationAngle(bone57, 0.6479F, 0.0F, 0.0F);
        bone57.texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        bone57.texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, 0.0F, false);
        bone57.texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone58 = new ModelRenderer(this);
        bone58.setPos(2.0F, 0.2425F, 0.0F);
        bone57.addChild(bone58);
        setRotationAngle(bone58, 0.0F, 0.0F, -0.245F);
        bone58.texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone59 = new ModelRenderer(this);
        bone59.setPos(-2.0F, 0.2425F, 0.0F);
        bone57.addChild(bone59);
        setRotationAngle(bone59, 0.0F, 0.0F, 0.245F);
        bone59.texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone60 = new ModelRenderer(this);
        bone60.setPos(2.9701F, 0.0F, 0.001F);
        bone57.addChild(bone60);
        setRotationAngle(bone60, 0.0F, 0.0F, -0.7854F);
        bone60.texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone61 = new ModelRenderer(this);
        bone61.setPos(-2.9701F, 0.0F, 0.001F);
        bone57.addChild(bone61);
        setRotationAngle(bone61, 0.0F, 0.0F, 0.7854F);
        bone61.texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone62 = new ModelRenderer(this);
        bone62.setPos(2.9701F, 0.0F, 0.001F);
        bone57.addChild(bone62);
        setRotationAngle(bone62, 0.0F, 0.0F, -0.3927F);
        bone62.texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone63 = new ModelRenderer(this);
        bone63.setPos(-2.9701F, 0.0F, 0.001F);
        bone57.addChild(bone63);
        setRotationAngle(bone63, 0.0F, 0.0F, 0.3927F);
        bone63.texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone64 = new ModelRenderer(this);
        bone64.setPos(2.9701F, 0.0F, 0.001F);
        bone57.addChild(bone64);
        setRotationAngle(bone64, 0.0F, 0.0F, -0.3927F);


        bone65 = new ModelRenderer(this);
        bone65.setPos(0.0F, -6.3F, 2.3956F);
        piece6.addChild(bone65);
        setRotationAngle(bone65, 0.3066F, 0.0F, 0.0F);


        bone66 = new ModelRenderer(this);
        bone66.setPos(1.0F, 0.124F, 2.4152F);
        bone65.addChild(bone66);
        setRotationAngle(bone66, 0.0F, 0.0F, -0.1244F);
        bone66.texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone66.texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone66.texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone66.texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        bone67 = new ModelRenderer(this);
        bone67.setPos(-1.0F, 0.124F, 2.4152F);
        bone65.addChild(bone67);
        setRotationAngle(bone67, 0.0F, 0.0F, 0.1244F);
        bone67.texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone67.texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone67.texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone67.texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        piece7 = new ModelRenderer(this);
        piece7.setPos(0.0F, 13.0F, 0.0F);
        laba.addChild(piece7);
        setRotationAngle(piece7, 0.0F, 1.5708F, 0.0F);


        bone68 = new ModelRenderer(this);
        bone68.setPos(0.0F, 0.0F, 7.1706F);
        piece7.addChild(bone68);
        setRotationAngle(bone68, 0.6479F, 0.0F, 0.0F);
        bone68.texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        bone68.texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, 0.0F, false);
        bone68.texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone69 = new ModelRenderer(this);
        bone69.setPos(2.0F, 0.2425F, 0.0F);
        bone68.addChild(bone69);
        setRotationAngle(bone69, 0.0F, 0.0F, -0.245F);
        bone69.texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone70 = new ModelRenderer(this);
        bone70.setPos(-2.0F, 0.2425F, 0.0F);
        bone68.addChild(bone70);
        setRotationAngle(bone70, 0.0F, 0.0F, 0.245F);
        bone70.texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone71 = new ModelRenderer(this);
        bone71.setPos(2.9701F, 0.0F, 0.001F);
        bone68.addChild(bone71);
        setRotationAngle(bone71, 0.0F, 0.0F, -0.7854F);
        bone71.texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone72 = new ModelRenderer(this);
        bone72.setPos(-2.9701F, 0.0F, 0.001F);
        bone68.addChild(bone72);
        setRotationAngle(bone72, 0.0F, 0.0F, 0.7854F);
        bone72.texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone73 = new ModelRenderer(this);
        bone73.setPos(2.9701F, 0.0F, 0.001F);
        bone68.addChild(bone73);
        setRotationAngle(bone73, 0.0F, 0.0F, -0.3927F);
        bone73.texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone74 = new ModelRenderer(this);
        bone74.setPos(-2.9701F, 0.0F, 0.001F);
        bone68.addChild(bone74);
        setRotationAngle(bone74, 0.0F, 0.0F, 0.3927F);
        bone74.texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone75 = new ModelRenderer(this);
        bone75.setPos(2.9701F, 0.0F, 0.001F);
        bone68.addChild(bone75);
        setRotationAngle(bone75, 0.0F, 0.0F, -0.3927F);


        bone76 = new ModelRenderer(this);
        bone76.setPos(0.0F, -6.3F, 2.3956F);
        piece7.addChild(bone76);
        setRotationAngle(bone76, 0.3066F, 0.0F, 0.0F);


        bone77 = new ModelRenderer(this);
        bone77.setPos(1.0F, 0.124F, 2.4152F);
        bone76.addChild(bone77);
        setRotationAngle(bone77, 0.0F, 0.0F, -0.1244F);
        bone77.texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone77.texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone77.texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone77.texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        bone78 = new ModelRenderer(this);
        bone78.setPos(-1.0F, 0.124F, 2.4152F);
        bone76.addChild(bone78);
        setRotationAngle(bone78, 0.0F, 0.0F, 0.1244F);
        bone78.texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone78.texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone78.texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone78.texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        piece8 = new ModelRenderer(this);
        piece8.setPos(0.0F, 13.0F, 0.0F);
        laba.addChild(piece8);
        setRotationAngle(piece8, 0.0F, 0.7854F, 0.0F);


        bone79 = new ModelRenderer(this);
        bone79.setPos(0.0F, 0.0F, 7.1706F);
        piece8.addChild(bone79);
        setRotationAngle(bone79, 0.6479F, 0.0F, 0.0F);
        bone79.texOffs(6, 11).addBox(-2.0F, -3.7575F, 0.0F, 4.0F, 4.0F, 0.0F, 0.0F, false);
        bone79.texOffs(10, 17).addBox(-1.0F, -8.7575F, 0.0F, 2.0F, 5.0F, 0.0F, 0.0F, false);
        bone79.texOffs(5, 24).addBox(-0.5F, 0.322F, -1.1959F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone80 = new ModelRenderer(this);
        bone80.setPos(2.0F, 0.2425F, 0.0F);
        bone79.addChild(bone80);
        setRotationAngle(bone80, 0.0F, 0.0F, -0.245F);
        bone80.texOffs(12, 0).addBox(0.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone81 = new ModelRenderer(this);
        bone81.setPos(-2.0F, 0.2425F, 0.0F);
        bone79.addChild(bone81);
        setRotationAngle(bone81, 0.0F, 0.0F, 0.245F);
        bone81.texOffs(12, 0).addBox(-1.0F, -8.0F, 0.0F, 1.0F, 8.0F, 0.0F, 0.0F, false);

        bone82 = new ModelRenderer(this);
        bone82.setPos(2.9701F, 0.0F, 0.001F);
        bone79.addChild(bone82);
        setRotationAngle(bone82, 0.0F, 0.0F, -0.7854F);
        bone82.texOffs(5, 24).addBox(-1.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone83 = new ModelRenderer(this);
        bone83.setPos(-2.9701F, 0.0F, 0.001F);
        bone79.addChild(bone83);
        setRotationAngle(bone83, 0.0F, 0.0F, 0.7854F);
        bone83.texOffs(5, 24).addBox(0.1968F, -1.1969F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone84 = new ModelRenderer(this);
        bone84.setPos(2.9701F, 0.0F, 0.001F);
        bone79.addChild(bone84);
        setRotationAngle(bone84, 0.0F, 0.0F, -0.3927F);
        bone84.texOffs(5, 24).addBox(-2.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone85 = new ModelRenderer(this);
        bone85.setPos(-2.9701F, 0.0F, 0.001F);
        bone79.addChild(bone85);
        setRotationAngle(bone85, 0.0F, 0.0F, 0.3927F);
        bone85.texOffs(5, 24).addBox(1.4845F, -0.6636F, -1.1969F, 1.0F, 1.0F, 1.0F, 0.1969F, false);

        bone86 = new ModelRenderer(this);
        bone86.setPos(2.9701F, 0.0F, 0.001F);
        bone79.addChild(bone86);
        setRotationAngle(bone86, 0.0F, 0.0F, -0.3927F);


        bone87 = new ModelRenderer(this);
        bone87.setPos(0.0F, -6.3F, 2.3956F);
        piece8.addChild(bone87);
        setRotationAngle(bone87, 0.3066F, 0.0F, 0.0F);


        bone88 = new ModelRenderer(this);
        bone88.setPos(1.0F, 0.124F, 2.4152F);
        bone87.addChild(bone88);
        setRotationAngle(bone88, 0.0F, 0.0F, -0.1244F);
        bone88.texOffs(14, 0).addBox(-0.9923F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone88.texOffs(0, 11).addBox(-0.7923F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone88.texOffs(10, 22).addBox(-0.7323F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone88.texOffs(6, 22).addBox(-0.6823F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        bone89 = new ModelRenderer(this);
        bone89.setPos(-1.0F, 0.124F, 2.4152F);
        bone87.addChild(bone89);
        setRotationAngle(bone89, 0.0F, 0.0F, 0.1244F);
        bone89.texOffs(14, 0).addBox(-0.0077F, -4.124F, -2.4152F, 1.0F, 4.0F, 0.0F, 0.0F, false);
        bone89.texOffs(0, 11).addBox(-0.2077F, -5.924F, -3.2142F, 1.0F, 2.0F, 1.0F, -0.2F, false);
        bone89.texOffs(10, 22).addBox(-0.2677F, -6.464F, -3.1542F, 1.0F, 1.0F, 1.0F, -0.26F, false);
        bone89.texOffs(6, 22).addBox(-0.3177F, -6.894F, -3.1042F, 1.0F, 1.0F, 1.0F, -0.31F, false);

        tube = new ModelRenderer(this);
        tube.setPos(0.75F, 24.0F, 1.5F);
        tube.texOffs(6, 19).addBox(-1.25F, -12.4297F, 5.2362F, 1.0F, 2.0F, 1.0F, 0.0F, false);
        tube.texOffs(0, 17).addBox(0.7637F, -9.416F, 5.2362F, 4.0F, 1.0F, 1.0F, 0.0F, false);
        tube.texOffs(0, 11).addBox(5.7773F, -9.416F, 0.2226F, 1.0F, 1.0F, 4.0F, 0.0F, false);
        tube.texOffs(9, 24).addBox(3.7637F, -9.416F, -1.7911F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone90 = new ModelRenderer(this);
        bone90.setPos(-0.75F, -12.8536F, 5.3536F);
        tube.addChild(bone90);
        setRotationAngle(bone90, -1.1781F, 0.0F, 0.0F);
        bone90.texOffs(10, 27).addBox(-0.5F, -0.6533F, -0.2706F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone97 = new ModelRenderer(this);
        bone97.setPos(6.2773F, -8.916F, -0.2774F);
        tube.addChild(bone97);
        setRotationAngle(bone97, 0.0F, 0.3927F, 0.0F);
        bone97.texOffs(0, 25).addBox(-0.7294F, -0.5F, -0.3467F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone98 = new ModelRenderer(this);
        bone98.setPos(6.2773F, -8.916F, -0.2774F);
        tube.addChild(bone98);
        setRotationAngle(bone98, 0.0F, 0.7854F, 0.0F);
        bone98.texOffs(0, 25).addBox(-0.6173F, -0.5F, -1.2168F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone99 = new ModelRenderer(this);
        bone99.setPos(6.2773F, -8.916F, -0.2774F);
        tube.addChild(bone99);
        setRotationAngle(bone99, 0.0F, 1.1781F, 0.0F);
        bone99.texOffs(0, 25).addBox(-0.1808F, -0.5F, -1.9777F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone94 = new ModelRenderer(this);
        bone94.setPos(3.2637F, -8.916F, 5.7362F);
        tube.addChild(bone94);
        setRotationAngle(bone94, 0.0F, 0.3927F, 0.0F);
        bone94.texOffs(7, 26).addBox(1.1945F, -0.5F, 0.036F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone95 = new ModelRenderer(this);
        bone95.setPos(3.2637F, -8.916F, 5.7362F);
        tube.addChild(bone95);
        setRotationAngle(bone95, 0.0F, 0.7854F, 0.0F);
        bone95.texOffs(7, 26).addBox(1.631F, -0.5F, 0.7969F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone96 = new ModelRenderer(this);
        bone96.setPos(3.2637F, -8.916F, 5.7362F);
        tube.addChild(bone96);
        setRotationAngle(bone96, 0.0F, 1.1781F, 0.0F);
        bone96.texOffs(3, 26).addBox(1.7431F, -0.5F, 1.667F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone91 = new ModelRenderer(this);
        bone91.setPos(-0.75F, -9.9297F, 5.7362F);
        tube.addChild(bone91);
        setRotationAngle(bone91, 0.0F, 0.0F, -0.3927F);
        bone91.texOffs(0, 27).addBox(-0.2706F, -0.6533F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone92 = new ModelRenderer(this);
        bone92.setPos(-0.75F, -9.9297F, 5.7362F);
        tube.addChild(bone92);
        setRotationAngle(bone92, 0.0F, 0.0F, -0.7854F);
        bone92.texOffs(0, 27).addBox(-0.3827F, 0.2168F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        bone93 = new ModelRenderer(this);
        bone93.setPos(-0.75F, -9.9297F, 5.7362F);
        tube.addChild(bone93);
        setRotationAngle(bone93, 0.0F, 0.0F, -1.1781F);
        bone93.texOffs(0, 27).addBox(-0.8192F, 0.9777F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);

        wheel = new ModelRenderer(this);
        wheel.setPos(5.0137F, 15.084F, 0.2089F);
        wheel.texOffs(12, 8).addBox(-1.3536F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, -0.1464F, false);

        bone9 = new ModelRenderer(this);
        bone9.setPos(-0.8536F, 0.0F, 0.0F);
        wheel.addChild(bone9);
        setRotationAngle(bone9, -0.7854F, 0.0F, 0.0F);
        bone9.texOffs(12, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, -0.1464F, false);

        bone100 = new ModelRenderer(this);
        bone100.setPos(-0.8536F, 0.0F, 0.0F);
        wheel.addChild(bone100);
        setRotationAngle(bone100, -1.5708F, 0.0F, 0.0F);
        bone100.texOffs(12, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, -0.1464F, false);

        bone101 = new ModelRenderer(this);
        bone101.setPos(-0.8536F, 0.0F, 0.0F);
        wheel.addChild(bone101);
        setRotationAngle(bone101, -2.3562F, 0.0F, 0.0F);
        bone101.texOffs(12, 8).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, -0.1464F, false);

        ruler = new ModelRenderer(this);
        ruler.setPos(0.0F, 24.0F, 0.0F);
        ruler.texOffs(0, 19).addBox(-1.0F, -12.0F, 7.0F, 2.0F, 5.0F, 1.0F, 0.0F, false);

        box = new ModelRenderer(this);
        box.setPos(0.0F, 24.0F, 0.0F);
        box.texOffs(0, 0).addBox(-8.0F, -1.0F, -8.0F, 16.0F, 1.0F, 16.0F, 0.0F, false);
        box.texOffs(0, 0).addBox(-8.0F, -1.75F, -8.0F, 16.0F, 1.0F, 16.0F, -0.25F, false);
        box.texOffs(0, 0).addBox(-8.0F, -6.25F, -8.0F, 16.0F, 1.0F, 16.0F, -0.25F, false);
        box.texOffs(0, 0).addBox(-8.0F, -7.0F, -8.0F, 16.0F, 1.0F, 16.0F, 0.0F, false);

        bone102 = new ModelRenderer(this);
        bone102.setPos(0.5F, 0.0F, -0.5F);
        box.addChild(bone102);
        bone102.texOffs(0, 0).addBox(4.0F, -5.5F, 5.0F, 3.0F, 4.0F, 3.0F, 0.0F, false);
        bone102.texOffs(0, 0).addBox(-8.0F, -5.5F, 5.0F, 3.0F, 4.0F, 3.0F, 0.0F, false);
        bone102.texOffs(0, 0).addBox(-8.0F, -5.5F, -7.0F, 3.0F, 4.0F, 3.0F, 0.0F, false);
        bone102.texOffs(0, 0).addBox(4.0F, -5.5F, -7.0F, 3.0F, 4.0F, 3.0F, 0.0F, false);
        bone102.texOffs(0, 17).addBox(-7.5F, -5.5F, -6.5F, 14.0F, 4.0F, 14.0F, 0.0F, false);
        bone102.texOffs(0, 7).addBox(-3.0F, -5.0F, -7.0F, 5.0F, 3.0F, 1.0F, 0.0F, false);

        disc = new ModelRenderer(this);
        disc.setPos(0.0F, 17.0F, 0.0F);
        disc.texOffs(0, 35).addBox(-1.0F, -1.0F, -5.0F, 2.0F, 1.0F, 10.0F, -0.0068F, false);
        disc.texOffs(12, 40).addBox(-6.5F, -6.501F, -6.5F, 13.0F, 11.0F, 13.0F, -5.5F, false);

        bone103 = new ModelRenderer(this);
        bone103.setPos(0.0F, 7.0F, 0.0F);
        disc.addChild(bone103);
        setRotationAngle(bone103, 0.0F, -0.3927F, 0.0F);
        bone103.texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, -0.0068F, false);

        bone104 = new ModelRenderer(this);
        bone104.setPos(0.0F, 7.0F, 0.0F);
        disc.addChild(bone104);
        setRotationAngle(bone104, 0.0F, -0.7854F, 0.0F);
        bone104.texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, -0.0068F, false);

        bone105 = new ModelRenderer(this);
        bone105.setPos(0.0F, 7.0F, 0.0F);
        disc.addChild(bone105);
        setRotationAngle(bone105, 0.0F, -1.1781F, 0.0F);
        bone105.texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, -0.0068F, false);

        bone106 = new ModelRenderer(this);
        bone106.setPos(0.0F, 7.0F, 0.0F);
        disc.addChild(bone106);
        setRotationAngle(bone106, 0.0F, -1.5708F, 0.0F);
        bone106.texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, -0.0068F, false);

        bone107 = new ModelRenderer(this);
        bone107.setPos(0.0F, 7.0F, 0.0F);
        disc.addChild(bone107);
        setRotationAngle(bone107, 0.0F, -1.9635F, 0.0F);
        bone107.texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, -0.0068F, false);

        bone108 = new ModelRenderer(this);
        bone108.setPos(0.0F, 7.0F, 0.0F);
        disc.addChild(bone108);
        setRotationAngle(bone108, 0.0F, -2.3562F, 0.0F);
        bone108.texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, -0.0068F, false);

        bone109 = new ModelRenderer(this);
        bone109.setPos(0.0F, 7.0F, 0.0F);
        disc.addChild(bone109);
        setRotationAngle(bone109, 0.0F, -2.7489F, 0.0F);
        bone109.texOffs(0, 35).addBox(-1.0F, -8.0F, -5.0F, 2.0F, 1.0F, 10.0F, -0.0068F, false);

        huahen = new ModelRenderer(this);
        huahen.setPos(0.0F, 7.0F, 0.0F);
        disc.addChild(huahen);
        huahen.texOffs(0, 47).addBox(-2.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, -0.1022F, false);
        huahen.texOffs(0, 47).addBox(1.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, -0.1022F, false);
        huahen.texOffs(0, 52).addBox(-4.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2044F, false);
        huahen.texOffs(0, 52).addBox(3.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2044F, false);

        bone110 = new ModelRenderer(this);
        bone110.setPos(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone110);
        setRotationAngle(bone110, 0.0F, -0.3927F, 0.0F);
        bone110.texOffs(0, 47).addBox(-2.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, -0.1022F, false);
        bone110.texOffs(0, 47).addBox(1.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, -0.1022F, false);

        bone111 = new ModelRenderer(this);
        bone111.setPos(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone111);
        setRotationAngle(bone111, 0.0F, -0.7854F, 0.0F);
        bone111.texOffs(0, 47).addBox(-2.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, -0.1022F, false);
        bone111.texOffs(0, 47).addBox(1.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, -0.1022F, false);

        bone112 = new ModelRenderer(this);
        bone112.setPos(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone112);
        setRotationAngle(bone112, 0.0F, -1.1781F, 0.0F);
        bone112.texOffs(0, 47).addBox(-2.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, -0.1022F, false);
        bone112.texOffs(0, 47).addBox(1.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, -0.1022F, false);

        bone113 = new ModelRenderer(this);
        bone113.setPos(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone113);
        setRotationAngle(bone113, 0.0F, -1.5708F, 0.0F);
        bone113.texOffs(0, 47).addBox(-2.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, -0.1022F, false);
        bone113.texOffs(0, 47).addBox(1.1022F, -8.1032F, -0.5F, 1.0F, 1.0F, 1.0F, -0.1022F, false);

        bone114 = new ModelRenderer(this);
        bone114.setPos(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone114);
        setRotationAngle(bone114, 0.0F, -0.3927F, 0.0F);
        bone114.texOffs(0, 52).addBox(-4.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2044F, false);
        bone114.texOffs(0, 52).addBox(3.2044F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2044F, false);

        bone115 = new ModelRenderer(this);
        bone115.setPos(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone115);
        setRotationAngle(bone115, 0.0F, -0.7854F, 0.0F);
        bone115.texOffs(0, 52).addBox(-4.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2044F, false);
        bone115.texOffs(0, 52).addBox(3.2044F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2044F, false);

        bone116 = new ModelRenderer(this);
        bone116.setPos(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone116);
        setRotationAngle(bone116, 0.0F, -1.1781F, 0.0F);
        bone116.texOffs(0, 52).addBox(-4.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2044F, false);
        bone116.texOffs(0, 52).addBox(3.2044F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2044F, false);

        bone117 = new ModelRenderer(this);
        bone117.setPos(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone117);
        setRotationAngle(bone117, 0.0F, -1.5708F, 0.0F);
        bone117.texOffs(0, 52).addBox(-4.2043F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2044F, false);
        bone117.texOffs(0, 52).addBox(3.2044F, -8.2053F, -1.0F, 1.0F, 1.0F, 2.0F, -0.2044F, false);

        getDiscBone = new ModelRenderer(this);
        getDiscBone.setPos(0.0F, 0.0F, 0.0F);

    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        laba.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        tube.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        wheel.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        ruler.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        box.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        disc.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        getDiscBone.render(matrixStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    public ModelRenderer getDiscBone() {
        return disc;
    }
}