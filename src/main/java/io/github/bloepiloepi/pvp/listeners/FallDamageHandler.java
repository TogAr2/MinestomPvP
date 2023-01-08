package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.entity.Tracker;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.gamedata.tags.Tag;
import net.minestom.server.instance.block.Block;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.ParticleCreator;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;

import java.util.Objects;

public class FallDamageHandler {
	public static void handleFallDamage(LivingEntity entity, Pos currentPosition,
	                                    Pos newPosition, boolean isOnGround) {
		double dy = newPosition.y() - currentPosition.y();
		double fallDistance = entity.hasTag(Tracker.FALL_DISTANCE) ? entity.getTag(Tracker.FALL_DISTANCE) : 0;
		
		if ((entity instanceof Player player && player.isFlying())
				|| EntityUtils.hasEffect(entity, PotionEffect.LEVITATION)
				|| EntityUtils.hasEffect(entity, PotionEffect.SLOW_FALLING) || dy > 0) {
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
				
				entity.sendPacketToViewersAndSelf(ParticleCreator.createParticlePacket(
						Particle.BLOCK,
						false,
						newPosition.x(), newPosition.y(), newPosition.z(),
						0, 0, 0,
						0.15f, particleCount,
						writer -> writer.writeVarInt(block.stateId())
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
				
				entity.damage(CustomDamageType.FALL, damage);
			}
		} else if (dy < 0) {
			entity.setTag(Tracker.FALL_DISTANCE, fallDistance - dy);
		}
	}
	
	private static int getFallDamage(LivingEntity livingEntity, double fallDistance) {
		float reduce = EntityUtils.hasEffect(livingEntity, PotionEffect.JUMP_BOOST)
				? EntityUtils.getEffect(livingEntity, PotionEffect.JUMP_BOOST).amplifier() + 1
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
