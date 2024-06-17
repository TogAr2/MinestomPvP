package io.github.togar2.pvp.potion.effect;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.potion.PotionEffect;

public class HealthBoostPotionEffect extends CustomPotionEffect {
	public HealthBoostPotionEffect(int color) {
		super(PotionEffect.HEALTH_BOOST, color);
	}
	
	@Override
	public void onRemoved(LivingEntity entity, byte amplifier, boolean legacy) {
		super.onRemoved(entity, amplifier, legacy);
		
		if (entity.getHealth() > entity.getAttributeValue(Attribute.GENERIC_MAX_HEALTH)) {
			entity.setHealth((float) entity.getAttributeValue(Attribute.GENERIC_MAX_HEALTH));
		}
	}
}
