package io.github.bloepiloepi.pvp.config;

import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

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
