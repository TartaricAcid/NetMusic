package com.github.tartaricacid.netmusic.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;

public class ModelMusicPlayer extends ModelBase {
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

    public ModelMusicPlayer() {
        textureWidth = 64;
        textureHeight = 64;

        laba = new ModelRenderer(this);
        laba.setRotationPoint(0.0F, 11.0F, 7.0F);
        setRotationAngle(laba, -2.3562F, 0.0F, 0.0F);
        laba.cubeList.add(new ModelBox(laba, 6, 19, -0.5F, 0.0F, -0.5F, 1, 2, 1, 0.0F, false));

        piece = new ModelRenderer(this);
        piece.setRotationPoint(0.0F, 13.0F, 0.0F);
        laba.addChild(piece);


        bone = new ModelRenderer(this);
        bone.setRotationPoint(0.0F, 0.0F, 7.1706F);
        piece.addChild(bone);
        setRotationAngle(bone, 0.6479F, 0.0F, 0.0F);
        bone.cubeList.add(new ModelBox(bone, 6, 11, -2.0F, -3.7575F, 0.0F, 4, 4, 0, 0.0F, false));
        bone.cubeList.add(new ModelBox(bone, 10, 17, -1.0F, -8.7575F, 0.0F, 2, 5, 0, 0.0F, false));
        bone.cubeList.add(new ModelBox(bone, 5, 24, -0.5F, 0.322F, -1.1959F, 1, 1, 1, 0.1969F, false));

        bone2 = new ModelRenderer(this);
        bone2.setRotationPoint(2.0F, 0.2425F, 0.0F);
        bone.addChild(bone2);
        setRotationAngle(bone2, 0.0F, 0.0F, -0.245F);
        bone2.cubeList.add(new ModelBox(bone2, 12, 0, 0.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone3 = new ModelRenderer(this);
        bone3.setRotationPoint(-2.0F, 0.2425F, 0.0F);
        bone.addChild(bone3);
        setRotationAngle(bone3, 0.0F, 0.0F, 0.245F);
        bone3.cubeList.add(new ModelBox(bone3, 12, 0, -1.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone16 = new ModelRenderer(this);
        bone16.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone.addChild(bone16);
        setRotationAngle(bone16, 0.0F, 0.0F, -0.7854F);
        bone16.cubeList.add(new ModelBox(bone16, 5, 24, -1.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone20 = new ModelRenderer(this);
        bone20.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone.addChild(bone20);
        setRotationAngle(bone20, 0.0F, 0.0F, 0.7854F);
        bone20.cubeList.add(new ModelBox(bone20, 5, 24, 0.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone17 = new ModelRenderer(this);
        bone17.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone.addChild(bone17);
        setRotationAngle(bone17, 0.0F, 0.0F, -0.3927F);
        bone17.cubeList.add(new ModelBox(bone17, 5, 24, -2.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone19 = new ModelRenderer(this);
        bone19.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone.addChild(bone19);
        setRotationAngle(bone19, 0.0F, 0.0F, 0.3927F);
        bone19.cubeList.add(new ModelBox(bone19, 5, 24, 1.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone18 = new ModelRenderer(this);
        bone18.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone.addChild(bone18);
        setRotationAngle(bone18, 0.0F, 0.0F, -0.3927F);


        bone5 = new ModelRenderer(this);
        bone5.setRotationPoint(0.0F, -6.3F, 2.3956F);
        piece.addChild(bone5);
        setRotationAngle(bone5, 0.3066F, 0.0F, 0.0F);


        bone6 = new ModelRenderer(this);
        bone6.setRotationPoint(1.0F, 0.124F, 2.4152F);
        bone5.addChild(bone6);
        setRotationAngle(bone6, 0.0F, 0.0F, -0.1244F);
        bone6.cubeList.add(new ModelBox(bone6, 14, 0, -0.9923F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone6.cubeList.add(new ModelBox(bone6, 0, 11, -0.7923F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone6.cubeList.add(new ModelBox(bone6, 10, 22, -0.7323F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone6.cubeList.add(new ModelBox(bone6, 6, 22, -0.6823F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        bone7 = new ModelRenderer(this);
        bone7.setRotationPoint(-1.0F, 0.124F, 2.4152F);
        bone5.addChild(bone7);
        setRotationAngle(bone7, 0.0F, 0.0F, 0.1244F);
        bone7.cubeList.add(new ModelBox(bone7, 14, 0, -0.0077F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone7.cubeList.add(new ModelBox(bone7, 0, 11, -0.2077F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone7.cubeList.add(new ModelBox(bone7, 10, 22, -0.2677F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone7.cubeList.add(new ModelBox(bone7, 6, 22, -0.3177F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        piece2 = new ModelRenderer(this);
        piece2.setRotationPoint(0.0F, 13.0F, 0.0F);
        laba.addChild(piece2);
        setRotationAngle(piece2, 0.0F, -0.7854F, 0.0F);


        bone4 = new ModelRenderer(this);
        bone4.setRotationPoint(0.0F, 0.0F, 7.1706F);
        piece2.addChild(bone4);
        setRotationAngle(bone4, 0.6479F, 0.0F, 0.0F);
        bone4.cubeList.add(new ModelBox(bone4, 6, 11, -2.0F, -3.7575F, 0.0F, 4, 4, 0, 0.0F, false));
        bone4.cubeList.add(new ModelBox(bone4, 10, 17, -1.0F, -8.7575F, 0.0F, 2, 5, 0, 0.0F, false));
        bone4.cubeList.add(new ModelBox(bone4, 5, 24, -0.5F, 0.322F, -1.1959F, 1, 1, 1, 0.1969F, false));

        bone8 = new ModelRenderer(this);
        bone8.setRotationPoint(2.0F, 0.2425F, 0.0F);
        bone4.addChild(bone8);
        setRotationAngle(bone8, 0.0F, 0.0F, -0.245F);
        bone8.cubeList.add(new ModelBox(bone8, 12, 0, 0.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone10 = new ModelRenderer(this);
        bone10.setRotationPoint(-2.0F, 0.2425F, 0.0F);
        bone4.addChild(bone10);
        setRotationAngle(bone10, 0.0F, 0.0F, 0.245F);
        bone10.cubeList.add(new ModelBox(bone10, 12, 0, -1.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone11 = new ModelRenderer(this);
        bone11.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone4.addChild(bone11);
        setRotationAngle(bone11, 0.0F, 0.0F, -0.7854F);
        bone11.cubeList.add(new ModelBox(bone11, 5, 24, -1.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone12 = new ModelRenderer(this);
        bone12.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone4.addChild(bone12);
        setRotationAngle(bone12, 0.0F, 0.0F, 0.7854F);
        bone12.cubeList.add(new ModelBox(bone12, 5, 24, 0.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone13 = new ModelRenderer(this);
        bone13.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone4.addChild(bone13);
        setRotationAngle(bone13, 0.0F, 0.0F, -0.3927F);
        bone13.cubeList.add(new ModelBox(bone13, 5, 24, -2.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone14 = new ModelRenderer(this);
        bone14.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone4.addChild(bone14);
        setRotationAngle(bone14, 0.0F, 0.0F, 0.3927F);
        bone14.cubeList.add(new ModelBox(bone14, 5, 24, 1.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone15 = new ModelRenderer(this);
        bone15.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone4.addChild(bone15);
        setRotationAngle(bone15, 0.0F, 0.0F, -0.3927F);


        bone21 = new ModelRenderer(this);
        bone21.setRotationPoint(0.0F, -6.3F, 2.3956F);
        piece2.addChild(bone21);
        setRotationAngle(bone21, 0.3066F, 0.0F, 0.0F);


        bone22 = new ModelRenderer(this);
        bone22.setRotationPoint(1.0F, 0.124F, 2.4152F);
        bone21.addChild(bone22);
        setRotationAngle(bone22, 0.0F, 0.0F, -0.1244F);
        bone22.cubeList.add(new ModelBox(bone22, 14, 0, -0.9923F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone22.cubeList.add(new ModelBox(bone22, 0, 11, -0.7923F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone22.cubeList.add(new ModelBox(bone22, 10, 22, -0.7323F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone22.cubeList.add(new ModelBox(bone22, 6, 22, -0.6823F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        bone23 = new ModelRenderer(this);
        bone23.setRotationPoint(-1.0F, 0.124F, 2.4152F);
        bone21.addChild(bone23);
        setRotationAngle(bone23, 0.0F, 0.0F, 0.1244F);
        bone23.cubeList.add(new ModelBox(bone23, 14, 0, -0.0077F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone23.cubeList.add(new ModelBox(bone23, 0, 11, -0.2077F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone23.cubeList.add(new ModelBox(bone23, 10, 22, -0.2677F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone23.cubeList.add(new ModelBox(bone23, 6, 22, -0.3177F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        piece3 = new ModelRenderer(this);
        piece3.setRotationPoint(0.0F, 13.0F, 0.0F);
        laba.addChild(piece3);
        setRotationAngle(piece3, 0.0F, -1.5708F, 0.0F);


        bone24 = new ModelRenderer(this);
        bone24.setRotationPoint(0.0F, 0.0F, 7.1706F);
        piece3.addChild(bone24);
        setRotationAngle(bone24, 0.6479F, 0.0F, 0.0F);
        bone24.cubeList.add(new ModelBox(bone24, 6, 11, -2.0F, -3.7575F, 0.0F, 4, 4, 0, 0.0F, false));
        bone24.cubeList.add(new ModelBox(bone24, 10, 17, -1.0F, -8.7575F, 0.0F, 2, 5, 0, 0.0F, false));
        bone24.cubeList.add(new ModelBox(bone24, 5, 24, -0.5F, 0.322F, -1.1959F, 1, 1, 1, 0.1969F, false));

        bone25 = new ModelRenderer(this);
        bone25.setRotationPoint(2.0F, 0.2425F, 0.0F);
        bone24.addChild(bone25);
        setRotationAngle(bone25, 0.0F, 0.0F, -0.245F);
        bone25.cubeList.add(new ModelBox(bone25, 12, 0, 0.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone26 = new ModelRenderer(this);
        bone26.setRotationPoint(-2.0F, 0.2425F, 0.0F);
        bone24.addChild(bone26);
        setRotationAngle(bone26, 0.0F, 0.0F, 0.245F);
        bone26.cubeList.add(new ModelBox(bone26, 12, 0, -1.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone27 = new ModelRenderer(this);
        bone27.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone24.addChild(bone27);
        setRotationAngle(bone27, 0.0F, 0.0F, -0.7854F);
        bone27.cubeList.add(new ModelBox(bone27, 5, 24, -1.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone28 = new ModelRenderer(this);
        bone28.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone24.addChild(bone28);
        setRotationAngle(bone28, 0.0F, 0.0F, 0.7854F);
        bone28.cubeList.add(new ModelBox(bone28, 5, 24, 0.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone29 = new ModelRenderer(this);
        bone29.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone24.addChild(bone29);
        setRotationAngle(bone29, 0.0F, 0.0F, -0.3927F);
        bone29.cubeList.add(new ModelBox(bone29, 5, 24, -2.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone30 = new ModelRenderer(this);
        bone30.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone24.addChild(bone30);
        setRotationAngle(bone30, 0.0F, 0.0F, 0.3927F);
        bone30.cubeList.add(new ModelBox(bone30, 5, 24, 1.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone31 = new ModelRenderer(this);
        bone31.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone24.addChild(bone31);
        setRotationAngle(bone31, 0.0F, 0.0F, -0.3927F);


        bone32 = new ModelRenderer(this);
        bone32.setRotationPoint(0.0F, -6.3F, 2.3956F);
        piece3.addChild(bone32);
        setRotationAngle(bone32, 0.3066F, 0.0F, 0.0F);


        bone33 = new ModelRenderer(this);
        bone33.setRotationPoint(1.0F, 0.124F, 2.4152F);
        bone32.addChild(bone33);
        setRotationAngle(bone33, 0.0F, 0.0F, -0.1244F);
        bone33.cubeList.add(new ModelBox(bone33, 14, 0, -0.9923F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone33.cubeList.add(new ModelBox(bone33, 0, 11, -0.7923F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone33.cubeList.add(new ModelBox(bone33, 10, 22, -0.7323F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone33.cubeList.add(new ModelBox(bone33, 6, 22, -0.6823F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        bone34 = new ModelRenderer(this);
        bone34.setRotationPoint(-1.0F, 0.124F, 2.4152F);
        bone32.addChild(bone34);
        setRotationAngle(bone34, 0.0F, 0.0F, 0.1244F);
        bone34.cubeList.add(new ModelBox(bone34, 14, 0, -0.0077F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone34.cubeList.add(new ModelBox(bone34, 0, 11, -0.2077F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone34.cubeList.add(new ModelBox(bone34, 10, 22, -0.2677F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone34.cubeList.add(new ModelBox(bone34, 6, 22, -0.3177F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        piece4 = new ModelRenderer(this);
        piece4.setRotationPoint(0.0F, 13.0F, 0.0F);
        laba.addChild(piece4);
        setRotationAngle(piece4, 0.0F, -2.3562F, 0.0F);


        bone35 = new ModelRenderer(this);
        bone35.setRotationPoint(0.0F, 0.0F, 7.1706F);
        piece4.addChild(bone35);
        setRotationAngle(bone35, 0.6479F, 0.0F, 0.0F);
        bone35.cubeList.add(new ModelBox(bone35, 6, 11, -2.0F, -3.7575F, 0.0F, 4, 4, 0, 0.0F, false));
        bone35.cubeList.add(new ModelBox(bone35, 10, 17, -1.0F, -8.7575F, 0.0F, 2, 5, 0, 0.0F, false));
        bone35.cubeList.add(new ModelBox(bone35, 5, 24, -0.5F, 0.322F, -1.1959F, 1, 1, 1, 0.1969F, false));

        bone36 = new ModelRenderer(this);
        bone36.setRotationPoint(2.0F, 0.2425F, 0.0F);
        bone35.addChild(bone36);
        setRotationAngle(bone36, 0.0F, 0.0F, -0.245F);
        bone36.cubeList.add(new ModelBox(bone36, 12, 0, 0.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone37 = new ModelRenderer(this);
        bone37.setRotationPoint(-2.0F, 0.2425F, 0.0F);
        bone35.addChild(bone37);
        setRotationAngle(bone37, 0.0F, 0.0F, 0.245F);
        bone37.cubeList.add(new ModelBox(bone37, 12, 0, -1.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone38 = new ModelRenderer(this);
        bone38.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone35.addChild(bone38);
        setRotationAngle(bone38, 0.0F, 0.0F, -0.7854F);
        bone38.cubeList.add(new ModelBox(bone38, 5, 24, -1.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone39 = new ModelRenderer(this);
        bone39.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone35.addChild(bone39);
        setRotationAngle(bone39, 0.0F, 0.0F, 0.7854F);
        bone39.cubeList.add(new ModelBox(bone39, 5, 24, 0.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone40 = new ModelRenderer(this);
        bone40.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone35.addChild(bone40);
        setRotationAngle(bone40, 0.0F, 0.0F, -0.3927F);
        bone40.cubeList.add(new ModelBox(bone40, 5, 24, -2.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone41 = new ModelRenderer(this);
        bone41.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone35.addChild(bone41);
        setRotationAngle(bone41, 0.0F, 0.0F, 0.3927F);
        bone41.cubeList.add(new ModelBox(bone41, 5, 24, 1.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone42 = new ModelRenderer(this);
        bone42.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone35.addChild(bone42);
        setRotationAngle(bone42, 0.0F, 0.0F, -0.3927F);


        bone43 = new ModelRenderer(this);
        bone43.setRotationPoint(0.0F, -6.3F, 2.3956F);
        piece4.addChild(bone43);
        setRotationAngle(bone43, 0.3066F, 0.0F, 0.0F);


        bone44 = new ModelRenderer(this);
        bone44.setRotationPoint(1.0F, 0.124F, 2.4152F);
        bone43.addChild(bone44);
        setRotationAngle(bone44, 0.0F, 0.0F, -0.1244F);
        bone44.cubeList.add(new ModelBox(bone44, 14, 0, -0.9923F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone44.cubeList.add(new ModelBox(bone44, 0, 11, -0.7923F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone44.cubeList.add(new ModelBox(bone44, 10, 22, -0.7323F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone44.cubeList.add(new ModelBox(bone44, 6, 22, -0.6823F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        bone45 = new ModelRenderer(this);
        bone45.setRotationPoint(-1.0F, 0.124F, 2.4152F);
        bone43.addChild(bone45);
        setRotationAngle(bone45, 0.0F, 0.0F, 0.1244F);
        bone45.cubeList.add(new ModelBox(bone45, 14, 0, -0.0077F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone45.cubeList.add(new ModelBox(bone45, 0, 11, -0.2077F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone45.cubeList.add(new ModelBox(bone45, 10, 22, -0.2677F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone45.cubeList.add(new ModelBox(bone45, 6, 22, -0.3177F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        piece5 = new ModelRenderer(this);
        piece5.setRotationPoint(0.0F, 13.0F, 0.0F);
        laba.addChild(piece5);
        setRotationAngle(piece5, 0.0F, 3.1416F, 0.0F);


        bone46 = new ModelRenderer(this);
        bone46.setRotationPoint(0.0F, 0.0F, 7.1706F);
        piece5.addChild(bone46);
        setRotationAngle(bone46, 0.6479F, 0.0F, 0.0F);
        bone46.cubeList.add(new ModelBox(bone46, 6, 11, -2.0F, -3.7575F, 0.0F, 4, 4, 0, 0.0F, false));
        bone46.cubeList.add(new ModelBox(bone46, 10, 17, -1.0F, -8.7575F, 0.0F, 2, 5, 0, 0.0F, false));
        bone46.cubeList.add(new ModelBox(bone46, 5, 24, -0.5F, 0.322F, -1.1959F, 1, 1, 1, 0.1969F, false));

        bone47 = new ModelRenderer(this);
        bone47.setRotationPoint(2.0F, 0.2425F, 0.0F);
        bone46.addChild(bone47);
        setRotationAngle(bone47, 0.0F, 0.0F, -0.245F);
        bone47.cubeList.add(new ModelBox(bone47, 12, 0, 0.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone48 = new ModelRenderer(this);
        bone48.setRotationPoint(-2.0F, 0.2425F, 0.0F);
        bone46.addChild(bone48);
        setRotationAngle(bone48, 0.0F, 0.0F, 0.245F);
        bone48.cubeList.add(new ModelBox(bone48, 12, 0, -1.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone49 = new ModelRenderer(this);
        bone49.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone46.addChild(bone49);
        setRotationAngle(bone49, 0.0F, 0.0F, -0.7854F);
        bone49.cubeList.add(new ModelBox(bone49, 5, 24, -1.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone50 = new ModelRenderer(this);
        bone50.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone46.addChild(bone50);
        setRotationAngle(bone50, 0.0F, 0.0F, 0.7854F);
        bone50.cubeList.add(new ModelBox(bone50, 5, 24, 0.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone51 = new ModelRenderer(this);
        bone51.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone46.addChild(bone51);
        setRotationAngle(bone51, 0.0F, 0.0F, -0.3927F);
        bone51.cubeList.add(new ModelBox(bone51, 5, 24, -2.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone52 = new ModelRenderer(this);
        bone52.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone46.addChild(bone52);
        setRotationAngle(bone52, 0.0F, 0.0F, 0.3927F);
        bone52.cubeList.add(new ModelBox(bone52, 5, 24, 1.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone53 = new ModelRenderer(this);
        bone53.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone46.addChild(bone53);
        setRotationAngle(bone53, 0.0F, 0.0F, -0.3927F);


        bone54 = new ModelRenderer(this);
        bone54.setRotationPoint(0.0F, -6.3F, 2.3956F);
        piece5.addChild(bone54);
        setRotationAngle(bone54, 0.3066F, 0.0F, 0.0F);


        bone55 = new ModelRenderer(this);
        bone55.setRotationPoint(1.0F, 0.124F, 2.4152F);
        bone54.addChild(bone55);
        setRotationAngle(bone55, 0.0F, 0.0F, -0.1244F);
        bone55.cubeList.add(new ModelBox(bone55, 14, 0, -0.9923F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone55.cubeList.add(new ModelBox(bone55, 0, 11, -0.7923F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone55.cubeList.add(new ModelBox(bone55, 10, 22, -0.7323F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone55.cubeList.add(new ModelBox(bone55, 6, 22, -0.6823F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        bone56 = new ModelRenderer(this);
        bone56.setRotationPoint(-1.0F, 0.124F, 2.4152F);
        bone54.addChild(bone56);
        setRotationAngle(bone56, 0.0F, 0.0F, 0.1244F);
        bone56.cubeList.add(new ModelBox(bone56, 14, 0, -0.0077F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone56.cubeList.add(new ModelBox(bone56, 0, 11, -0.2077F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone56.cubeList.add(new ModelBox(bone56, 10, 22, -0.2677F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone56.cubeList.add(new ModelBox(bone56, 6, 22, -0.3177F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        piece6 = new ModelRenderer(this);
        piece6.setRotationPoint(0.0F, 13.0F, 0.0F);
        laba.addChild(piece6);
        setRotationAngle(piece6, 0.0F, 2.3562F, 0.0F);


        bone57 = new ModelRenderer(this);
        bone57.setRotationPoint(0.0F, 0.0F, 7.1706F);
        piece6.addChild(bone57);
        setRotationAngle(bone57, 0.6479F, 0.0F, 0.0F);
        bone57.cubeList.add(new ModelBox(bone57, 6, 11, -2.0F, -3.7575F, 0.0F, 4, 4, 0, 0.0F, false));
        bone57.cubeList.add(new ModelBox(bone57, 10, 17, -1.0F, -8.7575F, 0.0F, 2, 5, 0, 0.0F, false));
        bone57.cubeList.add(new ModelBox(bone57, 5, 24, -0.5F, 0.322F, -1.1959F, 1, 1, 1, 0.1969F, false));

        bone58 = new ModelRenderer(this);
        bone58.setRotationPoint(2.0F, 0.2425F, 0.0F);
        bone57.addChild(bone58);
        setRotationAngle(bone58, 0.0F, 0.0F, -0.245F);
        bone58.cubeList.add(new ModelBox(bone58, 12, 0, 0.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone59 = new ModelRenderer(this);
        bone59.setRotationPoint(-2.0F, 0.2425F, 0.0F);
        bone57.addChild(bone59);
        setRotationAngle(bone59, 0.0F, 0.0F, 0.245F);
        bone59.cubeList.add(new ModelBox(bone59, 12, 0, -1.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone60 = new ModelRenderer(this);
        bone60.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone57.addChild(bone60);
        setRotationAngle(bone60, 0.0F, 0.0F, -0.7854F);
        bone60.cubeList.add(new ModelBox(bone60, 5, 24, -1.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone61 = new ModelRenderer(this);
        bone61.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone57.addChild(bone61);
        setRotationAngle(bone61, 0.0F, 0.0F, 0.7854F);
        bone61.cubeList.add(new ModelBox(bone61, 5, 24, 0.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone62 = new ModelRenderer(this);
        bone62.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone57.addChild(bone62);
        setRotationAngle(bone62, 0.0F, 0.0F, -0.3927F);
        bone62.cubeList.add(new ModelBox(bone62, 5, 24, -2.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone63 = new ModelRenderer(this);
        bone63.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone57.addChild(bone63);
        setRotationAngle(bone63, 0.0F, 0.0F, 0.3927F);
        bone63.cubeList.add(new ModelBox(bone63, 5, 24, 1.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone64 = new ModelRenderer(this);
        bone64.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone57.addChild(bone64);
        setRotationAngle(bone64, 0.0F, 0.0F, -0.3927F);


        bone65 = new ModelRenderer(this);
        bone65.setRotationPoint(0.0F, -6.3F, 2.3956F);
        piece6.addChild(bone65);
        setRotationAngle(bone65, 0.3066F, 0.0F, 0.0F);


        bone66 = new ModelRenderer(this);
        bone66.setRotationPoint(1.0F, 0.124F, 2.4152F);
        bone65.addChild(bone66);
        setRotationAngle(bone66, 0.0F, 0.0F, -0.1244F);
        bone66.cubeList.add(new ModelBox(bone66, 14, 0, -0.9923F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone66.cubeList.add(new ModelBox(bone66, 0, 11, -0.7923F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone66.cubeList.add(new ModelBox(bone66, 10, 22, -0.7323F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone66.cubeList.add(new ModelBox(bone66, 6, 22, -0.6823F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        bone67 = new ModelRenderer(this);
        bone67.setRotationPoint(-1.0F, 0.124F, 2.4152F);
        bone65.addChild(bone67);
        setRotationAngle(bone67, 0.0F, 0.0F, 0.1244F);
        bone67.cubeList.add(new ModelBox(bone67, 14, 0, -0.0077F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone67.cubeList.add(new ModelBox(bone67, 0, 11, -0.2077F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone67.cubeList.add(new ModelBox(bone67, 10, 22, -0.2677F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone67.cubeList.add(new ModelBox(bone67, 6, 22, -0.3177F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        piece7 = new ModelRenderer(this);
        piece7.setRotationPoint(0.0F, 13.0F, 0.0F);
        laba.addChild(piece7);
        setRotationAngle(piece7, 0.0F, 1.5708F, 0.0F);


        bone68 = new ModelRenderer(this);
        bone68.setRotationPoint(0.0F, 0.0F, 7.1706F);
        piece7.addChild(bone68);
        setRotationAngle(bone68, 0.6479F, 0.0F, 0.0F);
        bone68.cubeList.add(new ModelBox(bone68, 6, 11, -2.0F, -3.7575F, 0.0F, 4, 4, 0, 0.0F, false));
        bone68.cubeList.add(new ModelBox(bone68, 10, 17, -1.0F, -8.7575F, 0.0F, 2, 5, 0, 0.0F, false));
        bone68.cubeList.add(new ModelBox(bone68, 5, 24, -0.5F, 0.322F, -1.1959F, 1, 1, 1, 0.1969F, false));

        bone69 = new ModelRenderer(this);
        bone69.setRotationPoint(2.0F, 0.2425F, 0.0F);
        bone68.addChild(bone69);
        setRotationAngle(bone69, 0.0F, 0.0F, -0.245F);
        bone69.cubeList.add(new ModelBox(bone69, 12, 0, 0.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone70 = new ModelRenderer(this);
        bone70.setRotationPoint(-2.0F, 0.2425F, 0.0F);
        bone68.addChild(bone70);
        setRotationAngle(bone70, 0.0F, 0.0F, 0.245F);
        bone70.cubeList.add(new ModelBox(bone70, 12, 0, -1.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone71 = new ModelRenderer(this);
        bone71.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone68.addChild(bone71);
        setRotationAngle(bone71, 0.0F, 0.0F, -0.7854F);
        bone71.cubeList.add(new ModelBox(bone71, 5, 24, -1.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone72 = new ModelRenderer(this);
        bone72.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone68.addChild(bone72);
        setRotationAngle(bone72, 0.0F, 0.0F, 0.7854F);
        bone72.cubeList.add(new ModelBox(bone72, 5, 24, 0.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone73 = new ModelRenderer(this);
        bone73.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone68.addChild(bone73);
        setRotationAngle(bone73, 0.0F, 0.0F, -0.3927F);
        bone73.cubeList.add(new ModelBox(bone73, 5, 24, -2.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone74 = new ModelRenderer(this);
        bone74.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone68.addChild(bone74);
        setRotationAngle(bone74, 0.0F, 0.0F, 0.3927F);
        bone74.cubeList.add(new ModelBox(bone74, 5, 24, 1.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone75 = new ModelRenderer(this);
        bone75.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone68.addChild(bone75);
        setRotationAngle(bone75, 0.0F, 0.0F, -0.3927F);


        bone76 = new ModelRenderer(this);
        bone76.setRotationPoint(0.0F, -6.3F, 2.3956F);
        piece7.addChild(bone76);
        setRotationAngle(bone76, 0.3066F, 0.0F, 0.0F);


        bone77 = new ModelRenderer(this);
        bone77.setRotationPoint(1.0F, 0.124F, 2.4152F);
        bone76.addChild(bone77);
        setRotationAngle(bone77, 0.0F, 0.0F, -0.1244F);
        bone77.cubeList.add(new ModelBox(bone77, 14, 0, -0.9923F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone77.cubeList.add(new ModelBox(bone77, 0, 11, -0.7923F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone77.cubeList.add(new ModelBox(bone77, 10, 22, -0.7323F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone77.cubeList.add(new ModelBox(bone77, 6, 22, -0.6823F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        bone78 = new ModelRenderer(this);
        bone78.setRotationPoint(-1.0F, 0.124F, 2.4152F);
        bone76.addChild(bone78);
        setRotationAngle(bone78, 0.0F, 0.0F, 0.1244F);
        bone78.cubeList.add(new ModelBox(bone78, 14, 0, -0.0077F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone78.cubeList.add(new ModelBox(bone78, 0, 11, -0.2077F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone78.cubeList.add(new ModelBox(bone78, 10, 22, -0.2677F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone78.cubeList.add(new ModelBox(bone78, 6, 22, -0.3177F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        piece8 = new ModelRenderer(this);
        piece8.setRotationPoint(0.0F, 13.0F, 0.0F);
        laba.addChild(piece8);
        setRotationAngle(piece8, 0.0F, 0.7854F, 0.0F);


        bone79 = new ModelRenderer(this);
        bone79.setRotationPoint(0.0F, 0.0F, 7.1706F);
        piece8.addChild(bone79);
        setRotationAngle(bone79, 0.6479F, 0.0F, 0.0F);
        bone79.cubeList.add(new ModelBox(bone79, 6, 11, -2.0F, -3.7575F, 0.0F, 4, 4, 0, 0.0F, false));
        bone79.cubeList.add(new ModelBox(bone79, 10, 17, -1.0F, -8.7575F, 0.0F, 2, 5, 0, 0.0F, false));
        bone79.cubeList.add(new ModelBox(bone79, 5, 24, -0.5F, 0.322F, -1.1959F, 1, 1, 1, 0.1969F, false));

        bone80 = new ModelRenderer(this);
        bone80.setRotationPoint(2.0F, 0.2425F, 0.0F);
        bone79.addChild(bone80);
        setRotationAngle(bone80, 0.0F, 0.0F, -0.245F);
        bone80.cubeList.add(new ModelBox(bone80, 12, 0, 0.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone81 = new ModelRenderer(this);
        bone81.setRotationPoint(-2.0F, 0.2425F, 0.0F);
        bone79.addChild(bone81);
        setRotationAngle(bone81, 0.0F, 0.0F, 0.245F);
        bone81.cubeList.add(new ModelBox(bone81, 12, 0, -1.0F, -8.0F, 0.0F, 1, 8, 0, 0.0F, false));

        bone82 = new ModelRenderer(this);
        bone82.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone79.addChild(bone82);
        setRotationAngle(bone82, 0.0F, 0.0F, -0.7854F);
        bone82.cubeList.add(new ModelBox(bone82, 5, 24, -1.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone83 = new ModelRenderer(this);
        bone83.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone79.addChild(bone83);
        setRotationAngle(bone83, 0.0F, 0.0F, 0.7854F);
        bone83.cubeList.add(new ModelBox(bone83, 5, 24, 0.1968F, -1.1969F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone84 = new ModelRenderer(this);
        bone84.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone79.addChild(bone84);
        setRotationAngle(bone84, 0.0F, 0.0F, -0.3927F);
        bone84.cubeList.add(new ModelBox(bone84, 5, 24, -2.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone85 = new ModelRenderer(this);
        bone85.setRotationPoint(-2.9701F, 0.0F, 0.001F);
        bone79.addChild(bone85);
        setRotationAngle(bone85, 0.0F, 0.0F, 0.3927F);
        bone85.cubeList.add(new ModelBox(bone85, 5, 24, 1.4845F, -0.6636F, -1.1969F, 1, 1, 1, 0.1969F, false));

        bone86 = new ModelRenderer(this);
        bone86.setRotationPoint(2.9701F, 0.0F, 0.001F);
        bone79.addChild(bone86);
        setRotationAngle(bone86, 0.0F, 0.0F, -0.3927F);


        bone87 = new ModelRenderer(this);
        bone87.setRotationPoint(0.0F, -6.3F, 2.3956F);
        piece8.addChild(bone87);
        setRotationAngle(bone87, 0.3066F, 0.0F, 0.0F);


        bone88 = new ModelRenderer(this);
        bone88.setRotationPoint(1.0F, 0.124F, 2.4152F);
        bone87.addChild(bone88);
        setRotationAngle(bone88, 0.0F, 0.0F, -0.1244F);
        bone88.cubeList.add(new ModelBox(bone88, 14, 0, -0.9923F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone88.cubeList.add(new ModelBox(bone88, 0, 11, -0.7923F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone88.cubeList.add(new ModelBox(bone88, 10, 22, -0.7323F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone88.cubeList.add(new ModelBox(bone88, 6, 22, -0.6823F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        bone89 = new ModelRenderer(this);
        bone89.setRotationPoint(-1.0F, 0.124F, 2.4152F);
        bone87.addChild(bone89);
        setRotationAngle(bone89, 0.0F, 0.0F, 0.1244F);
        bone89.cubeList.add(new ModelBox(bone89, 14, 0, -0.0077F, -4.124F, -2.4152F, 1, 4, 0, 0.0F, false));
        bone89.cubeList.add(new ModelBox(bone89, 0, 11, -0.2077F, -5.924F, -3.2142F, 1, 2, 1, -0.2F, false));
        bone89.cubeList.add(new ModelBox(bone89, 10, 22, -0.2677F, -6.464F, -3.1542F, 1, 1, 1, -0.26F, false));
        bone89.cubeList.add(new ModelBox(bone89, 6, 22, -0.3177F, -6.894F, -3.1042F, 1, 1, 1, -0.31F, false));

        tube = new ModelRenderer(this);
        tube.setRotationPoint(0.75F, 24.0F, 1.5F);
        tube.cubeList.add(new ModelBox(tube, 6, 19, -1.25F, -12.4297F, 5.2362F, 1, 2, 1, 0.0F, false));
        tube.cubeList.add(new ModelBox(tube, 0, 17, 0.7637F, -9.416F, 5.2362F, 4, 1, 1, 0.0F, false));
        tube.cubeList.add(new ModelBox(tube, 0, 11, 5.7773F, -9.416F, 0.2226F, 1, 1, 4, 0.0F, false));
        tube.cubeList.add(new ModelBox(tube, 9, 24, 3.7637F, -9.416F, -1.7911F, 1, 1, 1, 0.0F, false));

        bone90 = new ModelRenderer(this);
        bone90.setRotationPoint(-0.75F, -12.8536F, 5.3536F);
        tube.addChild(bone90);
        setRotationAngle(bone90, -1.1781F, 0.0F, 0.0F);
        bone90.cubeList.add(new ModelBox(bone90, 10, 27, -0.5F, -0.6533F, -0.2706F, 1, 1, 1, 0.0F, false));

        bone97 = new ModelRenderer(this);
        bone97.setRotationPoint(6.2773F, -8.916F, -0.2774F);
        tube.addChild(bone97);
        setRotationAngle(bone97, 0.0F, 0.3927F, 0.0F);
        bone97.cubeList.add(new ModelBox(bone97, 0, 25, -0.7294F, -0.5F, -0.3467F, 1, 1, 1, 0.0F, false));

        bone98 = new ModelRenderer(this);
        bone98.setRotationPoint(6.2773F, -8.916F, -0.2774F);
        tube.addChild(bone98);
        setRotationAngle(bone98, 0.0F, 0.7854F, 0.0F);
        bone98.cubeList.add(new ModelBox(bone98, 0, 25, -0.6173F, -0.5F, -1.2168F, 1, 1, 1, 0.0F, false));

        bone99 = new ModelRenderer(this);
        bone99.setRotationPoint(6.2773F, -8.916F, -0.2774F);
        tube.addChild(bone99);
        setRotationAngle(bone99, 0.0F, 1.1781F, 0.0F);
        bone99.cubeList.add(new ModelBox(bone99, 0, 25, -0.1808F, -0.5F, -1.9777F, 1, 1, 1, 0.0F, false));

        bone94 = new ModelRenderer(this);
        bone94.setRotationPoint(3.2637F, -8.916F, 5.7362F);
        tube.addChild(bone94);
        setRotationAngle(bone94, 0.0F, 0.3927F, 0.0F);
        bone94.cubeList.add(new ModelBox(bone94, 7, 26, 1.1945F, -0.5F, 0.036F, 1, 1, 1, 0.0F, false));

        bone95 = new ModelRenderer(this);
        bone95.setRotationPoint(3.2637F, -8.916F, 5.7362F);
        tube.addChild(bone95);
        setRotationAngle(bone95, 0.0F, 0.7854F, 0.0F);
        bone95.cubeList.add(new ModelBox(bone95, 7, 26, 1.631F, -0.5F, 0.7969F, 1, 1, 1, 0.0F, false));

        bone96 = new ModelRenderer(this);
        bone96.setRotationPoint(3.2637F, -8.916F, 5.7362F);
        tube.addChild(bone96);
        setRotationAngle(bone96, 0.0F, 1.1781F, 0.0F);
        bone96.cubeList.add(new ModelBox(bone96, 3, 26, 1.7431F, -0.5F, 1.667F, 1, 1, 1, 0.0F, false));

        bone91 = new ModelRenderer(this);
        bone91.setRotationPoint(-0.75F, -9.9297F, 5.7362F);
        tube.addChild(bone91);
        setRotationAngle(bone91, 0.0F, 0.0F, -0.3927F);
        bone91.cubeList.add(new ModelBox(bone91, 0, 27, -0.2706F, -0.6533F, -0.5F, 1, 1, 1, 0.0F, false));

        bone92 = new ModelRenderer(this);
        bone92.setRotationPoint(-0.75F, -9.9297F, 5.7362F);
        tube.addChild(bone92);
        setRotationAngle(bone92, 0.0F, 0.0F, -0.7854F);
        bone92.cubeList.add(new ModelBox(bone92, 0, 27, -0.3827F, 0.2168F, -0.5F, 1, 1, 1, 0.0F, false));

        bone93 = new ModelRenderer(this);
        bone93.setRotationPoint(-0.75F, -9.9297F, 5.7362F);
        tube.addChild(bone93);
        setRotationAngle(bone93, 0.0F, 0.0F, -1.1781F);
        bone93.cubeList.add(new ModelBox(bone93, 0, 27, -0.8192F, 0.9777F, -0.5F, 1, 1, 1, 0.0F, false));

        wheel = new ModelRenderer(this);
        wheel.setRotationPoint(5.0137F, 15.084F, 0.2089F);
        wheel.cubeList.add(new ModelBox(wheel, 12, 8, -1.3536F, -1.0F, -0.5F, 1, 2, 1, -0.1464F, false));

        bone9 = new ModelRenderer(this);
        bone9.setRotationPoint(-0.8536F, 0.0F, 0.0F);
        wheel.addChild(bone9);
        setRotationAngle(bone9, -0.7854F, 0.0F, 0.0F);
        bone9.cubeList.add(new ModelBox(bone9, 12, 8, -0.5F, -1.0F, -0.5F, 1, 2, 1, -0.1464F, false));

        bone100 = new ModelRenderer(this);
        bone100.setRotationPoint(-0.8536F, 0.0F, 0.0F);
        wheel.addChild(bone100);
        setRotationAngle(bone100, -1.5708F, 0.0F, 0.0F);
        bone100.cubeList.add(new ModelBox(bone100, 12, 8, -0.5F, -1.0F, -0.5F, 1, 2, 1, -0.1464F, false));

        bone101 = new ModelRenderer(this);
        bone101.setRotationPoint(-0.8536F, 0.0F, 0.0F);
        wheel.addChild(bone101);
        setRotationAngle(bone101, -2.3562F, 0.0F, 0.0F);
        bone101.cubeList.add(new ModelBox(bone101, 12, 8, -0.5F, -1.0F, -0.5F, 1, 2, 1, -0.1464F, false));

        ruler = new ModelRenderer(this);
        ruler.setRotationPoint(0.0F, 24.0F, 0.0F);
        ruler.cubeList.add(new ModelBox(ruler, 0, 19, -1.0F, -12.0F, 7.0F, 2, 5, 1, 0.0F, false));

        box = new ModelRenderer(this);
        box.setRotationPoint(0.0F, 24.0F, 0.0F);
        box.cubeList.add(new ModelBox(box, 0, 0, -8.0F, -1.0F, -8.0F, 16, 1, 16, 0.0F, false));
        box.cubeList.add(new ModelBox(box, 0, 0, -8.0F, -1.75F, -8.0F, 16, 1, 16, -0.25F, false));
        box.cubeList.add(new ModelBox(box, 0, 0, -8.0F, -6.25F, -8.0F, 16, 1, 16, -0.25F, false));
        box.cubeList.add(new ModelBox(box, 0, 0, -8.0F, -7.0F, -8.0F, 16, 1, 16, 0.0F, false));

        bone102 = new ModelRenderer(this);
        bone102.setRotationPoint(0.5F, 0.0F, -0.5F);
        box.addChild(bone102);
        bone102.cubeList.add(new ModelBox(bone102, 0, 0, 4.0F, -5.5F, 5.0F, 3, 4, 3, 0.0F, false));
        bone102.cubeList.add(new ModelBox(bone102, 0, 0, -8.0F, -5.5F, 5.0F, 3, 4, 3, 0.0F, false));
        bone102.cubeList.add(new ModelBox(bone102, 0, 0, -8.0F, -5.5F, -7.0F, 3, 4, 3, 0.0F, false));
        bone102.cubeList.add(new ModelBox(bone102, 0, 0, 4.0F, -5.5F, -7.0F, 3, 4, 3, 0.0F, false));
        bone102.cubeList.add(new ModelBox(bone102, 0, 17, -7.5F, -5.5F, -6.5F, 14, 4, 14, 0.0F, false));
        bone102.cubeList.add(new ModelBox(bone102, 0, 7, -3.0F, -5.0F, -7.0F, 5, 3, 1, 0.0F, false));

        disc = new ModelRenderer(this);
        disc.setRotationPoint(0.0F, 17.0F, 0.0F);
        disc.cubeList.add(new ModelBox(disc, 0, 35, -1.0F, -1.0F, -5.0F, 2, 1, 10, -0.0068F, false));
        disc.cubeList.add(new ModelBox(disc, 12, 40, -6.5F, -6.501F, -6.5F, 13, 11, 13, -5.5F, false));

        bone103 = new ModelRenderer(this);
        bone103.setRotationPoint(0.0F, 7.0F, 0.0F);
        disc.addChild(bone103);
        setRotationAngle(bone103, 0.0F, -0.3927F, 0.0F);
        bone103.cubeList.add(new ModelBox(bone103, 0, 35, -1.0F, -8.0F, -5.0F, 2, 1, 10, -0.0068F, false));

        bone104 = new ModelRenderer(this);
        bone104.setRotationPoint(0.0F, 7.0F, 0.0F);
        disc.addChild(bone104);
        setRotationAngle(bone104, 0.0F, -0.7854F, 0.0F);
        bone104.cubeList.add(new ModelBox(bone104, 0, 35, -1.0F, -8.0F, -5.0F, 2, 1, 10, -0.0068F, false));

        bone105 = new ModelRenderer(this);
        bone105.setRotationPoint(0.0F, 7.0F, 0.0F);
        disc.addChild(bone105);
        setRotationAngle(bone105, 0.0F, -1.1781F, 0.0F);
        bone105.cubeList.add(new ModelBox(bone105, 0, 35, -1.0F, -8.0F, -5.0F, 2, 1, 10, -0.0068F, false));

        bone106 = new ModelRenderer(this);
        bone106.setRotationPoint(0.0F, 7.0F, 0.0F);
        disc.addChild(bone106);
        setRotationAngle(bone106, 0.0F, -1.5708F, 0.0F);
        bone106.cubeList.add(new ModelBox(bone106, 0, 35, -1.0F, -8.0F, -5.0F, 2, 1, 10, -0.0068F, false));

        bone107 = new ModelRenderer(this);
        bone107.setRotationPoint(0.0F, 7.0F, 0.0F);
        disc.addChild(bone107);
        setRotationAngle(bone107, 0.0F, -1.9635F, 0.0F);
        bone107.cubeList.add(new ModelBox(bone107, 0, 35, -1.0F, -8.0F, -5.0F, 2, 1, 10, -0.0068F, false));

        bone108 = new ModelRenderer(this);
        bone108.setRotationPoint(0.0F, 7.0F, 0.0F);
        disc.addChild(bone108);
        setRotationAngle(bone108, 0.0F, -2.3562F, 0.0F);
        bone108.cubeList.add(new ModelBox(bone108, 0, 35, -1.0F, -8.0F, -5.0F, 2, 1, 10, -0.0068F, false));

        bone109 = new ModelRenderer(this);
        bone109.setRotationPoint(0.0F, 7.0F, 0.0F);
        disc.addChild(bone109);
        setRotationAngle(bone109, 0.0F, -2.7489F, 0.0F);
        bone109.cubeList.add(new ModelBox(bone109, 0, 35, -1.0F, -8.0F, -5.0F, 2, 1, 10, -0.0068F, false));

        huahen = new ModelRenderer(this);
        huahen.setRotationPoint(0.0F, 7.0F, 0.0F);
        disc.addChild(huahen);
        huahen.cubeList.add(new ModelBox(huahen, 0, 47, -2.1022F, -8.1032F, -0.5F, 1, 1, 1, -0.1022F, false));
        huahen.cubeList.add(new ModelBox(huahen, 0, 47, 1.1022F, -8.1032F, -0.5F, 1, 1, 1, -0.1022F, false));
        huahen.cubeList.add(new ModelBox(huahen, 0, 52, -4.2043F, -8.2053F, -1.0F, 1, 1, 2, -0.2044F, false));
        huahen.cubeList.add(new ModelBox(huahen, 0, 52, 3.2043F, -8.2053F, -1.0F, 1, 1, 2, -0.2044F, false));

        bone110 = new ModelRenderer(this);
        bone110.setRotationPoint(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone110);
        setRotationAngle(bone110, 0.0F, -0.3927F, 0.0F);
        bone110.cubeList.add(new ModelBox(bone110, 0, 47, -2.1022F, -8.1032F, -0.5F, 1, 1, 1, -0.1022F, false));
        bone110.cubeList.add(new ModelBox(bone110, 0, 47, 1.1022F, -8.1032F, -0.5F, 1, 1, 1, -0.1022F, false));

        bone111 = new ModelRenderer(this);
        bone111.setRotationPoint(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone111);
        setRotationAngle(bone111, 0.0F, -0.7854F, 0.0F);
        bone111.cubeList.add(new ModelBox(bone111, 0, 47, -2.1022F, -8.1032F, -0.5F, 1, 1, 1, -0.1022F, false));
        bone111.cubeList.add(new ModelBox(bone111, 0, 47, 1.1022F, -8.1032F, -0.5F, 1, 1, 1, -0.1022F, false));

        bone112 = new ModelRenderer(this);
        bone112.setRotationPoint(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone112);
        setRotationAngle(bone112, 0.0F, -1.1781F, 0.0F);
        bone112.cubeList.add(new ModelBox(bone112, 0, 47, -2.1022F, -8.1032F, -0.5F, 1, 1, 1, -0.1022F, false));
        bone112.cubeList.add(new ModelBox(bone112, 0, 47, 1.1022F, -8.1032F, -0.5F, 1, 1, 1, -0.1022F, false));

        bone113 = new ModelRenderer(this);
        bone113.setRotationPoint(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone113);
        setRotationAngle(bone113, 0.0F, -1.5708F, 0.0F);
        bone113.cubeList.add(new ModelBox(bone113, 0, 47, -2.1022F, -8.1032F, -0.5F, 1, 1, 1, -0.1022F, false));
        bone113.cubeList.add(new ModelBox(bone113, 0, 47, 1.1022F, -8.1032F, -0.5F, 1, 1, 1, -0.1022F, false));

        bone114 = new ModelRenderer(this);
        bone114.setRotationPoint(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone114);
        setRotationAngle(bone114, 0.0F, -0.3927F, 0.0F);
        bone114.cubeList.add(new ModelBox(bone114, 0, 52, -4.2043F, -8.2053F, -1.0F, 1, 1, 2, -0.2044F, false));
        bone114.cubeList.add(new ModelBox(bone114, 0, 52, 3.2044F, -8.2053F, -1.0F, 1, 1, 2, -0.2044F, false));

        bone115 = new ModelRenderer(this);
        bone115.setRotationPoint(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone115);
        setRotationAngle(bone115, 0.0F, -0.7854F, 0.0F);
        bone115.cubeList.add(new ModelBox(bone115, 0, 52, -4.2043F, -8.2053F, -1.0F, 1, 1, 2, -0.2044F, false));
        bone115.cubeList.add(new ModelBox(bone115, 0, 52, 3.2044F, -8.2053F, -1.0F, 1, 1, 2, -0.2044F, false));

        bone116 = new ModelRenderer(this);
        bone116.setRotationPoint(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone116);
        setRotationAngle(bone116, 0.0F, -1.1781F, 0.0F);
        bone116.cubeList.add(new ModelBox(bone116, 0, 52, -4.2043F, -8.2053F, -1.0F, 1, 1, 2, -0.2044F, false));
        bone116.cubeList.add(new ModelBox(bone116, 0, 52, 3.2044F, -8.2053F, -1.0F, 1, 1, 2, -0.2044F, false));

        bone117 = new ModelRenderer(this);
        bone117.setRotationPoint(0.0F, 0.0F, 0.0F);
        huahen.addChild(bone117);
        setRotationAngle(bone117, 0.0F, -1.5708F, 0.0F);
        bone117.cubeList.add(new ModelBox(bone117, 0, 52, -4.2043F, -8.2053F, -1.0F, 1, 1, 2, -0.2044F, false));
        bone117.cubeList.add(new ModelBox(bone117, 0, 52, 3.2044F, -8.2053F, -1.0F, 1, 1, 2, -0.2044F, false));
    }

    @Override
    public void render(@Nullable Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        laba.render(f5);
        tube.render(f5);
        wheel.render(f5);
        ruler.render(f5);
        box.render(f5);
        disc.render(f5);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    public ModelRenderer getDiscBone() {
        return disc;
    }
}
