package com.voidplague.mod.client.model;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.client.animation.VoidlingAnimations;
import com.voidplague.mod.entity.VoidlingEntity;
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

public class VoidlingModel extends HierarchicalModel<VoidlingEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "voidling"), "main");

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart mandible_left;
	private final ModelPart mandible_right;
	private final ModelPart leg_front_left;
	private final ModelPart fl_femur;
	private final ModelPart fl_tibia;
	private final ModelPart fl_claw;
	private final ModelPart leg_front_right;
	private final ModelPart fr_femur;
	private final ModelPart fr_tibia;
	private final ModelPart fr_claw;
	private final ModelPart leg_back_left;
	private final ModelPart bl_femur;
	private final ModelPart bl_tibia;
	private final ModelPart bl_claw;
	private final ModelPart leg_back_right;
	private final ModelPart br_femur;
	private final ModelPart br_tibia;
	private final ModelPart br_claw;

	public VoidlingModel(ModelPart root) {
		this.root = root.getChild("root");
		this.body = this.root.getChild("body");
		this.head = this.body.getChild("head");
		this.mandible_left = this.head.getChild("mandible_left");
		this.mandible_right = this.head.getChild("mandible_right");
		this.leg_front_left = this.body.getChild("leg_front_left");
		this.fl_femur = this.leg_front_left.getChild("fl_femur");
		this.fl_tibia = this.fl_femur.getChild("fl_tibia");
		this.fl_claw = this.fl_tibia.getChild("fl_claw");
		this.leg_front_right = this.body.getChild("leg_front_right");
		this.fr_femur = this.leg_front_right.getChild("fr_femur");
		this.fr_tibia = this.fr_femur.getChild("fr_tibia");
		this.fr_claw = this.fr_tibia.getChild("fr_claw");
		this.leg_back_left = this.body.getChild("leg_back_left");
		this.bl_femur = this.leg_back_left.getChild("bl_femur");
		this.bl_tibia = this.bl_femur.getChild("bl_tibia");
		this.bl_claw = this.bl_tibia.getChild("bl_claw");
		this.leg_back_right = this.body.getChild("leg_back_right");
		this.br_femur = this.leg_back_right.getChild("br_femur");
		this.br_tibia = this.br_femur.getChild("br_tibia");
		this.br_claw = this.br_tibia.getChild("br_claw");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		PartDefinition rear_plate_r1 = body.addOrReplaceChild("rear_plate_r1", CubeListBuilder.create().texOffs(83, 19).addBox(-3.5F, -2.0F, 0.0F, 7.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 5.0F, -0.3491F, 0.0F, 0.0F));

		PartDefinition dorsal_fin_crest_r1 = body.addOrReplaceChild("dorsal_fin_crest_r1", CubeListBuilder.create().texOffs(0, 19).addBox(-1.0F, -5.0F, -2.0F, 2.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.6109F, 0.0F, 0.0F));

		PartDefinition flank_carapace_r_r1 = body.addOrReplaceChild("flank_carapace_r_r1", CubeListBuilder.create().texOffs(95, 0).addBox(-2.5F, -3.0F, -4.5F, 3.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.0F, 0.0F, 0.2618F, 0.0F, 0.3491F));

		PartDefinition flank_carapace_l_r1 = body.addOrReplaceChild("flank_carapace_l_r1", CubeListBuilder.create().texOffs(70, 0).addBox(-0.5F, -3.0F, -4.5F, 3.0F, 6.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -2.0F, 0.0F, 0.2618F, 0.0F, -0.3491F));

		PartDefinition carapace_hood_r1 = body.addOrReplaceChild("carapace_hood_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -7.5F, -5.0F, 6.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(19, 19).addBox(-3.5F, -2.0F, -7.0F, 7.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(106, 19).addBox(-2.5F, 1.0F, -6.0F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(44, 32).addBox(-0.4F, 2.2F, -6.8F, 0.8F, 2.2F, 0.8F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, -0.3491F, 0.0F, 0.0F));

		PartDefinition tooth_bot_r2_r1 = head.addOrReplaceChild("tooth_bot_r2_r1", CubeListBuilder.create().texOffs(93, 44).addBox(-0.3F, -1.5F, -0.3F, 0.6F, 1.5F, 0.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.2F, 3.9F, -6.0F, 0.0873F, 0.0F, -0.2094F));

		PartDefinition tooth_bot_l2_r1 = head.addOrReplaceChild("tooth_bot_l2_r1", CubeListBuilder.create().texOffs(88, 44).addBox(-0.3F, -1.5F, -0.3F, 0.6F, 1.5F, 0.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.2F, 3.9F, -6.0F, 0.0873F, 0.0F, 0.2094F));

		PartDefinition tooth_bot_r1_r1 = head.addOrReplaceChild("tooth_bot_r1_r1", CubeListBuilder.create().texOffs(83, 44).addBox(-0.3F, -2.0F, -0.4F, 0.6F, 2.0F, 0.7F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, 3.8F, -6.0F, 0.0873F, 0.0F, -0.1047F));

		PartDefinition tooth_bot_l1_r1 = head.addOrReplaceChild("tooth_bot_l1_r1", CubeListBuilder.create().texOffs(78, 44).addBox(-0.3F, -2.0F, -0.4F, 0.6F, 2.0F, 0.7F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4F, 3.8F, -6.0F, 0.0873F, 0.0F, 0.1047F));

		PartDefinition tooth_top_r2_r1 = head.addOrReplaceChild("tooth_top_r2_r1", CubeListBuilder.create().texOffs(73, 44).addBox(-0.3F, 0.0F, -0.2F, 0.6F, 1.4F, 0.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6F, 2.1F, -6.4F, -0.0873F, 0.0F, 0.2618F));

		PartDefinition tooth_top_l2_r1 = head.addOrReplaceChild("tooth_top_l2_r1", CubeListBuilder.create().texOffs(68, 44).addBox(-0.3F, 0.0F, -0.2F, 0.6F, 1.4F, 0.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6F, 2.1F, -6.4F, -0.0873F, 0.0F, -0.2618F));

		PartDefinition tooth_top_r1_r1 = head.addOrReplaceChild("tooth_top_r1_r1", CubeListBuilder.create().texOffs(63, 44).addBox(-0.35F, 0.0F, -0.3F, 0.7F, 1.8F, 0.7F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.85F, 2.2F, -6.4F, -0.0873F, 0.0F, 0.1396F));

		PartDefinition tooth_top_l1_r1 = head.addOrReplaceChild("tooth_top_l1_r1", CubeListBuilder.create().texOffs(58, 44).addBox(-0.35F, 0.0F, -0.3F, 0.7F, 1.8F, 0.7F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.85F, 2.2F, -6.4F, -0.0873F, 0.0F, -0.1396F));

		PartDefinition brow_crest_r1 = head.addOrReplaceChild("brow_crest_r1", CubeListBuilder.create().texOffs(66, 19).addBox(-1.0F, -3.0F, -6.0F, 2.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition mandible_left = head.addOrReplaceChild("mandible_left", CubeListBuilder.create().texOffs(48, 19).addBox(-0.75F, -1.0F, -0.75F, 1.5F, 8.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 2.0F, -4.0F, -0.4363F, 0.1745F, 0.2618F));

		PartDefinition mandible_left_fang_r1 = mandible_left.addOrReplaceChild("mandible_left_fang_r1", CubeListBuilder.create().texOffs(49, 32).addBox(-0.25F, 0.0F, -0.75F, 1.0F, 2.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, 7.0F, -0.25F, -0.2618F, -0.0873F, -0.1745F));

		PartDefinition mandible_right = head.addOrReplaceChild("mandible_right", CubeListBuilder.create().texOffs(57, 19).addBox(-0.75F, -1.0F, -0.75F, 1.5F, 8.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 2.0F, -4.0F, -0.4363F, -0.1745F, -0.2618F));

		PartDefinition mandible_right_fang_r1 = mandible_right.addOrReplaceChild("mandible_right_fang_r1", CubeListBuilder.create().texOffs(56, 32).addBox(-0.75F, 0.0F, -0.75F, 1.0F, 2.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 7.0F, -0.25F, -0.2618F, 0.0873F, 0.1745F));

		PartDefinition leg_front_left = body.addOrReplaceChild("leg_front_left", CubeListBuilder.create().texOffs(0, 32).addBox(0.0F, -1.25F, -1.25F, 1.5F, 2.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -1.0F, -2.0F, 0.1745F, 0.5236F, 0.2618F));

		PartDefinition fl_femur = leg_front_left.addOrReplaceChild("fl_femur", CubeListBuilder.create().texOffs(63, 32).addBox(0.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 0.0F, 0.0F, -0.2618F, 0.0F, -1.309F));

		PartDefinition fl_tibia = fl_femur.addOrReplaceChild("fl_tibia", CubeListBuilder.create().texOffs(88, 32).addBox(0.0F, -0.75F, -1.0F, 15.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0F, 0.0F, 0.0F, 0.0F, 0.0F, 2.5307F));

		PartDefinition fl_claw = fl_tibia.addOrReplaceChild("fl_claw", CubeListBuilder.create().texOffs(98, 44).addBox(0.0F, -0.5F, -0.5F, 2.7F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(15.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition leg_front_right = body.addOrReplaceChild("leg_front_right", CubeListBuilder.create().texOffs(11, 32).addBox(-1.5F, -1.25F, -1.25F, 1.5F, 2.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, -1.0F, -2.0F, 0.1745F, -0.5236F, -0.2618F));

		PartDefinition fr_femur = leg_front_right.addOrReplaceChild("fr_femur", CubeListBuilder.create().texOffs(0, 39).addBox(-10.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.0F, 0.0F, 0.2618F, 0.0F, 1.309F));

		PartDefinition fr_tibia = fr_femur.addOrReplaceChild("fr_tibia", CubeListBuilder.create().texOffs(25, 39).addBox(-15.0F, -0.75F, -1.0F, 15.0F, 1.5F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0F, 0.0F, 0.0F, 0.0F, 0.0F, -2.5307F));

		PartDefinition fr_claw = fr_tibia.addOrReplaceChild("fr_claw", CubeListBuilder.create().texOffs(107, 44).addBox(-2.7F, -0.5F, -0.5F, 2.7F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition leg_back_left = body.addOrReplaceChild("leg_back_left", CubeListBuilder.create().texOffs(22, 32).addBox(0.0F, -1.25F, -1.25F, 1.5F, 2.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 3.0F, -0.2618F, -0.6109F, 0.2618F));

		PartDefinition bl_femur = leg_back_left.addOrReplaceChild("bl_femur", CubeListBuilder.create().texOffs(60, 39).addBox(0.0F, -1.0F, -1.0F, 9.5F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 0.0F, 0.0F, 0.1745F, 0.0F, -1.2566F));

		PartDefinition bl_tibia = bl_femur.addOrReplaceChild("bl_tibia", CubeListBuilder.create().texOffs(85, 39).addBox(0.0F, -0.75F, -0.75F, 14.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.5F, 0.0F, 0.0F, 0.0F, 0.0F, 2.4435F));

		PartDefinition bl_claw = bl_tibia.addOrReplaceChild("bl_claw", CubeListBuilder.create().texOffs(116, 44).addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition leg_back_right = body.addOrReplaceChild("leg_back_right", CubeListBuilder.create().texOffs(33, 32).addBox(-1.5F, -1.25F, -1.25F, 1.5F, 2.5F, 2.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 3.0F, -0.2618F, 0.6109F, -0.2618F));

		PartDefinition br_femur = leg_back_right.addOrReplaceChild("br_femur", CubeListBuilder.create().texOffs(0, 44).addBox(-9.5F, -1.0F, -1.0F, 9.5F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.0F, 0.0F, -0.1745F, 0.0F, 1.2566F));

		PartDefinition br_tibia = br_femur.addOrReplaceChild("br_tibia", CubeListBuilder.create().texOffs(25, 44).addBox(-14.0F, -0.75F, -0.75F, 14.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.5F, 0.0F, 0.0F, 0.0F, 0.0F, -2.4435F));

		PartDefinition br_claw = br_tibia.addOrReplaceChild("br_claw", CubeListBuilder.create().texOffs(0, 49).addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

		return LayerDefinition.create(meshdefinition, 128, 64);
	}

	@Override
	public void setupAnim(VoidlingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);

		this.animate(entity.idleAnimationState, VoidlingAnimations.IDLE, ageInTicks);
		this.animate(entity.walkAnimationState, VoidlingAnimations.WALK, ageInTicks);
		this.animate(entity.attackAnimationState, VoidlingAnimations.ATTACK, ageInTicks);

		this.head.yRot += Mth.clamp(netHeadYaw * ((float) Math.PI / 180F) * 0.4F, -0.4F, 0.4F);
		this.head.xRot += Mth.clamp(headPitch * ((float) Math.PI / 180F) * 0.4F, -0.35F, 0.35F);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}
}
