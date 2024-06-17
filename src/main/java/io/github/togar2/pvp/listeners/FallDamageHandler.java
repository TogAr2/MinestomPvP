package io.github.togar2.pvp.listeners;

import io.github.togar2.pvp.entity.Tracker;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.sound.SoundEvent;

import java.util.Objects;

public class FallDamageHandler {
	/**
	 * Main method of fall damage handler, called on a move from a LivingEntity
	 */
	public void handleFallDamage(LivingEntity entity, Pos currentPosition,
	                             Pos newPosition, boolean isOnGround) {
		double dy = newPosition.y() - currentPosition.y();
		double fallDistance = entity.hasTag(Tracker.FALL_DISTANCE) ? entity.getTag(Tracker.FALL_DISTANCE) : 0;
		
		if ((entity instanceof Player player && player.isFlying())
				|| entity.hasEffect(PotionEffect.LEVITATION)
				|| entity.hasEffect(PotionEffect.SLOW_FALLING) || dy > 0) {
			entity.setTag(Tracker.FALL_DISTANCE, 0.0);
			return;
		}
		if (entity.isFlyingWithElytra() && entity.getVelocity().y() > -0.5) {
			entity.setTag(Tracker.FALL_DISTANCE, 1.0);
			return;
		}
		
		if (fallDistance > 3.0 && isOnGround) {
			assert entity.getInstance() != null;
			Block block = entity.getInstance().getBlock(getLandingPos(entity, newPosition));
			if (!block.isAir()) {
				double damageDistance = Math.ceil(fallDistance - 3.0);
				double particleMultiplier = Math.min(0.2 + damageDistance / 15.0, 2.5);
				int particleCount = (int) (150 * particleMultiplier);
				
				entity.sendPacketToViewersAndSelf(new ParticlePacket(
						Particle.BLOCK.withBlock(block),
						false,
						newPosition.x(), newPosition.y(), newPosition.z(),
						0, 0, 0,
						0.15f, particleCount
				));
			}
		}
		
		if (isOnGround) {
			entity.setTag(Tracker.FALL_DISTANCE, 0.0);
			
			if (entity instanceof Player player && !player.getGameMode().canTakeDamage()) return;
			int damage = getFallDamage(entity, fallDistance);
			if (damage > 0) {
				if (entity instanceof Player player) {
					entity.getViewersAsAudience().playSound(Sound.sound(
							damage > 4 ?
									SoundEvent.ENTITY_PLAYER_BIG_FALL :
									SoundEvent.ENTITY_PLAYER_SMALL_FALL,
							Sound.Source.PLAYER,
							1.0f, 1.0f
					), player);
				} else {
					entity.getViewersAsAudience().playSound(Sound.sound(
							damage > 4 ?
									SoundEvent.ENTITY_GENERIC_BIG_FALL :
									SoundEvent.ENTITY_GENERIC_SMALL_FALL,
							Sound.Source.HOSTILE,
							1.0f, 1.0f
					));
				}
				
				entity.damage(DamageType.FALL, damage);
			}
		} else if (dy < 0) {
			entity.setTag(Tracker.FALL_DISTANCE, fallDistance - dy);
		}
	}
	
	protected int getFallDamage(LivingEntity livingEntity, double fallDistance) {
		TimedPotion effect = livingEntity.getEffect(PotionEffect.JUMP_BOOST);
		float reduce = effect != null
				? effect.potion().amplifier() + 1
				: 0;
		return (int) Math.ceil(fallDistance - 3.0 - reduce);
	}
	
	private static Point getLandingPos(LivingEntity livingEntity, Pos position) {
		Point offset = position.add(0, -0.2, 0);
		if (Objects.requireNonNull(livingEntity.getInstance()).getBlock(offset).isAir()) {
			Point offsetDown = offset.add(0, -1, 0);
			Block block = livingEntity.getInstance().getBlock(offsetDown);
			Tag fences = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, "minecraft:fences");
			Tag walls = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, "minecraft:walls");
			Tag fenceGates = MinecraftServer.getTagManager().getTag(Tag.BasicType.BLOCKS, "minecraft:fence_gates");
			assert fences != null;
			assert walls != null;
			assert fenceGates != null;
			if (fences.contains(block.namespace()) || walls.contains(block.namespace()) || fenceGates.contains(block.namespace())) {
				return offsetDown;
			}
		}
		
		return offset;
	}
}
