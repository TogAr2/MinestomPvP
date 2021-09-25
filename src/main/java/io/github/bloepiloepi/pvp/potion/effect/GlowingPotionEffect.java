package io.github.bloepiloepi.pvp.potion.effect;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.PotionEffect;

public class GlowingPotionEffect extends CustomPotionEffect {
	public GlowingPotionEffect(int color) {
		super(PotionEffect.GLOWING, color);
	}
	
	@Override
	public void onApplied(LivingEntity entity, byte amplifier, boolean legacy) {
		entity.setGlowing(true);
	}
	
	@Override
	public void onRemoved(LivingEntity entity, byte amplifier, boolean legacy) {
		entity.setGlowing(false);
	}
}
