package com.voidplague.mod.client.model;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.entity.VoidWatcherEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class VoidWatcherModel extends HierarchicalModel<VoidWatcherEntity> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "void_watcher"), "main");

    private final ModelPart root;
    private final ModelPart eye;
    private final ModelPart eyelidTop;
    private final ModelPart eyelidBottom;

    public VoidWatcherModel(ModelPart root) {
        this.root = root;
        this.eye = root.getChild("eye");
        ModelPart tumor = root.getChild("tumor");
        ModelPart orbit = tumor.getChild("orbit");
        this.eyelidTop = orbit.getChild("eyelid_top");
        this.eyelidBottom = orbit.getChild("eyelid_bottom");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // BASE TUMORAL ADHERIDA AL MURO (Caja 12x12x3)
        PartDefinition tumor = root.addOrReplaceChild("tumor", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-6.0F, -6.0F, -8.0F, 12.0F, 12.0F, 3.0F),
                PartPose.ZERO);

        // Zarcillos y raíces de quitina que se clavan en la pared
        tumor.addOrReplaceChild("root_ul", CubeListBuilder.create().texOffs(0, 15).addBox(-8.0F, 3.0F, -7.0F, 4.0F, 5.0F, 2.0F), PartPose.ZERO);
        tumor.addOrReplaceChild("root_dl", CubeListBuilder.create().texOffs(12, 15).addBox(-7.0F, -8.0F, -7.0F, 3.0F, 4.0F, 2.0F), PartPose.ZERO);
        tumor.addOrReplaceChild("root_dr", CubeListBuilder.create().texOffs(24, 15).addBox(4.0F, -7.0F, -6.0F, 4.0F, 4.0F, 2.0F), PartPose.ZERO);
        tumor.addOrReplaceChild("root_ur", CubeListBuilder.create().texOffs(36, 15).addBox(4.0F, 3.0F, -7.0F, 4.0F, 3.0F, 2.0F), PartPose.ZERO);

        // CUENCA Y PÁRPADOS RASGADOS (Almendra depredadora que enmarca el ojo)
        PartDefinition orbit = tumor.addOrReplaceChild("orbit", CubeListBuilder.create(), PartPose.ZERO);
        orbit.addOrReplaceChild("eyelid_top", CubeListBuilder.create().texOffs(0, 22).addBox(-5.0F, 2.0F, -4.5F, 10.0F, 3.0F, 2.0F), PartPose.ZERO);
        orbit.addOrReplaceChild("eyelid_bottom", CubeListBuilder.create().texOffs(0, 27).addBox(-5.0F, -5.0F, -4.5F, 10.0F, 3.0F, 2.0F), PartPose.ZERO);
        orbit.addOrReplaceChild("eyelid_left", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, -3.0F, -4.5F, 2.0F, 6.0F, 2.0F), PartPose.ZERO);
        orbit.addOrReplaceChild("eyelid_right", CubeListBuilder.create().texOffs(8, 32).addBox(4.0F, -3.0F, -4.5F, 2.0F, 6.0F, 2.0F), PartPose.ZERO);

        // GLOBO OCULAR INDEPENDIENTE (Pivota sobre (0, 0, -4.0F))
        root.addOrReplaceChild("eye", CubeListBuilder.create()
                .texOffs(0, 42)
                .addBox(-2.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F),
                PartPose.offset(0.0F, 0.0F, -4.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(VoidWatcherEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // El globo ocular sigue nativamente la mirada calculada por Minecraft
        this.eye.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.eye.xRot = headPitch * ((float) Math.PI / 180F);

        // Clamping de seguridad para no girar hacia el interior de la pared
        this.eye.yRot = Mth.clamp(this.eye.yRot, (float) -Math.toRadians(55.0), (float) Math.toRadians(55.0));
        this.eye.xRot = Mth.clamp(this.eye.xRot, (float) -Math.toRadians(35.0), (float) Math.toRadians(35.0));

        // Párpados orgánicos animados
        float blinkCycle = ageInTicks % 120.0F;
        if (blinkCycle < 6.0F) {
            float blink = Mth.sin(blinkCycle / 6.0F * (float) Math.PI) * 0.8F;
            this.eyelidTop.y = blink;
            this.eyelidBottom.y = -blink;
        } else {
            this.eyelidTop.y = 0.0F;
            this.eyelidBottom.y = 0.0F;
        }
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
