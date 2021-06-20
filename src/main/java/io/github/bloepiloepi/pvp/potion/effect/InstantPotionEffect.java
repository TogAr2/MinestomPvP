package io.github.bloepiloepi.pvp.potion.effect;

import net.minestom.server.potion.PotionEffect;

public class InstantPotionEffect extends CustomPotionEffect {
	public InstantPotionEffect(PotionEffect potionEffect, int color) {
		super(potionEffect, color);
	}
	
	@Override
	public boolean isInstant() {
		return true;
	}
	
	@Override
	public boolean canApplyUpdateEffect(int duration, byte amplifier) {
		return duration >= 1;
	}
}
