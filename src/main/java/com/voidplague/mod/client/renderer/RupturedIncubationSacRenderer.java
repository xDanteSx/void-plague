package com.voidplague.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.entity.RupturedIncubationSacBlockEntity;
import com.voidplague.mod.world.RupturedIncubationSacBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class RupturedIncubationSacRenderer implements BlockEntityRenderer<RupturedIncubationSacBlockEntity> {
    public static final ResourceLocation RUPTURED_SAC_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            VoidPlagueMod.MOD_ID,
            "textures/entity/ruptured_incubation_sac.png"
    );

    public RupturedIncubationSacRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RupturedIncubationSacBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction facing = Direction.UP;
        if (blockEntity.hasLevel()) {
            BlockState state = blockEntity.getBlockState();
            if (state.hasProperty(RupturedIncubationSacBlock.FACING)) {
                facing = state.getValue(RupturedIncubationSacBlock.FACING);
            }
        }

        // RenderType sin culling para que ambas caras (interior y exterior) se dibujen siempre
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(RUPTURED_SAC_TEXTURE));

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

        // 2. Anclaje y zarcillos de quitina con sellado en la roca
        renderStaticAnchor(poseStack, consumer, packedLight);

        // 3. Copa abierta con bordes dentados y fondo completamente sellado
        poseStack.pushPose();
        poseStack.translate(0.0, -0.11, 0.0);
        poseStack.scale(1.25F, 1.10F, 1.25F);

        renderTornCup(poseStack, consumer, packedLight);

        poseStack.popPose();
        poseStack.popPose();
    }

    private void renderStaticAnchor(PoseStack poseStack, VertexConsumer consumer, int light) {
        PoseStack.Pose pose = poseStack.last();

        float w = 0.125f;
        float h = -0.11f;

        // Tapa superior en Y = 0.0 que sella el anclaje contra el bloque sólido de soporte
        quadUV(consumer, pose, -w, 0.0f, -w,  w, 0.0f, -w,  w, 0.0f,  w, -w, 0.0f,  w, 0.0f, 0.0f, 0.25f, 0.25f, light);
        quadUV(consumer, pose, -w, 0.0f,  w,  w, 0.0f,  w,  w, 0.0f, -w, -w, 0.0f, -w, 0.0f, 0.0f, 0.25f, 0.25f, light);

        // Caras del tallo marchito
        quadUV(consumer, pose, -w, 0.0f,  w,  w, 0.0f,  w,  w, h,  w, -w, h,  w, 0.0f, 0.0f, 0.25f, 0.35f, light);
        quadUV(consumer, pose,  w, 0.0f, -w, -w, 0.0f, -w, -w, h, -w,  w, h, -w, 0.25f, 0.0f, 0.50f, 0.35f, light);
        quadUV(consumer, pose, -w, 0.0f, -w, -w, 0.0f,  w, -w, h,  w, -w, h, -w, 0.50f, 0.0f, 0.75f, 0.35f, light);
        quadUV(consumer, pose,  w, 0.0f,  w,  w, 0.0f, -w,  w, h, -w,  w, h,  w, 0.75f, 0.0f, 1.00f, 0.35f, light);

        // 4 Zarcillos secos pegados a la roca
        float rEnd = 0.34f;
        float rDrop = -0.04f;

        quadUV(consumer, pose,  w, 0.0f, -0.06f,  w, 0.0f,  0.06f,  rEnd, rDrop,  0.04f,  rEnd, rDrop, -0.04f, 0.0f, 0.0f, 0.25f, 0.25f, light);
        quadUV(consumer, pose, -w, 0.0f,  0.06f, -w, 0.0f, -0.06f, -rEnd, rDrop, -0.04f, -rEnd, rDrop,  0.04f, 0.0f, 0.0f, 0.25f, 0.25f, light);
        quadUV(consumer, pose,  0.06f, 0.0f,  w, -0.06f, 0.0f,  w, -0.04f, rDrop,  rEnd,  0.04f, rDrop,  rEnd, 0.25f, 0.0f, 0.50f, 0.25f, light);
        quadUV(consumer, pose, -0.06f, 0.0f, -w,  0.06f, 0.0f, -w,  0.04f, rDrop, -rEnd, -0.04f, rDrop, -rEnd, 0.25f, 0.0f, 0.50f, 0.25f, light);
    }

    private void renderTornCup(PoseStack poseStack, VertexConsumer consumer, int light) {
        PoseStack.Pose pose = poseStack.last();

        int segments = 8;
        float[] cos = new float[segments + 1];
        float[] sin = new float[segments + 1];

        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2.0 * Math.PI / segments);
            cos[i] = Mth.cos(angle);
            sin[i] = Mth.sin(angle);
        }

        // Tiers de la copa rasgada:
        // Tier 0: Cuello superior (y = 0.0, radio 0.125)
        // Tier 1: Hombro dilatado (y = -0.10, radio 0.23)
        // Tier 2: Borde dentado y desgarrado hacia afuera (y = -0.24 a -0.32, radio 0.28)
        float y0 = 0.0f, r0 = 0.125f;
        float y1 = -0.10f, r1 = 0.23f;

        // =========================================================================
        // TAPA DE FONDO DE LA COPA (Sella la cavidad interna con tejido necrótico #0A0410)
        // =========================================================================
        // Se dibuja un abanico horizontal que cierra la base interna en y1 = -0.10f
        // impidiendo ver el bloque de soporte a través de la cavidad hueca.
        for (int i = 0; i < segments; i++) {
            float c1 = cos[i], s1 = sin[i];
            float c2 = cos[i + 1], s2 = sin[i + 1];
            float u1 = 0.75f + 0.20f * (float) i / segments;
            float u2 = 0.75f + 0.20f * (float) (i + 1) / segments;

            quadUV(consumer, pose,
                    0.0f, y1, 0.0f,
                    c1 * r1 * 0.95f, y1, s1 * r1 * 0.95f,
                    c2 * r1 * 0.95f, y1, s2 * r1 * 0.95f,
                    0.0f, y1, 0.0f,
                    u1, 0.75f, u2, 0.95f, light);
        }

        for (int i = 0; i < segments; i++) {
            float c1 = cos[i], s1 = sin[i];
            float c2 = cos[i + 1], s2 = sin[i + 1];

            float u1 = (float) i / segments;
            float u2 = (float) (i + 1) / segments;

            // Variación irregular en la altura del borde desgarrado
            float tearY1 = -0.22f - 0.08f * (float) Math.abs(Math.sin(i * 2.1));
            float tearY2 = -0.22f - 0.08f * (float) Math.abs(Math.sin((i + 1) * 2.1));
            float rTear1 = 0.27f + 0.03f * (float) Math.cos(i * 1.5);
            float rTear2 = 0.27f + 0.03f * (float) Math.cos((i + 1) * 1.5);

            // Tier 0 -> 1: Cuello a hombro exterior
            quadUV(consumer, pose,
                    c1 * r0, y0, s1 * r0,
                    c2 * r0, y0, s2 * r0,
                    c2 * r1, y1, s2 * r1,
                    c1 * r1, y1, s1 * r1,
                    u1, 0.35f, u2, 0.50f, light);

            // Tier 1 -> 2: Hombro a labios dentados y desgarrados
            quadUV(consumer, pose,
                    c1 * r1, y1, s1 * r1,
                    c2 * r1, y1, s2 * r1,
                    c2 * rTear2, tearY2, s2 * rTear2,
                    c1 * rTear1, tearY1, s1 * rTear1,
                    u1, 0.50f, u2, 0.72f, light);

            // Cavidad interior visible (Caras invertidas hacia adentro en color coagulado oscuro)
            quadUV(consumer, pose,
                    c2 * r1 * 0.95f, y1, s2 * r1 * 0.95f,
                    c1 * r1 * 0.95f, y1, s1 * r1 * 0.95f,
                    c1 * rTear1 * 0.95f, tearY1, s1 * rTear1 * 0.95f,
                    c2 * rTear2 * 0.95f, tearY2, s2 * rTear2 * 0.95f,
                    u1, 0.75f, u2, 0.95f, light);

            // Colgajos triangulares rasgados que cuelgan del borde
            if (i % 2 == 0) {
                float flapTipY = tearY1 - 0.07f;
                float midC = (c1 + c2) * 0.5f;
                float midS = (s1 + s2) * 0.5f;
                float midR = (rTear1 + rTear2) * 0.48f;

                quadUV(consumer, pose,
                        c1 * rTear1, tearY1, s1 * rTear1,
                        c2 * rTear2, tearY2, s2 * rTear2,
                        midC * midR, flapTipY, midS * midR,
                        midC * midR, flapTipY, midS * midR,
                        u1, 0.65f, u2, 0.85f, light);
            }
        }
    }

    private void quadUV(VertexConsumer consumer, PoseStack.Pose pose,
                        float x1, float y1, float z1,
                        float x2, float y2, float z2,
                        float x3, float y3, float z3,
                        float x4, float y4, float z4,
                        float u1, float v1, float u2, float v2,
                        int light) {
        consumer.addVertex(pose, x1, y1, z1).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, x2, y2, z2).setColor(255, 255, 255, 255).setUv(u2, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, x3, y3, z3).setColor(255, 255, 255, 255).setUv(u2, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, x4, y4, z4).setColor(255, 255, 255, 255).setUv(u1, v2).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
    }
}
