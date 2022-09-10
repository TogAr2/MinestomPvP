package io.github.bloepiloepi.pvp.config;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Creates an EventNode which contains the event nodes from all the child configs you specify.
 * The default and the legacy version are already specified: {@code PvPConfig.DEFAULT} and {@code PvPConfig.LEGACY}.
 * If you want more customization, you can also use the empty builder to enable features by yourself.
 */
public class PvPConfig extends ElementConfig<EntityInstanceEvent> {
	public static final PvPConfig DEFAULT = defaultBuilder().build();
	public static final PvPConfig LEGACY = legacyBuilder().build();
	
	public static final EventFilter<EntityInstanceEvent, Entity> ENTITY_INSTANCE_FILTER = EventFilter
			.from(EntityInstanceEvent.class, Entity.class, EntityEvent::getEntity);
	public static final EventFilter<PlayerInstanceEvent, Player> PLAYER_INSTANCE_FILTER = EventFilter
			.from(PlayerInstanceEvent.class, Player.class, PlayerEvent::getEntity);
	
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
