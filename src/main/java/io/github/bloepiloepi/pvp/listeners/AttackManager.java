package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.enums.Tool;
import io.github.bloepiloepi.pvp.events.FinalAttackEvent;
import io.github.bloepiloepi.pvp.legacy.LegacyKnockbackSettings;
import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entities.EntityGroup;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.events.EntityKnockbackEvent;
import io.github.bloepiloepi.pvp.events.LegacyKnockbackEvent;
import io.github.bloepiloepi.pvp.events.PlayerSpectateEvent;
import io.github.bloepiloepi.pvp.mixins.EntityAccessor;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class AttackManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttackManager.class);
	
	public static EventNode<EntityEvent> events(boolean legacy) {
		EventNode<EntityEvent> node = EventNode.type("attack-events", EventFilter.ENTITY);
		
		node.addListener(EntityAttackEvent.class, event -> entityHit(event.getEntity(), event.getTarget(), legacy));
		node.addListener(PlayerTickEvent.class, AttackManager::spectateTick);
		
		if (!legacy) {
			node.addListener(EventListener.builder(PlayerHandAnimationEvent.class).handler(event ->
					resetLastAttackedTicks(event.getPlayer())).build());
			
			node.addListener(EventListener.builder(PlayerChangeHeldSlotEvent.class).handler(event -> {
				if (!event.getPlayer().getItemInMainHand().isSimilar(event.getPlayer().getInventory().getItemStack(event.getSlot()))) {
					resetLastAttackedTicks(event.getPlayer());
				}
			}).build());
		}
		
		return node;
	}
	
	public static float getAttackCooldownProgressPerTick(Player player) {
		return (float) (1.0D / player.getAttributeValue(Attribute.ATTACK_SPEED) * 20.0D);
	}
	
	@SuppressWarnings("UnstableApiUsage")
	public static float getAttackCooldownProgress(Player player, float baseTime) {
		return MathUtils.clamp(((float) Tracker.lastAttackedTicks.get(player.getUuid()) + baseTime) / getAttackCooldownProgressPerTick(player), 0.0F, 1.0F);
	}
	
	public static void resetLastAttackedTicks(Player player) {
		Tracker.lastAttackedTicks.put(player.getUuid(), 0);
	}
	
	private static void spectateTick(PlayerTickEvent event) {
		Player player = event.getPlayer();
		Entity spectating = Tracker.spectating.get(player.getUuid());
		if (spectating == null || spectating == player) return;
		
		//This is to make sure other players don't see the player standing still while spectating
		//And when the player stops spectating, they are at the entities position instead of their position before spectating
		player.teleport(spectating.getPosition());
		
		if (player.getEntityMeta().isSneaking() || spectating.isRemoved()
				|| (spectating instanceof LivingEntity && ((LivingEntity) spectating).isDead())) {
			event.getPlayer().stopSpectating();
			Tracker.spectating.remove(event.getPlayer().getUuid());
		}
	}
	
	private static void entityHit(Entity entity, Entity target, boolean legacy) {
		if (target == null) return;
		if (!(entity instanceof Player player)) return;
		if (player.isDead()) return;
		if (entity.getDistanceSquared(target) >= 36.0D) return;
		
		if (target instanceof ItemEntity || target instanceof ExperienceOrb || target instanceof EntityProjectile || target == player) {
			player.kick(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
			LOGGER.error("Player " + player.getUsername() + " tried to attack invalid mob");
			return;
		}
		
		performAttack(player, target, legacy);
	}
	
	public static void performAttack(Player player, Entity target, boolean legacy) {
		if (player.getGameMode() == GameMode.SPECTATOR) {
			PlayerSpectateEvent playerSpectateEvent = new PlayerSpectateEvent(player, target);
			EventDispatcher.callCancellable(playerSpectateEvent, () -> {
				player.spectate(target);
				Tracker.spectating.put(player.getUuid(), target);
			});
			return;
		}
		
		float damage = player.getAttributeValue(Attribute.ATTACK_DAMAGE);
		float enchantedDamage;
		if (target instanceof LivingEntity) {
			enchantedDamage = EnchantmentUtils.getAttackDamage(player.getItemInMainHand(), EntityGroup.ofEntity((LivingEntity) target), legacy);
		} else {
			enchantedDamage = EnchantmentUtils.getAttackDamage(player.getItemInMainHand(), EntityGroup.DEFAULT, legacy);
		}
		
		float i = legacy ? 1.0F : getAttackCooldownProgress(player, 0.5F);
		damage *= 0.2F + i * i * 0.8F;
		enchantedDamage *= i;
		resetLastAttackedTicks(player);
		
		boolean strongAttack = i > 0.9F;
		boolean sprintAttack = false;
		int knockback = EnchantmentUtils.getKnockback(player);
		if (player.isSprinting() && strongAttack) {
			if (!legacy) SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_KNOCKBACK, Sound.Source.PLAYER, 1.0F, 1.0F);
			knockback++;
			sprintAttack = true;
		}
		
		boolean critical = strongAttack && !EntityUtils.isClimbing(player) && player.getVelocity().y() < 0 && !player.isOnGround() && !EntityUtils.hasEffect(player, PotionEffect.BLINDNESS) && player.getVehicle() == null && target instanceof LivingEntity;
		if (!legacy) {
			// Not sprinting required for critical in 1.9+
			critical = critical && !player.isSprinting();
		}
		
		boolean sweeping = false;
		if (!legacy && strongAttack && !critical && !sprintAttack && player.isOnGround()) {
			double lastMoveDistance = ((EntityAccessor) player).previousPosition().distance(player.getPosition()) * 0.6;
			if (lastMoveDistance < player.getAttributeValue(Attribute.MOVEMENT_SPEED)) {
				Tool tool = Tool.fromMaterial(player.getItemInMainHand().getMaterial());
				if (tool != null && tool.isSword()) {
					sweeping = true;
				}
			}
		}
		
		FinalAttackEvent finalAttackEvent = new FinalAttackEvent(player, target, critical, sweeping, damage, enchantedDamage);
		EventDispatcher.call(finalAttackEvent);
		
		if (finalAttackEvent.isCancelled()) {
			return;
		}
		
		critical = finalAttackEvent.isCritical();
		sweeping = finalAttackEvent.isSweeping();
		damage = finalAttackEvent.getBaseDamage();
		enchantedDamage = finalAttackEvent.getEnchantsExtraDamage();
		
		if (critical) {
			if (legacy) {
				damage += ThreadLocalRandom.current().nextInt((int) (damage / 2 + 2));
			} else {
				damage *= 1.5F;
			}
		}
		
		damage += enchantedDamage;
		
		float originalHealth = 0.0F;
		if (target instanceof LivingEntity) {
			originalHealth = ((LivingEntity) target).getHealth();
		}
		
		boolean damageSucceeded = EntityUtils.damage(target, CustomDamageType.player(player), damage);
		
		if (!damageSucceeded) {
			if (!legacy) SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_NODAMAGE, Sound.Source.PLAYER, 1.0F, 1.0F);
			return;
		}
		
		if (knockback > 0) {
			if (!legacy) {
				EntityKnockbackEvent entityKnockbackEvent = new EntityKnockbackEvent(target, player, true, false, knockback * 0.5F);
				EventDispatcher.callCancellable(entityKnockbackEvent, () -> {
					float strength = entityKnockbackEvent.getStrength();
					if (target instanceof LivingEntity) {
						target.takeKnockback(strength, Math.sin(Math.toRadians(player.getPosition().yaw())), -Math.cos(Math.toRadians(player.getPosition().yaw())));
					} else {
						target.setVelocity(target.getVelocity().add(-Math.sin(Math.toRadians(player.getPosition().yaw())) * strength, 0.1D, Math.cos(Math.toRadians(player.getPosition().yaw())) * strength));
					}
				});
			} else {
				float finalKnockback;
				if (target instanceof LivingEntity) {
					float knockbackResistance = ((LivingEntity) target).getAttributeValue(Attribute.KNOCKBACK_RESISTANCE);
					finalKnockback = knockback * (1 - knockbackResistance);
				} else {
					finalKnockback = knockback;
				}
				
				LegacyKnockbackEvent legacyKnockbackEvent = new LegacyKnockbackEvent(target, player, true);
				EventDispatcher.callCancellable(legacyKnockbackEvent, () -> {
					LegacyKnockbackSettings settings = legacyKnockbackEvent.getSettings();
					target.setVelocity(target.getVelocity().add(
							-Math.sin(player.getPosition().yaw() * Math.PI / 180.0F) * finalKnockback * settings.getExtraHorizontal(),
							settings.getExtraVertical(),
							Math.cos(player.getPosition().yaw() * Math.PI / 180.0F) * finalKnockback * settings.getExtraHorizontal()
					));
				});
			}
			
			((EntityAccessor) player).velocity(player.getVelocity().mul(0.6D, 1.0D, 0.6D));
			player.setSprinting(false);
		}
		
		if (sweeping) {
			float sweepingDamage = 1.0F + EnchantmentUtils.getSweepingMultiplier(player) * damage;
			BoundingBox boundingBox = target.getBoundingBox().expand(1.0D, 0.25D, 1.0D);
			Objects.requireNonNull(target.getInstance()).getEntities().stream()
					.filter(boundingBox::intersect).filter(entity -> entity instanceof LivingEntity)
					.map(entity -> (LivingEntity) entity).forEach(entity -> {
						if (entity == target) return;
						if (entity == player) return;
						if (entity.getEntityMeta() instanceof ArmorStandMeta) return;
						if (entity.getTeam() == player.getTeam()) return;
						
						if (player.getPosition().distanceSquared(entity.getPosition()) < 9.0) {
							EntityKnockbackEvent entityKnockbackEvent = new EntityKnockbackEvent(entity, player, false, true, 0.4F);
							EventDispatcher.callCancellable(entityKnockbackEvent, () -> {
								float strength = entityKnockbackEvent.getStrength();
								entity.takeKnockback(strength, Math.sin(Math.toRadians(player.getPosition().yaw())), -Math.cos(Math.toRadians(player.getPosition().yaw())));
							});
							entity.damage(CustomDamageType.player(player), sweepingDamage);
						}
					});
			
			SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_SWEEP, Sound.Source.PLAYER, 1.0F, 1.0F);
			Pos pos = player.getPosition();
			double x = -Math.sin(Math.toRadians(pos.yaw()));
			double z = Math.cos(Math.toRadians(pos.yaw()));
			
			ParticlePacket packet = ParticleCreator.createParticlePacket(
					Particle.SWEEP_ATTACK, false,
					pos.x() + x, EntityUtils.getBodyY(player, 0.5), pos.z() + z,
					(float) x, 0, (float) z,
					0, 0, null);
			
			player.sendPacketToViewersAndSelf(packet);
		}
		
		if (critical) {
			if (!legacy) SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_CRIT, Sound.Source.PLAYER, 1.0F, 1.0F);
			
			player.sendPacketToViewersAndSelf(new EntityAnimationPacket(target.getEntityId(), EntityAnimationPacket.Animation.CRITICAL_EFFECT));
		}
		
		if (!critical && !sweeping) {
			if (strongAttack) {
				if (!legacy) SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_STRONG, Sound.Source.PLAYER, 1.0F, 1.0F);
			} else {
				//noinspection ConstantConditions
				if (!legacy) SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_WEAK, Sound.Source.PLAYER, 1.0F, 1.0F);
			}
		}
		
		if (enchantedDamage > 0.0F) {
			player.sendPacketToViewersAndSelf(new EntityAnimationPacket(target.getEntityId(), EntityAnimationPacket.Animation.MAGICAL_CRITICAL_EFFECT));
		}
		
		if (target instanceof LivingEntity) {
			EnchantmentUtils.onUserDamaged((LivingEntity) target, player);
		}
		
		EnchantmentUtils.onTargetDamaged(player, target);
		//TODO target and user damaged should also work when non-player mob attacks (mobs, arrows, trident)
		//TODO damage itemstack
		
		if (target instanceof LivingEntity) {
			int fireAspect = EnchantmentUtils.getFireAspect(player);
			if (fireAspect > 0) {
				EntityUtils.setOnFireForSeconds(target, fireAspect * 4);
			}
			
			float damageDone = originalHealth - ((LivingEntity) target).getHealth();
			
			//Damage indicator particles
			if (damageDone > 2.0F) {
				int count = (int) ((double) damageDone * 0.5D);
				Pos targetPosition = target.getPosition();
				ParticlePacket packet = ParticleCreator.createParticlePacket(
						Particle.DAMAGE_INDICATOR, false,
						targetPosition.x(), EntityUtils.getBodyY(target, 0.5), targetPosition.z(),
						0.1F, 0F, 0.1F,
						0.2F, count, null);
				
				target.sendPacketToViewersAndSelf(packet);
			}
		}
		
		EntityUtils.addExhaustion(player, legacy ? 0.3F: 0.1F);
	}
}
