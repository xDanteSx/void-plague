package com.voidplague.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.entity.ClusterIncubationSacBlockEntity;
import com.voidplague.mod.world.ClusterIncubationSacBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ClusterIncubationSacRenderer implements BlockEntityRenderer<ClusterIncubationSacBlockEntity> {
    public static final ResourceLocation SAC_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            VoidPlagueMod.MOD_ID,
            "textures/entity/incubation_sac.png"
    );

    public ClusterIncubationSacRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ClusterIncubationSacBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction facing = Direction.UP;
        if (blockEntity.hasLevel()) {
            BlockState state = blockEntity.getBlockState();
            if (state.hasProperty(ClusterIncubationSacBlock.FACING)) {
                facing = state.getValue(ClusterIncubationSacBlock.FACING);
            }
        }

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(SAC_TEXTURE));

        poseStack.pushPose();

        // 1. Alineación direccional según cara de soporte
        switch (facing) {
            case UP -> poseStack.translate(0.5, 1.0, 0.5);
            case DOWN -> {
                poseStack.translate(0.5, 0.0, 0.5);
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
            }
            case NORTH -> {
                poseStack.translate(0.5, 0.5, 0.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
            }
            case SOUTH -> {
                poseStack.translate(0.5, 0.5, 1.0);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
            }
            case WEST -> {
                poseStack.translate(0.0, 0.5, 0.5);
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
            }
            case EAST -> {
                poseStack.translate(1.0, 0.5, 0.5);
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            }
        }

        long gameTime = blockEntity.hasLevel() ? blockEntity.getLevel().getGameTime() : 0;
        float time = gameTime + partialTick;

        // 2. Anclaje bifurcado en "Y" divergente con sellado sólido en Y = 0
        renderClusterAnchor(poseStack, consumer, packedLight);

        // 3. Rama A (Saco Principal - Escala 1.0F):
        // Traslación horizontal (-0.20F, -0.11F, -0.06F) e inclinación de -10° en Z
        poseStack.pushPose();
        poseStack.translate(-0.20F, -0.11F, -0.06F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-10.0F));

        float pulse1 = Mth.sin(time * 0.06F);
        float scaleY1 = 1.0F + 0.08F * pulse1;
        float scaleXZ1 = 1.0F - 0.04F * pulse1;
        poseStack.scale(1.20F * scaleXZ1, 1.15F * scaleY1, 1.20F * scaleXZ1);

        // Embrión A centrado en el saco principal
        IncubationSacRenderer.renderInternalEmbryo(poseStack, consumer, packedLight, time);
        IncubationSacRenderer.renderPulsingBulb(poseStack, consumer, packedLight);
        IncubationSacRenderer.renderAsymmetricalPustules(poseStack, consumer, packedLight, false);
        poseStack.popPose();

        // 4. Rama B (Saco Secundario - Escala 0.75F):
        // Traslación horizontal opuesta (+0.22F, -0.07F, +0.06F) e inclinación de +20° en Z
        // Separación total entre centros de ~0.42 bloques
        poseStack.pushPose();
        poseStack.translate(0.22F, -0.07F, 0.06F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(20.0F));

        float pulse2 = Mth.sin(time * 0.06F + 1.5F);
        float scaleY2 = 1.0F + 0.08F * pulse2;
        float scaleXZ2 = 1.0F - 0.04F * pulse2;
        // Escala 0.75F del principal: 1.20 * 0.75 = 0.90F, 1.15 * 0.75 = 0.86F
        poseStack.scale(0.90F * scaleXZ2, 0.86F * scaleY2, 0.90F * scaleXZ2);

        // Embrión B centrado en el saco secundario
        IncubationSacRenderer.renderInternalEmbryo(poseStack, consumer, packedLight, time + 45.0F);
        IncubationSacRenderer.renderPulsingBulb(poseStack, consumer, packedLight);
        IncubationSacRenderer.renderAsymmetricalPustules(poseStack, consumer, packedLight, true);
        poseStack.popPose();

        poseStack.popPose();
    }

    private void renderClusterAnchor(PoseStack poseStack, VertexConsumer consumer, int light) {
        PoseStack.Pose pose = poseStack.last();

        float w = 0.14f;
        float splitY = -0.04f;

        // Tapa superior sólida en Y = 0.0 que sella el tronco contra el bloque de soporte
        IncubationSacRenderer.quadUV(consumer, pose, -w, 0.0f, -w,  w, 0.0f, -w,  w, 0.0f,  w, -w, 0.0f,  w, 0.0f, 0.0f, 0.25f, 0.25f, light);
        IncubationSacRenderer.quadUV(consumer, pose, -w, 0.0f,  w,  w, 0.0f,  w,  w, 0.0f, -w, -w, 0.0f, -w, 0.0f, 0.0f, 0.25f, 0.25f, light);

        // Tronco común corto (Y: 0.0 -> splitY = -0.04)
        IncubationSacRenderer.quadUV(consumer, pose, -w, 0.0f,  w,  w, 0.0f,  w,  w, splitY,  w, -w, splitY,  w, 0.0f, 0.0f, 0.25f, 0.30f, light);
        IncubationSacRenderer.quadUV(consumer, pose,  w, 0.0f, -w, -w, 0.0f, -w, -w, splitY, -w,  w, splitY, -w, 0.25f, 0.0f, 0.50f, 0.30f, light);
        IncubationSacRenderer.quadUV(consumer, pose, -w, 0.0f, -w, -w, 0.0f,  w, -w, splitY,  w, -w, splitY, -w, 0.50f, 0.0f, 0.75f, 0.30f, light);
        IncubationSacRenderer.quadUV(consumer, pose,  w, 0.0f,  w,  w, 0.0f, -w,  w, splitY, -w,  w, splitY,  w, 0.75f, 0.0f, 1.00f, 0.30f, light);

        // Rama A bifurcada divergente hacia (-0.20F, -0.11F, -0.06F)
        float targetAx = -0.20f, targetAy = -0.11f, targetAz = -0.06f;
        float bw1 = 0.07f;
        IncubationSacRenderer.quadUV(consumer, pose,
                -bw1, splitY, -bw1 + targetAz * 0.3f,
                0.0f, splitY, bw1 + targetAz * 0.3f,
                targetAx + bw1, targetAy, targetAz + bw1,
                targetAx - bw1, targetAy, targetAz - bw1,
                0.0f, 0.0f, 0.25f, 0.35f, light);
        IncubationSacRenderer.quadUV(consumer, pose,
                0.0f, splitY, bw1 + targetAz * 0.3f,
                bw1, splitY, -bw1 + targetAz * 0.3f,
                targetAx + bw1, targetAy, targetAz - bw1,
                targetAx - bw1, targetAy, targetAz + bw1,
                0.25f, 0.0f, 0.50f, 0.35f, light);

        // Rama B bifurcada divergente hacia (+0.22F, -0.07F, +0.06F)
        float targetBx = 0.22f, targetBy = -0.07f, targetBz = 0.06f;
        float bw2 = 0.06f;
        IncubationSacRenderer.quadUV(consumer, pose,
                0.0f, splitY, -bw2 + targetBz * 0.3f,
                bw2, splitY, bw2 + targetBz * 0.3f,
                targetBx + bw2, targetBy, targetBz + bw2,
                targetBx - bw2, targetBy, targetBz - bw2,
                0.50f, 0.0f, 0.75f, 0.35f, light);
        IncubationSacRenderer.quadUV(consumer, pose,
                bw2, splitY, bw2 + targetBz * 0.3f,
                -bw2, splitY, -bw2 + targetBz * 0.3f,
                targetBx - bw2, targetBy, targetBz + bw2,
                targetBx + bw2, targetBy, targetBz - bw2,
                0.75f, 0.0f, 1.00f, 0.35f, light);

        // Zarcillos de agarre abiertos en abanico pegados a la roca
        float rEnd = 0.40f;
        float rDrop = -0.04f;
        IncubationSacRenderer.quadUV(consumer, pose,  w, 0.0f, -0.07f,  w, 0.0f,  0.07f,  rEnd, rDrop,  0.05f,  rEnd, rDrop, -0.05f, 0.0f, 0.0f, 0.25f, 0.25f, light);
        IncubationSacRenderer.quadUV(consumer, pose, -w, 0.0f,  0.07f, -w, 0.0f, -0.07f, -rEnd, rDrop, -0.05f, -rEnd, rDrop,  0.05f, 0.0f, 0.0f, 0.25f, 0.25f, light);
        IncubationSacRenderer.quadUV(consumer, pose,  0.07f, 0.0f,  w, -0.07f, 0.0f,  w, -0.05f, rDrop,  rEnd,  0.05f, rDrop,  rEnd, 0.25f, 0.0f, 0.50f, 0.25f, light);
        IncubationSacRenderer.quadUV(consumer, pose, -0.07f, 0.0f, -w,  0.07f, 0.0f, -w,  0.05f, rDrop, -rEnd, -0.05f, rDrop, -rEnd, 0.25f, 0.0f, 0.50f, 0.25f, light);
    }
}
