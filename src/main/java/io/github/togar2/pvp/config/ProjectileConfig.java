package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.feature.config.CombatConfiguration;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * @deprecated use {@link CombatConfiguration} instead
 */
@Deprecated
public class ProjectileConfig extends ElementConfig<EntityInstanceEvent> {
	public static final ProjectileConfig DEFAULT = new ProjectileConfig(false);
	public static final ProjectileConfig LEGACY = new ProjectileConfig(true);
	
	ProjectileConfig(boolean legacy) {
		super(legacy);
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		return new CombatConfiguration().legacy(isLegacy())
				.add(CombatFeatures.VANILLA_BOW)
				.add(CombatFeatures.VANILLA_CROSSBOW)
				.add(CombatFeatures.VANILLA_FISHING_ROD)
				.add(CombatFeatures.VANILLA_MISC_PROJECTILE)
				.add(CombatFeatures.VANILLA_PROJECTILE_ITEM)
				.add(CombatFeatures.VANILLA_TRIDENT)
				.add(CombatFeatures.VANILLA_ITEM_DAMAGE)
				.build().createNode();
	}
}
