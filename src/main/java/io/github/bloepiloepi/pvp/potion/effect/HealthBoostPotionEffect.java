package io.github.bloepiloepi.pvp.potion.effect;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.PotionEffect;

public class HealthBoostPotionEffect extends CustomPotionEffect {
	public HealthBoostPotionEffect(int color) {
		super(PotionEffect.HEALTH_BOOST, color);
	}
	
	@Override
	public void onRemoved(LivingEntity entity, byte amplifier, boolean legacy) {
		super.onRemoved(entity, amplifier, legacy);
		
		if (entity.getHealth() > entity.getMaxHealth()) {
			entity.setHealth(entity.getMaxHealth());
		}
	}
}
