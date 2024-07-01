package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.feature.config.CombatConfiguration;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * @deprecated use {@link CombatConfiguration} instead
 */
@Deprecated
public class DamageConfig extends ElementConfig<EntityInstanceEvent> {
	public static final DamageConfig DEFAULT = new DamageConfig(false);
	public static final DamageConfig LEGACY = new DamageConfig(true);
	
	public DamageConfig(boolean legacy) {
		super(legacy);
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		return new CombatConfiguration().legacy(isLegacy())
				.add(CombatFeatures.VANILLA_DAMAGE)
				.add(CombatFeatures.VANILLA_BLOCK)
				.add(CombatFeatures.VANILLA_ARMOR)
				.add(CombatFeatures.VANILLA_PLAYER_STATE)
				.add(CombatFeatures.VANILLA_TOTEM)
				.add(CombatFeatures.VANILLA_EXHAUSTION)
				.add(CombatFeatures.VANILLA_KNOCKBACK)
				.add(CombatFeatures.VANILLA_DEATH_MESSAGE)
				.add(CombatFeatures.VANILLA_ITEM_DAMAGE)
				.add(CombatFeatures.VANILLA_FALL)
				.build().createNode();
	}
}
