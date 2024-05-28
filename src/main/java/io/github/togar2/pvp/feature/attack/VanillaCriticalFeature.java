package io.github.togar2.pvp.feature.attack;

import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.config.FeatureType;
import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.PotionEffect;

import java.util.concurrent.ThreadLocalRandom;

public class VanillaCriticalFeature implements CriticalFeature, CombatFeature {
	private final CombatVersion version;
	
	public VanillaCriticalFeature(FeatureConfiguration configuration) {
		this.version = configuration.get(FeatureType.VERSION);
	}
	
	@Override
	public boolean shouldCrit(LivingEntity attacker, AttackValues.PreCritical values) {
		boolean critical = values.strong() && !EntityUtils.isClimbing(attacker)
				&& attacker.getVelocity().y() < 0 && !attacker.isOnGround()
				&& !attacker.hasEffect(PotionEffect.BLINDNESS)
				&& attacker.getVehicle() == null;
		if (version.legacy()) return critical;
		
		// Not sprinting required for critical in 1.9+
		return critical && !attacker.isSprinting();
	}
	
	@Override
	public float applyToDamage(float damage) {
		if (version.legacy()) {
			return damage + ThreadLocalRandom.current().nextInt((int) (damage / 2 + 2));
		} else {
			return damage * 1.5f;
		}
	}
}
