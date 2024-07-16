package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.feature.config.CombatConfiguration;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * @deprecated use {@link CombatConfiguration} instead
 */
@Deprecated
public class ArmorToolConfig extends ElementConfig<EntityInstanceEvent> {
	public static final ArmorToolConfig DEFAULT = new ArmorToolConfig(false);
	public static final ArmorToolConfig LEGACY = new ArmorToolConfig(true);
	
	ArmorToolConfig(boolean legacy) {
		super(legacy);
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		return new CombatConfiguration().legacy(isLegacy())
				.add(CombatFeatures.VANILLA_EQUIPMENT)
				.add(CombatFeatures.VANILLA_ITEM_COOLDOWN)
				.build().createNode();
	}
}
