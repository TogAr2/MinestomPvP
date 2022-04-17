package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.legacy.LegacyKnockbackSettings;
import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.damage.CustomEntityDamage;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.events.*;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.utils.DamageUtils;
import io.github.bloepiloepi.pvp.utils.ItemUtils;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.*;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.world.Difficulty;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class DamageListener {
	
	public static EventNode<EntityEvent> events(boolean legacy) {
		EventNode<EntityEvent> node = EventNode.type((legacy ? "legacy-" : "") + "damage-events", EventFilter.ENTITY);
		
		node.addListener(EventListener.builder(EntityDamageEvent.class)
				.handler(event -> handleEntityDamage(event, legacy))
				.build());
		
		node.addListener(PlayerMoveEvent.class, event -> {
			Player player = event.getPlayer();
			double dy = event.getNewPosition().y() - player.getPosition().y();
			Double fallDistance = Tracker.fallDistance.get(player.getUuid());
			
			if (player.isFlying() || EntityUtils.hasEffect(player, PotionEffect.LEVITATION)
					|| EntityUtils.hasEffect(player, PotionEffect.SLOW_FALLING) || dy > 0) {
				Tracker.fallDistance.put(player.getUuid(), 0.0);
				return;
			}
			if (player.isFlyingWithElytra() && player.getVelocity().y() > -0.5) {
				Tracker.fallDistance.put(player.getUuid(), 1.0);
				return;
			}
			
			if (fallDistance > 3.0 && event.isOnGround()) {
				Block block = Objects.requireNonNull(player.getInstance()).getBlock(getLandingPos(player, event.getNewPosition()));
				if (!block.isAir()) {
					double damageDistance = Math.ceil(fallDistance - 3.0);
					double d = Math.min(0.2 + damageDistance / 15.0, 2.5);
					int particleCount = (int) (150 * d);
					
					player.sendPacketToViewersAndSelf(ParticleCreator.createParticlePacket(
							Particle.BLOCK,
							false,
							event.getNewPosition().x(), event.getNewPosition().y(), event.getNewPosition().z(),
							0, 0, 0,
							0.15f, particleCount,
							writer -> writer.writeVarInt(block.stateId())
					));
				}
			}
			
			if (event.isOnGround()) {
				Tracker.fallDistance.put(player.getUuid(), 0.0);
				
				if (!player.getGameMode().canTakeDamage()) return;
				int damage = getFallDamage(player, fallDistance);
				if (damage > 0) {
					SoundEvent sound = damage > 4 ? SoundEvent.ENTITY_PLAYER_BIG_FALL : SoundEvent.ENTITY_PLAYER_SMALL_FALL;
					SoundManager.sendToAround(player, player, sound, Sound.Source.PLAYER, 1.0f, 1.0f);
					
					player.damage(CustomDamageType.FALL, damage);
				}
			} else if (dy < 0) {
				Tracker.fallDistance.put(player.getUuid(), fallDistance - dy);
			}
		});
		
		return node;
	}
	
	private static int getFallDamage(Player player, double fallDistance) {
		float reduce = EntityUtils.hasEffect(player, PotionEffect.JUMP_BOOST)
				? EntityUtils.getEffect(player, PotionEffect.JUMP_BOOST).amplifier() + 1
				: 0;
		return (int) Math.ceil(fallDistance - 3.0 - reduce);
	}
	
	private static Point getLandingPos(Player player, Pos position) {
		position = position.add(0, -0.2, 0);
		if (Objects.requireNonNull(player.getInstance()).getBlock(position).isAir()) {
			position = position.add(0, -1, 0);
			Block block = player.getInstance().getBlock(position);
			Tag fences = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, "minecraft:fences");
			Tag walls = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, "minecraft:walls");
			Tag fenceGates = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, "minecraft:fence_gates");
			assert fences != null;
			assert walls != null;
			assert fenceGates != null;
			if (fences.contains(block.namespace()) || walls.contains(block.namespace()) || fenceGates.contains(block.namespace())) {
				return position;
			}
		}
		
		return position;
	}
	
	public static void handleEntityDamage(EntityDamageEvent event, boolean legacy) {
		event.setAnimation(false);
		event.setSound(null);
		
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
			ItemUtils.damageArmor(entity, type, amount, EquipmentSlot.HELMET);
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
			
			if (!damageBlockEvent.isCancelled()) {
				if (amount >= 3) {
					int shieldDamage = 1 + (int) Math.floor(amount);
					Player.Hand hand = EntityUtils.getActiveHand(entity);
					ItemUtils.damageEquipment(entity, hand == Player.Hand.MAIN ? EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, shieldDamage);
					
					if (entity.getItemInHand(hand).isAir()) {
						((LivingEntityMeta) entity.getEntityMeta()).setHandActive(false);
						SoundManager.sendToAround(
								entity instanceof Player player ? player : null, entity,
								SoundEvent.ITEM_SHIELD_BREAK, Sound.Source.PLAYER,
								0.8F, 0.8F + ThreadLocalRandom.current().nextFloat() * 0.4F
						);
					}
				}
				
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
				for(i = attacker.getPosition().z() - entity.getPosition().z(); h * h + i * i < 0.0001; i = (Math.random() - Math.random()) * 0.01D) {
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
			} else if (type != CustomDamageType.DROWN && (!shield || amount > 0.0F)) {
				// Update velocity
				entity.setVelocity(entity.getVelocity());
			}
		}
		
		if (shield) {
			event.setCancelled(true);
			return;
		}
		
		SoundEvent sound = null;
		
		boolean death = false;
		float totalHealth = entity.getHealth() + (entity instanceof Player ? ((Player) entity).getAdditionalHearts() : 0);
		if (totalHealth - amount <= 0) {
			boolean totem = totemProtection(entity, type);
			
			if (totem) {
				event.setCancelled(true);
			} else {
				death = true;
				if (hurtSoundAndAnimation) {
					//Death sound
					sound = type.getDeathSound(entity);
				}
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
		
		if (death && !event.isCancelled()) {
			EntityPreDeathEvent entityPreDeathEvent = new EntityPreDeathEvent(entity, type);
			EventDispatcher.call(entityPreDeathEvent);
			if (entityPreDeathEvent.isCancelled()) {
				event.setCancelled(true);
			}
		}
		
		// The Minestom damage method should return false if there was no hurt animation,
		// because otherwise the AttackManager will deal extra knockback
		if (!event.isCancelled() && !hurtSoundAndAnimation) {
			event.setCancelled(true);
			damageManually(entity, amount);
		} else {
			event.setDamage(amount);
		}
	}
	
	public static boolean totemProtection(LivingEntity entity, CustomDamageType type) {
		if (type.isOutOfWorld()) return false;
		
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
			entity.setHealth(1.0F);
			entity.clearEffects();
			entity.addEffect(new Potion(PotionEffect.REGENERATION, (byte) 1, 900, PotionListener.defaultFlags()));
			entity.addEffect(new Potion(PotionEffect.ABSORPTION, (byte) 1, 100, PotionListener.defaultFlags()));
			entity.addEffect(new Potion(PotionEffect.FIRE_RESISTANCE, (byte) 0, 800, PotionListener.defaultFlags()));
			
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
	
	public static void damageManually(LivingEntity entity, float damage) {
		// Additional hearts support
		if (entity instanceof Player player) {
			final float additionalHearts = player.getAdditionalHearts();
			if (additionalHearts > 0) {
				if (damage > additionalHearts) {
					damage -= additionalHearts;
					player.setAdditionalHearts(0);
				} else {
					player.setAdditionalHearts(additionalHearts - damage);
					damage = 0;
				}
			}
		}
		
		// Set the final entity health
		entity.setHealth(entity.getHealth() - damage);
	}
}
