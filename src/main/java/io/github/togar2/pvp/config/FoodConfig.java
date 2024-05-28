package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.feature.config.CombatConfiguration;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * @deprecated use {@link CombatConfiguration} instead
 */
@Deprecated
public class FoodConfig extends ElementConfig<EntityInstanceEvent> {
	public static final FoodConfig DEFAULT = new FoodConfig(false);
	public static final FoodConfig LEGACY = new FoodConfig(true);
	
	FoodConfig(boolean legacy) {
		super(legacy);
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		return new CombatConfiguration().legacy(isLegacy())
				.add(CombatFeatures.VANILLA_FOOD)
				.add(CombatFeatures.VANILLA_EXHAUSTION)
				.add(CombatFeatures.VANILLA_REGENERATION)
				.build().createNode();
	}
}
