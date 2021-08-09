package io.github.bloepiloepi.pvp.damage.combat;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class CombatEntry {
	private final CustomDamageType damageType;
	private final float damage;
	private final String fallLocation;
	private final float fallDistance;
	
	public CombatEntry(CustomDamageType damageType, float damage,
	                   @Nullable String fallLocation, float fallDistance) {
		this.damageType = damageType;
		this.damage = damage;
		this.fallLocation = fallLocation;
		this.fallDistance = fallDistance;
	}
	
	public CustomDamageType getDamageType() {
		return damageType;
	}
	
	public float getDamage() {
		return damage;
	}
	
	public @Nullable String getFallLocation() {
		return fallLocation;
	}
	
	public String getMessageFallLocation() {
		return fallLocation == null ? "generic" : fallLocation;
	}
	
	public float getFallDistance() {
		return damageType.isOutOfWorld() ? Float.MAX_VALUE : fallDistance;
	}
	
	public boolean isCombat() {
		return damageType.getEntity() instanceof LivingEntity;
	}
	
	public @Nullable Entity getAttacker() {
		return damageType.getEntity();
	}
	
	public @Nullable Component getAttackerName() {
		return getAttacker() == null ? null : EntityUtils.getName(getAttacker());
	}
}
