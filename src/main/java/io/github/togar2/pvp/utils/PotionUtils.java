package io.github.togar2.pvp.utils;

import io.github.togar2.pvp.potion.effect.CustomPotionEffect;
import io.github.togar2.pvp.potion.effect.CustomPotionEffects;
import net.minestom.server.potion.Potion;

import java.util.Collection;

public class PotionUtils {
	public static int getPotionColor(Collection<Potion> effects) {
		if (effects.isEmpty()) {
			return 3694022;
		}
		
		float r = 0.0f;
		float g = 0.0f;
		float b = 0.0f;
		int totalAmplifier = 0;
		
		for (Potion potion : effects) {
			if (potion.hasParticles()) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.effect());
				int color = customPotionEffect.getColor();
				int amplifier = potion.amplifier() + 1;
				r += (float) (amplifier * (color >> 16 & 255)) / 255.0f;
				g += (float) (amplifier * (color >> 8 & 255)) / 255.0f;
				b += (float) (amplifier * (color & 255)) / 255.0f;
				totalAmplifier += amplifier;
			}
		}
		
		if (totalAmplifier == 0) {
			return 0;
		} else {
			r = r / (float) totalAmplifier * 255.0f;
			g = g / (float) totalAmplifier * 255.0f;
			b = b / (float) totalAmplifier * 255.0f;
			return (int) r << 16 | (int) g << 8 | (int) b;
		}
	}
	
	public static byte createFlags(boolean ambient, boolean particles, boolean icon) {
		byte flags = 0;
		if (ambient) {
			flags = (byte) (flags | 0x01);
		}
		if (particles) {
			flags = (byte) (flags | 0x02);
		}
		if (icon) {
			flags = (byte) (flags | 0x04);
		}
		return flags;
	}
	
	private static final byte DEFAULT_FLAGS = createFlags(false, true, true);
	public static byte defaultFlags() {
		return DEFAULT_FLAGS;
	}
}
