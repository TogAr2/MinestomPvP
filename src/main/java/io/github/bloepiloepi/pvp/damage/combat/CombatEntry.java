package io.github.bloepiloepi.pvp.damage.combat;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public record CombatEntry(CustomDamageType damageType, float damage,
                          @Nullable String fallLocation, double fallDistance) {
	
	public String getMessageFallLocation() {
		return fallLocation == null ? "generic" : fallLocation;
	}
	
	public double getFallDistance() {
		return damageType.isOutOfWorld() ? Double.MAX_VALUE : fallDistance;
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
