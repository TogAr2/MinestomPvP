package io.github.togar2.pvp.damage.combat;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.entity.EntityUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.Nullable;

public record CombatEntry(Damage damage, @Nullable String fallLocation, double fallDistance) {
	
	public String getMessageFallLocation() {
		return fallLocation == null ? "generic" : fallLocation;
	}
	
	public double getFallDistance() {
		DamageTypeInfo info = DamageTypeInfo.of(MinecraftServer.getDamageTypeRegistry().get(damage.getType()));
		return info.outOfWorld() ? Double.MAX_VALUE : fallDistance;
	}
	
	public boolean isCombat() {
		return damage.getAttacker() instanceof LivingEntity;
	}
	
	public @Nullable Entity getAttacker() {
		return damage.getAttacker();
	}
	
	public @Nullable Component getAttackerName() {
		return getAttacker() == null ? null : EntityUtils.getName(getAttacker());
	}
}
