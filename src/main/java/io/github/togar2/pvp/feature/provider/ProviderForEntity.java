package io.github.togar2.pvp.feature.provider;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.world.Difficulty;

public interface ProviderForEntity<T> extends CombatFeature {
	ProviderForEntity<Difficulty> DIFFICULTY = entity -> MinecraftServer.getDifficulty();
	
	T getValue(LivingEntity entity);
}
