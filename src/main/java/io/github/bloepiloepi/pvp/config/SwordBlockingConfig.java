package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.legacy.SwordBlockHandler;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerEvent;

public class SwordBlockingConfig extends ElementConfig<PlayerEvent> {
	public static final SwordBlockingConfig LEGACY = new SwordBlockingConfig();
	
	public SwordBlockingConfig() {
		super(true);
	}
	
	@Override
	public EventNode<PlayerEvent> createNode() {
		return SwordBlockHandler.events();
	}
}
