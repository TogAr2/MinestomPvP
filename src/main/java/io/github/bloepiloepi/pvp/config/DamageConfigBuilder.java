package io.github.bloepiloepi.pvp.config;

public class DamageConfigBuilder {
	private final boolean legacy;
	private boolean
			fallDamageEnabled, equipmentDamageEnabled, shieldEnabled,
			legacyShieldMechanics, armorEnabled, exhaustionEnabled,
			legacyKnockback, soundsEnabled, damageAnimation, performDamage;
	private int invulnerabilityTicks;
	
	DamageConfigBuilder(boolean legacy) {
		this.legacy = legacy;
	}
	
	public DamageConfigBuilder defaultOptions() {
		fallDamageEnabled = true;
		equipmentDamageEnabled = true;
		shieldEnabled = true;
		legacyShieldMechanics = false;
		invulnerabilityTicks = 10;
		armorEnabled = true;
		exhaustionEnabled = true;
		legacyKnockback = false;
		soundsEnabled = true;
		damageAnimation = true;
		performDamage = true;
		return this;
	}
	
	public DamageConfigBuilder legacyOptions() {
		fallDamageEnabled = true;
		equipmentDamageEnabled = true;
		shieldEnabled = true;
		legacyShieldMechanics = true;
		invulnerabilityTicks = 10;
		armorEnabled = true;
		exhaustionEnabled = true;
		legacyKnockback = true;
		soundsEnabled = true;
		damageAnimation = true;
		performDamage = true;
		return this;
	}
	
	public DamageConfigBuilder fallDamage(boolean fallDamageEnabled) {
		this.fallDamageEnabled = fallDamageEnabled;
		return this;
	}
	
	public DamageConfigBuilder equipmentDamage(boolean equipmentDamageEnabled) {
		this.equipmentDamageEnabled = equipmentDamageEnabled;
		return this;
	}
	
	public DamageConfigBuilder shield(boolean shieldEnabled) {
		this.shieldEnabled = shieldEnabled;
		return this;
	}
	
	public DamageConfigBuilder legacyShieldMechanics(boolean legacyShieldMechanics) {
		this.legacyShieldMechanics = legacyShieldMechanics;
		return this;
	}
	
	public DamageConfigBuilder invulnerabilityTicks(int invulnerabilityTicks) {
		this.invulnerabilityTicks = invulnerabilityTicks;
		return this;
	}
	
	public DamageConfigBuilder armor(boolean armorEnabled) {
		this.armorEnabled = armorEnabled;
		return this;
	}
	
	public DamageConfigBuilder exhaustion(boolean exhaustionEnabled) {
		this.exhaustionEnabled = exhaustionEnabled;
		return this;
	}
	
	public DamageConfigBuilder legacyKnockback(boolean legacyKnockback) {
		this.legacyKnockback = legacyKnockback;
		return this;
	}
	
	public DamageConfigBuilder sounds(boolean soundsEnabled) {
		this.soundsEnabled = soundsEnabled;
		return this;
	}
	
	public DamageConfigBuilder animation(boolean animation) {
		this.damageAnimation = animation;
		return this;
	}
	
	public DamageConfigBuilder performDamage(boolean performDamage) {
		this.performDamage = performDamage;
		return this;
	}
	
	public DamageConfig build() {
		return new DamageConfig(
				legacy, fallDamageEnabled, equipmentDamageEnabled,
				shieldEnabled, legacyShieldMechanics, invulnerabilityTicks,
				armorEnabled, exhaustionEnabled, legacyKnockback,
				soundsEnabled, damageAnimation, performDamage
		);
	}
}
