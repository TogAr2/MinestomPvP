package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entities.EntityGroup;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.events.EntityKnockbackEvent;
import io.github.bloepiloepi.pvp.events.PlayerSpectateEvent;
import io.github.bloepiloepi.pvp.mixins.EntityAccessor;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttackManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(AttackManager.class);
	
	public static EventNode<EntityEvent> events() {
		EventNode<EntityEvent> node = EventNode.type("attack-events", EventFilter.ENTITY);
		
		node.addListener(EventListener.builder(PlayerMoveEvent.class).handler(event -> Tracker.falling.put(event.getPlayer().getUuid(),
				event.getNewPosition().y() - event.getPlayer().getPosition().y() < 0)).ignoreCancelled(false).build());
		
		node.addListener(EntityAttackEvent.class, event -> {
			if (event.getEntity() instanceof Player) {
				onEntityHit((Player) event.getEntity(), event.getTarget());
			}
		});
		
		node.addListener(EventListener.builder(PlayerHandAnimationEvent.class).handler(event ->
				resetLastAttackedTicks(event.getPlayer())).ignoreCancelled(false).build());
		
		node.addListener(EventListener.builder(PlayerChangeHeldSlotEvent.class).handler(event -> {
			if (!event.getPlayer().getItemInMainHand().isSimilar(event.getPlayer().getInventory().getItemStack(event.getSlot()))) {
				resetLastAttackedTicks(event.getPlayer());
			}
		}).ignoreCancelled(false).build());
		
		node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			Entity spectating = Tracker.spectating.get(player.getUuid());
			if (spectating == player) return;
			
			//This is to make sure other players don't see the player standing still while spectating
			//And when the player stops spectating, they are at the entities position instead of their position before spectating
			player.teleport(spectating.getPosition());
			
			if (player.getEntityMeta().isSneaking() || spectating.isRemoved()
					|| (spectating instanceof LivingEntity && ((LivingEntity) spectating).isDead())) {
				event.getPlayer().stopSpectating();
			}
		});
		
		return node;
	}
	
	public static float getAttackCooldownProgressPerTick(Player player) {
		return (float) (1.0D / player.getAttributeValue(Attribute.ATTACK_SPEED) * 20.0D);
	}
	
	public static float getAttackCooldownProgress(Player player, float baseTime) {
		return MathUtils.clamp(((float) Tracker.lastAttackedTicks.get(player.getUuid()) + baseTime) / getAttackCooldownProgressPerTick(player), 0.0F, 1.0F);
	}
	
	public static void resetLastAttackedTicks(Player player) {
		Tracker.lastAttackedTicks.put(player.getUuid(), 0);
	}
	
	public static void onEntityHit(Player player, Entity target) {
		if (target == null) return;
		if (player.isDead()) return;
		if (player.getDistanceSquared(target) >= 36.0D) return;
		
		if (target instanceof ItemEntity || target instanceof ExperienceOrb || target instanceof EntityProjectile || target == player) {
			player.kick(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
			LOGGER.error("Player " + player.getUsername() + " tried to attack invalid mob");
			return;
		}
		
		performAttack(player, target);
	}
	
	public static void performAttack(Player player, Entity target) {
		if (player.getGameMode() == GameMode.SPECTATOR) {
			PlayerSpectateEvent playerSpectateEvent = new PlayerSpectateEvent(player, target);
			EventDispatcher.callCancellable(playerSpectateEvent, () -> player.spectate(target));
			return;
		}
		
		float damage = player.getAttributeValue(Attribute.ATTACK_DAMAGE);
		float enchantedDamage;
		if (target instanceof LivingEntity) {
			enchantedDamage = EnchantmentUtils.getAttackDamage(player.getItemInMainHand(), EntityGroup.ofEntity((LivingEntity) target));
		} else {
			enchantedDamage = EnchantmentUtils.getAttackDamage(player.getItemInMainHand(), EntityGroup.DEFAULT);
		}
		
		float i = getAttackCooldownProgress(player, 0.5F);
		damage *= 0.2F + i * i * 0.8F;
		enchantedDamage *= i;
		resetLastAttackedTicks(player);
		
		boolean strongAttack = i > 0.9F;
		boolean bl2 = false;
		int knockback = EnchantmentUtils.getKnockback(player);
		if (player.isSprinting() && strongAttack) {
			SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_KNOCKBACK, Sound.Source.PLAYER, 1.0F, 1.0F);
			knockback++;
			bl2 = true;
		}
		
		boolean critical = strongAttack && !EntityUtils.isClimbing(player) && Tracker.falling.get(player.getUuid()) && !player.isOnGround() && !EntityUtils.hasEffect(player, PotionEffect.BLINDNESS) && player.getVehicle() == null && target instanceof LivingEntity && !player.isSprinting();
		if (critical) {
			damage *= 1.5F;
		}
		
		damage += enchantedDamage;
		boolean sweeping = false;
		//TODO formula for sweeping
		
		float targetHealth = 0.0F;
		if (target instanceof LivingEntity) {
			targetHealth = ((LivingEntity) target).getHealth();
		}
		
		Vec originalVelocity = target.getVelocity();
		boolean damageSucceeded = EntityUtils.damage(target, CustomDamageType.player(player), damage);
		
		if (!damageSucceeded) {
			SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_NODAMAGE, Sound.Source.PLAYER, 1.0F, 1.0F);
			return;
		}
		
		if (knockback > 0) {
			EntityKnockbackEvent entityKnockbackEvent = new EntityKnockbackEvent(target, player, true, knockback * 0.5F);
			EventDispatcher.callCancellable(entityKnockbackEvent, () -> {
				float strength = entityKnockbackEvent.getStrength();
				if (target instanceof LivingEntity) {
					target.takeKnockback(strength, Math.sin(player.getPosition().yaw() * 0.017453292F), -Math.cos(player.getPosition().yaw() * 0.017453292F));
				} else {
					target.setVelocity(target.getVelocity().add(-Math.sin(player.getPosition().yaw() * 0.017453292F) * strength, 0.1D, Math.cos(player.getPosition().yaw() * 0.017453292F) * strength));
				}
			});
			
			((EntityAccessor) player).velocity(player.getVelocity().mul(0.6D, 1.0D, 0.6D));
			player.setSprinting(false);
		}
		
		if (sweeping) {
			//TODO sweeping
		}
		
		//if (target instanceof Player) {
		//	EntityVelocityPacket velocityPacket = new EntityVelocityPacket();
		//	velocityPacket.entityId = target.getEntityId();
		//	Vector velocity = target.getVelocity().clone().multiply(8000f / MinecraftServer.TICK_PER_SECOND);
		//	velocityPacket.velocityX = (short) velocity.getX();
		//	velocityPacket.velocityY = (short) velocity.getY();
		//	velocityPacket.velocityZ = (short) velocity.getZ();
		
		//	((Player) target).getPlayerConnection().sendPacket(velocityPacket);
		//	target.getVelocity().copy(originalVelocity);
		//}
		
		if (critical) {
			SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_CRIT, Sound.Source.PLAYER, 1.0F, 1.0F);
			
			EntityAnimationPacket packet = new EntityAnimationPacket();
			packet.entityId = target.getEntityId();
			packet.animation = EntityAnimationPacket.Animation.CRITICAL_EFFECT;
			player.sendPacketToViewersAndSelf(new EntityAnimationPacket());
		}
		
		if (!critical && !sweeping) {
			if (strongAttack) {
				SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_STRONG, Sound.Source.PLAYER, 1.0F, 1.0F);
			} else {
				SoundManager.sendToAround(player, SoundEvent.ENTITY_PLAYER_ATTACK_WEAK, Sound.Source.PLAYER, 1.0F, 1.0F);
			}
		}
		
		if (enchantedDamage > 0.0F) {
			EntityAnimationPacket packet = new EntityAnimationPacket();
			packet.entityId = target.getEntityId();
			packet.animation = EntityAnimationPacket.Animation.MAGICAL_CRITICAL_EFFECT;
			player.sendPacketToViewersAndSelf(new EntityAnimationPacket());
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
			
			float n = targetHealth - ((LivingEntity) target).getHealth();
			
			//Damage indicator particles
			if (n > 2.0F) {
				int count = (int) ((double) n * 0.5D);
				Pos targetPosition = target.getPosition();
				ParticlePacket packet = ParticleCreator.createParticlePacket(
						Particle.DAMAGE_INDICATOR, false,
						targetPosition.x(), EntityUtils.getBodyY(target, 0.5), targetPosition.z(),
						0.1F, 0F, 0.1F,
						0.2F, count, (writer) -> {});
				
				target.sendPacketToViewersAndSelf(packet);
			}
		}
		
		EntityUtils.addExhaustion(player, 0.1F);
	}
}
