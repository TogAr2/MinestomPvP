package io.github.togar2.pvp.feature.totem;

import io.github.togar2.pvp.events.TotemUseEvent;
import io.github.togar2.pvp.feature.IndependentFeature;
import io.github.togar2.pvp.potion.PotionListener;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

public class TotemFeatureImpl implements TotemFeature, IndependentFeature {
	@Override
	public boolean tryProtect(LivingEntity entity, DamageType type) {
		if (type == DamageType.OUT_OF_WORLD) return false;
		
		boolean hasTotem = false;
		for (Player.Hand hand : Player.Hand.values()) {
			ItemStack stack = entity.getItemInHand(hand);
			if (stack.material() == Material.TOTEM_OF_UNDYING) {
				TotemUseEvent totemUseEvent = new TotemUseEvent(entity, hand);
				EventDispatcher.call(totemUseEvent);
				
				if (totemUseEvent.isCancelled()) continue;
				
				hasTotem = true;
				entity.setItemInHand(hand, stack.withAmount(stack.amount() - 1));
				break;
			}
		}
		
		if (hasTotem) {
			entity.setHealth(1.0f);
			entity.clearEffects();
			entity.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 1, 900, PotionListener.defaultFlags()));
			entity.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 1, 100, PotionListener.defaultFlags()));
			entity.addEffect(new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, 800, PotionListener.defaultFlags()));
			
			// Totem particles
			entity.triggerStatus((byte) 35);
		}
		
		return hasTotem;
	}
}
