package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.legacy.SwordBlockHandler;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerInstanceEvent;

public class SwordBlockingConfig extends ElementConfig<PlayerInstanceEvent> {
	public static final SwordBlockingConfig LEGACY = new SwordBlockingConfig();
	
	public SwordBlockingConfig() {
		super(true);
	}
	
	@Override
	public EventNode<PlayerInstanceEvent> createNode() {
		return SwordBlockHandler.events();
	}
}
