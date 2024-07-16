package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.feature.config.CombatConfiguration;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * @deprecated use {@link CombatConfiguration} instead
 */
@Deprecated
public class AttackConfig extends ElementConfig<EntityInstanceEvent> {
	public static final AttackConfig DEFAULT = new AttackConfig(false);
	public static final AttackConfig LEGACY = new AttackConfig(true);
	
	AttackConfig(boolean legacy) {
		super(legacy);
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		return new CombatConfiguration().legacy(isLegacy())
				.add(CombatFeatures.VANILLA_ENCHANTMENT)
				.add(CombatFeatures.VANILLA_SPECTATE)
				.add(CombatFeatures.VANILLA_ATTACK)
				.add(CombatFeatures.VANILLA_ATTACK_COOLDOWN)
				.add(CombatFeatures.VANILLA_EXHAUSTION)
				.add(CombatFeatures.VANILLA_ITEM_DAMAGE)
				.add(CombatFeatures.VANILLA_CRITICAL)
				.add(CombatFeatures.VANILLA_SWEEPING)
				.add(CombatFeatures.VANILLA_KNOCKBACK)
				.build().createNode();
	}
}
