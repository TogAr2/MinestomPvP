package io.github.togar2.pvp.potion.effect;

import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.PotionEffect;

public class HealthBoostPotionEffect extends CustomPotionEffect {
	public HealthBoostPotionEffect(int color) {
		super(PotionEffect.HEALTH_BOOST, color);
	}
	
	@Override
	public void onRemoved(LivingEntity entity, byte amplifier, CombatVersion version) {
		super.onRemoved(entity, amplifier, version);
		
		if (entity.getHealth() > entity.getMaxHealth()) {
			entity.setHealth(entity.getMaxHealth());
		}
	}
}
