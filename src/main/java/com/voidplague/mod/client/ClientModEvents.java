package com.voidplague.mod.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.client.renderer.ClusterIncubationSacRenderer;
import com.voidplague.mod.client.renderer.GraspingLimbRenderer;
import com.voidplague.mod.client.renderer.IncubationSacRenderer;
import com.voidplague.mod.client.renderer.RupturedIncubationSacRenderer;
import com.voidplague.mod.client.renderer.VoidRiftRenderer;
import com.voidplague.mod.client.model.VoidWatcherModel;
import com.voidplague.mod.client.model.VoidWatcherSentinelModel;
import com.voidplague.mod.client.model.VoidlingModel;
import com.voidplague.mod.client.renderer.VoidWatcherRenderer;
import com.voidplague.mod.client.renderer.VoidWatcherSentinelRenderer;
import com.voidplague.mod.registry.ModBlockEntities;
import com.voidplague.mod.registry.ModBlocks;
import com.voidplague.mod.registry.ModEntities;
import com.voidplague.mod.registry.ModFluids;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.joml.Vector3f;

public class ClientModEvents {
    public static void setupClient(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_TENDRIL_HANGING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_TENDRIL_CARPET.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_POLYPS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.MIMIC_ORCHID.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_STALK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_BLOSSOM.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_SPIKE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.INCUBATION_SAC.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.CLUSTER_INCUBATION_SAC.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.RUPTURED_INCUBATION_SAC.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.GRASPING_LIMB.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.VOID_ICHOR_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.VOID_ICHOR_SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.VOID_ICHOR_FLOWING.get(), RenderType.translucent());
        });
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.VOIDLING.get(), VoidlingRenderer::new);
        event.registerEntityRenderer(ModEntities.VOID_WATCHER.get(), VoidWatcherRenderer::new);
        event.registerEntityRenderer(ModEntities.VOID_WATCHER_SENTINEL.get(), VoidWatcherSentinelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VOID_RIFT.get(), VoidRiftRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GRASPING_LIMB.get(), GraspingLimbRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INCUBATION_SAC.get(), IncubationSacRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CLUSTER_INCUBATION_SAC.get(), ClusterIncubationSacRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RUPTURED_INCUBATION_SAC.get(), RupturedIncubationSacRenderer::new);
    }

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(VoidlingModel.LAYER_LOCATION, VoidlingModel::createBodyLayer);
        event.registerLayerDefinition(VoidWatcherModel.LAYER_LOCATION, VoidWatcherModel::createBodyLayer);
        event.registerLayerDefinition(VoidWatcherSentinelModel.LAYER_LOCATION, VoidWatcherSentinelModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "block/void_ichor_still");
            private static final ResourceLocation FLOW = ResourceLocation.fromNamespaceAndPath(VoidPlagueMod.MOD_ID, "block/void_ichor_flow");

            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xFFFFFFFF; // ARGB blanco puro neutro para preservar los colores reales del PNG sin multiplicar a negro
            }

            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.14F, 0.05F, 0.24F); // Tono violeta abisal profundo
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(0.0F);
                RenderSystem.setShaderFogEnd(5.0F);
            }
        }, ModFluids.VOID_ICHOR_TYPE.get());
    }
}
