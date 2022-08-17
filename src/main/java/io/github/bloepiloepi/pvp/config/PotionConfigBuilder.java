package io.github.bloepiloepi.pvp.config;

public class PotionConfigBuilder {
	private final boolean legacy;
	private boolean
			updateEffectEnabled, applyEffectEnabled, instantEffectEnabled,
			drinkingEnabled, splashEnabled, lingeringEnabled, particlesEnabled;
	
	PotionConfigBuilder(boolean legacy) {
		this.legacy = legacy;
	}
	
	public PotionConfigBuilder defaultOptions() {
		updateEffectEnabled = true;
		applyEffectEnabled = true;
		instantEffectEnabled = true;
		drinkingEnabled = true;
		splashEnabled = true;
		lingeringEnabled = true;
		particlesEnabled = true;
		return this;
	}
	
	public PotionConfigBuilder updateEffect(boolean updateEffectEnabled) {
		this.updateEffectEnabled = updateEffectEnabled;
		return this;
	}
	
	public PotionConfigBuilder applyEffect(boolean applyEffectEnabled) {
		this.applyEffectEnabled = applyEffectEnabled;
		return this;
	}
	
	public PotionConfigBuilder instantEffect(boolean instantEffectEnabled) {
		this.instantEffectEnabled = instantEffectEnabled;
		return this;
	}
	
	public PotionConfigBuilder drinking(boolean drinkingEnabled) {
		this.drinkingEnabled = drinkingEnabled;
		return this;
	}
	
	public PotionConfigBuilder splash(boolean splashEnabled) {
		this.splashEnabled = splashEnabled;
		return this;
	}
	
	public PotionConfigBuilder lingering(boolean lingeringEnabled) {
		this.lingeringEnabled = lingeringEnabled;
		return this;
	}
	
	public PotionConfigBuilder particles(boolean particlesEnabled) {
		this.particlesEnabled = particlesEnabled;
		return this;
	}
	
	public PotionConfig build() {
		return new PotionConfig(
				legacy, updateEffectEnabled, applyEffectEnabled,
				instantEffectEnabled, drinkingEnabled, splashEnabled,
				lingeringEnabled, particlesEnabled
		);
	}
}
