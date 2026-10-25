package com.voidplague.mod.client;

import com.voidplague.mod.registry.ModBlocks;
import com.voidplague.mod.registry.ModEffects;
import com.voidplague.mod.registry.ModItems;
import com.voidplague.mod.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public class VoidFogHandler {
    private static float fogIntensity = 0.0F;
    private static int checkTimer = 0;
    private static int whisperCooldown = 0;
    private static boolean cachedNearRift = false;
    private static boolean cachedInChasm = false;

    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }

        boolean hasMask = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.VOID_MASK.get());
        boolean hasEffect = player.hasEffect(ModEffects.VOID_SICKNESS);
        boolean nearRift = isNearRift(mc.level, player.blockPosition());
        boolean inChasm = isInDeepChasm(mc.level, player.blockPosition());
        float target = (hasEffect || nearRift || inChasm) ? (hasMask ? 0.30F : 1.0F) : 0.0F;

        // Smooth fog interpolation
        fogIntensity += (target - fogIntensity) * 0.05F;

        if (fogIntensity > 0.005F) {
            float targetR = hasMask ? 0.04F : 0.08F;
            float targetG = hasMask ? 0.02F : 0.03F;
            float targetB = hasMask ? 0.07F : 0.13F;

            event.setRed(Mth.lerp(fogIntensity, event.getRed(), targetR));
            event.setGreen(Mth.lerp(fogIntensity, event.getGreen(), targetG));
            event.setBlue(Mth.lerp(fogIntensity, event.getBlue(), targetB));
        }
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.VOID_MASK.get())) {
            // Keep vision clear when wearing the Void Mask
            return;
        }

        if (fogIntensity > 0.01F && event.getMode() == FogRenderer.FogMode.FOG_TERRAIN) {
            boolean inChasm = (player != null && player.blockPosition().getY() <= 30 && cachedInChasm);
            float nearDistance = Mth.lerp(fogIntensity, event.getNearPlaneDistance(), inChasm ? 2.0F : 4.0F);
            float farDistance = Mth.lerp(fogIntensity, event.getFarPlaneDistance(), inChasm ? 12.0F : 18.0F);

            event.setNearPlaneDistance(nearDistance);
            event.setFarPlaneDistance(farDistance);
            event.setCanceled(true);
        }
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (whisperCooldown > 0) {
            whisperCooldown--;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }

        boolean isImmune = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.VOID_MASK.get())
                || player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.VOID_CHESTPLATE.get());
        if (isImmune) {
            return;
        }

        boolean hasEffect = player.hasEffect(ModEffects.VOID_SICKNESS);
        boolean nearRift = isNearRift(mc.level, player.blockPosition());

        if ((hasEffect || nearRift) && whisperCooldown <= 0 && player.getRandom().nextInt(100) == 0) {
            whisperCooldown = 600 + player.getRandom().nextInt(200); // 30-40 segundos de silencio obligatorio
            Vec3 lookVec = player.getViewVector(1.0F);
            Vec3 behindPos = player.getEyePosition().subtract(lookVec.scale(2.5));
            mc.level.playLocalSound(
                    behindPos.x,
                    behindPos.y,
                    behindPos.z,
                    ModSounds.VOID_WHISPERS.get(),
                    SoundSource.AMBIENT,
                    0.4F,
                    0.9F + player.getRandom().nextFloat() * 0.2F,
                    false
            );
        }
    }

    private static boolean isNearRift(Level level, BlockPos pos) {
        checkTimer++;
        if (checkTimer >= 10) {
            checkTimer = 0;
            cachedNearRift = false;
            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int dx = -12; dx <= 12; dx += 3) {
                for (int dy = -6; dy <= 6; dy += 3) {
                    for (int dz = -12; dz <= 12; dz += 3) {
                        cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                        if (level.hasChunkAt(cursor) && level.getBlockState(cursor).is(ModBlocks.VOID_RIFT.get())) {
                            cachedNearRift = true;
                            return true;
                        }
                    }
                }
            }
        }
        return cachedNearRift;
    }

    private static boolean isInDeepChasm(Level level, BlockPos pos) {
        if (pos.getY() > 30) {
            cachedInChasm = false;
            return false;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -3; dy <= 1; dy++) {
            cursor.set(pos.getX(), pos.getY() + dy, pos.getZ());
            if (level.hasChunkAt(cursor)) {
                var state = level.getBlockState(cursor);
                if (state.is(ModBlocks.VOID_MEMBRANE.get()) || state.is(ModBlocks.VOID_CARAPACE.get())
                        || state.is(ModBlocks.VOID_FLESH.get()) || state.is(ModBlocks.CALCIFIED_CARAPACE.get())) {
                    cachedInChasm = true;
                    return true;
                }
            }
        }
        cachedInChasm = false;
        return false;
    }
}
