package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.potion.PotionListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;

/**
 * Creates an EventNode with potion events.
 * This includes potion drinking, potion splashing and effects
 * for potion add and remove (like glowing and invisibility).
 */
public class PotionConfig extends ElementConfig<EntityEvent> {
	public static final PotionConfig DEFAULT = new PotionConfigBuilder(false).defaultOptions().build();
	public static final PotionConfig LEGACY = new PotionConfigBuilder(true).defaultOptions().build();
	
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
	public EventNode<EntityEvent> createNode() {
		return PotionListener.events(this);
	}
	
	public static PotionConfigBuilder builder(boolean legacy) {
		return new PotionConfigBuilder(legacy);
	}
}
