package io.github.togar2.pvp.test;

import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.GenerationUnit;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

public class DemoGenerator implements Generator {
	@Override
	public void generate(@NotNull GenerationUnit unit) {
		Point start = unit.absoluteStart();
		Point end = unit.absoluteEnd();
		for (int x = start.blockX(); x < end.blockX(); x++) {
			for (int z = start.blockZ(); z < end.blockZ(); z++) {
				for (int y = 0; y < 60; y++) {
					if (ThreadLocalRandom.current().nextInt(10) == 1) {
						unit.modifier().setBlock(x, y, z, Block.GOLD_BLOCK);
					} else {
						unit.modifier().setBlock(x, y, z, Block.QUARTZ_BLOCK);
					}
				}
			}
		}
	}
}
