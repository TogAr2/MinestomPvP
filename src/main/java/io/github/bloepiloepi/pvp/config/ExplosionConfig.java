package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.explosion.ExplosionListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;

/**
 * Creates an EventNode with explosion events.
 * This includes tnt, end crystals and respawn anchors.
 */
public class ExplosionConfig extends ElementConfig<EntityEvent> {
	public static final ExplosionConfig DEFAULT = new ExplosionConfigBuilder(false).defaultOptions().build();
	
	private final boolean tntEnabled, crystalEnabled, anchorEnabled;
	
	ExplosionConfig(boolean legacy, boolean tntEnabled, boolean crystalEnabled, boolean anchorEnabled) {
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
	
	/**
	 * Creates an empty builder which has everything disabled.
	 *
	 * @return An empty builder
	 */
	public static ExplosionConfigBuilder builder() {
		return new ExplosionConfigBuilder(false);
	}
}
