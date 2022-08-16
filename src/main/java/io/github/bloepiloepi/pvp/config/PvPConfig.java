package io.github.bloepiloepi.pvp.config;

import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;

public class PvPConfig extends ElementConfig<EntityEvent> {
	public static final PvPConfig DEFAULT = new PvPConfig(
			AttackConfig.DEFAULT, DamageConfig.DEFAULT,
			ExplosionConfig.DEFAULT, ArmorToolConfig.DEFAULT,
			FoodConfig.DEFAULT, PotionConfig.DEFAULT,
			ProjectileConfig.DEFAULT
	);
	public static final PvPConfig LEGACY = new PvPConfig(
			AttackConfig.LEGACY, DamageConfig.LEGACY,
			ExplosionConfig.DEFAULT, ArmorToolConfig.LEGACY,
			FoodConfig.LEGACY, PotionConfig.LEGACY,
			ProjectileConfig.LEGACY, SwordBlockingConfig.LEGACY
	);
	
	private final ElementConfig<?>[] elements;
	
	public PvPConfig(ElementConfig<?>... elements) {
		super(false); // Doesn't matter because it isn't used
		this.elements = elements;
	}
	
	@Override
	public EventNode<EntityEvent> createNode() {
		EventNode<EntityEvent> node = EventNode.type("pvp-events", EventFilter.ENTITY);
		
		for (ElementConfig<?> config : elements) {
			node.addChild(config.createNode());
		}
		
		return node;
	}
}
