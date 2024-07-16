package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.config.CombatConfiguration;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * @deprecated use {@link CombatConfiguration} instead
 */
@Deprecated
public class PvPConfig extends ElementConfig<EntityInstanceEvent> {
	public static final PvPConfig DEFAULT = defaultBuilder().build();
	public static final PvPConfig LEGACY = legacyBuilder().build();
	
	public static final EventFilter<EntityInstanceEvent, Entity> ENTITY_INSTANCE_FILTER = EventFilter
			.from(EntityInstanceEvent.class, Entity.class, EntityEvent::getEntity);
	
	private final ElementConfig<?>[] elements;
	
	PvPConfig(ElementConfig<?>... elements) {
		super(false); // Doesn't matter because it isn't used
		this.elements = elements;
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		EventNode<EntityInstanceEvent> node = EventNode.type("pvp-events", ENTITY_INSTANCE_FILTER);
		
		for (ElementConfig<?> config : elements) {
			if (config == null) continue;
			node.addChild(config.createNode());
		}
		
		return node;
	}
	
	/**
	 * Creates a builder with the default options.
	 *
	 * @return A builder with default options
	 */
	public static PvPConfigBuilder defaultBuilder() {
		return new PvPConfigBuilder().defaultOptions();
	}
	
	/**
	 * Creates a builder with the legacy options.
	 *
	 * @return A builder with legacy options
	 */
	public static PvPConfigBuilder legacyBuilder() {
		return new PvPConfigBuilder().legacyOptions();
	}
	
	/**
	 * Creates an empty builder which has everything disabled.
	 *
	 * @return An empty builder
	 */
	public static PvPConfigBuilder emptyBuilder() {
		return new PvPConfigBuilder();
	}
}
