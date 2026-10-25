package com.voidplague.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.entity.VoidRiftBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * Custom BlockEntityRenderer for the Void Rift Dimensional Tear & Biomorphic Monument.
 * Renders:
 * - 3 crossed vertical luminous planes with respiratory scale pulsing and outer distortion halo.
 * - 6 monumental 3D curved calcified abyssal claws / ribs wrapping around the vortex.
 * - 4 floating anti-gravity dark chitin shards orbiting with sinusoidal levitation.
 */
public class VoidRiftRenderer implements BlockEntityRenderer<VoidRiftBlockEntity> {
    public static final ResourceLocation TEAR_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            VoidPlagueMod.MOD_ID,
            "textures/entity/void_rift_tear.png"
    );

    public static final ResourceLocation CALCIFIED_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            VoidPlagueMod.MOD_ID,
            "textures/block/calcified_carapace.png"
    );

    public static final ResourceLocation CARAPACE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            VoidPlagueMod.MOD_ID,
            "textures/block/void_carapace.png"
    );

    public VoidRiftRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(VoidRiftBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        long gameTime = (level != null) ? level.getGameTime() : 0;
        float time = gameTime + partialTick;

        poseStack.pushPose();
        // Lift origin to 1.3 blocks above the base so the tear and monument float majestically
        poseStack.translate(0.5, 1.3, 0.5);

        // Breathing scale pulse: 1.0f + 0.15f * sin(time * 0.1f)
        float pulse = 1.0f + 0.15f * Mth.sin(time * 0.1f);
        float rotation = time * 1.5f;

        // 1. Central Dimensional Tear & Halo
        VertexConsumer tearConsumer = bufferSource.getBuffer(RenderType.entityTranslucentEmissive(TEAR_TEXTURE));

        // Core tear
        poseStack.pushPose();
        poseStack.scale(pulse, pulse, pulse);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        renderStarPlanes(poseStack, tearConsumer, 1.5f, 2.8f, 255, 255, 255, 255);
        poseStack.popPose();

        // Distortion halo
        poseStack.pushPose();
        float haloScale = pulse * 1.25f;
        poseStack.scale(haloScale, haloScale, haloScale);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation * 0.7f));
        renderStarPlanes(poseStack, tearConsumer, 1.9f, 3.4f, 210, 110, 255, 150);
        poseStack.popPose();

        // 2. Six 3D Monumental Curved Abyssal Claws / Ribs
        VertexConsumer clawConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CALCIFIED_TEXTURE));
        renderAbyssalClaws(poseStack, clawConsumer, time);

        // 3. Four Orbiting Anti-Gravity Chitin Shards
        VertexConsumer shardConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(CARAPACE_TEXTURE));
        renderFloatingShards(poseStack, shardConsumer, time);

        poseStack.popPose();
    }

    private void renderAbyssalClaws(PoseStack poseStack, VertexConsumer consumer, float time) {
        // Rib curve profile nodes: [radius, y]
        float[][] profile = {
                { 1.85f, -1.30f, 0.30f }, // Node 0: Base anchor at floor/spike ring
                { 2.25f, -0.60f, 0.28f }, // Node 1: Bulging outward rib cage
                { 2.15f,  0.30f, 0.24f }, // Node 2: Mid-height spine
                { 1.55f,  1.15f, 0.18f }, // Node 3: Inward curvature
                { 0.75f,  1.80f, 0.08f }  // Node 4: Curved talon apex over the rift
        };

        for (int i = 0; i < 6; i++) {
            float baseAngle = i * 60.0f;
            float sway = Mth.sin(time * 0.05f + i * 1.047f) * 2.5f;

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(baseAngle + sway));

            // Render ribbon segments between profile joints
            for (int seg = 0; seg < profile.length - 1; seg++) {
                float r1 = profile[seg][0];
                float y1 = profile[seg][1];
                float w1 = profile[seg][2] / 2.0f;

                float r2 = profile[seg + 1][0];
                float y2 = profile[seg + 1][1];
                float w2 = profile[seg + 1][2] / 2.0f;

                PoseStack.Pose pose = poseStack.last();

                // Outer face
                vertex(consumer, pose, r1, y1, -w1, 0.0f, 1.0f, 230, 220, 245, 255);
                vertex(consumer, pose, r1, y1,  w1, 1.0f, 1.0f, 230, 220, 245, 255);
                vertex(consumer, pose, r2, y2,  w2, 1.0f, 0.0f, 245, 235, 255, 255);
                vertex(consumer, pose, r2, y2, -w2, 0.0f, 0.0f, 245, 235, 255, 255);

                // Inner face
                vertex(consumer, pose, r2, y2, -w2, 0.0f, 0.0f, 210, 190, 230, 255);
                vertex(consumer, pose, r2, y2,  w2, 1.0f, 0.0f, 210, 190, 230, 255);
                vertex(consumer, pose, r1, y1,  w1, 1.0f, 1.0f, 200, 180, 225, 255);
                vertex(consumer, pose, r1, y1, -w1, 0.0f, 1.0f, 200, 180, 225, 255);

                // Side ribs thickness
                float thick1 = 0.12f;
                float thick2 = 0.06f;

                vertex(consumer, pose, r1 - thick1, y1, 0.0f, 0.0f, 1.0f, 180, 160, 210, 255);
                vertex(consumer, pose, r1, y1,  w1, 1.0f, 1.0f, 210, 190, 230, 255);
                vertex(consumer, pose, r2, y2,  w2, 1.0f, 0.0f, 230, 210, 245, 255);
                vertex(consumer, pose, r2 - thick2, y2, 0.0f, 0.0f, 0.0f, 200, 180, 225, 255);

                vertex(consumer, pose, r2 - thick2, y2, 0.0f, 0.0f, 0.0f, 200, 180, 225, 255);
                vertex(consumer, pose, r2, y2, -w2, 1.0f, 0.0f, 230, 210, 245, 255);
                vertex(consumer, pose, r1, y1, -w1, 1.0f, 1.0f, 210, 190, 230, 255);
                vertex(consumer, pose, r1 - thick1, y1, 0.0f, 0.0f, 1.0f, 180, 160, 210, 255);
            }

            poseStack.popPose();
        }
    }

    private void renderFloatingShards(PoseStack poseStack, VertexConsumer consumer, float time) {
        float orbitRadius = 1.35f;

        for (int i = 0; i < 4; i++) {
            float orbitAngle = time * 0.75f + i * 90.0f;
            float bobbing = Mth.sin(time * 0.08f + i * 1.57f) * 0.32f;

            poseStack.pushPose();
            // Position around the rift
            poseStack.mulPose(Axis.YP.rotationDegrees(orbitAngle));
            poseStack.translate(orbitRadius, bobbing, 0.0);

            // Shard self-tumbling
            poseStack.mulPose(Axis.XP.rotationDegrees(time * 1.4f + i * 45.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(time * 1.1f + i * 30.0f));

            renderOctahedronShard(poseStack, consumer, 0.18f, 0.32f, 0.18f);

            poseStack.popPose();
        }
    }

    private void renderOctahedronShard(PoseStack poseStack, VertexConsumer consumer, float hw, float hh, float hd) {
        PoseStack.Pose pose = poseStack.last();

        // 8 triangular faces connecting top/bottom to the 4 waist vertices:
        // Waist vertices: (+hw, 0, 0), (0, 0, +hd), (-hw, 0, 0), (0, 0, -hd)
        // Top vertex: (0, +hh, 0)
        // Bottom vertex: (0, -hh, 0)
        float[][] waist = {
                { hw, 0, 0 },
                { 0, 0, hd },
                { -hw, 0, 0 },
                { 0, 0, -hd }
        };

        for (int j = 0; j < 4; j++) {
            float[] p1 = waist[j];
            float[] p2 = waist[(j + 1) % 4];

            // Upper pyramid triangle
            vertex(consumer, pose, p1[0], p1[1], p1[2], 0.0f, 1.0f, 70, 75, 95, 255);
            vertex(consumer, pose, p2[0], p2[1], p2[2], 1.0f, 1.0f, 85, 90, 115, 255);
            vertex(consumer, pose, 0.0f,  hh,    0.0f,  0.5f, 0.0f, 120, 125, 155, 255);
            vertex(consumer, pose, 0.0f,  hh,    0.0f,  0.5f, 0.0f, 120, 125, 155, 255); // Degenerate quad for triangle

            // Lower pyramid triangle
            vertex(consumer, pose, 0.0f, -hh,    0.0f,  0.5f, 1.0f, 45, 50, 65, 255);
            vertex(consumer, pose, p2[0], p2[1], p2[2], 1.0f, 0.0f, 60, 65, 80, 255);
            vertex(consumer, pose, p1[0], p1[1], p1[2], 0.0f, 0.0f, 50, 55, 70, 255);
            vertex(consumer, pose, p1[0], p1[1], p1[2], 0.0f, 0.0f, 50, 55, 70, 255);
        }
    }

    private void renderStarPlanes(PoseStack poseStack, VertexConsumer consumer, float width, float height, int r, int g, int b, int a) {
        float halfW = width / 2.0f;
        float halfH = height / 2.0f;

        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(i * 60.0f));

            PoseStack.Pose pose = poseStack.last();

            // Double-sided quad: front face
            vertex(consumer, pose, -halfW, -halfH, 0.0f, 0.0f, 1.0f, r, g, b, a);
            vertex(consumer, pose,  halfW, -halfH, 0.0f, 1.0f, 1.0f, r, g, b, a);
            vertex(consumer, pose,  halfW,  halfH, 0.0f, 1.0f, 0.0f, r, g, b, a);
            vertex(consumer, pose, -halfW,  halfH, 0.0f, 0.0f, 0.0f, r, g, b, a);

            // Back face
            vertex(consumer, pose,  halfW, -halfH, 0.0f, 1.0f, 1.0f, r, g, b, a);
            vertex(consumer, pose, -halfW, -halfH, 0.0f, 0.0f, 1.0f, r, g, b, a);
            vertex(consumer, pose, -halfW,  halfH, 0.0f, 0.0f, 0.0f, r, g, b, a);
            vertex(consumer, pose,  halfW,  halfH, 0.0f, 1.0f, 0.0f, r, g, b, a);

            poseStack.popPose();
        }
    }

    private void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, float u, float v, int r, int g, int b, int a) {
        consumer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public boolean shouldRenderOffScreen(VoidRiftBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}
