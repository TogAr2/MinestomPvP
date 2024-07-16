package io.github.togar2.pvp.feature.provider;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.world.Difficulty;

/**
 * Certain mechanics in vanilla are dependent on difficulty.
 * This combat feature can provide which difficulty should be used.
 */
public interface DifficultyProvider extends CombatFeature {
	DifficultyProvider DEFAULT = entity -> MinecraftServer.getDifficulty();
	
	Difficulty getValue(LivingEntity entity);
}
