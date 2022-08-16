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
	public static final PotionConfig DEFAULT = new PotionConfig(
			false, true, true,
			true, true, true,
			true, true
	);
	public static final PotionConfig LEGACY = new PotionConfig(
			true, true, true,
			true, true, true,
			true, true
	);
	
	private final boolean updateEffectEnabled;
	private final boolean applyEffectEnabled;
	private final boolean instantEffectEnabled;
	private final boolean drinkingEnabled;
	private final boolean splashEnabled;
	private final boolean lingeringEnabled;
	private final boolean particlesEnabled;
	
	public PotionConfig(boolean legacy, boolean updateEffectEnabled, boolean applyEffectEnabled, boolean instantEffectEnabled, boolean drinkingEnabled, boolean splashEnabled, boolean lingeringEnabled, boolean particlesEnabled) {
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
}
