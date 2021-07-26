package me.gentworm.storymobs.world.modification;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;

import me.gentworm.storymobs.StoryMobs;
import me.gentworm.storymobs.world.ConfiguredStructures;
import me.gentworm.storymobs.world.StoryMobsStructures;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class PrisonModification {
	public static void biomeModification(final BiomeLoadingEvent event) {
		if (event.getCategory() == Biome.Category.PLAINS) {
			event.getGeneration().getStructures().add(() -> ConfiguredStructures.CONFIGURED_PRISON);
		}
	}
	
	private static Method GETCODEC_METHOD;

	@SuppressWarnings({ "unchecked", "resource" })
	public static void addDimensionalSpacing(final WorldEvent.Load event) {
		if (event.getWorld() instanceof ServerWorld) {
			ServerWorld serverWorld = (ServerWorld) event.getWorld();

			try {
				if (GETCODEC_METHOD == null)
					GETCODEC_METHOD = ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "getCodec");
				ResourceLocation cgRL = Registry.CHUNK_GENERATOR_CODEC
						.getKey((Codec<? extends ChunkGenerator>) GETCODEC_METHOD
								.invoke(serverWorld.getChunkProvider().generator));
				if (cgRL != null && cgRL.getNamespace().equals("terraforged"))
					return;
			} catch (Exception e) {
				StoryMobs.LOGGER.error("Was unable to check if " + serverWorld.getDimensionKey().getLocation()
						+ " is using Terraforged's ChunkGenerator.");
			}

			if (serverWorld.getChunkProvider().getChunkGenerator() instanceof FlatChunkGenerator
					&& serverWorld.getDimensionKey().equals(World.OVERWORLD)) {
				return;
			}

			Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(
					serverWorld.getChunkProvider().generator.func_235957_b_().func_236195_a_());
			tempMap.putIfAbsent(StoryMobsStructures.PRISON.get(),
					DimensionStructuresSettings.field_236191_b_.get(StoryMobsStructures.PRISON.get()));
			serverWorld.getChunkProvider().generator.func_235957_b_().field_236193_d_ = tempMap;
		}
	}
}
