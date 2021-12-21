package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.legacy.LegacyKnockbackSettings;
import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.damage.CustomEntityDamage;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.events.*;
import io.github.bloepiloepi.pvp.utils.DamageUtils;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.*;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.world.Difficulty;

public class DamageListener {
	
	public static EventNode<EntityEvent> events(boolean legacy) {
		EventNode<EntityEvent> node = EventNode.type((legacy ? "legacy-" : "") + "damage-events", EventFilter.ENTITY);
		
		node.addListener(PlayerTickEvent.class, event -> {
			if (event.getPlayer().isOnline()) {
				Tracker.hungerManager.get(event.getPlayer().getUuid()).update(legacy);
			}
		});
		
		node.addListener(EventListener.builder(EntityDamageEvent.class)
				.handler(event -> handleEntityDamage(event, legacy))
				.build());
		
		return node;
	}
	
	public static void handleEntityDamage(EntityDamageEvent event, boolean legacy) {
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
		
		if (event.getEntity() instanceof Player && type.isScaledWithDifficulty()) {
			Difficulty difficulty = MinecraftServer.getDifficulty();
			switch (difficulty) {
				case PEACEFUL -> {
					event.setCancelled(true);
					return;
				}
				case EASY -> amount = Math.min(amount / 2.0F + 1.0F, amount);
				case HARD -> amount = amount * 3.0F / 2.0F;
			}
		}
		
		LivingEntity entity = event.getEntity();
		if (type.isFire() && EntityUtils.hasEffect(entity, PotionEffect.FIRE_RESISTANCE)) {
			event.setCancelled(true);
			return;
		}
		
		if (type.damagesHelmet() && !entity.getEquipment(EquipmentSlot.HELMET).isAir()) {
			//TODO damage helmet item
			amount *= 0.75F;
		}
		
		Entity attacker = type.getEntity();
		if (entity instanceof Player && attacker instanceof LivingEntity) {
			Tracker.lastDamagedBy.put(entity.getUuid(), (LivingEntity) attacker);
			Tracker.lastDamageTime.put(entity.getUuid(), System.currentTimeMillis());
		}
		
		boolean shield = false;
		if (amount > 0.0F && EntityUtils.blockedByShield(entity, type, legacy)) {
			float resultingDamage = 0.0F;
			if (legacy) {
				resultingDamage = (amount + 1.0F) * 0.5F;
				if (resultingDamage < 0.0F)
					resultingDamage = 0.0F;
			}
			
			DamageBlockEvent damageBlockEvent = new DamageBlockEvent(entity, amount, resultingDamage);
			EventDispatcher.call(damageBlockEvent);
			
			//TODO damage shield item
			
			if (!damageBlockEvent.isCancelled()) {
				amount = damageBlockEvent.getResultingDamage();
				
				if (!legacy) {
					if (!type.isProjectile()) {
						if (attacker instanceof LivingEntity) {
							EntityUtils.takeShieldHit(entity, (LivingEntity) attacker, damageBlockEvent.knockbackAttacker());
						}
					}
					
					shield = true;
				}
			}
		}
		
		boolean hurtSoundAndAnimation = true;
		float amountBeforeProcessing = amount;
		if (Tracker.invulnerableTime.getOrDefault(entity.getUuid(), 0) > 10) {
			float lastDamage = Tracker.lastDamageTaken.get(entity.getUuid());
			
			if (amount <= lastDamage) {
				event.setCancelled(true);
				return;
			}
			
			amount = applyDamage(entity, type, amount - lastDamage, legacy);
			hurtSoundAndAnimation = false;
		} else {
			amount = applyDamage(entity, type, amount, legacy);
		}
		
		FinalDamageEvent finalDamageEvent = new FinalDamageEvent(entity, type, amount);
		EventDispatcher.call(finalDamageEvent);
		
		amount = finalDamageEvent.getDamage();
		
		if ((finalDamageEvent.getDamage() <= 0.0F && !legacy) || finalDamageEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
		
		Tracker.lastDamageTaken.put(entity.getUuid(), amountBeforeProcessing);
		
		if (hurtSoundAndAnimation) {
			Tracker.invulnerableTime.put(entity.getUuid(), finalDamageEvent.getInvulnerabilityTicks() + 10);
			
			if (shield) {
				entity.triggerStatus((byte) 29);
			} else if (type instanceof CustomEntityDamage && ((CustomEntityDamage) type).isThorns()) {
				entity.triggerStatus((byte) 33);
			} else {
				byte status;
				if (type == CustomDamageType.DROWN) {
					//Drown sound and animation
					status = 36;
				} else if (type.isFire()) {
					//Burn sound and animation
					status = 37;
				} else if (type == CustomDamageType.SWEET_BERRY_BUSH) {
					//Sweet berry bush sound and animation
					status = 44;
				} else if (type == CustomDamageType.FREEZE) {
					//Freeze sound and animation
					status = 57;
				} else {
					//Damage sound and animation
					status = 2;
				}
				
				entity.triggerStatus(status);
			}
			
			if (attacker != null && !shield) {
				double h = attacker.getPosition().x() - entity.getPosition().x();
				
				double i;
				for(i = attacker.getPosition().z() - entity.getPosition().z(); h * h + i * i < 1.0E-4D; i = (Math.random() - Math.random()) * 0.01D) {
					h = (Math.random() - Math.random()) * 0.01D;
				}
				
				Entity directAttacker = type.getDirectEntity();
				if (directAttacker == null) {
					directAttacker = attacker;
				}
				double finalH = h;
				double finalI = i;
				if (!legacy) {
					EntityKnockbackEvent entityKnockbackEvent = new EntityKnockbackEvent(entity, directAttacker, false, false, 0.4F);
					EventDispatcher.callCancellable(entityKnockbackEvent, () -> {
						float strength = entityKnockbackEvent.getStrength();
						entity.takeKnockback(strength, finalH, finalI);
					});
				} else {
					double magnitude = Math.sqrt(h * h + i * i);
					LegacyKnockbackEvent legacyKnockbackEvent = new LegacyKnockbackEvent(entity, directAttacker, false);
					EventDispatcher.callCancellable(legacyKnockbackEvent, () -> {
						LegacyKnockbackSettings settings = legacyKnockbackEvent.getSettings();
						Vec newVelocity = entity.getVelocity();
						
						double horizontal = settings.getHorizontal();
						newVelocity = newVelocity.withX((newVelocity.x() / 2) - (finalH / magnitude * horizontal));
						newVelocity = newVelocity.withY((newVelocity.y() / 2) + settings.getVertical());
						newVelocity = newVelocity.withZ((newVelocity.z() / 2) - (finalI / magnitude * horizontal));
						
						if (newVelocity.y() > settings.getVerticalLimit())
							newVelocity = newVelocity.withY(settings.getVerticalLimit());
						
						entity.setVelocity(newVelocity);
					});
				}
			}
		}
		
		if (shield) {
			event.setCancelled(true);
			return;
		}
		
		SoundEvent sound = null;
		
		float totalHealth = entity.getHealth() + (entity instanceof Player ? ((Player) entity).getAdditionalHearts() : 0);
		if (totalHealth - amount <= 0) {
			boolean totem = totemProtection(entity, type);
			
			if (totem) {
				event.setCancelled(true);
			} else if (hurtSoundAndAnimation) {
				//Death sound
				sound = type.getDeathSound(entity);
			}
		} else if (hurtSoundAndAnimation) {
			//Damage sound
			sound = type.getSound(entity);
		}
		
		//Play sound
		if (sound != null) {
			Sound.Source soundCategory;
			if (entity instanceof Player) {
				soundCategory = Sound.Source.PLAYER;
			} else {
				// TODO: separate living entity categories
				soundCategory = Sound.Source.HOSTILE;
			}
			
			SoundEffectPacket damageSoundPacket = new SoundEffectPacket(
					sound, soundCategory,
					entity.getPosition(),
					1.0f, 1.0f
			);
			entity.sendPacketToViewersAndSelf(damageSoundPacket);
		}
		
		event.setDamage(amount);
	}
	
	public static boolean totemProtection(LivingEntity entity, CustomDamageType type) {
		if (type.isOutOfWorld()) return false;
		
		boolean hasTotem = false;
		
		for (Player.Hand hand : Player.Hand.values()) {
			ItemStack stack = entity.getItemInHand(hand);
			if (stack.getMaterial() == Material.TOTEM_OF_UNDYING) {
				TotemUseEvent totemUseEvent = new TotemUseEvent(entity, hand);
				EventDispatcher.call(totemUseEvent);
				
				if (totemUseEvent.isCancelled()) continue;
				
				hasTotem = true;
				entity.setItemInHand(hand, stack.withAmount(stack.getAmount() - 1));
				break;
			}
		}
		
		if (hasTotem) {
			entity.setHealth(1.0F);
			entity.clearEffects();
			entity.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 1, 900));
			entity.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 1, 100));
			entity.addEffect(new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, 800));
			
			//Totem particles
			entity.triggerStatus((byte) 35);
		}
		
		return hasTotem;
	}
	
	public static float applyDamage(LivingEntity entity, CustomDamageType type, float amount, boolean legacy) {
		amount = applyArmorToDamage(entity, type, amount, legacy);
		amount = applyEnchantmentsToDamage(entity, type, amount, legacy);
		
		if (amount != 0.0F && entity instanceof Player) {
			EntityUtils.addExhaustion((Player) entity, type.getExhaustion() * (legacy ? 3 : 1));
			Tracker.combatManager.get(entity.getUuid()).recordDamage(type, amount);
		}
		
		return amount;
	}
	
	public static float applyArmorToDamage(LivingEntity entity, CustomDamageType type, float amount, boolean legacy) {
		if (!type.bypassesArmor()) {
			float armorValue = entity.getAttributeValue(Attribute.ARMOR);
			if (!legacy) {
				amount = DamageUtils.getDamageLeft(amount, (float) Math.floor(armorValue), entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS));
			} else {
				int i = 25 - (int) armorValue;
				float f1 = amount * (float) i;
				amount = f1 / 25.0F;
			}
		}
		
		return amount;
	}
	
	public static float applyEnchantmentsToDamage(LivingEntity entity, CustomDamageType type, float amount, boolean legacy) {
		if (type.isUnblockable()) {
			return amount;
		} else {
			int k;
			if (EntityUtils.hasEffect(entity, PotionEffect.RESISTANCE)) {
				k = (EntityUtils.getEffect(entity, PotionEffect.RESISTANCE).amplifier() + 1) * 5;
				int j = 25 - k;
				float f = amount * (float) j;
				amount = Math.max(f / 25.0F, 0.0F);
			}
			
			if (amount <= 0.0F) {
				return 0.0F;
			} else {
				k = EnchantmentUtils.getProtectionAmount(EntityUtils.getArmorItems(entity), type);
				if (!legacy) {
					if (k > 0) {
						amount = DamageUtils.getInflictedDamage(amount, (float) k);
					}
				} else {
					if (k > 20) {
						k = 20;
					}
					
					if (k > 0) {
						int j = 25 - k;
						float f = amount * (float) j;
						amount = f / 25.0F;
					}
				}
				
				return amount;
			}
		}
	}
}
