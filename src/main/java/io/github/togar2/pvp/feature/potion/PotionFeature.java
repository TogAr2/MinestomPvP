package io.github.togar2.pvp.feature.potion;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionType;

import java.util.Collection;
import java.util.List;

public interface PotionFeature extends CombatFeature {
	PotionFeature NO_OP = new PotionFeature() {};
	
	default List<Potion> getAllPotions(PotionMeta potion) {
		return getAllPotions(potion.getPotionType(), potion.getCustomPotionEffects());
	}
	
	List<Potion> getAllPotions(PotionType potionType, Collection<CustomPotionEffect> customEffects);
}
