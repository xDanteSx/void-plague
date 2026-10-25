package com.voidplague.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.client.model.VoidWatcherModel;
import com.voidplague.mod.entity.VoidWatcherEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class VoidWatcherRenderer extends MobRenderer<VoidWatcherEntity, VoidWatcherModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "textures/entity/void_watcher.png");
    private static final RenderType VOID_WATCHER_EYES =
            RenderType.eyes(ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "textures/entity/void_watcher_eyes.png"));

    public VoidWatcherRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidWatcherModel(context.bakeLayer(VoidWatcherModel.LAYER_LOCATION)), 0.0F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return VOID_WATCHER_EYES;
            }
        });
    }

    @Override
    protected void setupRotations(VoidWatcherEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        // Centrar pivote en el centro de la entidad
        poseStack.translate(0.0F, 0.4F, 0.0F);

        Direction face = entity.getAttachedFace();
        switch (face) {
            case SOUTH -> {} // Por defecto: anclado al muro norte (-Z), mirando al sur (+Z)
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }

        // Ajustar matriz hacia el origen del modelo
        poseStack.translate(0.0F, -1.501F + 0.4F, 0.0F);
    }

    @Nullable
    @Override
    protected RenderType getRenderType(VoidWatcherEntity entity, boolean bodyVisible, boolean translucent, boolean appearsGlowing) {
        return RenderType.entityCutoutNoCull(this.getTextureLocation(entity));
    }

    @Override
    public ResourceLocation getTextureLocation(VoidWatcherEntity entity) {
        return TEXTURE;
    }
}
