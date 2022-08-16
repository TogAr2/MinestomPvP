package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.listeners.AttackManager;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;

public class AttackConfig extends ElementConfig<EntityEvent> {
	public static final AttackConfig DEFAULT = new AttackConfig(
			false, true, true,
			false, true, true,
			true, true
	);
	public static final AttackConfig LEGACY = new AttackConfig(
			true, true, false,
			true, false, true,
			false, true
	);
	
	private final boolean spectatingEnabled;
	private final boolean attackCooldownEnabled;
	private final boolean legacyKnockback;
	private final boolean soundsEnabled;
	private final boolean toolDamageEnabled;
	private final boolean damageIndicatorParticlesEnabled;
	private final boolean exhaustionEnabled;
	
	public AttackConfig(boolean legacy, boolean spectatingEnabled, boolean attackCooldownEnabled, boolean legacyKnockback,
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
}
