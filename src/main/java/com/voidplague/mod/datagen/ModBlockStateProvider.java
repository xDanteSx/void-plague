package com.voidplague.mod.datagen;

import com.voidplague.mod.VoidPlagueMod;
import com.voidplague.mod.registry.ModBlocks;
import com.voidplague.mod.registry.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.MultifaceBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, VoidPlagueMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        simpleBlockWithItem(ModBlocks.VOID_CARAPACE.get(), cubeAll(ModBlocks.VOID_CARAPACE.get()));
        simpleBlockWithItem(ModBlocks.CALCIFIED_CARAPACE.get(), cubeAll(ModBlocks.CALCIFIED_CARAPACE.get()));

        // Void Spike (3D Angled Non-Cubic Needle Model)
        var spikeModel = models().withExistingParent("void_spike", mcLoc("block/block"))
                .texture("particle", modLoc("block/void_spike"))
                .texture("spike", modLoc("block/void_spike"))
                .renderType("cutout");

        spikeModel.element()
                .from(3.0f, 0.0f, 8.0f).to(13.0f, 16.0f, 8.0f)
                .face(Direction.NORTH).texture("#spike").uvs(0, 0, 16, 16).end()
                .face(Direction.SOUTH).texture("#spike").uvs(0, 0, 16, 16).end()
                .end();

        spikeModel.element()
                .from(3.0f, 0.0f, 8.0f).to(13.0f, 16.0f, 8.0f)
                .rotation().origin(8.0f, 8.0f, 8.0f).axis(Direction.Axis.Y).angle(45.0f).end()
                .face(Direction.NORTH).texture("#spike").uvs(0, 0, 16, 16).end()
                .face(Direction.SOUTH).texture("#spike").uvs(0, 0, 16, 16).end()
                .end();

        spikeModel.element()
                .from(8.0f, 0.0f, 3.0f).to(8.0f, 16.0f, 13.0f)
                .face(Direction.EAST).texture("#spike").uvs(0, 0, 16, 16).end()
                .face(Direction.WEST).texture("#spike").uvs(0, 0, 16, 16).end()
                .end();

        spikeModel.element()
                .from(3.0f, 0.0f, 8.0f).to(13.0f, 16.0f, 8.0f)
                .rotation().origin(8.0f, 8.0f, 8.0f).axis(Direction.Axis.Y).angle(-45.0f).end()
                .face(Direction.NORTH).texture("#spike").uvs(0, 0, 16, 16).end()
                .face(Direction.SOUTH).texture("#spike").uvs(0, 0, 16, 16).end()
                .end();

        simpleBlock(ModBlocks.VOID_SPIKE.get(), spikeModel);
        itemModels().withExistingParent("void_spike", mcLoc("item/generated"))
                .texture("layer0", modLoc("block/void_spike"));
        simpleBlockWithItem(ModBlocks.VOID_MEMBRANE.get(), cubeAll(ModBlocks.VOID_MEMBRANE.get()));
        simpleBlockWithItem(ModBlocks.VOID_FLESH.get(), cubeAll(ModBlocks.VOID_FLESH.get()));
        simpleBlockWithItem(ModBlocks.OOZING_FLESH.get(), cubeAll(ModBlocks.OOZING_FLESH.get()));
        simpleBlockWithItem(ModBlocks.VOID_CYST.get(), cubeAll(ModBlocks.VOID_CYST.get()));
        simpleBlockWithItem(ModBlocks.VOID_TENDRIL.get(), cubeAll(ModBlocks.VOID_TENDRIL.get()));
        simpleBlockWithItem(
                ModBlocks.VOID_TENDRIL_HANGING.get(),
                models().cross("void_tendril_hanging", blockTexture(ModBlocks.VOID_TENDRIL_HANGING.get())).renderType("cutout")
        );
        simpleBlockWithItem(
                ModBlocks.VOID_RIFT.get(),
                models().cubeAll("void_rift", blockTexture(ModBlocks.VOID_RIFT.get())).renderType("translucent")
        );

        // Void Stalk & Void Blossom (Cross Plant Models)
        simpleBlock(
                ModBlocks.VOID_STALK.get(),
                models().cross("void_stalk", blockTexture(ModBlocks.VOID_STALK.get())).renderType("cutout")
        );
        itemModels().withExistingParent("void_stalk", mcLoc("item/generated"))
                .texture("layer0", blockTexture(ModBlocks.VOID_STALK.get()));

        simpleBlock(
                ModBlocks.VOID_BLOSSOM.get(),
                models().cross("void_blossom", blockTexture(ModBlocks.VOID_BLOSSOM.get())).renderType("cutout")
        );
        itemModels().withExistingParent("void_blossom", mcLoc("item/generated"))
                .texture("layer0", blockTexture(ModBlocks.VOID_BLOSSOM.get()));

        // Void Tendril Carpet (Multiface Lichen Model)
        ModelFile carpetModel = models().withExistingParent("void_tendril_carpet", mcLoc("block/glow_lichen"))
                .texture("glow_lichen", blockTexture(ModBlocks.VOID_TENDRIL_CARPET.get()))
                .texture("particle", blockTexture(ModBlocks.VOID_TENDRIL_CARPET.get()))
                .renderType("cutout");

        MultiPartBlockStateBuilder carpetBuilder = getMultipartBuilder(ModBlocks.VOID_TENDRIL_CARPET.get());
        carpetBuilder.part().modelFile(carpetModel).addModel().condition(MultifaceBlock.getFaceProperty(Direction.NORTH), true).end();
        carpetBuilder.part().modelFile(carpetModel).rotationY(90).uvLock(true).addModel().condition(MultifaceBlock.getFaceProperty(Direction.EAST), true).end();
        carpetBuilder.part().modelFile(carpetModel).rotationY(180).uvLock(true).addModel().condition(MultifaceBlock.getFaceProperty(Direction.SOUTH), true).end();
        carpetBuilder.part().modelFile(carpetModel).rotationY(270).uvLock(true).addModel().condition(MultifaceBlock.getFaceProperty(Direction.WEST), true).end();
        carpetBuilder.part().modelFile(carpetModel).rotationX(270).uvLock(true).addModel().condition(MultifaceBlock.getFaceProperty(Direction.UP), true).end();
        carpetBuilder.part().modelFile(carpetModel).rotationX(90).uvLock(true).addModel().condition(MultifaceBlock.getFaceProperty(Direction.DOWN), true).end();

        itemModels().withExistingParent("void_tendril_carpet", mcLoc("item/generated"))
                .texture("layer0", blockTexture(ModBlocks.VOID_TENDRIL_CARPET.get()));

        // Calcified Rib (Axis Rotated Pillar)
        axisBlock(ModBlocks.CALCIFIED_RIB.get(), modLoc("block/calcified_rib_side"), modLoc("block/calcified_rib_top"));
        simpleBlockItem(ModBlocks.CALCIFIED_RIB.get(), models().cubeColumn("calcified_rib", modLoc("block/calcified_rib_side"), modLoc("block/calcified_rib_top")));

        // Incubation Sac (Bulbous Pustule Dome)
        var sacModel = models().withExistingParent("incubation_sac", mcLoc("block/block"))
                .texture("particle", modLoc("block/incubation_sac"))
                .texture("all", modLoc("block/incubation_sac"));
        sacModel.element()
                .from(2.0f, 0.0f, 2.0f).to(14.0f, 12.0f, 14.0f)
                .allFaces((dir, face) -> face.texture("#all").uvs(2, 4, 14, 16))
                .end();
        simpleBlock(ModBlocks.INCUBATION_SAC.get(), sacModel);
        itemModels().withExistingParent("incubation_sac", modLoc("block/incubation_sac"));

        // Cluster Incubation Sac (Twin Bulbous Cluster Model)
        var clusterSacModel = models().withExistingParent("cluster_incubation_sac", mcLoc("block/block"))
                .texture("particle", modLoc("block/incubation_sac"))
                .texture("all", modLoc("block/incubation_sac"));
        clusterSacModel.element()
                .from(1.0f, 0.0f, 1.0f).to(15.0f, 13.0f, 15.0f)
                .allFaces((dir, face) -> face.texture("#all").uvs(2, 4, 14, 16))
                .end();
        simpleBlock(ModBlocks.CLUSTER_INCUBATION_SAC.get(), clusterSacModel);
        itemModels().withExistingParent("cluster_incubation_sac", modLoc("block/cluster_incubation_sac"));

        // Ruptured Incubation Sac (Torn Open Withered Cup)
        var rupturedSacModel = models().withExistingParent("ruptured_incubation_sac", mcLoc("block/block"))
                .texture("particle", modLoc("block/ruptured_incubation_sac"))
                .texture("all", modLoc("block/ruptured_incubation_sac"));
        rupturedSacModel.element()
                .from(2.0f, 0.0f, 2.0f).to(14.0f, 10.0f, 14.0f)
                .allFaces((dir, face) -> face.texture("#all").uvs(2, 4, 14, 14))
                .end();
        simpleBlock(ModBlocks.RUPTURED_INCUBATION_SAC.get(), rupturedSacModel);
        itemModels().withExistingParent("ruptured_incubation_sac", modLoc("block/ruptured_incubation_sac"));

        // Void Polyps (Abyssal Marine Flora Cross)
        simpleBlock(
                ModBlocks.VOID_POLYPS.get(),
                models().cross("void_polyps", modLoc("block/void_polyps")).renderType("cutout")
        );
        itemModels().withExistingParent("void_polyps", mcLoc("item/generated"))
                .texture("layer0", modLoc("block/void_polyps"));

        // Mimic Orchid / Void Maw (Predatory Ground Trap Model)
        var mawModel = models().withExistingParent("mimic_orchid", mcLoc("block/block"))
                .texture("particle", modLoc("block/mimic_orchid"))
                .texture("maw", modLoc("block/mimic_orchid"))
                .renderType("cutout");
        mawModel.element()
                .from(1.0f, 0.0f, 1.0f).to(15.0f, 4.0f, 15.0f)
                .allFaces((dir, face) -> face.texture("#maw").uvs(1, 12, 15, 16))
                .end();
        mawModel.element()
                .from(2.0f, 4.0f, 3.0f).to(14.0f, 8.0f, 3.0f)
                .face(Direction.NORTH).texture("#maw").uvs(2, 0, 14, 4).end()
                .face(Direction.SOUTH).texture("#maw").uvs(2, 0, 14, 4).end()
                .end();
        mawModel.element()
                .from(2.0f, 4.0f, 13.0f).to(14.0f, 8.0f, 13.0f)
                .face(Direction.NORTH).texture("#maw").uvs(2, 0, 14, 4).end()
                .face(Direction.SOUTH).texture("#maw").uvs(2, 0, 14, 4).end()
                .end();
        simpleBlock(ModBlocks.MIMIC_ORCHID.get(), mawModel);
        itemModels().withExistingParent("mimic_orchid", mcLoc("item/generated"))
                .texture("layer0", modLoc("block/mimic_orchid"));

        // Grasping Limb
        simpleBlock(
                ModBlocks.GRASPING_LIMB.get(),
                models().getBuilder("grasping_limb").texture("particle", modLoc("block/grasping_limb"))
        );
        itemModels().withExistingParent("grasping_limb", mcLoc("item/generated"))
                .texture("layer0", modLoc("block/grasping_limb"));

        // Void Ichor Block (Liquid Dummy Model for DataGen)
        simpleBlock(
                ModBlocks.VOID_ICHOR_BLOCK.get(),
                models().getBuilder("void_ichor_block").texture("particle", modLoc("block/void_ichor_still"))
        );
    }
}
