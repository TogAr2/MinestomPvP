package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.listeners.DamageListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;

public class DamageConfig extends PvPConfig<EntityEvent> {
	public static final DamageConfig DEFAULT = new DamageConfig(
			false, true, true,
			true, false, 10,
			true, true, false,
			true
	);
	public static final DamageConfig LEGACY = new DamageConfig(
			true, true, true,
			true, true, 10,
			true, true, true,
			true
	);
	
	private final boolean fallDamageEnabled;
	private final boolean equipmentDamageEnabled;
	private final boolean shieldEnabled;
	private final boolean legacyShieldMechanics;
	private final int invulnerabilityTicks;
	private final boolean armorEnabled;
	private final boolean exhaustionEnabled;
	private final boolean legacyKnockback;
	private final boolean soundsEnabled;
	
	public DamageConfig(boolean legacy, boolean fallDamageEnabled, boolean equipmentDamageEnabled, boolean shieldEnabled, boolean legacyShieldMechanics, int invulnerabilityTicks, boolean armorEnabled, boolean exhaustionEnabled, boolean legacyKnockback, boolean soundsEnabled) {
		super(legacy);
		this.fallDamageEnabled = fallDamageEnabled;
		this.equipmentDamageEnabled = equipmentDamageEnabled;
		this.shieldEnabled = shieldEnabled;
		this.legacyShieldMechanics = legacyShieldMechanics;
		this.invulnerabilityTicks = invulnerabilityTicks;
		this.armorEnabled = armorEnabled;
		this.exhaustionEnabled = exhaustionEnabled;
		this.legacyKnockback = legacyKnockback;
		this.soundsEnabled = soundsEnabled;
	}
	
	public boolean isFallDamageEnabled() {
		return fallDamageEnabled;
	}
	
	public boolean isEquipmentDamageEnabled() {
		return equipmentDamageEnabled;
	}
	
	public boolean isShieldEnabled() {
		return shieldEnabled;
	}
	
	public boolean isLegacyShieldMechanics() {
		return legacyShieldMechanics;
	}
	
	public int getInvulnerabilityTicks() {
		return invulnerabilityTicks;
	}
	
	public boolean isArmorDisabled() {
		return !armorEnabled;
	}
	
	public boolean isExhaustionEnabled() {
		return exhaustionEnabled;
	}
	
	public boolean isLegacyKnockback() {
		return legacyKnockback;
	}
	
	public boolean isSoundsEnabled() {
		return soundsEnabled;
	}
	
	@Override
	public EventNode<EntityEvent> createNode() {
		return DamageListener.events(this);
	}
}
