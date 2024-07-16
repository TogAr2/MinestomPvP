package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.feature.config.CombatConfiguration;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * @deprecated use {@link CombatConfiguration} instead
 */
@Deprecated
public class PotionConfig extends ElementConfig<EntityInstanceEvent> {
	public static final PotionConfig DEFAULT = new PotionConfig(false);
	public static final PotionConfig LEGACY = new PotionConfig(true);
	
	PotionConfig(boolean legacy) {
		super(legacy);
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		return new CombatConfiguration().legacy(isLegacy())
				.add(CombatFeatures.VANILLA_EFFECT)
				.add(CombatFeatures.VANILLA_POTION)
				.build().createNode();
	}
}
