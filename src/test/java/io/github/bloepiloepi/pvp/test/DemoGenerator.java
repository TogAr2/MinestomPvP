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

public class DemoGenerator implements ChunkGenerator {
	@Override
	public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {
		for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
			for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
				for (int y = 0; y < 60; y++) {
					batch.setBlock(x, y, z, Block.STONE);
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
