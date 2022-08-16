package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.listeners.AttackManager;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;

/**
 * Creates an EventNode with attack events.
 * This includes entity hitting, attack cooldown
 * and spectating entities as a spectator.
 */
public class AttackConfig extends ElementConfig<EntityEvent> {
	public static final AttackConfig DEFAULT = new AttackConfigBuilder(false).defaultOptions().build();
	public static final AttackConfig LEGACY = new AttackConfigBuilder(true).legacyOptions().build();
	
	private final boolean
			spectatingEnabled, attackCooldownEnabled, legacyKnockback,
			soundsEnabled, toolDamageEnabled, damageIndicatorParticlesEnabled,
			exhaustionEnabled;
	
	AttackConfig(boolean legacy, boolean spectatingEnabled, boolean attackCooldownEnabled, boolean legacyKnockback,
	             boolean soundsEnabled, boolean toolDamageEnabled, boolean damageIndicatorParticlesEnabled,
	             boolean exhaustionEnabled) {
		super(legacy);
		this.spectatingEnabled = spectatingEnabled;
		this.attackCooldownEnabled = attackCooldownEnabled;
		this.legacyKnockback = legacyKnockback;
		this.soundsEnabled = soundsEnabled;
		this.toolDamageEnabled = toolDamageEnabled;
		this.damageIndicatorParticlesEnabled = damageIndicatorParticlesEnabled;
		this.exhaustionEnabled = exhaustionEnabled;
	}
	
	public boolean isSpectatingEnabled() {
		return spectatingEnabled;
	}
	
	public boolean isAttackCooldownEnabled() {
		return attackCooldownEnabled;
	}
	
	public boolean isLegacyKnockback() {
		return legacyKnockback;
	}
	
	public boolean isSoundsEnabled() {
		return soundsEnabled;
	}
	
	public boolean isToolDamageEnabled() {
		return toolDamageEnabled;
	}
	
	public boolean isDamageIndicatorParticlesEnabled() {
		return damageIndicatorParticlesEnabled;
	}
	
	public boolean isExhaustionEnabled() {
		return exhaustionEnabled;
	}
	
	@Override
	public EventNode<EntityEvent> createNode() {
		return AttackManager.events(this);
	}
	
	public static AttackConfigBuilder builder(boolean legacy) {
		return new AttackConfigBuilder(legacy);
	}
}
