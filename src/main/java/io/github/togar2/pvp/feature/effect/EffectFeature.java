package io.github.togar2.pvp.feature.effect;

import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.projectile.Arrow;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface EffectFeature extends CombatFeature {
	EffectFeature NO_OP = new EffectFeature() {
		@Override
		public int getPotionColor(PotionContents contents) {
			return 0;
		}
		
		@Override
		public List<Potion> getAllPotions(PotionType potionType, Collection<CustomPotionEffect> customEffects) {
			return List.of();
		}
		
		@Override public void updatePotionVisibility(LivingEntity entity) {}
		@Override public void addArrowEffects(LivingEntity entity, Arrow arrow) {}
		@Override public void addSplashPotionEffects(LivingEntity entity, List<Potion> potions, double proximity,
		                                             @Nullable Entity source, @Nullable Entity attacker) {}
	};
	
	int getPotionColor(PotionContents contents);
	
	default List<Potion> getAllPotions(@Nullable PotionContents potionContents) {
		if (potionContents == null) return List.of();
		return getAllPotions(potionContents.potion(), potionContents.customEffects());
	}
	
	List<Potion> getAllPotions(PotionType potionType, Collection<CustomPotionEffect> customEffects);
	
	void updatePotionVisibility(LivingEntity entity);
	
	void addArrowEffects(LivingEntity entity, Arrow arrow);
	
	void addSplashPotionEffects(LivingEntity entity, List<Potion> potions, double proximity,
	                            @Nullable Entity source, @Nullable Entity attacker);
}
