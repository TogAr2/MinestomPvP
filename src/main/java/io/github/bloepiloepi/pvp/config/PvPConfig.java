package io.github.bloepiloepi.pvp.config;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

public abstract class PvPConfig<E extends Event> {
	private final boolean legacy;
	
	public PvPConfig(boolean legacy) {
		this.legacy = legacy;
	}
	
	public boolean isLegacy() {
		return legacy;
	}
	
	public abstract EventNode<E> createNode();
}
