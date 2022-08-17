package io.github.bloepiloepi.pvp.config;

public class AttackConfigBuilder {
	private final boolean legacy;
	private boolean
			spectatingEnabled, attackCooldownEnabled, legacyKnockback,
			soundsEnabled, toolDamageEnabled, damageIndicatorParticlesEnabled,
			exhaustionEnabled;
	
	AttackConfigBuilder(boolean legacy) {
		this.legacy = legacy;
	}
	
	public AttackConfigBuilder defaultOptions() {
		spectatingEnabled = true;
		attackCooldownEnabled = true;
		legacyKnockback = false;
		soundsEnabled = true;
		toolDamageEnabled = true;
		damageIndicatorParticlesEnabled = true;
		exhaustionEnabled = true;
		return this;
	}
	
	public AttackConfigBuilder legacyOptions() {
		spectatingEnabled = true;
		attackCooldownEnabled = false;
		legacyKnockback = true;
		soundsEnabled = false;
		toolDamageEnabled = true;
		damageIndicatorParticlesEnabled = false;
		exhaustionEnabled = true;
		return this;
	}
	
	public AttackConfigBuilder spectating(boolean spectatingEnabled) {
		this.spectatingEnabled = spectatingEnabled;
		return this;
	}
	
	public AttackConfigBuilder attackCooldown(boolean attackCooldownEnabled) {
		this.attackCooldownEnabled = attackCooldownEnabled;
		return this;
	}
	
	public AttackConfigBuilder legacyKnockback(boolean legacyKnockback) {
		this.legacyKnockback = legacyKnockback;
		return this;
	}
	
	public AttackConfigBuilder sounds(boolean soundsEnabled) {
		this.soundsEnabled = soundsEnabled;
		return this;
	}
	
	public AttackConfigBuilder toolDamage(boolean toolDamageEnabled) {
		this.toolDamageEnabled = toolDamageEnabled;
		return this;
	}
	
	public AttackConfigBuilder damageIndicatorParticles(boolean damageIndicatorParticlesEnabled) {
		this.damageIndicatorParticlesEnabled = damageIndicatorParticlesEnabled;
		return this;
	}
	
	public AttackConfigBuilder exhaustion(boolean exhaustionEnabled) {
		this.exhaustionEnabled = exhaustionEnabled;
		return this;
	}
	
	public AttackConfig build() {
		return new AttackConfig(
				legacy, spectatingEnabled, attackCooldownEnabled,
				legacyKnockback, soundsEnabled, toolDamageEnabled,
				damageIndicatorParticlesEnabled, exhaustionEnabled
		);
	}
}
