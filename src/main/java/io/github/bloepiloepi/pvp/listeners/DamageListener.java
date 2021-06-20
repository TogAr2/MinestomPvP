package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.damage.CustomEntityDamage;
import io.github.bloepiloepi.pvp.damage.CustomEntityProjectileDamage;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.events.DamageBlockEvent;
import io.github.bloepiloepi.pvp.events.FinalDamageEvent;
import io.github.bloepiloepi.pvp.utils.DamageUtils;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.*;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.potion.PotionEffect;

public class DamageListener {
	
	public static void register(EventNode<EntityEvent> eventNode) {
		EventNode<EntityEvent> node = EventNode.type("damage-events", EventFilter.ENTITY);
		eventNode.addChild(node);
		
		node.addListener(EntityDamageEvent.class, event -> {
			if (event.isCancelled()) return;
			float amount = event.getDamage();
			
			CustomDamageType type;
			if (event.getDamageType() instanceof CustomDamageType) {
				type = (CustomDamageType) event.getDamageType();
			} else {
				if (event.getDamageType() == DamageType.GRAVITY) {
					type = CustomDamageType.FALL;
				} else if (event.getDamageType() == DamageType.ON_FIRE) {
					type = CustomDamageType.ON_FIRE;
				} else {
					type = CustomDamageType.OUT_OF_WORLD;
				}
			}
			
			LivingEntity entity = event.getEntity();
			if (type.isFire() && EntityUtils.hasEffect(entity, PotionEffect.FIRE_RESISTANCE)) {
				event.setCancelled(true);
				return;
			}
			
			if ((type == CustomDamageType.FALLING_BLOCK || type == CustomDamageType.ANVIL) && !entity.getEquipment(EquipmentSlot.HELMET).isAir()) {
				amount *= 0.75F;
			}
			
			Entity attacker = null;
			if (type instanceof CustomEntityDamage) {
				attacker = type.getSource();
			}
			
			boolean shield = false;
			if (amount > 0.0F && EntityUtils.blockedByShield(entity, type)) {
				DamageBlockEvent damageBlockEvent = new DamageBlockEvent(entity);
				EventDispatcher.call(damageBlockEvent);
				
				if (!damageBlockEvent.isCancelled()) {
					amount = 0.0F;
					
					if (!(type instanceof CustomEntityProjectileDamage)) {
						if (attacker instanceof LivingEntity) {
							EntityUtils.takeShieldHit(entity, (LivingEntity) attacker);
						}
					}
					
					shield = true;
				}
			}
			
			boolean hurtSoundAndAnimation = true;
			if (Tracker.timeUntilRegen.getOrDefault(entity.getUuid(), 0) > 10.0F) {
				float lastDamage = Tracker.lastDamageTaken.get(entity.getUuid());
				
				if (amount <= lastDamage) {
					event.setCancelled(true);
					return;
				}
				
				Tracker.lastDamageTaken.put(entity.getUuid(), amount);
				amount = applyDamage(entity, type, amount - lastDamage);
				hurtSoundAndAnimation = false;
			} else {
				Tracker.lastDamageTaken.put(entity.getUuid(), amount);
				Tracker.timeUntilRegen.put(entity.getUuid(), 20);
				amount = applyDamage(entity, type, amount);
			}
			
			if (hurtSoundAndAnimation) {
				if (shield) {
					entity.triggerStatus((byte) 29);
				} else if (type instanceof CustomEntityDamage && ((CustomEntityDamage) type).isThorns()) {
					entity.triggerStatus((byte) 33);
				} else {
					byte e;
					if (type == CustomDamageType.DROWN) {
						//Drown sound and animation
						e = 36;
					} else if (type.isFire()) {
						//Burn sound and animation
						e = 37;
					} else if (type == CustomDamageType.SWEET_BERRY_BUSH) {
						//Sweet berry bush sound and animation
						e = 44;
					} else {
						//Damage sound and animation
						e = 2;
					}
					
					entity.triggerStatus(e);
				}
				
				if (attacker != null && !shield) {
					double h = attacker.getPosition().getX() - entity.getPosition().getX();
					
					double i;
					for(i = attacker.getPosition().getZ() - entity.getPosition().getZ(); h * h + i * i < 1.0E-4D; i = (Math.random() - Math.random()) * 0.01D) {
						h = (Math.random() - Math.random()) * 0.01D;
					}
					
					EntityUtils.takeKnockback(entity, 0.4F, h, i);
				}
			}
			
			if (shield) {
				event.setCancelled(true);
				return;
			}
			
			FinalDamageEvent finalDamageEvent = new FinalDamageEvent(entity, type, amount);
			EventDispatcher.call(finalDamageEvent);
			if (finalDamageEvent.isCancelled() || finalDamageEvent.getDamage() <= 0.0F) {
				event.setCancelled(true);
			}
		});
	}
	
	public static float applyDamage(LivingEntity entity, CustomDamageType type, float amount) {
		amount = applyArmorToDamage(entity, type, amount);
		amount = applyEnchantmentsToDamage(entity, type, amount);
		
		if (amount != 0.0F && entity instanceof Player) {
			EntityUtils.addExhaustion((Player) entity, type.getExhaustion());
		}
		
		return amount;
	}
	
	public static float applyArmorToDamage(LivingEntity entity, CustomDamageType type, float amount) {
		if (!type.bypassesArmor()) {
			amount = DamageUtils.getDamageLeft(amount, (float) Math.floor(entity.getAttributeValue(Attribute.ARMOR)), entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS));
		}
		
		return amount;
	}
	
	public static float applyEnchantmentsToDamage(LivingEntity entity, CustomDamageType type, float amount) {
		if (type.isUnblockable()) {
			return amount;
		} else {
			int k;
			if (EntityUtils.hasEffect(entity, PotionEffect.DAMAGE_RESISTANCE)) {
				k = (EntityUtils.getEffect(entity, PotionEffect.DAMAGE_RESISTANCE).getAmplifier() + 1) * 5;
				int j = 25 - k;
				float f = amount * (float) j;
				amount = Math.max(f / 25.0F, 0.0F);
			}
			
			if (amount <= 0.0F) {
				return 0.0F;
			} else {
				k = EnchantmentUtils.getProtectionAmount(EntityUtils.getArmorItems(entity), type);
				if (k > 0) {
					amount = DamageUtils.getInflictedDamage(amount, (float) k);
				}
				
				return amount;
			}
		}
	}
}
