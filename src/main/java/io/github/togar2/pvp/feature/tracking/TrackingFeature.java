package io.github.togar2.pvp.feature.tracking;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.Nullable;

public interface TrackingFeature extends CombatFeature {
	TrackingFeature NO_OP = (player, attacker, damage) -> {};
	
	void recordDamage(Player player, @Nullable Entity attacker, Damage damage);
}
