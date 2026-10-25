package com.voidplague.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.entity.IncubationSacBlockEntity;
import com.voidplague.mod.world.IncubationSacBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class IncubationSacRenderer implements BlockEntityRenderer<IncubationSacBlockEntity> {
    public static final ResourceLocation SAC_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            VoidPlagueMod.MOD_ID,
            "textures/entity/incubation_sac.png"
    );

    public IncubationSacRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(IncubationSacBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Direction facing = Direction.UP;

        if (blockEntity.hasLevel()) {
            BlockState state = blockEntity.getBlockState();
            if (state.hasProperty(IncubationSacBlock.FACING)) {
                facing = state.getValue(IncubationSacBlock.FACING);
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

        // 2. Anclaje y zarcillos estáticos pegados a la roca
        renderStaticAnchor(poseStack, consumer, packedLight);

        // 3. Bulbo con latido orgánico
        poseStack.pushPose();
        poseStack.translate(0.0, -0.11, 0.0);

        float pulse = Mth.sin(time * 0.06F);
        float scaleY = 1.0F + 0.08F * pulse;
        float scaleXZ = 1.0F - 0.04F * pulse;
        // Escala aplicada a toda la matriz del bulbo (incluyendo al embrión para evitar clipping)
        poseStack.scale(1.30F * scaleXZ, 1.25F * scaleY, 1.30F * scaleXZ);

        // A. EMBRIÓN DENTRO DE LA MATRIZ ESCALADA (centrado estricto y escala idéntica)
        renderInternalEmbryo(poseStack, consumer, packedLight, time);

        // B. CÁPSULA EXTERIOR TRANSLÚCIDA
        renderPulsingBulb(poseStack, consumer, packedLight);

        // C. PÚSTULAS SECUNDARIAS ASIMÉTRICAS
        renderAsymmetricalPustules(poseStack, consumer, packedLight, false);

        poseStack.popPose();
        poseStack.popPose();
    }

    public static void renderInternalEmbryo(PoseStack poseStack, VertexConsumer consumer, int light, float time) {
        // Oscilación fetal reducida estrictamente a máx 0.02F y rotación lenta
        float embryoWiggle = Mth.sin(time * 0.04F) * 0.02F;
        float embryoRot = (time * 0.3F) % 360.0F;

        poseStack.pushPose();
        // Centrado estrictamente en el centro de masa del saco: Y = -0.22F
        poseStack.translate(0.0F, -0.22F + embryoWiggle, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(embryoRot));

        // Dimensiones del prisma fetal: 2x3x2 píxeles (w=2/16=0.125 -> hw=0.0625, h=3/16=0.1875 -> topH=0.09375, botH=-0.09375)
        float hw = 0.0625f;
        float topH = 0.09375f;
        float botH = -0.09375f;

        PoseStack.Pose pose = poseStack.last();
        // Patch UV del embrión: u: [0.75, 1.0], v: [0.0, 0.25] (Negro azabache abisal #05020A)
        float u1 = 0.75f, u2 = 1.00f;
        float v1 = 0.00f, v2 = 0.25f;

        quadUV(consumer, pose, -hw, topH, -hw,  hw, topH, -hw,  hw, botH, -hw, -hw, botH, -hw, u1, v1, u2, v2, light);
        quadUV(consumer, pose,  hw, topH,  hw, -hw, topH,  hw, -hw, botH,  hw,  hw, botH,  hw, u1, v1, u2, v2, light);
        quadUV(consumer, pose,  hw, topH, -hw,  hw, topH,  hw,  hw, botH,  hw,  hw, botH, -hw, u1, v1, u2, v2, light);
        quadUV(consumer, pose, -hw, topH,  hw, -hw, topH, -hw, -hw, botH, -hw, -hw, botH,  hw, u1, v1, u2, v2, light);
        quadUV(consumer, pose, -hw, topH,  hw,  hw, topH,  hw,  hw, topH, -hw, -hw, topH, -hw, u1, v1, u2, v2, light);
        quadUV(consumer, pose, -hw, botH, -hw,  hw, botH, -hw,  hw, botH,  hw, -hw, botH,  hw, u1, v1, u2, v2, light);

        poseStack.popPose();
    }

    public static void renderAsymmetricalPustules(PoseStack poseStack, VertexConsumer consumer, int light, boolean flip) {
        PoseStack.Pose pose = poseStack.last();
        float sign = flip ? -1.0f : 1.0f;

        // Pústula 1 (superior): 2x2 px en el hombro del bulbo (y = -0.16, x = sign * 0.22)
        float px1 = sign * 0.21f;
        float py1 = -0.16f;
        float pz1 = 0.07f;
        float pr1 = 0.055f;

        renderBlisterBox(consumer, pose, px1, py1, pz1, pr1, light);

        // Pústula 2 (inferior): 2x2 px en el vientre lateral bajo (y = -0.30, x = sign * 0.24)
        float px2 = sign * 0.23f;
        float py2 = -0.30f;
        float pz2 = -0.05f;
        float pr2 = 0.050f;

        renderBlisterBox(consumer, pose, px2, py2, pz2, pr2, light);
    }

    private static void renderBlisterBox(VertexConsumer consumer, PoseStack.Pose pose, float cx, float cy, float cz, float r, int light) {
        float u1 = 0.35f, u2 = 0.50f;
        float v1 = 0.40f, v2 = 0.55f;

        quadUV(consumer, pose, cx - r, cy + r, cz - r, cx + r, cy + r, cz - r, cx + r, cy - r, cz - r, cx - r, cy - r, cz - r, u1, v1, u2, v2, light);
        quadUV(consumer, pose, cx + r, cy + r, cz + r, cx - r, cy + r, cz + r, cx - r, cy - r, cz + r, cx + r, cy - r, cz + r, u1, v1, u2, v2, light);
        quadUV(consumer, pose, cx + r, cy + r, cz - r, cx + r, cy + r, cz + r, cx + r, cy - r, cz + r, cx + r, cy - r, cz - r, u1, v1, u2, v2, light);
        quadUV(consumer, pose, cx - r, cy + r, cz + r, cx - r, cy + r, cz - r, cx - r, cy - r, cz - r, cx - r, cy - r, cz + r, u1, v1, u2, v2, light);
        quadUV(consumer, pose, cx - r, cy + r, cz + r, cx + r, cy + r, cz + r, cx + r, cy + r, cz - r, cx - r, cy + r, cz - r, u1, v1, u2, v2, light);
        quadUV(consumer, pose, cx - r, cy - r, cz - r, cx + r, cy - r, cz - r, cx + r, cy - r, cz + r, cx - r, cy - r, cz + r, u1, v1, u2, v2, light);
    }

    public static void renderStaticAnchor(PoseStack poseStack, VertexConsumer consumer, int light) {
        PoseStack.Pose pose = poseStack.last();

        float w = 0.125f;
        float h = -0.11f;

        // Tapa superior en Y = 0.0 que sella el anclaje contra el bloque sólido de soporte
        quadUV(consumer, pose, -w, 0.0f, -w,  w, 0.0f, -w,  w, 0.0f,  w, -w, 0.0f,  w, 0.0f, 0.0f, 0.25f, 0.25f, light);
        quadUV(consumer, pose, -w, 0.0f,  w,  w, 0.0f,  w,  w, 0.0f, -w, -w, 0.0f, -w, 0.0f, 0.0f, 0.25f, 0.25f, light);

        // Tallo central corto
        quadUV(consumer, pose, -w, 0.0f,  w,  w, 0.0f,  w,  w, h,  w, -w, h,  w, 0.0f, 0.0f, 0.25f, 0.35f, light);
        quadUV(consumer, pose,  w, 0.0f, -w, -w, 0.0f, -w, -w, h, -w,  w, h, -w, 0.25f, 0.0f, 0.50f, 0.35f, light);
        quadUV(consumer, pose, -w, 0.0f, -w, -w, 0.0f,  w, -w, h,  w, -w, h, -w, 0.50f, 0.0f, 0.75f, 0.35f, light);
        quadUV(consumer, pose,  w, 0.0f,  w,  w, 0.0f, -w,  w, h, -w,  w, h,  w, 0.75f, 0.0f, 1.00f, 0.35f, light);

        // 4 Zarcillos abiertos en abanico
        float rEnd = 0.34f;
        float rDrop = -0.04f;

        quadUV(consumer, pose,  w, 0.0f, -0.06f,  w, 0.0f,  0.06f,  rEnd, rDrop,  0.04f,  rEnd, rDrop, -0.04f, 0.0f, 0.0f, 0.25f, 0.25f, light);
        quadUV(consumer, pose, -w, 0.0f,  0.06f, -w, 0.0f, -0.06f, -rEnd, rDrop, -0.04f, -rEnd, rDrop,  0.04f, 0.0f, 0.0f, 0.25f, 0.25f, light);
        quadUV(consumer, pose,  0.06f, 0.0f,  w, -0.06f, 0.0f,  w, -0.04f, rDrop,  rEnd,  0.04f, rDrop,  rEnd, 0.25f, 0.0f, 0.50f, 0.25f, light);
        quadUV(consumer, pose, -0.06f, 0.0f, -w,  0.06f, 0.0f, -w,  0.04f, rDrop, -rEnd, -0.04f, rDrop, -rEnd, 0.25f, 0.0f, 0.50f, 0.25f, light);
    }

    public static void renderPulsingBulb(PoseStack poseStack, VertexConsumer consumer, int light) {
        PoseStack.Pose pose = poseStack.last();

        int segments = 8;
        float[] cos = new float[segments + 1];
        float[] sin = new float[segments + 1];

        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2.0 * Math.PI / segments);
            cos[i] = Mth.cos(angle);
            sin[i] = Mth.sin(angle);
        }

        float y0 = 0.0f,    r0 = 0.125f;
        float y1 = -0.10f,  r1 = 0.23f;
        float y2 = -0.32f,  r2 = 0.27f;
        float y3 = -0.52f,  r3 = 0.18f;
        float y4 = -0.625f, r4 = 0.0f;

        for (int i = 0; i < segments; i++) {
            float c1 = cos[i], s1 = sin[i];
            float c2 = cos[i + 1], s2 = sin[i + 1];

            float u1 = (float) i / segments;
            float u2 = (float) (i + 1) / segments;

            // Tier 0 -> 1: Cuello superior a hombro
            quadUV(consumer, pose,
                    c1 * r0, y0, s1 * r0,
                    c2 * r0, y0, s2 * r0,
                    c2 * r1, y1, s2 * r1,
                    c1 * r1, y1, s1 * r1,
                    u1, 0.38f, u2, 0.50f, light);

            // Tier 1 -> 2: Hombro a vientre máximo
            quadUV(consumer, pose,
                    c1 * r1, y1, s1 * r1,
                    c2 * r1, y1, s2 * r1,
                    c2 * r2, y2, s2 * r2,
                    c1 * r2, y2, s1 * r2,
                    u1, 0.50f, u2, 0.72f, light);

            // Tier 2 -> 3: Vientre máximo a cintura inferior
            quadUV(consumer, pose,
                    c1 * r2, y2, s1 * r2,
                    c2 * r2, y2, s2 * r2,
                    c2 * r3, y3, s2 * r3,
                    c1 * r3, y3, s1 * r3,
                    u1, 0.72f, u2, 0.88f, light);

            // Tier 3 -> 4: Punta cónica inferior
            quadUV(consumer, pose,
                    c1 * r3, y3, s1 * r3,
                    c2 * r3, y3, s2 * r3,
                    c2 * r4, y4, s2 * r4,
                    c1 * r4, y4, s1 * r4,
                    u1, 0.88f, u2, 1.00f, light);
        }
    }

    public static void quadUV(VertexConsumer consumer, PoseStack.Pose pose,
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
