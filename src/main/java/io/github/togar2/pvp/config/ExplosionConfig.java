package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.config.CombatConfiguration;
import io.github.togar2.pvp.feature.config.CombatFeatures;
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
		return new CombatConfiguration().legacy(isLegacy())
				.add(CombatFeatures.VANILLA_EXPLOSION)
				.build().createNode();
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
