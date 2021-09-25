package io.github.bloepiloepi.pvp.potion.effect;

import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.PotionEffect;

public class AbsorptionPotionEffect extends CustomPotionEffect {
	public AbsorptionPotionEffect(int color) {
		super(PotionEffect.ABSORPTION, color);
	}
	
	@Override
	public void onApplied(LivingEntity entity, byte amplifier, boolean legacy) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			player.setAdditionalHearts(player.getAdditionalHearts() + (float) (4 * (amplifier + 1)));
		}
		
		super.onApplied(entity, amplifier, legacy);
	}
	
	@Override
	public void onRemoved(LivingEntity entity, byte amplifier, boolean legacy) {
		if (entity instanceof Player) {
			Player player = (Player) entity;
			player.setAdditionalHearts(Math.max(player.getAdditionalHearts() - (float) (4 * (amplifier + 1)), 0));
		}
		
		super.onRemoved(entity, amplifier, legacy);
	}
}
