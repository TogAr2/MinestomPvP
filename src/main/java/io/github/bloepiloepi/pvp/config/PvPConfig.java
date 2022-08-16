package io.github.bloepiloepi.pvp.config;

import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;

/**
 * Creates an EventNode which contains the event nodes from all the child configs you specify.
 * The default and the legacy version are already specified: {@code PvPConfig.DEFAULT} and {@code PvPConfig.LEGACY}.
 *
 * If you want more customization, you can also use the empty builder to enable features by yourself.
 */
public class PvPConfig extends ElementConfig<EntityEvent> {
	public static final PvPConfig DEFAULT = new PvPConfigBuilder().defaultOptions().build();
	public static final PvPConfig LEGACY = new PvPConfigBuilder().legacyOptions().build();
	
	private final ElementConfig<?>[] elements;
	
	PvPConfig(ElementConfig<?>... elements) {
		super(false); // Doesn't matter because it isn't used
		this.elements = elements;
	}
	
	@Override
	public EventNode<EntityEvent> createNode() {
		EventNode<EntityEvent> node = EventNode.type("pvp-events", EventFilter.ENTITY);
		
		for (ElementConfig<?> config : elements) {
			if (config == null) continue;
			node.addChild(config.createNode());
		}
		
		return node;
	}
	
	/**
	 * Creates an empty builder which has everything disabled.
	 *
	 * @return An empty builder
	 */
	public static PvPConfigBuilder builder() {
		return new PvPConfigBuilder();
	}
}
