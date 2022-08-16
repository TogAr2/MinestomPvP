package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.projectile.ProjectileListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerEvent;

public class ProjectileConfig extends ElementConfig<PlayerEvent> {
	public static final ProjectileConfig DEFAULT = new ProjectileConfig(
			false, true, true,
			true, true, true,
			true
	);
	public static final ProjectileConfig LEGACY = new ProjectileConfig(
			true, true, true,
			true, true, true,
			true
	);
	
	private final boolean fishingRodEnabled;
	private final boolean snowballEnabled;
	private final boolean eggEnabled;
	private final boolean enderPearlEnabled;
	private final boolean crossbowEnabled;
	private final boolean bowEnabled;
	
	public ProjectileConfig(boolean legacy, boolean fishingRodEnabled, boolean snowballEnabled, boolean eggEnabled, boolean enderPearlEnabled, boolean crossbowEnabled, boolean bowEnabled) {
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
}
