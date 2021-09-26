package io.github.bloepiloepi.pvp.test;

import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.ChunkGenerator;
import net.minestom.server.instance.ChunkPopulator;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DemoGenerator implements ChunkGenerator {
	@Override
	public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
		for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
			for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
				for (int y = 0; y < 60; y++) {
					if (ThreadLocalRandom.current().nextInt(10) == 1) {
						batch.setBlock(x, y, z, Block.GOLD_BLOCK);
					} else {
						batch.setBlock(x, y, z, Block.QUARTZ_BLOCK);
					}
				}
			}
		}
	}
	
	@Override
	public void fillBiomes(@NotNull Biome[] biomes, int chunkX, int chunkZ) {
		Arrays.fill(biomes, Biome.PLAINS);
	}
	
	@Override
	public @Nullable List<ChunkPopulator> getPopulators() {
		return null;
	}
}
