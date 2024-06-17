package io.github.togar2.pvp.listeners;

import io.github.togar2.pvp.config.DamageConfig;
import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.enchantment.EnchantmentUtils;
import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.entity.Tracker;
import io.github.togar2.pvp.enums.Tool;
import io.github.togar2.pvp.events.*;
import io.github.togar2.pvp.legacy.LegacyKnockbackSettings;
import io.github.togar2.pvp.potion.PotionListener;
import io.github.togar2.pvp.utils.ItemUtils;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.DamageEventPacket;
import net.minestom.server.network.packet.server.play.HitAnimationPacket;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class DamageHandler {
	public static final Tag<Long> LAST_DAMAGE_TIME = Tag.Long("lastDamageTime");
	public static final Tag<Long> NEW_DAMAGE_TIME = Tag.Long("newDamageTime");
	public static final Tag<Float> LAST_DAMAGE_AMOUNT = Tag.Float("lastDamageAmount");
	public static final Tag<Integer> LAST_DAMAGED_BY = Tag.Integer("lastDamagedBy");
	
	/**
	 * The main method of the damage handler
	 */
	public void handleEvent(EntityDamageEvent event, DamageConfig config) {
		event.setAnimation(false);
		SoundEvent sound = event.getSound();
		event.setSound(null);
		
		Damage damage = event.getDamage();
		float amount = damage.getAmount();
		DamageTypeInfo typeInfo = DamageTypeInfo.of(MinecraftServer.getDamageTypeRegistry().get(damage.getType()));
		if (event.getEntity() instanceof Player && typeInfo.shouldScaleWithDifficulty(damage))
			amount = scaleWithDifficulty(amount);
		
		LivingEntity entity = event.getEntity();
		if (typeInfo.fire() && entity.hasEffect(PotionEffect.FIRE_RESISTANCE)) {
			event.setCancelled(true);
			return;
		}
		
		Entity attacker = damage.getAttacker();
		Pair<Boolean, Float> result = handleShield(entity, attacker, damage, typeInfo, amount, config);
		boolean shield = result.first();
		amount = result.second();
		
		if (typeInfo.freeze() && Objects.requireNonNull(MinecraftServer.getTagManager().getTag(
						net.minestom.server.gamedata.tags.Tag.BasicType.ENTITY_TYPES, "minecraft:freeze_hurts_extra_types"))
				.contains(entity.getEntityType().namespace())) {
			amount *= 5.0F;
		}
		
		if (config.isEquipmentDamageEnabled() && typeInfo.damagesHelmet()
				&& !entity.getEquipment(EquipmentSlot.HELMET).isAir()) {
			ItemUtils.damageArmor(entity, typeInfo, amount, EquipmentSlot.HELMET);
			amount *= 0.75F;
		}
		
		if (entity instanceof Player && attacker instanceof LivingEntity) {
			entity.setTag(DamageHandler.LAST_DAMAGED_BY, attacker.getEntityId());
			entity.setTag(DamageHandler.LAST_DAMAGE_TIME, System.currentTimeMillis());
		}
		
		// Invulnerability ticks
		boolean hurtSoundAndAnimation = true;
		float amountBeforeProcessing = amount;
		long newDamageTime = entity.hasTag(DamageHandler.NEW_DAMAGE_TIME) ? entity.getTag(DamageHandler.NEW_DAMAGE_TIME) : -10000;
		if (entity.getAliveTicks() - newDamageTime < 0) {
			float lastDamage = entity.hasTag(DamageHandler.LAST_DAMAGE_AMOUNT) ? entity.getTag(DamageHandler.LAST_DAMAGE_AMOUNT) : 0;
			
			if (amount <= lastDamage) {
				event.setCancelled(true);
				return;
			}
			
			hurtSoundAndAnimation = false;
			amount = amount - lastDamage;
		}
		
		// Process armor and effects
		amount = getDamageWithProtection(entity, typeInfo, amount, config);
		
		damage.setAmount(amount);
		FinalDamageEvent finalDamageEvent = new FinalDamageEvent(entity, damage, config.getInvulnerabilityTicks());
		EventDispatcher.call(finalDamageEvent);
		// New amount has been set in the Damage class
		amount = damage.getAmount();
		
		boolean register = config.isLegacy() || amount > 0;
		if (register && entity instanceof Player)
			Tracker.combatManager.get(entity.getUuid()).recordDamage(damage);
		
		if (finalDamageEvent.isCancelled()) {
			//TODO this will make damage from snowballs and eggs not display
			event.setCancelled(true);
			return;
		}
		
		// Exhaustion from damage
		if (config.isExhaustionEnabled() && amountBeforeProcessing != 0 && entity instanceof Player)
			EntityUtils.addExhaustion((Player) entity,
					(float) MinecraftServer.getDamageTypeRegistry().get(damage.getType()).exhaustion() * (config.isLegacy() ? 3 : 1));
		
		if (register) entity.setTag(DamageHandler.LAST_DAMAGE_AMOUNT, amountBeforeProcessing);
		
		if (hurtSoundAndAnimation) {
			entity.setTag(DamageHandler.NEW_DAMAGE_TIME, entity.getAliveTicks() + finalDamageEvent.getInvulnerabilityTicks());
			
			entity.sendPacketToViewersAndSelf(new DamageEventPacket(
					entity.getEntityId(),
					MinecraftServer.getDamageTypeRegistry().getId(damage.getType()),
					damage.getAttacker() == null ? 0 : damage.getAttacker().getEntityId() + 1,
					damage.getSource() == null ? 0 : damage.getSource().getEntityId() + 1,
					null
			));
			
			if (!shield && damage.getType() != DamageType.DROWN) {
				if (attacker != null && !typeInfo.explosive()) {
					applyKnockback(entity, attacker, damage.getSource(), config);
				} else {
					// Update velocity
					entity.setVelocity(entity.getVelocity());
				}
			}
		}
		
		if (shield) {
			event.setCancelled(true);
			return;
		}
		
		boolean death = false;
		float totalHealth = entity.getHealth() +
				(entity instanceof Player player ? player.getAdditionalHearts() : 0);
		if (totalHealth - amount <= 0) {
			boolean totem = totemProtection(entity, typeInfo);
			
			if (totem) {
				event.setCancelled(true);
			} else {
				death = true;
				if (hurtSoundAndAnimation) {
					// Death sound
					sound = entity instanceof Player ? SoundEvent.ENTITY_PLAYER_DEATH : SoundEvent.ENTITY_GENERIC_DEATH;
				}
			}
		} else if (hurtSoundAndAnimation) {
			// Workaround to have different types make a different sound,
			// but only if the sound has not been changed by damage#getSound
			//TODO improve
			if (entity instanceof Player && sound == SoundEvent.ENTITY_PLAYER_HURT) {
				if (typeInfo.fire()) {
					sound = SoundEvent.ENTITY_PLAYER_HURT_ON_FIRE;
				} else if (typeInfo.thorns()) {
					sound = SoundEvent.ENCHANT_THORNS_HIT;
				} else if (damage.getType() == DamageType.DROWN) {
					sound = SoundEvent.ENTITY_PLAYER_HURT_DROWN;
				} else if (damage.getType() == DamageType.SWEET_BERRY_BUSH) {
					sound = SoundEvent.ENTITY_PLAYER_HURT_SWEET_BERRY_BUSH;
				} else if (damage.getType() == DamageType.FREEZE) {
					sound = SoundEvent.ENTITY_PLAYER_HURT_FREEZE;
				}
			}
		}
		
		// Play sound (copied from Minestom, because of complications with cancelling)
		if (config.isSoundsEnabled() && sound != null) entity.sendPacketToViewersAndSelf(new SoundEffectPacket(
				sound, entity instanceof Player ? Sound.Source.PLAYER : Sound.Source.HOSTILE,
				entity.getPosition(),
				//TODO seed randomizing?
				1.0f, 1.0f, 0
		));
		
		if (death && !event.isCancelled()) {
			EntityPreDeathEvent entityPreDeathEvent = new EntityPreDeathEvent(entity, MinecraftServer.getDamageTypeRegistry().get(damage.getType()));
			EventDispatcher.call(entityPreDeathEvent);
			if (entityPreDeathEvent.isCancelled()) event.setCancelled(true);
			if (entityPreDeathEvent.isCancelDeath()) amount = 0;
		}
		
		event.getDamage().setAmount(amount);
		
		// lastDamage field is set when event is not cancelled but should also when cancelled
		if (register) EntityUtils.setLastDamage(entity, damage);
		
		if (config.shouldPerformDamage()) {
			// The Minestom damage method should return false if there was no hurt animation,
			// because otherwise the AttackManager will deal extra knockback
			if (!event.isCancelled() && !hurtSoundAndAnimation) {
				event.setCancelled(true);
				damageManually(entity, amount);
			}
		} else {
			event.setCancelled(true);
		}
	}
	
	protected float scaleWithDifficulty(float amount) {
		return switch (MinecraftServer.getDifficulty()) {
			case PEACEFUL -> -1;
			case EASY -> Math.min(amount / 2.0f + 1.0f, amount);
			case HARD -> amount * 3.0f / 2.0f;
			default -> amount;
		};
	}
	
	protected void takeShieldHit(LivingEntity entity, LivingEntity attacker, boolean applyKnockback) {
		if (applyKnockback) {
			Pos entityPos = entity.getPosition();
			Pos attackerPos = attacker.getPosition();
			attacker.takeKnockback(0.5F,
					attackerPos.x() - entityPos.x(),
					attackerPos.z() - entityPos.z()
			);
		}
		
		if (!(entity instanceof Player)) return;
		Tool tool = Tool.fromMaterial(attacker.getItemInMainHand().material());
		if (tool != null && tool.isAxe()) {
			disableShield((Player) entity);
		}
	}
	
	protected static void disableShield(Player player) {
		Tracker.setCooldown(player, Material.SHIELD, 100);
		
		// Shield disable status
		player.triggerStatus((byte) 30);
		player.triggerStatus((byte) 9);
		
		Player.Hand hand = player.getPlayerMeta().getActiveHand();
		player.refreshActiveHand(false, hand == Player.Hand.OFF, false);
	}
	
	protected Pair<Boolean, Float> handleShield(LivingEntity entity, Entity attacker,
	                                                   Damage damage, DamageTypeInfo typeInfo,
	                                                   float amount, DamageConfig config) {
		if (!config.isShieldEnabled() || amount <= 0
				|| !EntityUtils.blockedByShield(entity, damage, typeInfo, config.isLegacyShieldMechanics()))
			return Pair.of(false, amount);
		
		float resultingDamage = config.isLegacyShieldMechanics() ? Math.max(0, (amount + 1) * 0.5f) : 0;
		
		DamageBlockEvent damageBlockEvent = new DamageBlockEvent(entity, amount, resultingDamage);
		EventDispatcher.call(damageBlockEvent);
		if (damageBlockEvent.isCancelled()) return Pair.of(false, amount);
		
		if (config.isEquipmentDamageEnabled() && amount >= 3) {
			int shieldDamage = 1 + (int) Math.floor(amount);
			Player.Hand hand = ((LivingEntityMeta) entity.getEntityMeta()).getActiveHand();
			ItemUtils.damageEquipment(
					entity,
					hand == Player.Hand.MAIN ?
							EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND,
					shieldDamage
			);
			
			if (entity.getItemInHand(hand).isAir()) {
				((LivingEntityMeta) entity.getEntityMeta()).setHandActive(false);
				entity.getViewersAsAudience().playSound(Sound.sound(
						SoundEvent.ITEM_SHIELD_BREAK, Sound.Source.PLAYER,
						0.8f, 0.8f + ThreadLocalRandom.current().nextFloat(0.4f)
				));
			}
		}
		
		amount = damageBlockEvent.getResultingDamage();
		
		if (config.isLegacyShieldMechanics())
			return Pair.of(false, amount);
		
		// If not legacy, attacker takes shield hit (knockback and disabling)
		if (!typeInfo.projectile() && attacker instanceof LivingEntity)
			takeShieldHit(entity, (LivingEntity) attacker, damageBlockEvent.knockbackAttacker());
		
		return Pair.of(amount == 0, amount);
	}
	
	protected void applyKnockback(LivingEntity entity, Entity attacker,
	                              @Nullable Entity directAttacker, DamageConfig config) {
		double dx = attacker.getPosition().x() - entity.getPosition().x();
		double dz = attacker.getPosition().z() - entity.getPosition().z();
		
		// Randomize direction
		ThreadLocalRandom random = ThreadLocalRandom.current();
		while (dx * dx + dz * dz < 0.0001) {
			dx = random.nextDouble(-1, 1) * 0.01;
			dz = random.nextDouble(-1, 1) * 0.01;
		}
		
		double finalDx = dx;
		double finalDz = dz;
		if (config.isLegacyKnockback()) {
			LegacyKnockbackEvent legacyKnockbackEvent = new LegacyKnockbackEvent(
					entity, directAttacker == null ? attacker : directAttacker, false);
			EventDispatcher.call(legacyKnockbackEvent);
			if (legacyKnockbackEvent.isCancelled()) return;
			
			LegacyKnockbackSettings settings = legacyKnockbackEvent.getSettings();
			
			float kbResistance = (float) entity.getAttributeValue(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
			double horizontal = settings.horizontal() * (1 - kbResistance);
			double vertical = settings.vertical() * (1 - kbResistance);
			Vec horizontalModifier = new Vec(finalDx, finalDz).normalize().mul(horizontal);
			
			Vec velocity = entity.getVelocity();
			//TODO divide by 2 at y component or not? (also AttackManager)
			entity.setVelocity(new Vec(
					velocity.x() / 2d - horizontalModifier.x(),
					entity.isOnGround() ? Math.min(
							settings.verticalLimit(), velocity.y() + vertical) : velocity.y(),
					velocity.z() / 2d - horizontalModifier.z()
			));
		} else {
			EntityKnockbackEvent knockbackEvent = new EntityKnockbackEvent(
					entity, directAttacker == null ? attacker : directAttacker,
					false, false,
					0.4F
			);
			EventDispatcher.call(knockbackEvent);
			if (knockbackEvent.isCancelled()) return;
			
			entity.takeKnockback(knockbackEvent.getStrength(), finalDx, finalDz);
		}
		
		if (entity instanceof Player player) {
			float hurtDir = (float) (Math.toDegrees(Math.atan2(dz, dx)) - player.getPosition().yaw());
			player.sendPacket(new HitAnimationPacket(player.getEntityId(), hurtDir));
		}
	}
	
	protected boolean totemProtection(LivingEntity entity, DamageTypeInfo typeInfo) {
		if (typeInfo.outOfWorld()) return false;
		
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
	
	protected float getDamageWithProtection(LivingEntity entity, DamageTypeInfo typeInfo,
	                                        float amount, DamageConfig config) {
		amount = getDamageWithArmor(entity, typeInfo, amount, config);
		return getDamageWithEnchantments(entity, typeInfo, amount, config);
	}
	
	protected float getDamageWithArmor(LivingEntity entity, DamageTypeInfo typeInfo,
	                                   float amount, DamageConfig config) {
		if (config.isArmorDisabled()) return amount;
		if (typeInfo.bypassesArmor()) return amount;
		
		float armorValue = (float) entity.getAttributeValue(Attribute.GENERIC_ARMOR);
		if (config.isLegacy()) {
			int armorMultiplier = 25 - (int) armorValue;
			return (amount * (float) armorMultiplier) / 25;
		} else {
			return getDamageLeft(
					amount, (float) Math.floor(armorValue),
                    (float) entity.getAttributeValue(Attribute.GENERIC_ARMOR_TOUGHNESS)
            );
		}
	}
	
	protected float getDamageWithEnchantments(LivingEntity entity, DamageTypeInfo typeInfo,
	                                          float amount, DamageConfig config) {
		if (typeInfo.unblockable()) {
			return amount;
		}
		
		int k;
		TimedPotion effect = entity.getEffect(PotionEffect.RESISTANCE);
		if (effect != null) {
			k = (effect.potion().amplifier() + 1) * 5;
			int j = 25 - k;
			float f = amount * (float) j;
			amount = Math.max(f / 25, 0);
		}
		
		if (config.isArmorDisabled()) return amount;
		
		if (amount <= 0) {
			return 0;
		} else {
			k = EnchantmentUtils.getProtectionAmount(EntityUtils.getArmorItems(entity), typeInfo);
			if (!config.isLegacy()) {
				if (k > 0) {
					amount = getInflictedDamage(amount, (float) k);
				}
			} else {
				if (k > 20) {
					k = 20;
				}
				
				if (k > 0) {
					int j = 25 - k;
					float f = amount * (float) j;
					amount = f / 25;
				}
			}
			
			return amount;
		}
	}
	
	private static float getDamageLeft(float damage, float armor, float armorToughness) {
		float f = 2.0f + armorToughness / 4.0f;
		float g = MathUtils.clamp(armor - damage / f, armor * 0.2f, 20.0f);
		return damage * (1.0F - g / 25.0F);
	}
	
	private static float getInflictedDamage(float damageDealt, float protection) {
		float f = MathUtils.clamp(protection, 0.0f, 20.0f);
		return damageDealt * (1.0f - f / 25.0f);
	}
	
	private static void damageManually(LivingEntity entity, float damage) {
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
