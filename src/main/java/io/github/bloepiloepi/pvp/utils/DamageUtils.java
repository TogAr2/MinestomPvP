package io.github.bloepiloepi.pvp.utils;

import net.minestom.server.utils.MathUtils;

public class DamageUtils {
	public static float getDamageLeft(float damage, float armor, float armorToughness) {
		float f = 2.0F + armorToughness / 4.0F;
		float g = MathUtils.clamp(armor - damage / f, armor * 0.2F, 20.0F);
		return damage * (1.0F - g / 25.0F);
	}
	
	public static float getInflictedDamage(float damageDealt, float protection) {
		float f = MathUtils.clamp(protection, 0.0F, 20.0F);
		return damageDealt * (1.0F - f / 25.0F);
	}
}
