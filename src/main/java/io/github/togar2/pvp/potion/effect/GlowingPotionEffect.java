package io.github.togar2.pvp.potion.effect;

import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.potion.PotionEffect;

public class GlowingPotionEffect extends CustomPotionEffect {
	public GlowingPotionEffect(int color) {
		super(PotionEffect.GLOWING, color);
	}
	
	@Override
	public void onApplied(LivingEntity entity, byte amplifier, CombatVersion version) {
		entity.setGlowing(true);
	}
	
	@Override
	public void onRemoved(LivingEntity entity, byte amplifier, CombatVersion version) {
		entity.setGlowing(false);
	}
}
