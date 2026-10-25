package com.voidplague.mod.client.model;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.client.animation.VoidWatcherSentinelAnimations;
import com.voidplague.mod.entity.VoidWatcherSentinelEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class VoidWatcherSentinelModel extends HierarchicalModel<VoidWatcherSentinelEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "void_watcher_sentinel"), "main");

    private final ModelPart root;
    private final ModelPart cabeza;
    private final ModelPart ojo;

    public VoidWatcherSentinelModel(ModelPart root) {
        this.root = root.getChild("root");
        this.cabeza = this.root.getChild("cabeza");
        this.ojo = this.cabeza.getChild("ojo");
    }

    public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition cabeza = root.addOrReplaceChild("cabeza", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition caparazon = cabeza.addOrReplaceChild("caparazon", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -6.0F, -7.0F, 18.0F, 12.0F, 18.0F, new CubeDeformation(0.0F))
		.texOffs(41, 31).addBox(-7.0F, -9.0F, -5.0F, 14.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(57, 52).addBox(-8.0F, -3.0F, 7.0F, 16.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition horn_right_r1 = caparazon.addOrReplaceChild("horn_right_r1", CubeListBuilder.create().texOffs(9, 90).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, -5.0F, 1.0F, 0.4363F, 0.0F, 0.4363F));

		PartDefinition horn_left_r1 = caparazon.addOrReplaceChild("horn_left_r1", CubeListBuilder.create().texOffs(0, 90).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -5.0F, 1.0F, 0.4363F, 0.0F, -0.4363F));

		PartDefinition flank_right_r1 = caparazon.addOrReplaceChild("flank_right_r1", CubeListBuilder.create().texOffs(98, 0).addBox(-1.0F, -5.0F, -5.0F, 2.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.0F, 0.0F, 3.0F, 0.0F, -0.0873F, 0.0F));

		PartDefinition flank_left_r1 = caparazon.addOrReplaceChild("flank_left_r1", CubeListBuilder.create().texOffs(73, 0).addBox(-1.0F, -5.0F, -5.0F, 2.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, 0.0F, 3.0F, 0.0F, 0.0873F, 0.0F));

		PartDefinition mandibula_cuenca = cabeza.addOrReplaceChild("mandibula_cuenca", CubeListBuilder.create().texOffs(0, 99).addBox(-8.0F, -6.0F, -2.0F, 16.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(39, 99).addBox(-8.0F, 3.0F, -2.0F, 16.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -7.0F));

		PartDefinition tooth_bottom_centro_der_r1 = mandibula_cuenca.addOrReplaceChild("tooth_bottom_centro_der_r1", CubeListBuilder.create().texOffs(18, 106).addBox(-0.75F, -2.5F, -0.75F, 1.5F, 2.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.25F, 5.5F, -1.75F, 0.2618F, 0.0F, -0.1745F));

		PartDefinition tooth_bottom_der_r1 = mandibula_cuenca.addOrReplaceChild("tooth_bottom_der_r1", CubeListBuilder.create().texOffs(9, 106).addBox(-0.75F, -2.5F, -0.75F, 1.5F, 2.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.75F, 5.0F, -1.75F, 0.2618F, 0.0F, -0.3491F));

		PartDefinition tooth_bottom_centro_izq_r1 = mandibula_cuenca.addOrReplaceChild("tooth_bottom_centro_izq_r1", CubeListBuilder.create().texOffs(0, 106).addBox(-0.75F, -2.5F, -0.75F, 1.5F, 2.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.25F, 5.5F, -1.75F, 0.2618F, 0.0F, 0.1745F));

		PartDefinition tooth_bottom_izq_r1 = mandibula_cuenca.addOrReplaceChild("tooth_bottom_izq_r1", CubeListBuilder.create().texOffs(114, 99).addBox(-0.75F, -2.5F, -0.75F, 1.5F, 2.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.75F, 5.0F, -1.75F, 0.2618F, 0.0F, 0.3491F));

		PartDefinition tooth_top_centro_der_r1 = mandibula_cuenca.addOrReplaceChild("tooth_top_centro_der_r1", CubeListBuilder.create().texOffs(105, 99).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 2.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.25F, -5.5F, -1.75F, -0.2618F, 0.0F, -0.1745F));

		PartDefinition tooth_top_der_r1 = mandibula_cuenca.addOrReplaceChild("tooth_top_der_r1", CubeListBuilder.create().texOffs(96, 99).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 2.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.75F, -5.0F, -1.75F, -0.2618F, 0.0F, -0.3491F));

		PartDefinition tooth_top_centro_izq_r1 = mandibula_cuenca.addOrReplaceChild("tooth_top_centro_izq_r1", CubeListBuilder.create().texOffs(87, 99).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 2.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.25F, -5.5F, -1.75F, -0.2618F, 0.0F, 0.1745F));

		PartDefinition tooth_top_izq_r1 = mandibula_cuenca.addOrReplaceChild("tooth_top_izq_r1", CubeListBuilder.create().texOffs(78, 99).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 2.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.75F, -5.0F, -1.75F, -0.2618F, 0.0F, 0.3491F));

		PartDefinition ojo = cabeza.addOrReplaceChild("ojo", CubeListBuilder.create().texOffs(0, 31).addBox(-5.0F, -5.0F, -5.75F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -3.0F));

		PartDefinition collarin = cabeza.addOrReplaceChild("collarin", CubeListBuilder.create().texOffs(0, 52).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 3.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition tentaculos = root.addOrReplaceChild("tentaculos", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition tentacle_0 = tentaculos.addOrReplaceChild("tentacle_0", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -5.5F));

		PartDefinition t0_seg_0 = tentacle_0.addOrReplaceChild("t0_seg_0", CubeListBuilder.create().texOffs(102, 52).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition t0_seg_1 = t0_seg_0.addOrReplaceChild("t0_seg_1", CubeListBuilder.create().texOffs(115, 52).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t0_seg_2 = t0_seg_1.addOrReplaceChild("t0_seg_2", CubeListBuilder.create().texOffs(18, 90).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t0_seg_3 = t0_seg_2.addOrReplaceChild("t0_seg_3", CubeListBuilder.create().texOffs(0, 70).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 7.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition tentacle_1 = tentaculos.addOrReplaceChild("tentacle_1", CubeListBuilder.create(), PartPose.offset(4.0F, 0.0F, -4.0F));

		PartDefinition t1_seg_0 = tentacle_1.addOrReplaceChild("t1_seg_0", CubeListBuilder.create().texOffs(9, 70).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3039F, -0.0469F, -0.3039F));

		PartDefinition t1_seg_1 = t1_seg_0.addOrReplaceChild("t1_seg_1", CubeListBuilder.create().texOffs(22, 70).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t1_seg_2 = t1_seg_1.addOrReplaceChild("t1_seg_2", CubeListBuilder.create().texOffs(27, 90).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t1_seg_3 = t1_seg_2.addOrReplaceChild("t1_seg_3", CubeListBuilder.create().texOffs(35, 70).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 7.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition tentacle_2 = tentaculos.addOrReplaceChild("tentacle_2", CubeListBuilder.create(), PartPose.offset(5.5F, 0.0F, 0.0F));

		PartDefinition t2_seg_0 = tentacle_2.addOrReplaceChild("t2_seg_0", CubeListBuilder.create().texOffs(44, 70).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition t2_seg_1 = t2_seg_0.addOrReplaceChild("t2_seg_1", CubeListBuilder.create().texOffs(57, 70).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t2_seg_2 = t2_seg_1.addOrReplaceChild("t2_seg_2", CubeListBuilder.create().texOffs(36, 90).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t2_seg_3 = t2_seg_2.addOrReplaceChild("t2_seg_3", CubeListBuilder.create().texOffs(70, 70).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 7.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition tentacle_3 = tentaculos.addOrReplaceChild("tentacle_3", CubeListBuilder.create(), PartPose.offset(4.0F, 0.0F, 4.0F));

		PartDefinition t3_seg_0 = tentacle_3.addOrReplaceChild("t3_seg_0", CubeListBuilder.create().texOffs(79, 70).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3039F, 0.0469F, -0.3039F));

		PartDefinition t3_seg_1 = t3_seg_0.addOrReplaceChild("t3_seg_1", CubeListBuilder.create().texOffs(92, 70).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t3_seg_2 = t3_seg_1.addOrReplaceChild("t3_seg_2", CubeListBuilder.create().texOffs(45, 90).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t3_seg_3 = t3_seg_2.addOrReplaceChild("t3_seg_3", CubeListBuilder.create().texOffs(105, 70).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 7.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition tentacle_4 = tentaculos.addOrReplaceChild("tentacle_4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 5.5F));

		PartDefinition t4_seg_0 = tentacle_4.addOrReplaceChild("t4_seg_0", CubeListBuilder.create().texOffs(114, 70).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition t4_seg_1 = t4_seg_0.addOrReplaceChild("t4_seg_1", CubeListBuilder.create().texOffs(0, 80).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t4_seg_2 = t4_seg_1.addOrReplaceChild("t4_seg_2", CubeListBuilder.create().texOffs(54, 90).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t4_seg_3 = t4_seg_2.addOrReplaceChild("t4_seg_3", CubeListBuilder.create().texOffs(13, 80).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 7.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition tentacle_5 = tentaculos.addOrReplaceChild("tentacle_5", CubeListBuilder.create(), PartPose.offset(-4.0F, 0.0F, 4.0F));

		PartDefinition t5_seg_0 = tentacle_5.addOrReplaceChild("t5_seg_0", CubeListBuilder.create().texOffs(22, 80).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3039F, -0.0469F, 0.3039F));

		PartDefinition t5_seg_1 = t5_seg_0.addOrReplaceChild("t5_seg_1", CubeListBuilder.create().texOffs(35, 80).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t5_seg_2 = t5_seg_1.addOrReplaceChild("t5_seg_2", CubeListBuilder.create().texOffs(63, 90).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t5_seg_3 = t5_seg_2.addOrReplaceChild("t5_seg_3", CubeListBuilder.create().texOffs(48, 80).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 7.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition tentacle_6 = tentaculos.addOrReplaceChild("tentacle_6", CubeListBuilder.create(), PartPose.offset(-5.5F, 0.0F, 0.0F));

		PartDefinition t6_seg_0 = tentacle_6.addOrReplaceChild("t6_seg_0", CubeListBuilder.create().texOffs(57, 80).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition t6_seg_1 = t6_seg_0.addOrReplaceChild("t6_seg_1", CubeListBuilder.create().texOffs(70, 80).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t6_seg_2 = t6_seg_1.addOrReplaceChild("t6_seg_2", CubeListBuilder.create().texOffs(72, 90).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t6_seg_3 = t6_seg_2.addOrReplaceChild("t6_seg_3", CubeListBuilder.create().texOffs(83, 80).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 7.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition tentacle_7 = tentaculos.addOrReplaceChild("tentacle_7", CubeListBuilder.create(), PartPose.offset(-4.0F, 0.0F, -4.0F));

		PartDefinition t7_seg_0 = tentacle_7.addOrReplaceChild("t7_seg_0", CubeListBuilder.create().texOffs(92, 80).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3039F, 0.0469F, 0.3039F));

		PartDefinition t7_seg_1 = t7_seg_0.addOrReplaceChild("t7_seg_1", CubeListBuilder.create().texOffs(105, 80).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 6.0F, 2.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t7_seg_2 = t7_seg_1.addOrReplaceChild("t7_seg_2", CubeListBuilder.create().texOffs(81, 90).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition t7_seg_3 = t7_seg_2.addOrReplaceChild("t7_seg_3", CubeListBuilder.create().texOffs(118, 80).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 7.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

    @Override
    public void setupAnim(VoidWatcherSentinelEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.animate(entity.idleAnimationState, VoidWatcherSentinelAnimations.IDLE, ageInTicks);
        this.animate(entity.moveAnimationState, VoidWatcherSentinelAnimations.MOVE, ageInTicks);
        this.animate(entity.attackAnimationState, VoidWatcherSentinelAnimations.ATTACK, ageInTicks);

        this.ojo.yRot += Mth.clamp(netHeadYaw * ((float) Math.PI / 180F), -0.35F, 0.35F);
        this.ojo.xRot += Mth.clamp(headPitch * ((float) Math.PI / 180F), -0.25F, 0.25F);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}