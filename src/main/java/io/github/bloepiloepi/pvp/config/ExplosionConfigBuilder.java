package io.github.bloepiloepi.pvp.config;

public class ExplosionConfigBuilder {
	private final boolean legacy;
	private boolean tntEnabled, crystalEnabled, anchorEnabled;
	
	ExplosionConfigBuilder(boolean legacy) {
		this.legacy = legacy;
	}
	
	public ExplosionConfigBuilder defaultOptions() {
		tntEnabled = true;
		crystalEnabled = true;
		anchorEnabled = true;
		return this;
	}
	
	public ExplosionConfigBuilder tnt(boolean tntEnabled) {
		this.tntEnabled = tntEnabled;
		return this;
	}
	
	public ExplosionConfigBuilder crystal(boolean crystalEnabled) {
		this.crystalEnabled = crystalEnabled;
		return this;
	}
	
	public ExplosionConfigBuilder anchor(boolean anchorEnabled) {
		this.anchorEnabled = anchorEnabled;
		return this;
	}
	
	public ExplosionConfig build() {
		return new ExplosionConfig(legacy, tntEnabled, crystalEnabled, anchorEnabled);
	}
}
