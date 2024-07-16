package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.config.CombatConfiguration;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * @deprecated use {@link CombatConfiguration} instead
 */
@Deprecated
public abstract class ElementConfig<E extends EntityInstanceEvent> {
	private final boolean legacy;
	
	public ElementConfig(boolean legacy) {
		this.legacy = legacy;
	}
	
	public boolean isLegacy() {
		return legacy;
	}
	
	public abstract EventNode<E> createNode();
}
