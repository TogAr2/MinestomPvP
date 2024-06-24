package io.github.togar2.pvp.feature.effect;

import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.projectile.Arrow;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface EffectFeature extends CombatFeature {
	EffectFeature NO_OP = new EffectFeature() {};
	
	//TODO probably should be in PotionFeature (or the other way around)
	void addArrowEffects(LivingEntity entity, Arrow arrow);
	
	void addSplashPotionEffects(LivingEntity entity, List<Potion> potions, double proximity,
	                            @Nullable Entity source, @Nullable Entity attacker);
	
	boolean hasInstantEffect(Collection<Potion> effects);
	
	int getPotionColor(PotionContents contents);
	
	int getPotionColor(Collection<Potion> effects);
}
