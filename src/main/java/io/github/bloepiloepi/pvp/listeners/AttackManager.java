package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.config.AttackConfig;
import io.github.bloepiloepi.pvp.config.PvPConfig;
import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entity.EntityGroup;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.entity.PvpPlayer;
import io.github.bloepiloepi.pvp.entity.Tracker;
import io.github.bloepiloepi.pvp.enums.Tool;
import io.github.bloepiloepi.pvp.events.EntityKnockbackEvent;
import io.github.bloepiloepi.pvp.events.FinalAttackEvent;
import io.github.bloepiloepi.pvp.events.LegacyKnockbackEvent;
import io.github.bloepiloepi.pvp.events.PlayerSpectateEvent;
import io.github.bloepiloepi.pvp.legacy.LegacyKnockbackSettings;
import io.github.bloepiloepi.pvp.utils.ItemUtils;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;

import java.util.concurrent.ThreadLocalRandom;

public class AttackManager {
	public static final Tag<Long> LAST_ATTACKED_TICKS = Tag.Long("lastAttackedTicks");
	
	public static EventNode<EntityInstanceEvent> events(AttackConfig config) {
		EventNode<EntityInstanceEvent> node = EventNode.type("attack-events", PvPConfig.ENTITY_INSTANCE_FILTER);
		
		node.addListener(EntityAttackEvent.class, event -> performAttack(event.getEntity(), event.getTarget(), config));
		
		if (!config.isLegacy()) {
			node.addListener(EventListener.builder(PlayerHandAnimationEvent.class).handler(event ->
					resetCooldownProgress(event.getPlayer())).build());
			
			node.addListener(EventListener.builder(PlayerChangeHeldSlotEvent.class).handler(event -> {
				if (!event.getPlayer().getItemInMainHand().isSimilar(event.getPlayer().getInventory().getItemStack(event.getSlot()))) {
					resetCooldownProgress(event.getPlayer());
				}
			}).build());
		}
		
		return node;
	}
	
	public static float getAttackCooldownProgressPerTick(Player player) {
		return 1 / player.getAttributeValue(Attribute.ATTACK_SPEED) * 20;
	}
	
	@SuppressWarnings({"UnstableApiUsage", "ConstantConditions"})
	public static double getAttackCooldownProgress(Player player) {
		long lastAttacked = player.getTag(LAST_ATTACKED_TICKS);
		long timeSinceLastAttacked = player.getInstance().getWorldAge() - lastAttacked;
		return MathUtils.clamp((timeSinceLastAttacked + 0.5) / getAttackCooldownProgressPerTick(player), 0, 1);
	}
	
	@SuppressWarnings("ConstantConditions")
	public static void resetCooldownProgress(Player player) {
		player.setTag(LAST_ATTACKED_TICKS, player.getInstance().getWorldAge());
	}
	
	public static void spectateTick(Player player) {
		Entity spectating = Tracker.spectating.get(player.getUuid());
		if (spectating == null || spectating == player) return;
		
		//This is to make sure other players don't see the player standing still while spectating
		//And when the player stops spectating, they are at the entities position instead of their position before spectating
		player.teleport(spectating.getPosition());
		
		if (player.getEntityMeta().isSneaking() || spectating.isRemoved() || (spectating instanceof LivingEntity livingSpectating && livingSpectating.isDead())) {
			player.stopSpectating();
			Tracker.spectating.remove(player.getUuid());
		}
	}
	
	public static void makeSpectate(Player player, Entity target) {
		PlayerSpectateEvent playerSpectateEvent = new PlayerSpectateEvent(player, target);
		EventDispatcher.callCancellable(playerSpectateEvent, () -> {
			player.spectate(target);
			Tracker.spectating.put(player.getUuid(), target);
		});
	}
	
	public static void performAttack(Entity entity, Entity target, AttackConfig config) {
		if (!(entity instanceof LivingEntity attacker)) return;
		if (attacker.isDead()) return;
		if (entity.getDistanceSquared(target) >= 36.0D) return;
		
		if (attacker instanceof Player player && player.getGameMode() == GameMode.SPECTATOR && config.isSpectatingEnabled()) {
			makeSpectate(player, target);
			return;
		}
		
		float damage = attacker.getAttributeValue(Attribute.ATTACK_DAMAGE);
		float enchantedDamage;
		if (target instanceof LivingEntity livingTarget) {
			enchantedDamage = EnchantmentUtils.getAttackDamage(attacker.getItemInMainHand(), EntityGroup.ofEntity(livingTarget), config.isLegacy());
		} else {
			enchantedDamage = EnchantmentUtils.getAttackDamage(attacker.getItemInMainHand(), EntityGroup.DEFAULT, config.isLegacy());
		}
		
		double cooldownProgress = config.isAttackCooldownEnabled() && attacker instanceof Player player ? getAttackCooldownProgress(player) : 1;
		damage *= 0.2 + cooldownProgress * cooldownProgress * 0.8;
		enchantedDamage *= cooldownProgress;
		if (attacker instanceof Player player) resetCooldownProgress(player);
		
		boolean strongAttack = cooldownProgress > 0.9F;
		boolean sprintAttack = attacker.isSprinting() && strongAttack;
		int knockback = EnchantmentUtils.getKnockback(attacker);
		
		boolean critical = strongAttack && !EntityUtils.isClimbing(attacker) && attacker.getVelocity().y() < 0 && !attacker.isOnGround() && !EntityUtils.hasEffect(attacker, PotionEffect.BLINDNESS) && attacker.getVehicle() == null && target instanceof LivingEntity;
		if (!config.isLegacy()) {
			// Not sprinting required for critical in 1.9+
			critical = critical && !attacker.isSprinting();
		}
		
		boolean sweeping = false;
		if (!config.isLegacy() && strongAttack && !critical && !sprintAttack && attacker.isOnGround()) {
			Pos previousPosition = EntityUtils.getPreviousPosition(attacker);
			if (previousPosition != null) {
				double lastMoveDistance = previousPosition.distance(attacker.getPosition()) * 0.6;
				if (lastMoveDistance < attacker.getAttributeValue(Attribute.MOVEMENT_SPEED)) {
					Tool tool = Tool.fromMaterial(attacker.getItemInMainHand().material());
					if (tool != null && tool.isSword()) sweeping = true;
				}
			}
		}
		
		FinalAttackEvent finalAttackEvent = new FinalAttackEvent(attacker, target, sprintAttack, critical, sweeping, damage, enchantedDamage, config.isSoundsEnabled(), true);
		EventDispatcher.call(finalAttackEvent);
		if (finalAttackEvent.isCancelled()) return;
		
		sprintAttack = finalAttackEvent.isSprint();
		critical = finalAttackEvent.isCritical();
		sweeping = finalAttackEvent.isSweeping();
		damage = finalAttackEvent.getBaseDamage();
		enchantedDamage = finalAttackEvent.getEnchantsExtraDamage();
		
		if (critical) {
			if (config.isLegacy()) {
				damage += ThreadLocalRandom.current().nextInt((int) (damage / 2 + 2));
			} else {
				damage *= 1.5F;
			}
		}
		
		damage += enchantedDamage;
		
		float originalHealth = 0;
		if (target instanceof LivingEntity livingTarget)
			originalHealth = livingTarget.getHealth();
		
		if (config.isLegacy() && attacker instanceof PvpPlayer custom)
			custom.afterSprintAttack();
		
		boolean damageSucceeded = EntityUtils.damage(target, attacker instanceof Player player ? CustomDamageType.player(player) : CustomDamageType.mob(attacker), damage);
		
		if (sprintAttack) {
			if (config.isSoundsEnabled() && finalAttackEvent.hasAttackSounds()) {
				if (damageSucceeded || finalAttackEvent.playSoundsOnFail()) {
					SoundManager.sendHostileToAround(attacker, SoundEvent.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.0F);
				}
			}
			
			knockback++;
		}
		
		if (!damageSucceeded) {
			if (config.isSoundsEnabled() && finalAttackEvent.hasAttackSounds() && finalAttackEvent.playSoundsOnFail()) {
				SoundManager.sendHostileToAround(attacker, SoundEvent.ENTITY_PLAYER_ATTACK_NODAMAGE, 1.0F, 1.0F);
			}
			return;
		}
		
		if (knockback > 0) {
			if (!config.isLegacyKnockback()) {
				EntityKnockbackEvent entityKnockbackEvent = new EntityKnockbackEvent(target, attacker, true, false, knockback * 0.5F);
				EventDispatcher.callCancellable(entityKnockbackEvent, () -> {
					float strength = entityKnockbackEvent.getStrength();
					if (target instanceof LivingEntity living) {
						living.takeKnockback(strength, Math.sin(Math.toRadians(attacker.getPosition().yaw())), -Math.cos(Math.toRadians(attacker.getPosition().yaw())));
					} else {
						target.setVelocity(target.getVelocity().add(-Math.sin(Math.toRadians(attacker.getPosition().yaw())) * strength, 0.1D, Math.cos(Math.toRadians(attacker.getPosition().yaw())) * strength));
					}
				});
			} else {
				float finalKnockback;
				if (target instanceof LivingEntity livingTarget) {
					float knockbackResistance = livingTarget.getAttributeValue(Attribute.KNOCKBACK_RESISTANCE);
					finalKnockback = knockback * (1 - knockbackResistance);
				} else {
					finalKnockback = knockback;
				}
				
				LegacyKnockbackEvent legacyKnockbackEvent = new LegacyKnockbackEvent(target, attacker, true);
				EventDispatcher.callCancellable(legacyKnockbackEvent, () -> {
					LegacyKnockbackSettings settings = legacyKnockbackEvent.getSettings();
					target.setVelocity(target.getVelocity().add(
							-Math.sin(attacker.getPosition().yaw() * Math.PI / 180.0F) * finalKnockback * settings.extraHorizontal(),
							settings.extraVertical(),
							Math.cos(attacker.getPosition().yaw() * Math.PI / 180.0F) * finalKnockback * settings.extraHorizontal()
					));
				});
			}
			
			if (!config.isLegacy() && attacker instanceof PvpPlayer custom)
				custom.afterSprintAttack();
			
			attacker.setSprinting(false);
		}
		
		if (sweeping) {
			float sweepingDamage = 1.0F + EnchantmentUtils.getSweepingMultiplier(attacker) * damage;
			BoundingBox boundingBox = target.getBoundingBox().expand(1.0D, 0.25D, 1.0D);
			for (Entity nearbyEntity : target.getInstance().getNearbyEntities(target.getPosition(), 2)) {
				if (nearbyEntity == target || nearbyEntity == attacker) continue;
				if (!(nearbyEntity instanceof LivingEntity living)) continue;
				if (nearbyEntity.getEntityMeta() instanceof ArmorStandMeta) continue;
				if (!boundingBox.intersectEntity(target.getPosition(), nearbyEntity)) continue;
				
				if (attacker.getPosition().distanceSquared(nearbyEntity.getPosition()) < 9.0) {
					EntityKnockbackEvent entityKnockbackEvent = new EntityKnockbackEvent(nearbyEntity, attacker, false, true, 0.4F);
					EventDispatcher.callCancellable(entityKnockbackEvent, () -> {
						float strength = entityKnockbackEvent.getStrength();
						entity.takeKnockback(strength, Math.sin(Math.toRadians(attacker.getPosition().yaw())), -Math.cos(Math.toRadians(attacker.getPosition().yaw())));
					});
					living.damage(attacker instanceof Player player ? CustomDamageType.player(player) : CustomDamageType.mob(attacker), sweepingDamage);
				}
			}
			
			if (config.isSoundsEnabled() && finalAttackEvent.hasAttackSounds()) SoundManager.sendHostileToAround(attacker, SoundEvent.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.0F);
			Pos pos = attacker.getPosition();
			double x = -Math.sin(Math.toRadians(pos.yaw()));
			double z = Math.cos(Math.toRadians(pos.yaw()));
			
			ParticlePacket packet = ParticleCreator.createParticlePacket(
					Particle.SWEEP_ATTACK, false,
					pos.x() + x, EntityUtils.getBodyY(attacker, 0.5), pos.z() + z,
					(float) x, 0, (float) z,
					0, 0, null);
			
			attacker.sendPacketToViewersAndSelf(packet);
		}
		
		if (critical) {
			if (config.isSoundsEnabled() && finalAttackEvent.hasAttackSounds()) SoundManager.sendHostileToAround(attacker, SoundEvent.ENTITY_PLAYER_ATTACK_CRIT, 1.0F, 1.0F);
			attacker.sendPacketToViewersAndSelf(new EntityAnimationPacket(target.getEntityId(), EntityAnimationPacket.Animation.CRITICAL_EFFECT));
		}
		
		if (!critical && !sweeping) {
			if (strongAttack) {
				if (config.isSoundsEnabled() && finalAttackEvent.hasAttackSounds()) SoundManager.sendHostileToAround(attacker, SoundEvent.ENTITY_PLAYER_ATTACK_STRONG, 1.0F, 1.0F);
			} else {
				if (config.isSoundsEnabled() && finalAttackEvent.hasAttackSounds()) SoundManager.sendHostileToAround(attacker, SoundEvent.ENTITY_PLAYER_ATTACK_WEAK, 1.0F, 1.0F);
			}
		}
		
		if (enchantedDamage > 0) attacker.sendPacketToViewersAndSelf(new EntityAnimationPacket(target.getEntityId(), EntityAnimationPacket.Animation.MAGICAL_CRITICAL_EFFECT));
		
		if (target instanceof LivingEntity livingTarget) EnchantmentUtils.onUserDamaged(livingTarget, attacker);
		EnchantmentUtils.onTargetDamaged(attacker, target);
		//TODO target and user damaged should also work when non-player mob attacks (mobs, arrows, trident)
		
		Tool tool = Tool.fromMaterial(attacker.getItemInMainHand().material());
		if (tool != null && config.isToolDamageEnabled())
			ItemUtils.damageEquipment(attacker, EquipmentSlot.MAIN_HAND, (tool.isSword() || tool == Tool.TRIDENT) ? 1 : 2);
		
		if (target instanceof LivingEntity livingTarget) {
			int fireAspect = EnchantmentUtils.getFireAspect(attacker);
			if (fireAspect > 0) EntityUtils.setOnFireForSeconds(target, fireAspect * 4);
			
			float damageDone = originalHealth - livingTarget.getHealth();
			
			// Damage indicator particles
			if (config.isDamageIndicatorParticlesEnabled() && damageDone > 2) {
				int count = (int) (damageDone * 0.5);
				Pos targetPosition = target.getPosition();
				ParticlePacket packet = ParticleCreator.createParticlePacket(
						Particle.DAMAGE_INDICATOR, false,
						targetPosition.x(), EntityUtils.getBodyY(target, 0.5), targetPosition.z(),
						0.1F, 0F, 0.1F,
						0.2F, count, null);
				
				target.sendPacketToViewersAndSelf(packet);
			}
		}
		
		if (config.isExhaustionEnabled() && attacker instanceof Player player) EntityUtils.addExhaustion(player, config.isLegacy() ? 0.3F: 0.1F);
	}
}
