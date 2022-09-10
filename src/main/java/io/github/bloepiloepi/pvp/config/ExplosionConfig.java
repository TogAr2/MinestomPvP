package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.explosion.ExplosionListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * Creates an EventNode with explosion events.
 * This includes tnt, end crystals and respawn anchors.
 */
public class ExplosionConfig extends ElementConfig<EntityInstanceEvent> {
	public static final ExplosionConfig DEFAULT = defaultBuilder().build();
	
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
	public EventNode<EntityInstanceEvent> createNode() {
		return ExplosionListener.events(this);
	}
	
	/**
	 * Creates a builder with the default options.
	 *
	 * @return A builder with default options
	 */
	public static ExplosionConfigBuilder defaultBuilder() {
		return new ExplosionConfigBuilder(false).defaultOptions();
	}
	
	/**
	 * Creates a builder with the legacy options.
	 *
	 * @return A builder with legacy options
	 */
	public static ExplosionConfigBuilder legacyBuilder() {
		return new ExplosionConfigBuilder(true).defaultOptions();
	}
	
	/**
	 * Creates an empty builder which has everything disabled.
	 *
	 * @return An empty builder
	 */
	public static ExplosionConfigBuilder emptyBuilder(boolean legacy) {
		return new ExplosionConfigBuilder(legacy);
	}
}
