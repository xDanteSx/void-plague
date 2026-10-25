package com.voidplague.mod.world.gen;

import com.mojang.serialization.Codec;
import com.voidplague.mod.registry.ModBlocks;
import com.voidplague.mod.registry.ModEntities;
import com.voidplague.mod.world.ClusterIncubationSacBlock;
import com.voidplague.mod.world.IncubationSacBlock;
import com.voidplague.mod.world.RupturedIncubationSacBlock;
import com.voidplague.mod.world.VoidPlagueSavedData;
import net.minecraft.util.RandomSource;
import com.voidplague.mod.world.biome.ModBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import com.voidplague.mod.entity.VoidWatcherEntity;
import com.voidplague.mod.entity.VoidWatcherSentinelEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Monumental Biological Tectonic Fault Line Feature.
 * Features:
 * - 4 Highly Distinct Procedural Archetypes (Titan, Serpentine, Impact Crater, Minor Maw)
 * - Natural Bed Adaptation: Zero forced flat membrane floor. Adapts to natural relief & deep caves
 * - Knife-Edge Sharp Endcaps with 3D chaotic noise & Void Spikes (Zero stadium amphitheatres)
 * - Minimal Altar Anchor: Spike ring + floating Void Rift (Full 3D claws & shards drawn by BER)
 * - Mandatory Wall Lining: Zero exposed vanilla rock/dirt
 * - 3D Void Chasm Biome injection directly into chunk palettes
 */
public class VoidChasmFeature extends Feature<NoneFeatureConfiguration> {
    private static final int GRID_SIZE = 384; // 24 chunks per macro-cell

    public VoidChasmFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /**
     * Mathematical definition of a world-scale Void Fault Line.
     */
    public static class VoidFault {
        public final int archetype; // 0 = Great Titan, 1 = Serpentine Canyon, 2 = Impact Crater, 3 = Minor Maw
        public final double centerX;
        public final double centerZ;
        public final double length;
        public final double dirX;
        public final double dirZ;
        public final double normX;
        public final double normZ;
        public final double curveFreq;
        public final double curveAmp;
        public final double curvePhase;
        public final double maxWidth;
        public final double floorWidth;
        public final double maxDepth;
        public final double carpetReachMultiplier;
        public final double[] altarSValues;

        public VoidFault(int arch, double cx, double cz, double len, double dx, double dz, double nx, double nz,
                         double cf, double ca, double cp, double mw, double fw, double md, double crm, double[] altars) {
            this.archetype = arch;
            this.centerX = cx;
            this.centerZ = cz;
            this.length = len;
            this.dirX = dx;
            this.dirZ = dz;
            this.normX = nx;
            this.normZ = nz;
            this.curveFreq = cf;
            this.curveAmp = ca;
            this.curvePhase = cp;
            this.maxWidth = mw;
            this.floorWidth = fw;
            this.maxDepth = md;
            this.carpetReachMultiplier = crm;
            this.altarSValues = altars;
        }

        public double evaluate(double x, double z, double[] outParams) {
            double dx = x - this.centerX;
            double dz = z - this.centerZ;

            double s = dx * this.dirX + dz * this.dirZ;
            double n = dx * this.normX + dz * this.normZ;

            double halfL = this.length / 2.0;
            double offset = this.curveAmp * Math.sin(s * this.curveFreq + this.curvePhase);
            double distNormal = n - offset;

            double dist;
            if (Math.abs(s) <= halfL) {
                dist = Math.abs(distNormal);
            } else {
                double clampedS = Math.max(-halfL, Math.min(halfL, s));
                double endOffset = this.curveAmp * Math.sin(clampedS * this.curveFreq + this.curvePhase);
                double endX = this.centerX + clampedS * this.dirX + endOffset * this.normX;
                double endZ = this.centerZ + clampedS * this.dirZ + endOffset * this.normZ;
                dist = Math.hypot(x - endX, z - endZ);
            }

            double progress = Math.min(1.0, Math.abs(s) / halfL);

            // Razor-sharp knife-edge endcaps:
            // Outer 35% of length narrows drastically down to a 1-to-2 block crack without stadium steps
            double baseRad;
            double depthAtS;
            if (progress < 0.65) {
                baseRad = this.maxWidth / 2.0;
                depthAtS = this.maxDepth;
            } else if (progress >= 1.0) {
                baseRad = 0.0;
                depthAtS = 0.0;
            } else {
                double edgeP = (1.0 - progress) / 0.35; // 1.0 at 0.65 down to 0.0 at 1.0
                // Width drops rapidly to a razor slit
                baseRad = Math.max(1.0, (this.maxWidth / 2.0) * Math.pow(edgeP, 1.8));
                // Depth stays deep into the ground until extinguishing
                depthAtS = this.maxDepth * Math.max(0.35, Math.pow(edgeP, 0.5));
            }

            // Asymmetric wall radii
            double radLeft = baseRad * (0.85 + 0.35 * Math.sin(s * 0.07 + this.curvePhase));
            double radRight = baseRad * (0.85 + 0.35 * Math.sin(s * 0.07 + this.curvePhase + 2.6));

            boolean isLeft = distNormal < 0;
            double activeRadiusSurf = isLeft ? radLeft : radRight;

            if (outParams != null && outParams.length >= 7) {
                outParams[0] = activeRadiusSurf;
                outParams[1] = depthAtS;
                outParams[2] = s;
                outParams[3] = radLeft;
                outParams[4] = radRight;
                outParams[5] = isLeft ? -1.0 : 1.0;
                outParams[6] = progress;
            }

            return dist;
        }

        public boolean isBridgeBlock(double s, double distNormal, int y, int surfaceY, double activeRadiusSurf) {
            if (this.archetype != 0 && this.archetype != 1) {
                return false;
            }
            double halfL = this.length / 2.0;
            double bridgeSpacing = (this.archetype == 0) ? 40.0 : 50.0;
            double sMod = ((s % bridgeSpacing) + bridgeSpacing) % bridgeSpacing;
            double distToArch = Math.min(sMod, bridgeSpacing - sMod);

            if (distToArch <= 1.8 && Math.abs(s) <= halfL - 25.0) {
                double d = Math.abs(distNormal);
                if (d <= activeRadiusSurf + 2.0) {
                    double relDist = Math.min(1.0, d / Math.max(1.0, activeRadiusSurf));
                    double archCenterY = (surfaceY - 17.0) + 7.0 * (relDist * relDist);
                    if (Math.abs(y - archCenterY) <= 2.0) {
                        return true;
                    }
                }
            }
            return false;
        }

        public boolean isRibBlock(double s, double distNormal, int y, int surfaceY, double activeRadiusSurf) {
            if (this.archetype != 0) {
                return false;
            }
            double halfL = this.length / 2.0;
            double[] stations = new double[] { -60.0, 0.0, 60.0 };
            for (double st : stations) {
                if (Math.abs(st) > halfL - 25.0) continue;

                double distRib1 = Math.abs((s - st) - 0.22 * distNormal);
                double distRib2 = Math.abs((s - st) + 0.22 * distNormal);
                double minRibDist = Math.min(distRib1, distRib2);

                if (minRibDist <= 1.2) {
                    double d = Math.abs(distNormal);
                    if (d <= activeRadiusSurf + 1.0) {
                        double relDist = Math.min(1.0, d / Math.max(1.0, activeRadiusSurf));
                        double archCenterY = (surfaceY - 26.0) + 16.0 * (1.0 - relDist * relDist);
                        if (Math.abs(y - archCenterY) <= 1.5) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    private static long hashGrid(long seed, int gx, int gz) {
        long h = seed + gx * 341873128712L + gz * 132897987541L;
        h = h ^ (h >>> 16);
        h *= 0x85ebca6bL;
        h = h ^ (h >>> 13);
        h *= 0xc2b2ae35L;
        h = h ^ (h >>> 16);
        return h;
    }

    private static VoidFault getFaultInGrid(long seed, int gx, int gz) {
        long h = hashGrid(seed, gx, gz);
        double chance = ((h & 0xFFFF) / 65536.0);
        if (chance > 0.32) {
            return null;
        }

        long s2 = h ^ 0x5DEECE66DL;
        double r1 = ((s2 & 0xFFFF) / 65536.0);
        double r2 = (((s2 >>> 16) & 0xFFFF) / 65536.0);
        double r3 = (((s2 >>> 32) & 0xFFFF) / 65536.0);
        double r4 = (((s2 >>> 48) & 0xFFFF) / 65536.0);

        double centerX = gx * GRID_SIZE + 48.0 + r1 * (GRID_SIZE - 96.0);
        double centerZ = gz * GRID_SIZE + 48.0 + r2 * (GRID_SIZE - 96.0);

        double angle = r4 * Math.PI * 2.0;
        double dirX = Math.cos(angle);
        double dirZ = Math.sin(angle);
        double normX = -dirZ;
        double normZ = dirX;

        double curvePhase = r3 * Math.PI * 2.0;

        int archetype;
        double length;
        double maxWidth;
        double floorWidth;
        double maxDepth;
        double curveFreq;
        double curveAmp;
        double carpetReachMultiplier;
        double[] altars;

        double archRoll = r3 * 100.0;
        if (archRoll < 25.0) {
            // ARQUETIPO 0: "GRAN FISURA TITÁNICA" (25% prob)
            archetype = 0;
            length = 220.0 + r4 * 60.0; // 220 to 280
            maxWidth = 48.0 + r1 * 12.0; // 48 to 60
            floorWidth = 20.0 + r2 * 6.0; // 20 to 26
            maxDepth = 55.0 + r1 * 10.0; // 55 to 65
            curveFreq = 0.008 + r1 * 0.003;
            curveAmp = 10.0 + r2 * 6.0;
            carpetReachMultiplier = 1.0;
            altars = new double[] { -50.0, 50.0 }; // 2 distinct altars 100 blocks apart
        } else if (archRoll < 60.0) {
            // ARQUETIPO 1: "CAÑÓN SERPENTINO PROFUNDO" (35% prob)
            archetype = 1;
            length = 120.0 + r4 * 40.0; // 120 to 160
            maxWidth = 28.0 + r1 * 10.0; // 28 to 38
            floorWidth = 14.0 + r2 * 4.0; // 14 to 18
            maxDepth = 40.0 + r1 * 10.0; // 40 to 50
            curveFreq = 0.055 + r1 * 0.010; // High sinuosity (~0.06)
            curveAmp = 7.0 + r2 * 3.0; // ~8 blocks amplitude
            carpetReachMultiplier = 1.0;
            altars = new double[] { 0.0 }; // 1 single altar in the deepest central bend
        } else if (archRoll < 80.0) {
            // ARQUETIPO 2: "FRACTURA RUPTURISTA / CRÁTER DE IMPACTO" (20% prob)
            archetype = 2;
            length = 60.0 + r4 * 20.0; // 60 to 80 (short, elliptical)
            maxWidth = 35.0 + r1 * 10.0; // 35 to 45 (wide relative to length)
            floorWidth = 16.0 + r2 * 6.0; // 16 to 22
            maxDepth = 30.0 + r1 * 8.0; // 30 to 38
            curveFreq = 0.015;
            curveAmp = 4.0;
            carpetReachMultiplier = 2.2; // Massive tendril spillover
            altars = new double[] { 0.0 };
        } else {
            // ARQUETIPO 3: "HENDEDURA MENOR / SIMA VORAZ" (20% prob)
            archetype = 3;
            length = 45.0 + r4 * 20.0; // 45 to 65
            maxWidth = 14.0 + r1 * 6.0; // 14 to 20 (narrow, aggressive)
            floorWidth = 6.0 + r2 * 3.0; // 6 to 9
            maxDepth = 25.0 + r1 * 7.0; // 25 to 32
            curveFreq = 0.035;
            curveAmp = 6.0;
            carpetReachMultiplier = 0.8;
            altars = new double[] { 0.0 };
        }

        return new VoidFault(archetype, centerX, centerZ, length, dirX, dirZ, normX, normZ,
                curveFreq, curveAmp, curvePhase, maxWidth, floorWidth, maxDepth, carpetReachMultiplier, altars);
    }

    private List<VoidFault> getNearbyFaults(long seed, int chunkX, int chunkZ) {
        int blockCenterX = (chunkX << 4) + 8;
        int blockCenterZ = (chunkZ << 4) + 8;

        int currentGx = Math.floorDiv(blockCenterX, GRID_SIZE);
        int currentGz = Math.floorDiv(blockCenterZ, GRID_SIZE);

        List<VoidFault> faults = new ArrayList<>(2);

        // Scan 5x5 macro-cells with bounding reach
        for (int gx = currentGx - 2; gx <= currentGx + 2; gx++) {
            for (int gz = currentGz - 2; gz <= currentGz + 2; gz++) {
                VoidFault fault = getFaultInGrid(seed, gx, gz);
                if (fault != null) {
                    double distToFaultCenter = Math.hypot(blockCenterX - fault.centerX, blockCenterZ - fault.centerZ);
                    double maxReach = (fault.length / 2.0) + fault.maxWidth + fault.curveAmp + 48.0;
                    if (distToFaultCenter <= maxReach) {
                        faults.add(fault);
                    }
                }
            }
        }

        return faults;
    }

    private boolean isSafeToModify(WorldGenLevel level, BlockPos pos) {
        if (level instanceof WorldGenRegion region) {
            int chunkX = SectionPos.blockToSectionCoord(pos.getX());
            int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
            if (!region.hasChunk(chunkX, chunkZ)) {
                return false;
            }
            return region.ensureCanWrite(pos);
        }
        return true;
    }

    private boolean isLushOrVanillaBiomeBlock(BlockState state) {
        if (state.isAir() || state.is(Blocks.BEDROCK)) {
            return false;
        }
        return state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MOSS_CARPET)
                || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)
                || state.is(Blocks.SPORE_BLOSSOM) || state.is(Blocks.BIG_DRIPLEAF)
                || state.is(Blocks.BIG_DRIPLEAF_STEM) || state.is(Blocks.SMALL_DRIPLEAF)
                || state.is(Blocks.AZALEA) || state.is(Blocks.FLOWERING_AZALEA)
                || state.is(Blocks.HANGING_ROOTS) || state.is(Blocks.CLAY)
                || state.is(Blocks.MUD) || state.is(Blocks.MUDDY_MANGROVE_ROOTS)
                || state.is(Blocks.GLOW_LICHEN) || state.is(Blocks.VINE)
                || state.is(Blocks.LILY_PAD);
    }

    private BlockState transmuteLushOrVanilla(BlockState state, int x, int y, int z) {
        if (state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MOSS_CARPET)) {
            double roll = Math.abs(Math.sin(x * 12.3 + y * 45.6 + z * 78.9));
            return (roll < 0.65)
                    ? ModBlocks.VOID_CARAPACE.get().defaultBlockState()
                    : ModBlocks.OOZING_FLESH.get().defaultBlockState();
        }
        if (state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT)) {
            return ModBlocks.VOID_TENDRIL_HANGING.get().defaultBlockState();
        }
        if (state.is(Blocks.SPORE_BLOSSOM) || state.is(Blocks.BIG_DRIPLEAF) || state.is(Blocks.BIG_DRIPLEAF_STEM)
                || state.is(Blocks.SMALL_DRIPLEAF) || state.is(Blocks.AZALEA) || state.is(Blocks.FLOWERING_AZALEA)
                || state.is(Blocks.HANGING_ROOTS) || state.is(Blocks.VINE) || state.is(Blocks.LILY_PAD)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.is(Blocks.CLAY) || state.is(Blocks.MUD) || state.is(Blocks.MUDDY_MANGROVE_ROOTS)) {
            return ModBlocks.OOZING_FLESH.get().defaultBlockState();
        }
        if (state.is(Blocks.GLOW_LICHEN)) {
            return ModBlocks.VOID_TENDRIL_CARPET.get().defaultBlockState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    private boolean isVanillaRockOrEarth(BlockState state) {
        if (state.isAir() || state.is(Blocks.BEDROCK)) {
            return false;
        }
        return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.GRANITE) || state.is(Blocks.DIORITE)
                || state.is(Blocks.ANDESITE) || state.is(Blocks.TUFF)
                || state.is(Blocks.DRIPSTONE_BLOCK) || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)
                || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.ROOTED_DIRT)
                || state.is(Blocks.GRAVEL) || state.is(Blocks.COBBLED_DEEPSLATE)
                || state.is(Blocks.COBBLESTONE) || isLushOrVanillaBiomeBlock(state);
    }

    private BlockState getWallBlockState(int x, int y, int z) {
        // Calcified bone ribs / bands (~25%)
        double boneNoise = Math.sin(x * 0.45 + y * 0.35) * Math.cos(z * 0.45 + y * 0.25);
        if (boneNoise > 0.26) {
            return ModBlocks.CALCIFIED_CARAPACE.get().defaultBlockState();
        }

        // Rare sporadic clusters of glowing cysts (~1%, reduced by 75%)
        double cystNoise = Math.sin(x * 0.7 + z * 0.7) + Math.cos(y * 0.55);
        if (cystNoise > 1.91) {
            return ModBlocks.VOID_CYST.get().defaultBlockState();
        }

        // Biological flesh patches (~15%)
        double fleshNoise = Math.sin(x * 0.33 + y * 0.22) * Math.cos(z * 0.33 + y * 0.18);
        if (fleshNoise > 0.42) {
            return ModBlocks.VOID_FLESH.get().defaultBlockState();
        }

        return ModBlocks.VOID_CARAPACE.get().defaultBlockState();
    }

    private BlockState getRandomSacState(RandomSource random, Direction facing) {
        int roll = random.nextInt(100);
        if (roll < 60) {
            // 60%: incubation_sac
            return ModBlocks.INCUBATION_SAC.get().defaultBlockState()
                    .setValue(IncubationSacBlock.FACING, facing);
        } else if (roll < 80) {
            // 20%: cluster_incubation_sac
            return ModBlocks.CLUSTER_INCUBATION_SAC.get().defaultBlockState()
                    .setValue(ClusterIncubationSacBlock.FACING, facing);
        } else {
            // 20%: ruptured_incubation_sac
            return ModBlocks.RUPTURED_INCUBATION_SAC.get().defaultBlockState()
                    .setValue(RupturedIncubationSacBlock.FACING, facing);
        }
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        long seed = level.getSeed();

        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());

        List<VoidFault> nearbyFaults = getNearbyFaults(seed, chunkX, chunkZ);
        if (nearbyFaults.isEmpty()) {
            return false;
        }

        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        boolean modifiedAny = false;
        ServerLevel serverLevel = level.getLevel();
        ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
        double[] params = new double[7];

        Holder<Biome> voidBiomeHolder = level.registryAccess()
                .lookup(Registries.BIOME)
                .flatMap(reg -> reg.get(ModBiomes.VOID_CHASM))
                .orElse(null);

        // Pre-evaluate all 256 columns in the chunk
        ColumnInfo[][] cols = new ColumnInfo[16][16];
        for (int lx = 0; lx < 16; lx++) {
            int x = minX + lx;
            for (int lz = 0; lz < 16; lz++) {
                int z = minZ + lz;

                VoidFault bestFault = null;
                double minDist = Double.MAX_VALUE;
                double bestRadiusSurf = 0;
                double bestDepth = 0;
                double bestS = 0;
                double bestRadLeft = 0;
                double bestRadRight = 0;
                boolean bestIsLeft = false;
                double bestProgress = 0.0;

                for (VoidFault fault : nearbyFaults) {
                    double dist = fault.evaluate(x, z, params);
                    if (dist < minDist) {
                        minDist = dist;
                        bestFault = fault;
                        bestRadiusSurf = params[0];
                        bestDepth = params[1];
                        bestS = params[2];
                        bestRadLeft = params[3];
                        bestRadRight = params[4];
                        bestIsLeft = params[5] < 0;
                        bestProgress = params[6];
                    }
                }

                if (bestFault == null || bestRadiusSurf < 0.3) {
                    cols[lx][lz] = new ColumnInfo(x, z, null, minDist, 0, 0, 0, 0, 0, 0, false, 0, 0, 0, 0, 0, false, false);
                    continue;
                }

                double carpetReach = bestFault.carpetReachMultiplier * (6.0 + (Math.sin(x * 0.15 + z * 0.18) * 0.5 + 0.5) * 6.0);
                if (minDist > bestRadiusSurf + carpetReach) {
                    cols[lx][lz] = new ColumnInfo(x, z, null, minDist, 0, 0, 0, 0, 0, 0, false, 0, 0, 0, 0, 0, false, false);
                    continue;
                }

                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                if (surfaceY <= level.getMinBuildHeight() + 10) {
                    surfaceY = 64;
                }

                int targetBottomY = Math.max(level.getMinBuildHeight() + 14, -50);
                int effectiveDepth = Math.max(0, (int) bestDepth);
                int bottomY = Math.max(targetBottomY, surfaceY - effectiveDepth);
                int topY = Math.min(level.getMaxBuildHeight() - 1, Math.max(surfaceY + 20, level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) + 16));

                BlockPos surfPos = new BlockPos(x, surfaceY, z);
                BlockState surfState = level.getBlockState(surfPos);
                boolean isWaterColumn = surfState.is(Blocks.WATER) || level.getFluidState(surfPos).is(FluidTags.WATER)
                        || (surfaceY > 1 && level.getBlockState(surfPos.below()).is(Blocks.WATER));

                double rBottom = Math.max(1.0, bestFault.floorWidth / 2.0);

                cols[lx][lz] = new ColumnInfo(x, z, bestFault, minDist, bestRadiusSurf, rBottom,
                        bestDepth, bestS, bestRadLeft, bestRadRight, bestIsLeft, bestProgress,
                        surfaceY, bottomY, topY, carpetReach, isWaterColumn, true);
            }
        }


        // =========================================================================
        // PASO 1: VACIADO DE CAVIDAD EN Blocks.AIR
        // =========================================================================
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid) continue;

                // Shore Dike against oceans / rivers
                if (col.isWaterColumn && col.minDist <= col.rTop + 3.0) {
                    int dikeTop = Math.max(col.surfaceY + 2, 66);
                    for (int y = col.bottomY; y <= dikeTop; y++) {
                        BlockPos pos = new BlockPos(col.x, y, col.z);
                        if (isSafeToModify(level, pos)) {
                            BlockState dikeBlock = ((col.x + y + col.z) % 3 == 0)
                                    ? ModBlocks.CALCIFIED_CARAPACE.get().defaultBlockState()
                                    : ModBlocks.VOID_CARAPACE.get().defaultBlockState();
                            level.setBlock(pos, dikeBlock, 2);
                            modifiedAny = true;
                        }
                    }
                }

                // Vertical cavity excavation
                if (col.bottomY < col.surfaceY) {
                    for (int y = col.bottomY; y <= col.topY; y++) {
                        double rAtY = col.getRadiusAtY(y);
                        if (col.minDist <= rAtY) {
                            BlockPos pos = new BlockPos(col.x, y, col.z);
                            if (isSafeToModify(level, pos)) {
                                BlockState st = level.getBlockState(pos);
                                if (!st.is(Blocks.BEDROCK)) {
                                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                                    modifiedAny = true;
                                }
                            }
                        }
                    }

                    // Complete sky cleanup above topY: remove tree trunks, foliage, floating leaves
                    if (col.minDist <= col.rTop) {
                        for (int y = col.topY + 1; y < Math.min(level.getMaxBuildHeight(), col.topY + 30); y++) {
                            BlockPos pos = new BlockPos(col.x, y, col.z);
                            if (!isSafeToModify(level, pos)) break;
                            BlockState st = level.getBlockState(pos);
                            if (st.isAir()) continue;
                            if (!st.is(Blocks.BEDROCK)) {
                                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                                modifiedAny = true;
                            }
                        }
                    }
                }

                // 3D Biome Injection
                if (voidBiomeHolder != null && col.minDist <= col.rTop + 8.0) {
                    int qX = QuartPos.fromBlock(col.x);
                    int qZ = QuartPos.fromBlock(col.z);
                    for (int y = col.bottomY; y <= col.surfaceY + 4; y += 4) {
                        int qY = QuartPos.fromBlock(y);
                        int sectionIdx = chunk.getSectionIndex(y);
                        if (sectionIdx >= 0 && sectionIdx < chunk.getSections().length) {
                            LevelChunkSection section = chunk.getSection(sectionIdx);
                            if (section.getBiomes() instanceof PalettedContainer<Holder<Biome>> biomes) {
                                biomes.set(QuartPos.quartLocal(qX), QuartPos.quartLocal(qY), QuartPos.quartLocal(qZ), voidBiomeHolder);
                            }
                        }
                    }
                }
            }
        }


        // =========================================================================
        // PASO 2: FORRADO DE PAREDES Y FONDO CON VOID_CARAPACE Y CALCIFIED_CARAPACE
        // =========================================================================
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid) continue;

                // 2A. Wall Lining: 4.5 blocks border beyond cavity
                if (col.bottomY < col.surfaceY) {
                    for (int y = col.bottomY; y <= col.surfaceY; y++) {
                        double rAtY = col.getRadiusAtY(y);
                        if (col.minDist > rAtY && col.minDist <= rAtY + 4.5) {
                            BlockPos pos = new BlockPos(col.x, y, col.z);
                            if (isSafeToModify(level, pos)) {
                                BlockState st = level.getBlockState(pos);
                                boolean isFluid = !level.getFluidState(pos).isEmpty();
                                boolean isGravel = st.is(Blocks.GRAVEL) || st.is(Blocks.SAND);

                                if (isFluid) {
                                    // Hermetically seal fluids in walls with solid carapace (NO waterfalls)
                                    level.setBlock(pos, ModBlocks.VOID_CARAPACE.get().defaultBlockState(), 2);
                                    modifiedAny = true;
                                } else if (isGravel) {
                                    level.setBlock(pos, ModBlocks.VOID_CARAPACE.get().defaultBlockState(), 2);
                                    modifiedAny = true;
                                } else if (isLushOrVanillaBiomeBlock(st)) {
                                    level.setBlock(pos, transmuteLushOrVanilla(st, col.x, y, col.z), 2);
                                    modifiedAny = true;
                                } else if (isVanillaRockOrEarth(st) || (!st.isAir() && !st.is(Blocks.BEDROCK))) {
                                    level.setBlock(pos, getWallBlockState(col.x, y, col.z), 2);
                                    modifiedAny = true;
                                }
                            }
                        }
                    }

                    // 2B. Transmutation of Natural Floor Bed under carved air
                    for (int y = col.surfaceY - 4; y >= level.getMinBuildHeight() + 5; y--) {
                        BlockPos floorPos = new BlockPos(col.x, y, col.z);
                        BlockState floorState = level.getBlockState(floorPos);
                        BlockState aboveState = level.getBlockState(floorPos.above());

                        if (aboveState.isAir() && (isVanillaRockOrEarth(floorState) || isLushOrVanillaBiomeBlock(floorState) || floorState.isSolid())) {
                            if (isSafeToModify(level, floorPos)) {
                                if (isLushOrVanillaBiomeBlock(floorState)) {
                                    level.setBlock(floorPos, transmuteLushOrVanilla(floorState, col.x, y, col.z), 2);
                                } else {
                                    double roll = Math.abs(Math.sin(col.x * 12.3 + col.z * 45.6 + y * 7.8));
                                    BlockState transmuted = (roll < 0.65)
                                            ? ModBlocks.VOID_CARAPACE.get().defaultBlockState()
                                            : ModBlocks.CALCIFIED_CARAPACE.get().defaultBlockState();
                                    level.setBlock(floorPos, transmuted, 2);
                                }
                                modifiedAny = true;
                            }
                            break;
                        }
                    }
                }

                // 2C. Surface Ground Transmutation near edges
                if (col.minDist > col.rTop && col.minDist <= col.rTop + col.carpetReach) {
                    BlockPos groundPos = new BlockPos(col.x, col.surfaceY - 1, col.z);
                    if (isSafeToModify(level, groundPos)) {
                        BlockState groundState = level.getBlockState(groundPos);
                        if (groundState.isSolid() && !groundState.is(Blocks.BEDROCK) && level.getFluidState(groundPos).isEmpty()) {
                            double prob = 1.0 - (col.minDist - col.rTop) / col.carpetReach;
                            if (prob > 0.38) {
                                level.setBlock(groundPos, ModBlocks.VOID_CARAPACE.get().defaultBlockState(), 2);
                                modifiedAny = true;
                            }
                        }
                    }
                }
            }
        }


        // 2D. Cave Sealing Pass
        BlockPos.MutableBlockPos caveCursor = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos adjCursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                ColumnInfo col = cols[x - minX][z - minZ];
                if (col == null || !col.valid || col.minDist > col.rTop + 10.0) continue;

                int bY = Math.max(level.getMinBuildHeight() + 14, -50);
                for (int y = bY; y <= col.surfaceY - 5; y++) {
                    caveCursor.set(x, y, z);
                    if (level.getBlockState(caveCursor).isAir()) {
                        for (Direction dir : Direction.values()) {
                            adjCursor.set(caveCursor.getX() + dir.getStepX(), caveCursor.getY() + dir.getStepY(), caveCursor.getZ() + dir.getStepZ());
                            if (isSafeToModify(level, adjCursor)) {
                                BlockState adjState = level.getBlockState(adjCursor);
                                if (!level.getFluidState(adjCursor).isEmpty()) {
                                    // Hermetically seal cave fluids with solid carapace
                                    level.setBlock(adjCursor, ModBlocks.VOID_CARAPACE.get().defaultBlockState(), 2);
                                    modifiedAny = true;
                                } else if (adjState.is(Blocks.GRAVEL) || adjState.is(Blocks.SAND)) {
                                    level.setBlock(adjCursor, ModBlocks.VOID_CARAPACE.get().defaultBlockState(), 2);
                                    modifiedAny = true;
                                } else if (isLushOrVanillaBiomeBlock(adjState)) {
                                    level.setBlock(adjCursor, transmuteLushOrVanilla(adjState, adjCursor.getX(), adjCursor.getY(), adjCursor.getZ()), 2);
                                    modifiedAny = true;
                                } else if (isVanillaRockOrEarth(adjState)) {
                                    level.setBlock(adjCursor, ModBlocks.VOID_CARAPACE.get().defaultBlockState(), 2);
                                    modifiedAny = true;
                                }
                            }
                        }
                    }
                }
            }
        }


        // =========================================================================
        // PASO 3: GENERACIÓN DEL ALTAR, COSTILLAS TITÁNICAS Y RUINAS (POST-VACIADO)
        // =========================================================================

        // 3A. ALTAR ANCHOR
        for (VoidFault fault : nearbyFaults) {
            for (double altarS : fault.altarSValues) {
                double offset = fault.curveAmp * Math.sin(altarS * fault.curveFreq + fault.curvePhase);
                double altarWorldX = fault.centerX + altarS * fault.dirX + offset * fault.normX;
                double altarWorldZ = fault.centerZ + altarS * fault.dirZ + offset * fault.normZ;
                int altarChunkX = (int) Math.floor(altarWorldX / 16.0);
                int altarChunkZ = (int) Math.floor(altarWorldZ / 16.0);

                if (chunkX == altarChunkX && chunkZ == altarChunkZ) {
                    int rx = Mth.clamp((int) Math.round(altarWorldX), minX + 3, maxX - 3);
                    int rz = Mth.clamp((int) Math.round(altarWorldZ), minZ + 3, maxZ - 3);
                    int surfY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, rx, rz);
                    int floorY = -1;
                    for (int y = surfY - 4; y >= level.getMinBuildHeight() + 10; y--) {
                        BlockPos chk = new BlockPos(rx, y, rz);
                        if (level.getBlockState(chk).isSolid() && level.getBlockState(chk.above()).isAir()) {
                            floorY = y;
                            break;
                        }
                    }
                    if (floorY == -1) floorY = Math.max(level.getMinBuildHeight() + 14, -50);

                    // Circular platform of Calcified Carapace
                    for (int dx = -3; dx <= 3; dx++) {
                        for (int dz = -3; dz <= 3; dz++) {
                            if (dx * dx + dz * dz <= 10) {
                                BlockPos pitPos = new BlockPos(rx + dx, floorY, rz + dz);
                                setBlockIfSafe(level, pitPos, ModBlocks.CALCIFIED_CARAPACE.get().defaultBlockState());
                                setBlockIfSafe(level, pitPos.below(), ModBlocks.CALCIFIED_CARAPACE.get().defaultBlockState());
                            }
                        }
                    }

                    // Spike ring
                    int[][] spikeOffsets = {
                            { 2, 0 }, { 2, 1 }, { 0, 2 }, { -1, 2 },
                            { -2, 0 }, { -2, -1 }, { 0, -2 }, { 1, -2 }
                    };
                    for (int[] so : spikeOffsets) {
                        BlockPos spk = new BlockPos(rx + so[0], floorY + 1, rz + so[1]);
                        setBlockIfSafe(level, spk, ModBlocks.VOID_SPIKE.get().defaultBlockState());
                    }

                    // Floating Void Rift Monolith
                    BlockPos riftPos = new BlockPos(rx, floorY + 2, rz);
                    setBlockIfSafe(level, riftPos, ModBlocks.VOID_RIFT.get().defaultBlockState());
                    if (serverLevel != null) {
                        VoidPlagueSavedData.get(serverLevel).addRift(riftPos);
                    }
                    modifiedAny = true;
                }
            }
        }


        // 3B. COSTILLAS DE LEVIATÁN (calcified_rib)
        // En cada tramo de 40 bloques de longitud en la falla:
        // Cota entre Y = 10 e Y = 35. Arco transversal de pared a pared.
        // SOLO en bloques que actualmente sean Blocks.AIR.
        for (VoidFault fault : nearbyFaults) {
            double halfL = fault.length / 2.0;
            for (double st = -halfL + 20.0; st <= halfL - 20.0; st += 40.0) {
                double offset = fault.curveAmp * Math.sin(st * fault.curveFreq + fault.curvePhase);
                double prog = Math.abs(st) / halfL;
                double rStation = (fault.maxWidth / 2.0) * Math.sqrt(Math.max(0.0, 1.0 - prog * prog));
                if (rStation < 6.0) continue;

                long ribHash = ((long) Math.round(st * 100.0)) ^ seed;
                ribHash = ribHash ^ (ribHash >>> 16);
                int ribBaseY = 12 + Math.abs((int) ((ribHash >> 4) % 12)); // 12 to 23
                int ribRise = 8 + Math.abs((int) ((ribHash >> 8) % 6));   // Rise of 8 to 13 -> Apex 20 to 36

                Direction.Axis transverseAxis = (Math.abs(fault.normX) > Math.abs(fault.normZ)) ? Direction.Axis.X : Direction.Axis.Z;

                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        double dx = x - fault.centerX;
                        double dz = z - fault.centerZ;
                        double s = dx * fault.dirX + dz * fault.dirZ;
                        double n = dx * fault.normX + dz * fault.normZ;

                        if (Math.abs(s - st) <= 1.4) {
                            double distNorm = n - offset;
                            if (Math.abs(distNorm) <= rStation) {
                                double rel = distNorm / rStation;
                                int archY = ribBaseY + (int) Math.round(ribRise * (1.0 - rel * rel));

                                // Orient AXIS along the parabolic curve: vertical on flanks, transverse at apex
                                Direction.Axis ribAxis = (Math.abs(rel) > 0.55) ? Direction.Axis.Y : transverseAxis;
                                BlockState ribState = ModBlocks.CALCIFIED_RIB.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, ribAxis);

                                for (int dy = 0; dy <= 1; dy++) {
                                    BlockPos ribPos = new BlockPos(x, archY + dy, z);
                                    if (isSafeToModify(level, ribPos) && level.getBlockState(ribPos).isAir()) {
                                        level.setBlock(ribPos, ribState, 2);
                                        modifiedAny = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        // 3C. RUINAS ASIMILADAS DE ICATHIA
        // Cornisas/terrazas con suelo sólido y aire encima (alturas Y entre 0 y 40)
        // 1 o 2 columnas derruidas por tramo (altura 2-4, STONE_BRICKS, CRACKED_STONE_BRICKS, CALCIFIED_CARAPACE)
        for (VoidFault fault : nearbyFaults) {
            long ruinHash = ((long) chunkX * 73856093L) ^ ((long) chunkZ * 19349663L) ^ (seed + 104729L);
            ruinHash = ruinHash ^ (ruinHash >>> 16);
            double chunkRoll = ((ruinHash & 0xFFFF) / 65535.0);

            if (chunkRoll < 0.45) {
                List<BlockPos> candidateLedges = new ArrayList<>();
                for (int x = minX + 2; x <= maxX - 2; x++) {
                    for (int z = minZ + 2; z <= maxZ - 2; z++) {
                        ColumnInfo col = cols[x - minX][z - minZ];
                        if (col == null || !col.valid) continue;

                        for (int y = 2; y <= 38; y++) {
                            double rAtY = col.getRadiusAtY(y);
                            if (col.minDist <= rAtY + 2.0 && col.minDist >= rAtY - 3.0) {
                                BlockPos checkPos = new BlockPos(x, y, z);
                                if (level.getBlockState(checkPos).isSolid()
                                        && level.getBlockState(checkPos.above()).isAir()
                                        && level.getBlockState(checkPos.above(2)).isAir()) {
                                    candidateLedges.add(checkPos);
                                }
                            }
                        }
                    }
                }

                if (!candidateLedges.isEmpty()) {
                    int columnsCount = 1 + (((int) (ruinHash >>> 8)) % 2);
                    for (int c = 0; c < columnsCount; c++) {
                        int idx = Math.abs((int) ((ruinHash >> (c * 6)) % candidateLedges.size()));
                        BlockPos ledgeBase = candidateLedges.get(idx);
                        int colHeight = 2 + (((int) (ruinHash >> (c * 4 + 12))) % 3);

                        for (int h = 1; h <= colHeight; h++) {
                            BlockPos bPos = ledgeBase.above(h);
                            if (!isSafeToModify(level, bPos) || !level.getBlockState(bPos).isAir()) break;

                            int bRoll = Math.abs((bPos.getX() * 13 + bPos.getZ() * 29 + h * 7)) % 10;
                            BlockState colBlock = (bRoll < 5)
                                    ? Blocks.STONE_BRICKS.defaultBlockState()
                                    : (bRoll < 8 ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState() : ModBlocks.CALCIFIED_CARAPACE.get().defaultBlockState());
                            level.setBlock(bPos, colBlock, 2);
                            modifiedAny = true;

                            if (h == colHeight) {
                                BlockPos topCarpet = bPos.above();
                                if (isSafeToModify(level, topCarpet) && level.getBlockState(topCarpet).isAir()) {
                                    BlockState carpet = ModBlocks.VOID_TENDRIL_CARPET.get().defaultBlockState()
                                            .setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);
                                    level.setBlock(topCarpet, carpet, 2);
                                }
                            }
                        }

                        // Tendril carpets on adjacent floor tiles of the ruin terrace
                        for (Direction d : Direction.Plane.HORIZONTAL) {
                            BlockPos adjLedge = ledgeBase.relative(d);
                            BlockPos adjAbove = adjLedge.above();
                            if (isSafeToModify(level, adjAbove) && level.getBlockState(adjLedge).isSolid() && level.getBlockState(adjAbove).isAir()) {
                                if ((adjLedge.getX() + adjLedge.getZ()) % 2 == 0) {
                                    BlockState carpet = ModBlocks.VOID_TENDRIL_CARPET.get().defaultBlockState()
                                            .setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);
                                    level.setBlock(adjAbove, carpet, 2);
                                }
                            }
                        }
                    }
                }
            }
        }


        // =========================================================================
        // PASO 4: SIEMBRA DE VEGETACIÓN Y FLUIDOS (ICOR, PÓLIPOS, ORQUÍDEAS, PÚSTULAS)
        // =========================================================================
        // PASO 4: SIEMBRA DE VEGETACIÓN, FLUIDOS Y ABERRACIONES BIOMECÁNICAS
        // =========================================================================

        // 4A. Lecho Abisal y Charcos Estancados Mínimos de Icor
        // PROHIBIDO transmutar acuíferos o crear cascadas.
        // Solo 1 o 2 charcos estancados diminutos (máximo 3x3 y 1 de profundidad), rodeados por bloques sólidos.
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid || col.bottomY >= col.surfaceY) continue;

                for (int y = col.surfaceY - 4; y >= level.getMinBuildHeight() + 5; y--) {
                    BlockPos floorPos = new BlockPos(col.x, y, col.z);
                    if (level.getBlockState(floorPos).isSolid() && level.getBlockState(floorPos.above()).isAir()) {
                        if (y <= col.bottomY + 5) {
                            level.setBlock(floorPos, ModBlocks.VOID_MEMBRANE.get().defaultBlockState(), 2);
                            modifiedAny = true;
                        }
                        break;
                    }
                }
            }
        }


        // Generar únicamente de 1 a 2 charcos diminutos cerrados por falla
        for (VoidFault fault : nearbyFaults) {
            double[] puddleStations = { -20.0, 25.0 };
            for (double pS : puddleStations) {
                double offset = fault.curveAmp * Math.sin(pS * fault.curveFreq + fault.curvePhase);
                int pX = (int) Math.round(fault.centerX + pS * fault.dirX + offset * fault.normX);
                int pZ = (int) Math.round(fault.centerZ + pS * fault.dirZ + offset * fault.normZ);
                int pChunkX = Math.floorDiv(pX, 16);
                int pChunkZ = Math.floorDiv(pZ, 16);

                if (chunkX == pChunkX && chunkZ == pChunkZ) {
                    int surfY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pX, pZ);
                    int pFloorY = -1;
                    for (int y = surfY - 4; y >= level.getMinBuildHeight() + 6; y--) {
                        BlockPos chk = new BlockPos(pX, y, pZ);
                        if (level.getBlockState(chk).isSolid() && level.getBlockState(chk.above()).isAir()) {
                            pFloorY = y;
                            break;
                        }
                    }

                    if (pFloorY != -1) {
                        // Charco de máximo 3x3 (dx, dz en -1..1) y 1 de profundidad, rodeado de membrana
                        for (int dx = -2; dx <= 2; dx++) {
                            for (int dz = -2; dz <= 2; dz++) {
                                BlockPos bPos = new BlockPos(pX + dx, pFloorY, pZ + dz);
                                BlockPos underPos = bPos.below();

                                if (isSafeToModify(level, underPos)) {
                                    level.setBlock(underPos, ModBlocks.VOID_MEMBRANE.get().defaultBlockState(), 2);
                                }

                                if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                                    if (isSafeToModify(level, bPos)) {
                                        level.setBlock(bPos, ModBlocks.VOID_ICHOR_BLOCK.get().defaultBlockState(), 2);
                                        // Erradicar flotantes sobre el charco
                                        for (int clr = 1; clr <= 3; clr++) {
                                            BlockPos abovePos = bPos.above(clr);
                                            if (isSafeToModify(level, abovePos) && !level.getBlockState(abovePos).isAir()) {
                                                level.setBlock(abovePos, Blocks.AIR.defaultBlockState(), 2);
                                            }
                                        }
                                        modifiedAny = true;
                                    }
                                } else {
                                    // Borde estanco de membrana para impedir desbordes
                                    if (isSafeToModify(level, bPos)) {
                                        level.setBlock(bPos, ModBlocks.VOID_MEMBRANE.get().defaultBlockState(), 2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        // 4A2. Fondo y Escombros Fosilizados (montículos de 2 a 5 bloques de CALCIFIED_RIB y CALCIFIED_CARAPACE)
        for (int lx = 1; lx < 15; lx++) {
            for (int lz = 1; lz < 15; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid || col.bottomY >= col.surfaceY) continue;

                double debrisNoise = Math.sin(col.x * 0.38 + 1.7) * Math.cos(col.z * 0.38 - 0.9);
                if (debrisNoise > 0.42) {
                    for (int y = col.surfaceY - 4; y >= level.getMinBuildHeight() + 5; y--) {
                        BlockPos floorPos = new BlockPos(col.x, y, col.z);
                        BlockState floorState = level.getBlockState(floorPos);
                        BlockPos abovePos = floorPos.above();

                        if (floorState.isSolid() && level.getBlockState(abovePos).isAir()) {
                            long dHash = ((long) col.x * 3129871L) ^ ((long) col.z * 116129781L) ^ ((long) y * 982451653L);
                            int moundSize = 2 + Math.abs((int) (dHash % 4)); // 2 to 5 blocks in cluster
                            Direction.Axis randomAxis = (dHash % 3 == 0) ? Direction.Axis.X : (dHash % 3 == 1 ? Direction.Axis.Z : Direction.Axis.Y);
                            BlockState ribState = ModBlocks.CALCIFIED_RIB.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, randomAxis);
                            BlockState boneBlock = (dHash % 2 == 0) ? ribState : ModBlocks.CALCIFIED_CARAPACE.get().defaultBlockState();

                            level.setBlock(floorPos, boneBlock, 2);
                            modifiedAny = true;

                            int placed = 1;
                            Direction[] spreadDirs = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
                            for (Direction d : spreadDirs) {
                                if (placed >= moundSize) break;
                                BlockPos adj = floorPos.relative(d);
                                if (isSafeToModify(level, adj) && level.getBlockState(adj).isSolid() && level.getBlockState(adj.above()).isAir()) {
                                    Direction.Axis adjAxis = ((dHash + placed) % 2 == 0) ? Direction.Axis.X : Direction.Axis.Z;
                                    BlockState adjBone = ((dHash + placed) % 2 == 0)
                                            ? ModBlocks.CALCIFIED_RIB.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, adjAxis)
                                            : ModBlocks.CALCIFIED_CARAPACE.get().defaultBlockState();
                                    level.setBlock(adj, adjBone, 2);
                                    placed++;
                                }
                            }
                            if (placed < moundSize && isSafeToModify(level, abovePos) && level.getBlockState(abovePos).isAir()) {
                                level.setBlock(abovePos, boneBlock, 2);
                            }
                            break;
                        }
                    }
                }
            }
        }


        // 4B. Espinas Óseas y Nervios (void_polyps) - 1 o 2 ejemplares aislados junto a costillas fósiles
        long polypHash = ((long) chunkX * 982451653L) ^ ((long) chunkZ * 73856093L) ^ (seed + 41235L);
        polypHash = polypHash ^ (polypHash >>> 16);
        if ((polypHash & 0xFF) < 140) {
            int targetPolyps = 1 + ((int) (polypHash >> 8) % 2); // 1 or 2
            int placedPolyps = 0;

            for (int lx = 2; lx < 14 && placedPolyps < targetPolyps; lx++) {
                for (int lz = 2; lz < 14 && placedPolyps < targetPolyps; lz++) {
                    ColumnInfo col = cols[lx][lz];
                    if (col == null || !col.valid) continue;

                    for (int y = level.getMinBuildHeight() + 6; y <= col.bottomY + 12; y++) {
                        BlockPos ground = new BlockPos(col.x, y, col.z);
                        BlockPos above = ground.above();

                        if (level.getBlockState(ground).isSolid() && level.getBlockState(above).isAir()) {
                            boolean nearBone = false;
                            for (Direction d : Direction.Plane.HORIZONTAL) {
                                if (level.getBlockState(ground.relative(d)).is(ModBlocks.CALCIFIED_RIB.get())
                                        || level.getBlockState(ground.relative(d)).is(ModBlocks.CALCIFIED_CARAPACE.get())
                                        || level.getBlockState(above.relative(d)).is(ModBlocks.CALCIFIED_RIB.get())) {
                                    nearBone = true;
                                    break;
                                }
                            }
                            if (nearBone) {
                                level.setBlock(above, ModBlocks.VOID_POLYPS.get().defaultBlockState(), 2);
                                placedPolyps++;
                                modifiedAny = true;
                                break;
                            }
                        }
                    }
                }
            }
        }


        // 4C. Sacos de Incubación (incubation_sac) en Nidos Biológicos Ocultos
        // EXCLUSIVAMENTE en concavidades bajo techo o hendiduras sombrías (reducido en un 75%, máx 2-3 contiguos)
        long nestChunkHash = ((long) chunkX * 3129871L) ^ ((long) chunkZ * 116129781L) ^ (seed + 89123L);
        nestChunkHash = nestChunkHash ^ (nestChunkHash >>> 16);
        boolean chunkHasNest = (Math.abs(nestChunkHash % 100) < 25); // 75% reduction

        if (chunkHasNest) {
            List<BlockPos> candidateNiches = new ArrayList<>();
            for (int x = minX + 1; x <= maxX - 1; x++) {
                for (int z = minZ + 1; z <= maxZ - 1; z++) {
                    ColumnInfo col = cols[x - minX][z - minZ];
                    if (col == null || !col.valid) continue;

                    for (int y = col.bottomY + 4; y <= col.surfaceY - 8; y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        double rAtY = col.getRadiusAtY(y);

                        // Only deep in recessed nooks, never on open ledge rims
                        if (col.minDist >= rAtY - 0.8 && col.minDist <= rAtY + 2.0) {
                            if (level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isSolid()) {
                                int solidNeighbors = 0;
                                for (Direction d : Direction.Plane.HORIZONTAL) {
                                    if (level.getBlockState(pos.relative(d)).isSolid()) {
                                        solidNeighbors++;
                                    }
                                }
                                // Must be a shaded alcove with at least 2 enclosing walls
                                if (solidNeighbors >= 2) {
                                    candidateNiches.add(pos);
                                }
                            }
                        }
                    }
                }
            }

            if (!candidateNiches.isEmpty()) {
                int nicheIdx = Math.abs((int) ((nestChunkHash >> 4) % candidateNiches.size()));
                BlockPos nestCenter = candidateNiches.get(nicheIdx);
                int nestSize = 1 + Math.abs((int) ((nestChunkHash >> 8) % 3)); // 1, 2, or 3 sacs MAX

                level.setBlock(nestCenter, getRandomSacState(random, Direction.UP), 2);
                modifiedAny = true;

                int placedSacs = 1;
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (placedSacs >= nestSize) break;
                    BlockPos adj = nestCenter.relative(d);
                    if (isSafeToModify(level, adj) && level.getBlockState(adj).isAir() && level.getBlockState(adj.above()).isSolid()) {
                        level.setBlock(adj, getRandomSacState(random, Direction.UP), 2);
                        placedSacs++;
                    }
                }
            }
        }


        // 4C2. Estalactitas Carnosas Goticulantes
        // Bajo cualquier cornisa o saliente con al menos 4 bloques de caída libre
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid) continue;

                for (int y = col.surfaceY - 4; y >= col.bottomY + 8; y--) {
                    BlockPos ceilingPos = new BlockPos(col.x, y, col.z);
                    double rAtY = col.getRadiusAtY(y);

                    if (col.minDist >= rAtY - 3.5 && col.minDist <= rAtY + 2.5) {
                        BlockState ceilingState = level.getBlockState(ceilingPos);
                        BlockPos startDrop = ceilingPos.below();

                        if (ceilingState.isSolid() && level.getBlockState(startDrop).isAir()) {
                            boolean has4FreeFall = true;
                            for (int drop = 1; drop <= 4; drop++) {
                                BlockPos check = ceilingPos.below(drop);
                                if (!isSafeToModify(level, check) || !level.getBlockState(check).isAir()) {
                                    has4FreeFall = false;
                                    break;
                                }
                            }

                            if (has4FreeFall) {
                                double stalactiteRoll = Math.sin(col.x * 0.83 + col.z * 1.17 + y * 0.41);
                                if (stalactiteRoll > 0.45) { // Organic spacing
                                    long stHash = ((long) col.x * 73856093L) ^ ((long) col.z * 19349663L) ^ ((long) y * 83492791L);
                                    int stalactiteHeight = 2 + Math.abs((int) ((stHash >> 2) % 3)); // 2 to 4 blocks

                                    for (int h = 1; h <= stalactiteHeight; h++) {
                                        BlockPos fleshPos = ceilingPos.below(h);
                                        if (isSafeToModify(level, fleshPos) && level.getBlockState(fleshPos).isAir()) {
                                            level.setBlock(fleshPos, ModBlocks.OOZING_FLESH.get().defaultBlockState(), 2);
                                            modifiedAny = true;
                                        }
                                    }

                                    BlockPos tipPos = ceilingPos.below(stalactiteHeight + 1);
                                    if (isSafeToModify(level, tipPos) && level.getBlockState(tipPos).isAir()) {
                                        level.setBlock(tipPos, ModBlocks.VOID_TENDRIL_HANGING.get().defaultBlockState(), 2);
                                        modifiedAny = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        // 4D. Fauces Dentadas (mimic_orchid / void_maw) - 1 o 2 por chunk escondidas a ras de suelo en terrazas
        long mawHash = ((long) chunkX * 19349663L) ^ ((long) chunkZ * 3129871L) ^ (seed + 104729L);
        mawHash = mawHash ^ (mawHash >>> 16);
        int mawCount = 1 + ((int) (mawHash >> 4) % 2); // 1 or 2 per chunk
        int placedMaws = 0;

        for (int lx = 2; lx < 14 && placedMaws < mawCount; lx++) {
            for (int lz = 2; lz < 14 && placedMaws < mawCount; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid) continue;

                for (int y = col.bottomY + 3; y <= col.surfaceY - 4; y++) {
                    BlockPos ground = new BlockPos(col.x, y, col.z);
                    BlockPos above = ground.above();
                    if (level.getBlockState(ground).isSolid() && level.getBlockState(above).isAir()) {
                        double rAtY = col.getRadiusAtY(y);
                        if (col.minDist >= rAtY - 2.5 && col.minDist <= rAtY + 1.5) {
                            if ((col.x * 7 + col.z * 13 + y * 3) % 17 == 0) {
                                level.setBlock(above, ModBlocks.MIMIC_ORCHID.get().defaultBlockState(), 2);
                                placedMaws++;
                                modifiedAny = true;
                                break;
                            }
                        }
                    }
                }
            }
        }


        // Borde superior (labio de la grieta): Exclusivamente manto de zarcillos (CERO flores en hilera)
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid) continue;

                if (col.minDist > col.rTop && col.minDist <= col.rTop + col.carpetReach) {
                    BlockPos groundPos = new BlockPos(col.x, col.surfaceY - 1, col.z);
                    BlockPos carpetPos = groundPos.above();

                    if (isSafeToModify(level, carpetPos) && level.getBlockState(carpetPos).isAir() && level.getBlockState(groundPos).isSolid()) {
                        BlockState carpetState = ModBlocks.VOID_TENDRIL_CARPET.get().defaultBlockState()
                                .setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);
                        level.setBlock(carpetPos, carpetState, 2);
                        modifiedAny = true;
                    }
                }
            }
        }


        // 4F. Ojos Vigías (void_watcher_eye) - 1 a 3 por chunk en paredes verticales mirando al centro
        long eyeHash = ((long) chunkX * 83492791L) ^ ((long) chunkZ * 297121507L) ^ (seed + 67891L);
        eyeHash = eyeHash ^ (eyeHash >>> 16);
        int eyeCount = 1 + ((int) (eyeHash >> 4) % 3); // 1, 2, or 3
        int placedEyes = 0;

        for (int lx = 1; lx < 15 && placedEyes < eyeCount; lx++) {
            for (int lz = 1; lz < 15 && placedEyes < eyeCount; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid) continue;

                for (int y = -35; y <= 25 && placedEyes < eyeCount; y += 3) {
                    BlockPos wallPos = new BlockPos(col.x, y, col.z);
                    double rAtY = col.getRadiusAtY(y);

                    if (col.minDist >= rAtY - 0.5 && col.minDist <= rAtY + 2.0) {
                        BlockState wallState = level.getBlockState(wallPos);
                        if (wallState.isSolid() && !wallState.is(Blocks.BEDROCK)) {
                            double dxToCenter = col.fault.centerX - col.x;
                            double dzToCenter = col.fault.centerZ - col.z;
                            Direction facing = Direction.getNearest(dxToCenter, 0.0, dzToCenter);
                            if (facing.getAxis().isHorizontal()) {
                                BlockPos frontPos = wallPos.relative(facing);

                                if (level.getBlockState(frontPos).isAir()) {
                                    VoidWatcherEntity watcher = ModEntities.VOID_WATCHER.get().create(level.getLevel());
                                    if (watcher != null) {
                                        watcher.moveTo(frontPos.getX() + 0.5, frontPos.getY() + 0.5, frontPos.getZ() + 0.5, facing.toYRot(), 0.0F);
                                        watcher.setAttachedFace(facing);
                                        level.addFreshEntity(watcher);
                                        placedEyes++;
                                        modifiedAny = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        // 4F-2. Centinelas Abisales Flotantes (void_watcher_sentinel) - patrullaje aéreo en la sima
        if ((eyeHash & 3) == 0) {
            ColumnInfo centerCol = cols[8][8];
            if (centerCol != null && centerCol.valid && centerCol.bottomY < centerCol.surfaceY) {
                int airY = (centerCol.bottomY + centerCol.surfaceY) / 2;
                BlockPos airPos = new BlockPos(centerCol.x, airY, centerCol.z);
                if (level.getBlockState(airPos).isAir()) {
                    VoidWatcherSentinelEntity sentinel = ModEntities.VOID_WATCHER_SENTINEL.get().create(level.getLevel());
                    if (sentinel != null) {
                        sentinel.moveTo(airPos.getX() + 0.5, airPos.getY() + 0.5, airPos.getZ() + 0.5, (float) (eyeHash % 360), 0.0F);
                        level.addFreshEntity(sentinel);
                        modifiedAny = true;
                    }
                }
            }
        }

        // 4G. Extremidades Suplicantes (grasping_limb) - 2 a 4 por chunk en el fondo abisal
        long limbHash = ((long) chunkX * 41235123L) ^ ((long) chunkZ * 982451653L) ^ (seed + 99121L);
        limbHash = limbHash ^ (limbHash >>> 16);
        int limbCount = 2 + ((int) (limbHash >> 6) % 3); // 2, 3, or 4
        int placedLimbs = 0;

        for (int lx = 1; lx < 15 && placedLimbs < limbCount; lx++) {
            for (int lz = 1; lz < 15 && placedLimbs < limbCount; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid || col.bottomY >= col.surfaceY) continue;

                for (int y = col.surfaceY - 4; y >= level.getMinBuildHeight() + 5; y--) {
                    BlockPos floorPos = new BlockPos(col.x, y, col.z);
                    BlockState floorState = level.getBlockState(floorPos);
                    BlockPos abovePos = floorPos.above();

                    if (y <= col.bottomY + 5 && floorState.isSolid() && level.getBlockState(abovePos).isAir()) {
                        if (((col.x * 11 + col.z * 17 + y * 7) % 13) == 0) {
                            level.setBlock(abovePos, ModBlocks.GRASPING_LIMB.get().defaultBlockState(), 2);
                            placedLimbs++;
                            modifiedAny = true;
                            break;
                        }
                    }
                }
            }
        }


        // 4E. Vegetación de cornisas intermedias (tallos, flores, zarcillos colgantes y carne supurante)
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid) continue;

                for (int y = col.bottomY; y < col.surfaceY; y++) {
                    BlockPos pos = new BlockPos(col.x, y, col.z);
                    double rAtY = col.getRadiusAtY(y);

                    if (col.minDist > rAtY && col.minDist <= rAtY + 4.5) {
                        BlockPos abovePos = pos.above();
                        if (level.getBlockState(abovePos).isAir() && level.getBlockState(pos).isSolid()) {
                            double vegRoll = Math.abs(Math.sin(col.x * 12.3 + col.z * 34.5 + y * 5.7));
                            if (vegRoll < 0.15) {
                                level.setBlock(abovePos, ModBlocks.OOZING_FLESH.get().defaultBlockState(), 2);
                                modifiedAny = true;
                            } else if (vegRoll < 0.60) {
                                BlockState carpet = ModBlocks.VOID_TENDRIL_CARPET.get().defaultBlockState()
                                        .setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true);
                                level.setBlock(abovePos, carpet, 2);
                                modifiedAny = true;
                            }
                        }

                        // Ceilings: Oozing Flesh & Hanging Tendrils
                        BlockPos belowPos = pos.below();
                        if (y > col.bottomY + 3 && level.getBlockState(belowPos).isAir() && level.getBlockState(pos).isSolid()) {
                            double ceilRoll = Math.sin(col.x * 0.5) * Math.cos(col.z * 0.5) + Math.sin(y * 0.4);
                            if (ceilRoll > 0.68) {
                                level.setBlock(pos, ModBlocks.OOZING_FLESH.get().defaultBlockState(), 2);
                            } else if (ceilRoll > 0.42) {
                                int hangLen = 2 + (Math.abs(col.x * 31 + col.z * 17 + y) % 4);
                                for (int dy = 1; dy <= hangLen; dy++) {
                                    BlockPos hPos = pos.below(dy);
                                    if (!isSafeToModify(level, hPos)) break;
                                    if (level.getBlockState(hPos).isAir()) {
                                        level.setBlock(hPos, ModBlocks.VOID_TENDRIL_HANGING.get().defaultBlockState(), 2);
                                        modifiedAny = true;
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        // =========================================================================
        // PASO 5: PURGA TOTAL VOLUMÉTRICA DE LUSH CAVES Y BIOMAS INTRUSOS
        // =========================================================================
        // Erradica cualquier presencia de bloques verdes vanilla dentro de la sima
        BlockPos.MutableBlockPos purgePos = new BlockPos.MutableBlockPos();
        for (int lx = 0; lx < 16; lx++) {
            for (int lz = 0; lz < 16; lz++) {
                ColumnInfo col = cols[lx][lz];
                if (col == null || !col.valid || col.bottomY >= col.surfaceY) continue;

                // Rango de influencia: desde el centro de la sima hasta 6 bloques tras la pared
                for (int y = col.bottomY - 2; y <= col.surfaceY + 2; y++) {
                    double rAtY = col.getRadiusAtY(y);
                    if (col.minDist <= rAtY + 6.0) {
                        purgePos.set(col.x, y, col.z);
                        if (isSafeToModify(level, purgePos)) {
                            BlockState state = level.getBlockState(purgePos);
                            if (isLushOrVanillaBiomeBlock(state)) {
                                level.setBlock(purgePos, transmuteLushOrVanilla(state, col.x, y, col.z), 2);
                                modifiedAny = true;
                            } else if (!state.isAir() && col.minDist <= rAtY && !state.is(Blocks.BEDROCK)) {
                                // Dentro de la cavidad abierta: eliminar agua estancada o lianas residuales
                                if (!level.getFluidState(purgePos).isEmpty() || state.is(Blocks.WATER)) {
                                    level.setBlock(purgePos, Blocks.AIR.defaultBlockState(), 2);
                                    modifiedAny = true;
                                }
                            }
                        }
                    }
                }
            }
        }


        return modifiedAny;
    }

    private static class ColumnInfo {
        final int x;
        final int z;
        final VoidFault fault;
        final double minDist;
        final double rTop;
        final double rBottom;
        final double bestDepth;
        final double bestS;
        final double bestRadLeft;
        final double bestRadRight;
        final boolean bestIsLeft;
        final double bestProgress;
        final int surfaceY;
        final int bottomY;
        final int topY;
        final double carpetReach;
        final boolean isWaterColumn;
        final boolean valid;

        ColumnInfo(int x, int z, VoidFault fault, double minDist, double rTop, double rBottom,
                   double bestDepth, double bestS, double bestRadLeft, double bestRadRight,
                   boolean bestIsLeft, double bestProgress, int surfaceY, int bottomY, int topY,
                   double carpetReach, boolean isWaterColumn, boolean valid) {
            this.x = x;
            this.z = z;
            this.fault = fault;
            this.minDist = minDist;
            this.rTop = rTop;
            this.rBottom = rBottom;
            this.bestDepth = bestDepth;
            this.bestS = bestS;
            this.bestRadLeft = bestRadLeft;
            this.bestRadRight = bestRadRight;
            this.bestIsLeft = bestIsLeft;
            this.bestProgress = bestProgress;
            this.surfaceY = surfaceY;
            this.bottomY = bottomY;
            this.topY = topY;
            this.carpetReach = carpetReach;
            this.isWaterColumn = isWaterColumn;
            this.valid = valid;
        }

        double getRadiusAtY(int y) {
            if (y >= surfaceY) {
                return rTop;
            }
            double h = (double) (y - bottomY) / (double) Math.max(1, surfaceY - bottomY);
            h = Math.max(0.0, Math.min(1.0, h));
            double baseR = rBottom + (rTop - rBottom) * Math.pow(h, 0.65);

            double noise3D = 0.0;
            if (bestProgress >= 0.65) {
                noise3D = Math.sin(x * 0.15 + y * 0.20 + z * 0.15) * Math.cos(x * 0.12 - z * 0.18 + y * 0.14) * 6.0;
            }

            double terraceCycle = ((y - bottomY) % 7.0) / 7.0;
            double terraceStep = (bestProgress < 0.65) ? Math.sin(terraceCycle * Math.PI) * 2.0 : 0.0;

            if (bestIsLeft) {
                double terraceNoiseLeft = terraceStep * (0.8 + 0.2 * Math.sin(bestS * 0.05));
                return Math.max(1.0, baseR + terraceNoiseLeft + noise3D);
            } else {
                double sheerVariation = 0.6 * Math.cos(y * 0.4 + bestS * 0.03);
                return Math.max(1.0, baseR + sheerVariation + noise3D);
            }
        }

    }

    private void setBlockIfSafe(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (isSafeToModify(level, pos)) {
            level.setBlock(pos, state, 2);
        }
    }
}
