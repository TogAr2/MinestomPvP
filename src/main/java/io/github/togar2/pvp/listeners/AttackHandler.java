package io.github.togar2.pvp.listeners;

import io.github.togar2.pvp.config.AttackConfig;
import io.github.togar2.pvp.enchantment.EnchantmentUtils;
import io.github.togar2.pvp.entity.EntityGroup;
import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.entity.PvpPlayer;
import io.github.togar2.pvp.enums.Tool;
import io.github.togar2.pvp.events.EntityKnockbackEvent;
import io.github.togar2.pvp.events.FinalAttackEvent;
import io.github.togar2.pvp.events.LegacyKnockbackEvent;
import io.github.togar2.pvp.feature.attack.AttackValues;
import io.github.togar2.pvp.legacy.LegacyKnockbackSettings;
import io.github.togar2.pvp.utils.ItemUtils;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class AttackHandler {
	public static final Tag<Long> LAST_ATTACKED_TICKS = Tag.Long("lastAttackedTicks");
	public static final Tag<Integer> SPECTATING = Tag.Integer("spectating");
	
	protected double getAttackCooldownProgressPerTick(Player player) {
		return (1 / player.getAttributeValue(Attribute.ATTACK_SPEED)) * 20;
	}
	
	@SuppressWarnings({"UnstableApiUsage"})
	protected double getAttackCooldownProgress(Player player) {
		long lastAttacked = player.getTag(AttackHandler.LAST_ATTACKED_TICKS);
		long timeSinceLastAttacked = player.getAliveTicks() - lastAttacked;
		return MathUtils.clamp(
				(timeSinceLastAttacked + 0.5) / getAttackCooldownProgressPerTick(player),
				0, 1
		);
	}
	
	protected void resetCooldownProgress(Player player) {
		player.setTag(AttackHandler.LAST_ATTACKED_TICKS, player.getAliveTicks());
	}
	
	/**
	 * The main method of the attack handler, gets called whenever an attack event happens
	 */
	public void performAttack(Entity entity, Entity target, AttackConfig config) {
		if (!(entity instanceof LivingEntity attacker)) return;
		if (attacker.isDead()) return;
		if (entity.getDistanceSquared(target) >= 36.0D) return;
		
		AttackValues.Final attack = prepareAttack(attacker, target, config);
		if (attack == null) return; // Event cancelled
		
		// If legacy, attacker velocity is reduced before the knockback
		if (config.isLegacy() && attacker instanceof PvpPlayer custom)
			custom.afterSprintAttack();
		
		float originalHealth = 0;
		boolean damageSucceeded = false;
		if (target instanceof LivingEntity livingTarget) {
			originalHealth = livingTarget.getHealth();
			damageSucceeded = livingTarget.damage(new Damage(
					attacker instanceof Player ? DamageType.PLAYER_ATTACK : DamageType.MOB_ATTACK,
					attacker, attacker,
					null, attack.damage()
			));
		}
		
		if (!damageSucceeded) {
			// No damage sound
			if (config.isSoundsEnabled() && attack.sounds() && attack.playSoundsOnFail()) {
				ViewUtil.viewersAndSelf(attacker).playSound(Sound.sound(
						SoundEvent.ENTITY_PLAYER_ATTACK_NODAMAGE, Sound.Source.PLAYER,
						1.0f, 1.0f
				), attacker);
			}
			return;
		}
		
		// Target is always living now, because the damage would not have succeeded if it wasn't
		LivingEntity living = (LivingEntity) target;
		
		// Knockback and sweeping
		applyKnockback(attacker, living, attack.knockback(), config);
		if (attack.sweeping()) applySweeping(attacker, living, attack.damage());
		
		if (target instanceof PvpPlayer customPlayer)
			customPlayer.sendImmediateVelocityUpdate();
		
		// Play attack sounds
		if (config.isSoundsEnabled() && attack.sounds()) {
			Audience audience = attacker.getViewersAsAudience();
			if (attacker instanceof Player player)
				audience = Audience.audience(audience, player);
			
			if (attack.sprint()) audience.playSound(Sound.sound(
					SoundEvent.ENTITY_PLAYER_ATTACK_KNOCKBACK, Sound.Source.PLAYER,
					1.0f, 1.0f
			), attacker);
			
			if (attack.sweeping()) audience.playSound(Sound.sound(
					SoundEvent.ENTITY_PLAYER_ATTACK_SWEEP, Sound.Source.PLAYER,
					1.0f, 1.0f
			), attacker);
			
			if (attack.critical()) audience.playSound(Sound.sound(
					SoundEvent.ENTITY_PLAYER_ATTACK_CRIT, Sound.Source.PLAYER,
					1.0f, 1.0f
			), attacker);
			
			if (!attack.critical() && !attack.sweeping()) audience.playSound(Sound.sound(
					attack.strong() ?
							SoundEvent.ENTITY_PLAYER_ATTACK_STRONG :
							SoundEvent.ENTITY_PLAYER_ATTACK_WEAK,
					Sound.Source.PLAYER, 1.0f, 1.0f
			), attacker);
		}
		
		// Play attack effects
		if (attack.critical()) attacker.sendPacketToViewersAndSelf(new EntityAnimationPacket(
				target.getEntityId(),
				EntityAnimationPacket.Animation.CRITICAL_EFFECT
		));
		if (attack.magical()) attacker.sendPacketToViewersAndSelf(new EntityAnimationPacket(
				target.getEntityId(),
				EntityAnimationPacket.Animation.MAGICAL_CRITICAL_EFFECT
		));
		
		// Thorns
		EnchantmentUtils.onUserDamaged(living, attacker);
		EnchantmentUtils.onTargetDamaged(attacker, target);
		
		// Damage item
		Tool tool = Tool.fromMaterial(attacker.getItemInMainHand().material());
		if (tool != null && config.isToolDamageEnabled())
			ItemUtils.damageEquipment(attacker, EquipmentSlot.MAIN_HAND,
					(tool.isSword() || tool == Tool.TRIDENT) ? 1 : 2);
		
		if (attack.fireAspect() > 0)
			EntityUtils.setOnFireForSeconds(target, attack.fireAspect() * 4);
		
		// Damage indicator particles
		float damageDone = originalHealth - living.getHealth();
		if (config.isDamageIndicatorParticlesEnabled() && damageDone > 2) {
			int particleCount = (int) (damageDone * 0.5);
			Pos targetPosition = target.getPosition();
			target.sendPacketToViewersAndSelf(new ParticlePacket(
					Particle.DAMAGE_INDICATOR, false,
					targetPosition.x(), EntityUtils.getBodyY(target, 0.5), targetPosition.z(),
					0.1f, 0, 0.1f,
					0.2F, particleCount
			));
		}
		
		if (config.isExhaustionEnabled() && attacker instanceof Player player)
			EntityUtils.addExhaustion(player, config.isLegacy() ? 0.3f: 0.1f);
	}
	
	protected @Nullable AttackValues.Final prepareAttack(LivingEntity attacker, Entity target,
	                                                     AttackConfig config) {
		float damage = attacker.getAttributeValue(Attribute.ATTACK_DAMAGE);
		float enchantedDamage = EnchantmentUtils.getAttackDamage(
				attacker.getItemInMainHand(),
				target instanceof LivingEntity living ? EntityGroup.ofEntity(living) : EntityGroup.DEFAULT,
				config.isLegacy()
		);
		
		double cooldownProgress = 1;
		if (config.isAttackCooldownEnabled() && attacker instanceof Player player) {
			cooldownProgress = getAttackCooldownProgress(player);
			resetCooldownProgress(player);
		}
		
		// Apply cooldownProgress to damage
		damage *= 0.2 + cooldownProgress * cooldownProgress * 0.8;
		enchantedDamage *= cooldownProgress;
		
		// Calculate attacks
		boolean strongAttack = cooldownProgress > 0.9;
		boolean sprintAttack = attacker.isSprinting() && strongAttack;
		int knockback = EnchantmentUtils.getKnockback(attacker);
		boolean critical = isCritical(attacker, target, strongAttack, config.isLegacy());
		boolean sweeping = !config.isLegacy() && shouldSweep(attacker, strongAttack, critical, sprintAttack);
		int fireAspect = EnchantmentUtils.getFireAspect(attacker);
		
		FinalAttackEvent finalAttackEvent = new FinalAttackEvent(
				attacker, target, sprintAttack, critical, sweeping, damage,
				enchantedDamage, config.isSoundsEnabled(), true
		);
		EventDispatcher.call(finalAttackEvent);
		if (finalAttackEvent.isCancelled()) return null;
		
		sprintAttack = finalAttackEvent.isSprint();
		critical = finalAttackEvent.isCritical();
		sweeping = finalAttackEvent.isSweeping();
		damage = finalAttackEvent.getBaseDamage();
		enchantedDamage = finalAttackEvent.getEnchantsExtraDamage();
		
		if (critical) damage = applyCritical(damage, config.isLegacy());
		damage += enchantedDamage;
		
		if (sprintAttack) knockback++;
		
		return new AttackValues.Final(
				damage, strongAttack, sprintAttack, knockback, critical,
				enchantedDamage > 0, fireAspect, sweeping,
				finalAttackEvent.hasAttackSounds(),
				finalAttackEvent.playSoundsOnFail()
		);
	}
	
	protected boolean isCritical(LivingEntity attacker, Entity target,
	                                  boolean strongAttack, boolean legacy) {
		boolean critical = strongAttack && !EntityUtils.isClimbing(attacker)
				&& attacker.getVelocity().y() < 0 && !attacker.isOnGround()
				&& !attacker.hasEffect(PotionEffect.BLINDNESS)
				&& attacker.getVehicle() == null && target instanceof LivingEntity;
		if (legacy) return critical;
		
		// Not sprinting required for critical in 1.9+
		return critical && !attacker.isSprinting();
	}
	
	protected boolean shouldSweep(LivingEntity attacker, boolean strongAttack,
	                              boolean critical, boolean sprintAttack) {
		if (!strongAttack || critical || sprintAttack || !attacker.isOnGround()) return false;
		
		Pos previousPosition = EntityUtils.getPreviousPosition(attacker);
		if (previousPosition == null) return false;
		double lastMoveDistance = previousPosition.distance(attacker.getPosition()) * 0.6;
		if (lastMoveDistance >= attacker.getAttributeValue(Attribute.MOVEMENT_SPEED)) return false;
		
		Tool tool = Tool.fromMaterial(attacker.getItemInMainHand().material());
		return tool != null && tool.isSword();
	}
	
	protected float applyCritical(float damage, boolean legacy) {
		if (legacy) {
			return damage + ThreadLocalRandom.current().nextInt((int) (damage / 2 + 2));
		} else {
			return damage * 1.5f;
		}
	}
	
	protected void applyKnockback(LivingEntity attacker, LivingEntity target,
	                              int knockback, AttackConfig config) {
		if (knockback <= 0) return;
		
		if (config.isLegacyKnockback()) {
			LegacyKnockbackEvent knockbackEvent = new LegacyKnockbackEvent(target, attacker, true);
			EventDispatcher.callCancellable(knockbackEvent, () -> {
				LegacyKnockbackSettings settings = knockbackEvent.getSettings();
				
				float kbResistance = target.getAttributeValue(Attribute.KNOCKBACK_RESISTANCE);
				double horizontal = settings.extraHorizontal() * (1 - kbResistance) * knockback;
				double vertical = settings.extraVertical() * (1 - kbResistance) * knockback;
				
				Vec horizontalModifier = new Vec(
						Math.sin(Math.toRadians(attacker.getPosition().yaw())),
						-Math.cos(Math.toRadians(attacker.getPosition().yaw()))
				).normalize().mul(horizontal);
				
				Vec velocity = target.getVelocity();
				target.setVelocity(new Vec(
						velocity.x() / 2d - horizontalModifier.x(),
						target.isOnGround() ? Math.min(
								settings.verticalLimit(), velocity.y() + vertical) : velocity.y(),
						velocity.z() / 2d - horizontalModifier.z()
				));
			});
		} else {
			EntityKnockbackEvent knockbackEvent = new EntityKnockbackEvent(
					target, attacker,
					EntityKnockbackEvent.KnockbackType.ATTACK,
					knockback * 0.5F
			);
			EventDispatcher.callCancellable(knockbackEvent, () -> {
				float strength = knockbackEvent.getStrength();
				target.takeKnockback(strength,
						Math.sin(Math.toRadians(attacker.getPosition().yaw())),
						-Math.cos(Math.toRadians(attacker.getPosition().yaw()))
				);
			});
		}
		
		// If not legacy, attacker velocity is reduced after the knockback
		if (!config.isLegacy() && attacker instanceof PvpPlayer custom)
			custom.afterSprintAttack();
		
		attacker.setSprinting(false);
	}
	
	protected void applySweeping(LivingEntity attacker, LivingEntity target, float damage) {
		float sweepingMultiplier = 0;
		int sweepingLevel = EnchantmentUtils.getSweeping(attacker);
		if (sweepingLevel > 0) sweepingMultiplier = 1.0f - (1.0f / (float) (sweepingLevel + 1));
		float sweepingDamage = 1.0f + sweepingMultiplier * damage;
		
		// Loop and check for colliding entities
		BoundingBox boundingBox = target.getBoundingBox().expand(1.0, 0.25, 1.0);
		assert target.getInstance() != null;
		for (Entity nearbyEntity : target.getInstance().getNearbyEntities(target.getPosition(), 2)) {
			if (nearbyEntity == target || nearbyEntity == attacker) continue;
			if (!(nearbyEntity instanceof LivingEntity living)) continue;
			if (nearbyEntity.getEntityType() == EntityType.ARMOR_STAND) continue;
			if (!boundingBox.intersectEntity(target.getPosition(), nearbyEntity)) continue;
			
			// Apply sweeping knockback and damage to the entity
			if (attacker.getPosition().distanceSquared(nearbyEntity.getPosition()) < 9.0) {
				EntityKnockbackEvent knockbackEvent = new EntityKnockbackEvent(
						nearbyEntity, attacker,
						EntityKnockbackEvent.KnockbackType.SWEEPING,
						0.4F
				);
				EventDispatcher.callCancellable(knockbackEvent, () -> nearbyEntity.takeKnockback(
						knockbackEvent.getStrength(),
						Math.sin(Math.toRadians(attacker.getPosition().yaw())),
						-Math.cos(Math.toRadians(attacker.getPosition().yaw()))
				));
				living.damage(new Damage(
						attacker instanceof Player ? DamageType.PLAYER_ATTACK : DamageType.MOB_ATTACK,
						attacker, attacker,
						null, sweepingDamage
				));
			}
		}
		
		// Spawn sweeping particles
		Pos pos = attacker.getPosition();
		double x = -Math.sin(Math.toRadians(pos.yaw()));
		double z = Math.cos(Math.toRadians(pos.yaw()));
		
		attacker.sendPacketToViewersAndSelf(new ParticlePacket(
				Particle.SWEEP_ATTACK, false,
				pos.x() + x, EntityUtils.getBodyY(attacker, 0.5), pos.z() + z,
				(float) x, 0, (float) z,
				0, 0
		));
	}
}
