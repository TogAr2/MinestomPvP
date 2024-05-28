package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.feature.config.CombatConfiguration;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * @deprecated use {@link CombatConfiguration} instead
 */
@Deprecated
public class SwordBlockingConfig extends ElementConfig<EntityInstanceEvent> {
	public static final SwordBlockingConfig LEGACY = new SwordBlockingConfig();
	
	public SwordBlockingConfig() {
		super(true);
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		return new CombatConfiguration().legacy(isLegacy())
				.add(CombatFeatures.LEGACY_VANILLA_BLOCK)
				.build().createNode();
	}
}
