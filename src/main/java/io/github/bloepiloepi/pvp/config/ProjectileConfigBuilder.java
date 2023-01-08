package io.github.bloepiloepi.pvp.config;

public class ProjectileConfigBuilder {
	private final boolean legacy;
	private boolean
			fishingRodEnabled, snowballEnabled, eggEnabled,
			enderPearlEnabled, crossbowEnabled, bowEnabled,
			tridentEnabled;
	
	public ProjectileConfigBuilder(boolean legacy) {
		this.legacy = legacy;
	}
	
	public ProjectileConfigBuilder defaultOptions() {
		fishingRodEnabled = true;
		snowballEnabled = true;
		eggEnabled = true;
		enderPearlEnabled = true;
		crossbowEnabled = true;
		bowEnabled = true;
		tridentEnabled = true;
		return this;
	}
	
	public ProjectileConfigBuilder fishingRod(boolean fishingRodEnabled) {
		this.fishingRodEnabled = fishingRodEnabled;
		return this;
	}
	
	public ProjectileConfigBuilder snowball(boolean snowballEnabled) {
		this.snowballEnabled = snowballEnabled;
		return this;
	}
	
	public ProjectileConfigBuilder egg(boolean eggEnabled) {
		this.eggEnabled = eggEnabled;
		return this;
	}
	
	public ProjectileConfigBuilder enderPearl(boolean enderPearlEnabled) {
		this.enderPearlEnabled = enderPearlEnabled;
		return this;
	}
	
	public ProjectileConfigBuilder crossbow(boolean crossbowEnabled) {
		this.crossbowEnabled = crossbowEnabled;
		return this;
	}
	
	public ProjectileConfigBuilder bow(boolean bowEnabled) {
		this.bowEnabled = bowEnabled;
		return this;
	}
	
	public ProjectileConfigBuilder trident(boolean tridentEnabled) {
		this.tridentEnabled = tridentEnabled;
		return this;
	}
	
	public ProjectileConfig build() {
		return new ProjectileConfig(
				legacy, fishingRodEnabled, snowballEnabled, eggEnabled,
				enderPearlEnabled, crossbowEnabled, bowEnabled, tridentEnabled
		);
	}
}
