package com.voidplague.mod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.client.model.VoidWatcherSentinelModel;
import com.voidplague.mod.entity.VoidWatcherSentinelEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class VoidWatcherSentinelRenderer extends MobRenderer<VoidWatcherSentinelEntity, VoidWatcherSentinelModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "textures/entity/void_watcher_sentinel.png");
    private static final RenderType SENTINEL_EYES =
            RenderType.eyes(ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "textures/entity/void_watcher_sentinel_eyes.png"));

    public VoidWatcherSentinelRenderer(EntityRendererProvider.Context context) {
        super(context, new VoidWatcherSentinelModel(context.bakeLayer(VoidWatcherSentinelModel.LAYER_LOCATION)), 1.2F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return SENTINEL_EYES;
            }
        });
    }

    @Override
    protected void scale(VoidWatcherSentinelEntity entity, PoseStack poseStack, float partialTick) {
        // Escala colosal a 2.2F para flotar imponentemente sobre el jugador
        poseStack.scale(2.2F, 2.2F, 2.2F);
        super.scale(entity, poseStack, partialTick);
    }

    @Override
    public ResourceLocation getTextureLocation(VoidWatcherSentinelEntity entity) {
        return TEXTURE;
    }
}
