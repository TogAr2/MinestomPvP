package io.github.bloepiloepi.pvp.potion.item;

import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffects;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionType;

import java.util.Collection;
import java.util.List;

public class CustomPotionType {
	private final PotionType potionType;
	private final List<Potion> effects;
	private List<Potion> legacyEffects;
	
	public CustomPotionType(PotionType potionType, Potion... effects) {
		this.potionType = potionType;
		this.effects = List.of(effects);
	}
	
	public CustomPotionType legacy(Potion... effects) {
		legacyEffects = List.of(effects);
		return this;
	}
	
	public PotionType getPotionType() {
		return potionType;
	}
	
	public List<Potion> getEffects() {
		return effects;
	}
	
	public List<Potion> getLegacyEffects() {
		if (legacyEffects != null)
			return legacyEffects;
		return effects;
	}
	
	public boolean hasInstantEffect() {
		return hasInstantEffect(effects);
	}
	
	public static boolean hasInstantEffect(Collection<Potion> effects) {
		if (effects.isEmpty()) return false;
		
		for (Potion potion : effects) {
			if (CustomPotionEffects.get(potion.effect()).isInstant()) {
				return true;
			}
		}
		
		return false;
	}
}
