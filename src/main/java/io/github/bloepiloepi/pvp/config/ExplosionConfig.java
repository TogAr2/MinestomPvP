package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.explosion.ExplosionListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;

public class ExplosionConfig extends ElementConfig<EntityEvent> {
	public static final ExplosionConfig DEFAULT = new ExplosionConfig(
			false, true, true, true
	);
	
	private final boolean tntEnabled;
	private final boolean crystalEnabled;
	private final boolean anchorEnabled;
	
	public ExplosionConfig(boolean legacy, boolean tntEnabled, boolean crystalEnabled, boolean anchorEnabled) {
		super(legacy);
		this.tntEnabled = tntEnabled;
		this.crystalEnabled = crystalEnabled;
		this.anchorEnabled = anchorEnabled;
	}
	
	public boolean isTntEnabled() {
		return tntEnabled;
	}
	
	public boolean isCrystalEnabled() {
		return crystalEnabled;
	}
	
	public boolean isAnchorEnabled() {
		return anchorEnabled;
	}
	
	@Override
	public EventNode<EntityEvent> createNode() {
		return ExplosionListener.events(this);
	}
}
