package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.potion.PotionListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * Creates an EventNode with potion events.
 * This includes potion drinking, potion splashing and effects
 * for potion add and remove (like glowing and invisibility).
 */
public class PotionConfig extends ElementConfig<EntityInstanceEvent> {
	public static final PotionConfig DEFAULT = defaultBuilder().build();
	public static final PotionConfig LEGACY = legacyBuilder().build();
	
	private final boolean
			updateEffectEnabled, applyEffectEnabled, instantEffectEnabled,
			drinkingEnabled, splashEnabled, lingeringEnabled, particlesEnabled;
	
	PotionConfig(boolean legacy, boolean updateEffectEnabled, boolean applyEffectEnabled,
	             boolean instantEffectEnabled, boolean drinkingEnabled, boolean splashEnabled,
	             boolean lingeringEnabled, boolean particlesEnabled) {
		super(legacy);
		this.updateEffectEnabled = updateEffectEnabled;
		this.applyEffectEnabled = applyEffectEnabled;
		this.instantEffectEnabled = instantEffectEnabled;
		this.drinkingEnabled = drinkingEnabled;
		this.splashEnabled = splashEnabled;
		this.lingeringEnabled = lingeringEnabled;
		this.particlesEnabled = particlesEnabled;
	}
	
	public boolean isUpdateEffectEnabled() {
		return updateEffectEnabled;
	}
	
	public boolean isApplyEffectEnabled() {
		return applyEffectEnabled;
	}
	
	public boolean isInstantEffectEnabled() {
		return instantEffectEnabled;
	}
	
	public boolean isDrinkingEnabled() {
		return drinkingEnabled;
	}
	
	public boolean isSplashEnabled() {
		return splashEnabled;
	}
	
	public boolean isLingeringEnabled() {
		return lingeringEnabled;
	}
	
	public boolean isParticlesEnabled() {
		return particlesEnabled;
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		return PotionListener.events(this);
	}
	
	/**
	 * Creates a builder with the default options.
	 *
	 * @return A builder with default options
	 */
	public static PotionConfigBuilder defaultBuilder() {
		return new PotionConfigBuilder(false).defaultOptions();
	}
	
	/**
	 * Creates a builder with the legacy options.
	 *
	 * @return A builder with legacy options
	 */
	public static PotionConfigBuilder legacyBuilder() {
		return new PotionConfigBuilder(true).defaultOptions();
	}
	
	/**
	 * Creates an empty builder which has everything disabled.
	 *
	 * @return An empty builder
	 */
	public static PotionConfigBuilder emptyBuilder(boolean legacy) {
		return new PotionConfigBuilder(legacy);
	}
}
