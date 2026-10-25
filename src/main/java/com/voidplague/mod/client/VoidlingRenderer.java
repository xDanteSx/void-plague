package com.voidplague.mod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.client.model.VoidlingModel;
import com.voidplague.mod.entity.VoidlingEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class VoidlingRenderer extends MobRenderer<VoidlingEntity, VoidlingModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "textures/entity/voidling.png");
    private static final RenderType VOIDLING_EYES =
            RenderType.eyes(ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "textures/entity/voidling_eyes.png"));

    public VoidlingRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidlingModel(context.bakeLayer(VoidlingModel.LAYER_LOCATION)), 0.7F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return VOIDLING_EYES;
            }
        });
    }

    @Override
    protected void scale(VoidlingEntity entity, PoseStack poseStack, float partialTickTime) {
        float scale = entity.getScale();
        poseStack.scale(scale, scale, scale);
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(VoidlingEntity entity) {
        return TEXTURE;
    }
}
