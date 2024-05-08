package io.github.togar2.pvp.feature.tracking;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;

public interface TrackingFeature extends CombatFeature {
	void recordDamage(Player player, Entity attacker, Damage damage);
}
