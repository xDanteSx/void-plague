package com.voidplague.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.entity.GraspingLimbBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class GraspingLimbRenderer implements BlockEntityRenderer<GraspingLimbBlockEntity> {
    public static final ResourceLocation LIMB_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            VoidPlagueMod.MOD_ID,
            "textures/block/grasping_limb.png"
    );

    public GraspingLimbRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(GraspingLimbBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float time = blockEntity.getAnimTime();
        boolean nearby = blockEntity.isPlayerNearby();
        float reach = blockEntity.getReachProgress();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(LIMB_TEXTURE));

        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);

        // =========================================================================
        // 1. BASE: MASA TUMULAR DE CARNE DESGARRADA EN EL SUELO (box 3, 0, 3, 13, 2, 13)
        // =========================================================================
        renderFleshBase(poseStack, consumer, packedLight);

        // =========================================================================
        // 2. BRAZO/ANTEBRAZO: PLACAS ÓSEAS (#5A5463) CON HACES DE TENDONES (#3B144A)
        // =========================================================================
        poseStack.pushPose();
        poseStack.translate(0.0, 0.125, 0.0);

        if (nearby || reach > 0.01f) {
            float targetDeg = (float) Math.toDegrees(blockEntity.getTargetAngle());
            // Smoothly rotate towards player
            poseStack.mulPose(Axis.YP.rotationDegrees(targetDeg));
            // Lean forward aggressively towards player
            float forwardLean = 22.0f + 25.0f * reach;
            poseStack.mulPose(Axis.XP.rotationDegrees(forwardLean));
            // Spasmodic micro-twitching
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(time * 2.0f) * 6.0f));
        } else {
            // At rest: Slow spasms (angle = Mth.sin(gameTime * 0.05F) * 8.0F)
            float restSway = Mth.sin(time) * 8.0f;
            poseStack.mulPose(Axis.XP.rotationDegrees(14.0f + Mth.cos(time * 0.8f) * 4.0f));
            poseStack.mulPose(Axis.YP.rotationDegrees(restSway));
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(time * 0.5f) * 5.0f));
        }

        // Render Forearm (10 pixels high = 0.625 blocks)
        renderForearmSegment(poseStack, consumer, 0.625f, packedLight);

        // =========================================================================
        // 3. MUÑECA Y MANO: CARPO ARTICULADO Y 4 DEDOS LARGOS COMO AGUJAS
        // =========================================================================
        poseStack.translate(0.0, 0.625, 0.0);

        // Wrist joint articulation
        float wristTwitch = nearby ? Mth.sin(time * 3.0f) * 12.0f : Mth.sin(time * 1.2f) * 5.0f;
        poseStack.mulPose(Axis.ZP.rotationDegrees(wristTwitch));

        renderWristJoint(poseStack, consumer, packedLight);

        // 4 Claw fingers
        poseStack.translate(0.0, 0.08, 0.0);

        // Dynamic clawing animation
        float clawGrasp;
        if (nearby) {
            // Rapid clawing spasms: opening fully and clutching furiously
            clawGrasp = (Mth.sin(time * 3.5f) * 0.5f + 0.5f) * 65.0f - 10.0f;
        } else {
            // Gentle resting curl with periodic twitch
            clawGrasp = 20.0f + Mth.sin(time * 1.5f) * 8.0f;
        }

        float[] yawOffsets = { -45.0f, -15.0f, 15.0f, 45.0f };
        float[] lengths    = {  0.42f,  0.50f,  0.52f, 0.44f };

        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(yawOffsets[i]));
            // Inward curl
            poseStack.mulPose(Axis.XP.rotationDegrees(clawGrasp + (i % 2 == 0 ? 6.0f : 0.0f)));
            renderClawFinger(poseStack, consumer, lengths[i], packedLight);
            poseStack.popPose();
        }

        poseStack.popPose();
        poseStack.popPose();
    }

    private void renderFleshBase(PoseStack poseStack, VertexConsumer consumer, int light) {
        PoseStack.Pose pose = poseStack.last();
        // Base plate: torn flesh (#2E143D -> 46, 20, 61), deep shadow (#160824 -> 22, 8, 36)
        int fr = 46, fg = 20, fb = 61;
        int dr = 22, dg = 8, db = 36;

        float minX = -0.3125f; // 3 pixels from edge
        float maxX =  0.3125f;
        float minZ = -0.3125f;
        float maxZ =  0.3125f;
        float y    =  0.125f;  // 2 pixels high

        // Top irregular surface
        quad(consumer, pose, minX, y, maxZ, maxX, y, maxZ, maxX, y, minZ, minX, y, minZ, fr, fg, fb, light);
        // North face
        quad(consumer, pose, minX, 0.0f, minZ, maxX, 0.0f, minZ, maxX, y, minZ, minX, y, minZ, dr, dg, db, light);
        // South face
        quad(consumer, pose, maxX, 0.0f, maxZ, minX, 0.0f, maxZ, minX, y, maxZ, maxX, y, maxZ, dr, dg, db, light);
        // West face
        quad(consumer, pose, minX, 0.0f, maxZ, minX, 0.0f, minZ, minX, y, minZ, minX, y, maxZ, dr, dg, db, light);
        // East face
        quad(consumer, pose, maxX, 0.0f, minZ, maxX, 0.0f, maxZ, maxX, y, maxZ, maxX, y, minZ, dr, dg, db, light);
    }

    private void renderForearmSegment(PoseStack poseStack, VertexConsumer consumer, float height, int light) {
        PoseStack.Pose pose = poseStack.last();
        // Bone plates of calcified chitin: #5A5463 (90, 84, 99)
        int br = 90, bg = 84, bb = 99;
        // Tendons inside: #3B144A (59, 20, 74)
        int tr = 59, tg = 20, tb = 74;

        float hw = 0.075f; // Half width
        float hd = 0.075f; // Half depth

        // 1. Anterior Bone Plate (Front splint)
        float zFront = hd;
        float zFrontIn = hd - 0.035f;
        quad(consumer, pose, -hw, 0.0f, zFront,  hw, 0.0f, zFront,  hw * 0.8f, height, zFront * 0.8f, -hw * 0.8f, height, zFront * 0.8f, br, bg, bb, light);
        quad(consumer, pose,  hw, 0.0f, zFrontIn, -hw, 0.0f, zFrontIn, -hw * 0.8f, height, zFrontIn * 0.8f,  hw * 0.8f, height, zFrontIn * 0.8f, (int)(br * 0.8f), (int)(bg * 0.8f), (int)(bb * 0.8f), light);

        // 2. Posterior Bone Plate (Back splint)
        float zBack = -hd;
        float zBackIn = -hd + 0.035f;
        quad(consumer, pose,  hw, 0.0f, zBack, -hw, 0.0f, zBack, -hw * 0.8f, height, zBack * 0.8f,  hw * 0.8f, height, zBack * 0.8f, br, bg, bb, light);
        quad(consumer, pose, -hw, 0.0f, zBackIn,  hw, 0.0f, zBackIn,  hw * 0.8f, height, zBackIn * 0.8f, -hw * 0.8f, height, zBackIn * 0.8f, (int)(br * 0.8f), (int)(bg * 0.8f), (int)(bb * 0.8f), light);

        // 3. Exposed Tendon Bundles running up between the two plates
        float tw = hw * 0.55f;
        quad(consumer, pose, -tw, 0.0f, -0.02f,  tw, 0.0f, -0.02f,  tw * 0.8f, height, -0.02f, -tw * 0.8f, height, -0.02f, tr, tg, tb, light);
        quad(consumer, pose,  tw, 0.0f,  0.02f, -tw, 0.0f,  0.02f, -tw * 0.8f, height,  0.02f,  tw * 0.8f, height,  0.02f, tr, tg, tb, light);
        quad(consumer, pose, -tw, 0.0f,  0.02f, -tw, 0.0f, -0.02f, -tw * 0.8f, height, -0.02f, -tw * 0.8f, height,  0.02f, (int)(tr * 0.9f), (int)(tg * 0.9f), (int)(tb * 0.9f), light);
        quad(consumer, pose,  tw, 0.0f, -0.02f,  tw, 0.0f,  0.02f,  tw * 0.8f, height,  0.02f,  tw * 0.8f, height, -0.02f, (int)(tr * 0.9f), (int)(tg * 0.9f), (int)(tb * 0.9f), light);
    }

    private void renderWristJoint(PoseStack poseStack, VertexConsumer consumer, int light) {
        PoseStack.Pose pose = poseStack.last();
        // Articulated carpus knot
        int br = 80, bg = 75, bb = 88;
        float r = 0.065f;
        float h = 0.08f;

        quad(consumer, pose, -r, 0.0f,  r,  r, 0.0f,  r,  r, h,  r, -r, h,  r, br, bg, bb, light);
        quad(consumer, pose,  r, 0.0f, -r, -r, 0.0f, -r, -r, h, -r,  r, h, -r, br, bg, bb, light);
        quad(consumer, pose, -r, 0.0f, -r, -r, 0.0f,  r, -r, h,  r, -r, h, -r, br, bg, bb, light);
        quad(consumer, pose,  r, 0.0f,  r,  r, 0.0f, -r,  r, h, -r,  r, h,  r, br, bg, bb, light);
        quad(consumer, pose, -r, h,  r,  r, h,  r,  r, h, -r, -r, h, -r, br, bg, bb, light);
    }

    private void renderClawFinger(PoseStack poseStack, VertexConsumer consumer, float length, int light) {
        PoseStack.Pose pose = poseStack.last();
        // Finger: Proximal bone segment + sharp claw needle tip
        int br = 90, bg = 84, bb = 99;   // Bone
        int cr = 18, cg = 19, cb = 26;   // Sharp chitin needle claw tip

        float w = 0.035f;
        float mid = length * 0.55f;

        // Proximal segment (tapers from w to w * 0.6)
        quad(consumer, pose, -w, 0.0f,  w,  w, 0.0f,  w,  w * 0.6f, mid,  w * 0.6f, -w * 0.6f, mid,  w * 0.6f, br, bg, bb, light);
        quad(consumer, pose,  w, 0.0f, -w, -w, 0.0f, -w, -w * 0.6f, mid, -w * 0.6f,  w * 0.6f, mid, -w * 0.6f, br, bg, bb, light);
        quad(consumer, pose, -w, 0.0f, -w, -w, 0.0f,  w, -w * 0.6f, mid,  w * 0.6f, -w * 0.6f, mid, -w * 0.6f, br, bg, bb, light);
        quad(consumer, pose,  w, 0.0f,  w,  w, 0.0f, -w,  w * 0.6f, mid, -w * 0.6f,  w * 0.6f, mid,  w * 0.6f, br, bg, bb, light);

        // Distal needle tip (tapers from w * 0.6 to a sharp point at length)
        quad(consumer, pose, -w * 0.6f, mid,  w * 0.6f,  w * 0.6f, mid,  w * 0.6f, 0.0f, length, 0.0f, 0.0f, length, 0.0f, cr, cg, cb, light);
        quad(consumer, pose,  w * 0.6f, mid, -w * 0.6f, -w * 0.6f, mid, -w * 0.6f, 0.0f, length, 0.0f, 0.0f, length, 0.0f, cr, cg, cb, light);
        quad(consumer, pose, -w * 0.6f, mid, -w * 0.6f, -w * 0.6f, mid,  w * 0.6f, 0.0f, length, 0.0f, 0.0f, length, 0.0f, cr, cg, cb, light);
        quad(consumer, pose,  w * 0.6f, mid,  w * 0.6f,  w * 0.6f, mid, -w * 0.6f, 0.0f, length, 0.0f, 0.0f, length, 0.0f, cr, cg, cb, light);
    }

    private void quad(VertexConsumer consumer, PoseStack.Pose pose,
                      float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3,
                      float x4, float y4, float z4,
                      int r, int g, int b, int light) {
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, 255).setUv(0.0f, 0.0f).setOverlay(0).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, 255).setUv(1.0f, 0.0f).setOverlay(0).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, x3, y3, z3).setColor(r, g, b, 255).setUv(1.0f, 1.0f).setOverlay(0).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
        consumer.addVertex(pose, x4, y4, z4).setColor(r, g, b, 255).setUv(0.0f, 1.0f).setOverlay(0).setLight(light).setNormal(0.0f, 1.0f, 0.0f);
    }
}
