package me.gentworm.storymobs.world.structure;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

import me.gentworm.storymobs.StoryMobs;
import me.gentworm.storymobs.entity.PrisonZombieEntity;
import me.gentworm.storymobs.init.EntityInit;
import me.gentworm.storymobs.init.StoryMobsStructures;
import net.minecraft.block.Blocks;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructurePiece;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.TemplateStructurePiece;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class PrisonStructure extends Structure<NoFeatureConfig> {

	private static final List<MobSpawnInfo.Spawners> PRISON_ENEMIES = ImmutableList
			.of(new MobSpawnInfo.Spawners(EntityInit.PRISON_ZOMBIE_ENTITY.get(), 1, 1, 1));

	public PrisonStructure(Codec<NoFeatureConfig> p_i51440_1_) {
		super(p_i51440_1_);
	}

	@Override
	public List<MobSpawnInfo.Spawners> getDefaultSpawnList() {
		return PRISON_ENEMIES;
	}

	@Override
	public String getStructureName() {
		return StoryMobs.MODID + ":prison";
	}

	protected boolean isFeatureChunk(ChunkGenerator p_230363_1_, BiomeProvider p_230363_2_, long p_230363_3_,
			SharedSeedRandom p_230363_5_, int p_230363_6_, int p_230363_7_, Biome p_230363_8_, ChunkPos p_230363_9_,
			NoFeatureConfig p_230363_10_) {
		int i = p_230363_6_ >> 4;
		int j = p_230363_7_ >> 4;
		p_230363_5_.setSeed((long) (i ^ j << 4) ^ p_230363_3_);
		p_230363_5_.nextInt();
		for (int k = p_230363_6_ - 10; k <= p_230363_6_ + 10; ++k) {
			for (int l = p_230363_7_ - 10; l <= p_230363_7_ + 10; ++l) {
				ChunkPos chunkpos = Structure.VILLAGE.getChunkPosForStructure(
						p_230363_1_.func_235957_b_().func_236197_a_(Structure.VILLAGE), p_230363_3_, p_230363_5_, k, l);
				if (k == chunkpos.x && l == chunkpos.z) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public GenerationStage.Decoration getDecorationStage() {
		return GenerationStage.Decoration.SURFACE_STRUCTURES;
	}

	@Override
	public IStartFactory<NoFeatureConfig> getStartFactory() {
		return PrisonStructure.Start::new;
	}

	public static class Start extends StructureStart<NoFeatureConfig> {
		public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ,
				MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn) {
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
		}

		@Override
		public void func_230364_a_(DynamicRegistries dynamicRegistryManager, ChunkGenerator generator,
				TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biome, NoFeatureConfig config) {
			Rotation rotation = Rotation.values()[this.rand.nextInt(Rotation.values().length)];
			int x = (chunkX << 4) + 7;
			int z = (chunkZ << 4) + 7;
			int surfaceY = Math.max(
					generator.getNoiseHeightMinusOne(x + 12, z + 12, Heightmap.Type.WORLD_SURFACE_WG) - 1,
					generator.getGroundHeight() - 1);
			BlockPos blockpos = new BlockPos(x, surfaceY, z);
			Piece.start(templateManagerIn, blockpos, rotation, this.components, this.rand);
			this.recalculateStructureSize();
		}
	}

	public static class Piece extends TemplateStructurePiece {
		private ResourceLocation resourceLocation;
		private Rotation rotation;

		public Piece(TemplateManager templateManagerIn, ResourceLocation resourceLocationIn, BlockPos pos,
				Rotation rotationIn) {
			super(StoryMobsStructures.PRISON_PIECE_TYPE, 0);
			this.resourceLocation = resourceLocationIn;
			this.templatePosition = pos;
			this.rotation = rotationIn;
			this.setupPiece(templateManagerIn);
		}

		public Piece(TemplateManager templateManagerIn, CompoundNBT tagCompound) {
			super(StoryMobsStructures.PRISON_PIECE_TYPE, tagCompound);
			this.resourceLocation = new ResourceLocation(tagCompound.getString("Template"));
			this.rotation = Rotation.valueOf(tagCompound.getString("Rot"));
			this.setupPiece(templateManagerIn);
		}

		public static void start(TemplateManager templateManager, BlockPos pos, Rotation rotation,
				List<StructurePiece> pieceList, Random random) {
			int x = pos.getX();
			int z = pos.getZ();
			BlockPos rotationOffSet = new BlockPos(0, 0, 0).rotate(rotation);
			BlockPos blockpos = rotationOffSet.add(x, pos.getY(), z);
			pieceList.add(new Piece(templateManager, new ResourceLocation(StoryMobs.MODID, "prison/cell"), blockpos,
					rotation));
		}

		private void setupPiece(TemplateManager templateManager) {
			Template template = templateManager.getTemplateDefaulted(this.resourceLocation);
			PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation)
					.setMirror(Mirror.NONE);
			this.setup(template, this.templatePosition, placementsettings);
		}

		@Override
		protected void readAdditional(CompoundNBT tagCompound) {
			super.readAdditional(tagCompound);
			tagCompound.putString("Template", this.resourceLocation.toString());
			tagCompound.putString("Rot", this.rotation.name());
		}

		@Override
		public boolean func_230383_a_(ISeedReader seedReader, StructureManager structureManager,
				ChunkGenerator chunkGenerator, Random randomIn, MutableBoundingBox structureBoundingBoxIn,
				ChunkPos chunkPos, BlockPos pos) {
			PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation)
					.setMirror(Mirror.NONE).addProcessor(BlockIgnoreStructureProcessor.AIR_AND_STRUCTURE_BLOCK);
			BlockPos blockpos = BlockPos.ZERO;
			this.templatePosition.add(Template.transformedBlockPos(placementsettings,
					new BlockPos(-blockpos.getX(), 0, -blockpos.getZ())));
			return super.func_230383_a_(seedReader, structureManager, chunkGenerator, randomIn, structureBoundingBoxIn,
					chunkPos, pos);
		}

		@Override
		protected void handleDataMarker(String function, BlockPos pos, IServerWorld worldIn, Random rand,
				MutableBoundingBox sbb) {
			
			PrisonZombieEntity entity = EntityInit.PRISON_ZOMBIE_ENTITY.get().create(worldIn.getWorld());

			if ("chest".equals(function)) {
				worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
				TileEntity tileentity = worldIn.getTileEntity(pos.down());
				if (tileentity instanceof ChestTileEntity) {
					((ChestTileEntity) tileentity).setLootTable(LootTables.CHESTS_SIMPLE_DUNGEON, rand.nextLong());
				}
			}
			worldIn.addEntity(entity);
		}
	}
}
