package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.config.DamageConfig;
import io.github.bloepiloepi.pvp.config.PvPConfig;
import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.damage.CustomEntityDamage;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.entity.Tracker;
import io.github.bloepiloepi.pvp.enums.Tool;
import io.github.bloepiloepi.pvp.events.*;
import io.github.bloepiloepi.pvp.legacy.LegacyKnockbackSettings;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.utils.ItemUtils;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SoundEffectPacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class DamageListener {
	public static final Tag<Long> LAST_DAMAGE_TIME = Tag.Long("lastDamageTime");
	public static final Tag<Long> NEW_DAMAGE_TIME = Tag.Long("newDamageTime");
	public static final Tag<Float> LAST_DAMAGE_AMOUNT = Tag.Float("lastDamageAmount");
	public static final Tag<Integer> LAST_DAMAGED_BY = Tag.Integer("lastDamagedBy");
	
	public static EventNode<EntityInstanceEvent> events(DamageConfig config) {
		EventNode<EntityInstanceEvent> node = EventNode.type("damage-events", PvPConfig.ENTITY_INSTANCE_FILTER);
		
		node.addListener(EventListener.builder(EntityDamageEvent.class)
				.handler(event -> handleEntityDamage(event, config))
				.build());
		
		if (config.isFallDamageEnabled()) {
			node.addListener(EntityTickEvent.class, event -> {
				if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
				if (livingEntity instanceof Player) return;
				Pos previousPosition = EntityUtils.getPreviousPosition(livingEntity);
				if (previousPosition == null) return;
				FallDamageHandler.handleFallDamage(
						livingEntity, previousPosition,
						livingEntity.getPosition(), livingEntity.isOnGround()
				);
			});
			node.addListener(PlayerMoveEvent.class, event -> {
				Player player = event.getPlayer();
				FallDamageHandler.handleFallDamage(
						player, player.getPosition(),
						event.getNewPosition(), event.isOnGround()
				);
			});
		}
		
		return node;
	}
	
	private static CustomDamageType getCustomDamageType(EntityDamageEvent event) {
		if (event.getDamageType() instanceof CustomDamageType) {
			return (CustomDamageType) event.getDamageType();
		} else {
			if (event.getDamageType() == DamageType.GRAVITY) {
				return CustomDamageType.FALL;
			} else if (event.getDamageType() == DamageType.ON_FIRE) {
				return CustomDamageType.ON_FIRE;
			} else {
				return CustomDamageType.OUT_OF_WORLD;
			}
		}
	}
	
	private static float scaleWithDifficulty(float amount) {
		return switch (MinecraftServer.getDifficulty()) {
			case PEACEFUL -> -1;
			case EASY -> Math.min(amount / 2.0F + 1.0F, amount);
			case HARD -> amount * 3.0F / 2.0F;
			default -> amount;
		};
	}
	
	private static void takeShieldHit(LivingEntity entity, LivingEntity attacker, boolean applyKnockback) {
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
			disableShield((Player) entity, true); // For some reason the vanilla server always passes true
		}
	}
	
	private static void disableShield(Player player, boolean sprinting) {
		float chance = 0.25F + (float) EnchantmentUtils.getBlockEfficiency(player) * 0.05F;
		if (sprinting) chance += 0.75F;
		
		if (ThreadLocalRandom.current().nextFloat() < chance) {
			Tracker.setCooldown(player, Material.SHIELD, 100);
			
			// Shield disable status
			player.triggerStatus((byte) 30);
			player.triggerStatus((byte) 9);
			
			Player.Hand hand = player.getEntityMeta().getActiveHand();
			player.refreshActiveHand(false, hand == Player.Hand.OFF, false);
		}
	}
	
	private static Pair<Boolean, Float> handleShield(LivingEntity entity, Entity attacker,
	                                                 CustomDamageType type, float amount,
	                                                 DamageConfig config) {
		if (!config.isShieldEnabled() || amount <= 0
				|| !EntityUtils.blockedByShield(entity, type, config.isLegacyShieldMechanics()))
			return Pair.of(false, amount);
		
		float resultingDamage = config.isLegacyShieldMechanics() ? Math.max(0, (amount + 1) * 0.5f) : 0;
		
		DamageBlockEvent damageBlockEvent = new DamageBlockEvent(entity, amount, resultingDamage);
		EventDispatcher.call(damageBlockEvent);
		if (damageBlockEvent.isCancelled()) return Pair.of(false, amount);
		
		if (config.isEquipmentDamageEnabled() && amount >= 3) {
			int shieldDamage = 1 + (int) Math.floor(amount);
			Player.Hand hand = EntityUtils.getActiveHand(entity);
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
		if (!type.isProjectile() && attacker instanceof LivingEntity)
			takeShieldHit(entity, (LivingEntity) attacker, damageBlockEvent.knockbackAttacker());
		
		boolean everythingBlocked = damageBlockEvent.getResultingDamage() == 0;
		return Pair.of(everythingBlocked, amount);
	}
	
	private static void displayAnimation(LivingEntity entity, CustomDamageType type,
	                                     boolean shield, DamageConfig config) {
		if (shield) {
			entity.triggerStatus((byte) 29);
			return;
		}
		if (!config.isDamageAnimation()) return;
		
		if (type instanceof CustomEntityDamage && ((CustomEntityDamage) type).isThorns()) {
			entity.triggerStatus((byte) 33);
			return;
		}
		
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
	
	private static void applyKnockback(LivingEntity entity, Entity attacker,
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
			EventDispatcher.callCancellable(legacyKnockbackEvent, () -> {
				LegacyKnockbackSettings settings = legacyKnockbackEvent.getSettings();
				
				float kbResistance = entity.getAttributeValue(Attribute.KNOCKBACK_RESISTANCE);
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
			});
		} else {
			EntityKnockbackEvent knockbackEvent = new EntityKnockbackEvent(
					entity, directAttacker == null ? attacker : directAttacker,
					false, false,
					0.4F
			);
			EventDispatcher.callCancellable(knockbackEvent, () ->
					entity.takeKnockback(knockbackEvent.getStrength(), finalDx, finalDz));
		}
	}
	
	public static void handleEntityDamage(EntityDamageEvent event, DamageConfig config) {
		event.setAnimation(false);
		event.setSound(null);
		
		float amount = event.getDamage();
		CustomDamageType type = getCustomDamageType(event);
		if (event.getEntity() instanceof Player && type.isScaledWithDifficulty())
			amount = scaleWithDifficulty(amount);
		
		LivingEntity entity = event.getEntity();
		if (type.isFire() && EntityUtils.hasEffect(entity, PotionEffect.FIRE_RESISTANCE)) {
			event.setCancelled(true);
			return;
		}
		
		if (config.isEquipmentDamageEnabled() && type.damagesHelmet()
				&& !entity.getEquipment(EquipmentSlot.HELMET).isAir()) {
			ItemUtils.damageArmor(entity, type, amount, EquipmentSlot.HELMET);
			amount *= 0.75F;
		}
		
		Entity attacker = type.getEntity();
		if (entity instanceof Player && attacker instanceof LivingEntity) {
			entity.setTag(LAST_DAMAGED_BY, attacker.getEntityId());
			entity.setTag(LAST_DAMAGE_TIME, System.currentTimeMillis());
		}
		
		Pair<Boolean, Float> result = handleShield(entity, attacker, type, amount, config);
		boolean shield = result.first();
		amount = result.second();
		
		// Invulnerability ticks
		boolean hurtSoundAndAnimation = true;
		float amountBeforeProcessing = amount;
		long newDamageTime = entity.hasTag(NEW_DAMAGE_TIME) ? entity.getTag(NEW_DAMAGE_TIME) : -10000;
		if (entity.getAliveTicks() - newDamageTime < 0) {
			float lastDamage = entity.hasTag(LAST_DAMAGE_AMOUNT) ? entity.getTag(LAST_DAMAGE_AMOUNT) : 0;
			
			if (amount <= lastDamage) {
				event.setCancelled(true);
				return;
			}
			
			hurtSoundAndAnimation = false;
			amount = amount - lastDamage;
		}
		
		// Process armor and effects
		amount = getDamageWithProtection(entity, type, amount, config);
		
		FinalDamageEvent finalDamageEvent = new FinalDamageEvent(entity, type, amount, config.getInvulnerabilityTicks());
		EventDispatcher.call(finalDamageEvent);
		amount = finalDamageEvent.getDamage();
		
		boolean register = config.isLegacy() || finalDamageEvent.getDamage() > 0;
		if (register && entity instanceof Player)
			Tracker.combatManager.get(entity.getUuid()).recordDamage(type, amount);
		
		if (!register || finalDamageEvent.isCancelled()) {
			event.setCancelled(true);
			return;
		}
		
		// Exhaustion from damage
		if (config.isExhaustionEnabled() && amount != 0 && entity instanceof Player)
			EntityUtils.addExhaustion((Player) entity,
					type.getExhaustion() * (config.isLegacy() ? 3 : 1));
		
		entity.setTag(LAST_DAMAGE_AMOUNT, amountBeforeProcessing);
		
		if (hurtSoundAndAnimation) {
			entity.setTag(NEW_DAMAGE_TIME, entity.getAliveTicks() + finalDamageEvent.getInvulnerabilityTicks());
			
			displayAnimation(entity, type, shield, config);
			
			if (!shield) {
				if (attacker != null) {
					applyKnockback(entity, attacker, type.getDirectEntity(), config);
				} else if (type != CustomDamageType.DROWN) {
					// Update velocity
					entity.setVelocity(entity.getVelocity());
				}
			}
		}
		
		if (shield) {
			event.setCancelled(true);
			return;
		}
		
		SoundEvent sound = null;
		
		boolean death = false;
		float totalHealth = entity.getHealth() +
				(entity instanceof Player ? ((Player) entity).getAdditionalHearts() : 0);
		if (totalHealth - amount <= 0) {
			boolean totem = totemProtection(entity, type);
			
			if (totem) {
				event.setCancelled(true);
			} else {
				death = true;
				if (hurtSoundAndAnimation) {
					// Death sound
					sound = type.getDeathSound(entity);
				}
			}
		} else if (hurtSoundAndAnimation) {
			// Damage sound
			sound = type.getSound(entity);
		}
		
		// Play sound (copied from Minestom, because of complications with cancelling)
		if (config.isSoundsEnabled() && sound != null) entity.sendPacketToViewersAndSelf(new SoundEffectPacket(
				sound, entity instanceof Player ? Sound.Source.PLAYER : Sound.Source.HOSTILE,
				entity.getPosition(),
				1.0f, 1.0f
		));
		
		if (death && !event.isCancelled()) {
			EntityPreDeathEvent entityPreDeathEvent = new EntityPreDeathEvent(entity, type);
			EventDispatcher.call(entityPreDeathEvent);
			if (entityPreDeathEvent.isCancelled()) event.setCancelled(true);
			if (entityPreDeathEvent.isCancelDeath()) amount = 0;
		}
		
		if (config.shouldPerformDamage()) {
			// The Minestom damage method should return false if there was no hurt animation,
			// because otherwise the AttackManager will deal extra knockback
			if (!event.isCancelled() && !hurtSoundAndAnimation) {
				event.setCancelled(true);
				damageManually(entity, amount);
			} else {
				event.setDamage(amount);
			}
		} else {
			event.setCancelled(true);
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
	
	public static float getDamageWithProtection(LivingEntity entity, CustomDamageType type,
	                                            float amount, DamageConfig config) {
		amount = getDamageWithArmor(entity, type, amount, config);
		return getDamageWithEnchantments(entity, type, amount, config);
	}
	
	public static float getDamageWithArmor(LivingEntity entity, CustomDamageType type,
	                                       float amount, DamageConfig config) {
		if (config.isArmorDisabled()) return amount;
		if (type.bypassesArmor()) return amount;
		
		float armorValue = entity.getAttributeValue(Attribute.ARMOR);
		if (config.isLegacy()) {
			int armorMultiplier = 25 - (int) armorValue;
			return (amount * (float) armorMultiplier) / 25;
		} else {
			return getDamageLeft(
					amount, (float) Math.floor(armorValue),
					entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS)
			);
		}
	}
	
	public static float getDamageWithEnchantments(LivingEntity entity, CustomDamageType type,
	                                              float amount, DamageConfig config) {
		if (type.isUnblockable()) {
			return amount;
		}
		
		int k;
		if (EntityUtils.hasEffect(entity, PotionEffect.RESISTANCE)) {
			k = (EntityUtils.getEffect(entity, PotionEffect.RESISTANCE).amplifier() + 1) * 5;
			int j = 25 - k;
			float f = amount * (float) j;
			amount = Math.max(f / 25, 0);
		}
		
		if (config.isArmorDisabled()) return amount;
		
		if (amount <= 0) {
			return 0;
		} else {
			k = EnchantmentUtils.getProtectionAmount(EntityUtils.getArmorItems(entity), type);
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
