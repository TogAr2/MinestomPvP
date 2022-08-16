package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.projectile.ProjectileListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerEvent;

/**
 * Creates an EventNode with projectile events.
 * This includes fishing rods, snowballs, eggs,
 * ender pearls, bows and crossbows.
 */
public class ProjectileConfig extends ElementConfig<PlayerEvent> {
	public static final ProjectileConfig DEFAULT = new ProjectileConfigBuilder(false).defaultOptions().build();
	public static final ProjectileConfig LEGACY = new ProjectileConfigBuilder(true).defaultOptions().build();
	
	private final boolean
			fishingRodEnabled, snowballEnabled, eggEnabled,
			enderPearlEnabled, crossbowEnabled, bowEnabled;
	
	ProjectileConfig(boolean legacy, boolean fishingRodEnabled, boolean snowballEnabled,
	                        boolean eggEnabled, boolean enderPearlEnabled, boolean crossbowEnabled,
	                        boolean bowEnabled) {
		super(legacy);
		this.fishingRodEnabled = fishingRodEnabled;
		this.snowballEnabled = snowballEnabled;
		this.eggEnabled = eggEnabled;
		this.enderPearlEnabled = enderPearlEnabled;
		this.crossbowEnabled = crossbowEnabled;
		this.bowEnabled = bowEnabled;
	}
	
	public boolean isFishingRodEnabled() {
		return fishingRodEnabled;
	}
	
	public boolean isSnowballEnabled() {
		return snowballEnabled;
	}
	
	public boolean isEggEnabled() {
		return eggEnabled;
	}
	
	public boolean isEnderPearlEnabled() {
		return enderPearlEnabled;
	}
	
	public boolean isCrossbowEnabled() {
		return crossbowEnabled;
	}
	
	public boolean isBowEnabled() {
		return bowEnabled;
	}
	
	@Override
	public EventNode<PlayerEvent> createNode() {
		return ProjectileListener.events(this);
	}
	
	public static ProjectileConfigBuilder builder(boolean legacy) {
		return new ProjectileConfigBuilder(legacy);
	}
}
