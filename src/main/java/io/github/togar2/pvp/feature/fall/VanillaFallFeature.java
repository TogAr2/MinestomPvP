package io.github.togar2.pvp.feature.fall;

import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureType;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.gamedata.tags.TagManager;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.particle.data.BlockParticleData;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;

public class VanillaFallFeature implements FallFeature, CombatFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaFallFeature> DEFINED = new DefinedFeature<>(
			FeatureType.FALL, configuration -> new VanillaFallFeature()
	);
	
	public static final Tag<Block> LAST_CLIMBED_BLOCK = Tag.Short("lastClimbedBlock").map(Block::fromStateId, Block::stateId);
	public static final Tag<Double> FALL_DISTANCE = Tag.Double("fallDistance");
	
	@Override
	public void initPlayer(Player player, boolean firstInit) {
		player.setTag(FALL_DISTANCE, 0.0);
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		// For living non-player entities, handle fall damage every tick
		node.addListener(EntityTickEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;
			if (livingEntity instanceof Player) return;
			
			Pos previousPosition = livingEntity.getPreviousPosition();
			handleFallDamage(livingEntity, previousPosition, livingEntity.getPosition(), livingEntity.isOnGround());
		});
		
		// For players, handle fall damage on move event
		node.addListener(PlayerMoveEvent.class, event -> {
			Player player = event.getPlayer();
			handleFallDamage(
					player, player.getPosition(),
					event.getNewPosition(), event.isOnGround()
			);
		});
		
		node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			if (player.isOnGround()) player.removeTag(LAST_CLIMBED_BLOCK);
		});
		
		node.addListener(PlayerMoveEvent.class, event -> {
			Player player = event.getPlayer();
			if (EntityUtils.isClimbing(player)) {
				player.setTag(LAST_CLIMBED_BLOCK, player.getInstance().getBlock(player.getPosition()));
				player.setTag(FALL_DISTANCE, 0.0);
			}
		});
	}
	
	public void handleFallDamage(LivingEntity entity, Pos currPos, Pos newPos, boolean onGround) {
		double dy = newPos.y() - currPos.y();
		double fallDistance = getFallDistance(entity);
		
		if ((entity instanceof Player player && player.isFlying())
				|| entity.hasEffect(PotionEffect.LEVITATION)
				|| entity.hasEffect(PotionEffect.SLOW_FALLING) || dy > 0) {
			entity.setTag(FALL_DISTANCE, 0.0);
			return;
		}
		
		if (entity.isFlyingWithElytra() && entity.getVelocity().y() > -0.5) {
			entity.setTag(FALL_DISTANCE, 1.0);
			return;
		}
		
		if (!onGround) {
			if (dy < 0) entity.setTag(FALL_DISTANCE, fallDistance - dy);
			return;
		}
		
		if (fallDistance > 3.0) {
			assert entity.getInstance() != null;
			Block block = entity.getInstance().getBlock(getLandingPos(entity, newPos));
			if (!block.isAir()) {
				double damageDistance = Math.ceil(fallDistance - 3.0);
				double particleMultiplier = Math.min(0.2 + damageDistance / 15.0, 2.5);
				int particleCount = (int) (150 * particleMultiplier);
				
				entity.sendPacketToViewersAndSelf(new ParticlePacket(
						Particle.BLOCK.withData(new BlockParticleData(block)),
						false,
						newPos.x(), newPos.y(), newPos.z(),
						0, 0, 0,
						0.15f, particleCount
				));
			}
		}
		
		entity.setTag(FALL_DISTANCE, 0.0);
		
		if (entity instanceof Player player && !player.getGameMode().canTakeDamage()) return;
		int damage = getFallDamage(entity, fallDistance);
		if (damage > 0) {
			playFallSound(entity, damage);
			entity.damage(DamageType.FALL, damage);
		}
	}
	
	public void playFallSound(LivingEntity entity, int damage) {
		boolean bigFall = damage > 4;
		
		entity.getViewersAsAudience().playSound(Sound.sound(
				bigFall ?
						SoundEvent.ENTITY_PLAYER_BIG_FALL :
						SoundEvent.ENTITY_PLAYER_SMALL_FALL,
				entity instanceof  Player ? Sound.Source.PLAYER : Sound.Source.HOSTILE,
				1.0f, 1.0f
		), entity);
	}
	
	@Override
	public int getFallDamage(LivingEntity entity, double fallDistance) {
		TimedPotion effect = entity.getEffect(PotionEffect.JUMP_BOOST);
		double reduce = effect != null
				? effect.potion().amplifier() + 1
				: 0.0;
		
		return (int) Math.floor(fallDistance - 3.0 - reduce);
	}
	
	@Override
	public double getFallDistance(LivingEntity entity) {
		return entity.hasTag(FALL_DISTANCE) ? entity.getTag(FALL_DISTANCE) : 0.0;
	}
	
	@Override
	public void resetFallDistance(LivingEntity entity) {
		entity.setTag(FALL_DISTANCE, 0.0);
	}
	
	@Override
	public Block getLastClimbedBlock(LivingEntity entity) {
		return entity.hasTag(LAST_CLIMBED_BLOCK) ? entity.getTag(LAST_CLIMBED_BLOCK) : Block.AIR;
	}
	
	protected Point getLandingPos(LivingEntity livingEntity, Pos position) {
		Point offset = position.add(0, -0.2, 0);
		Instance instance = livingEntity.getInstance();
		
		if (instance == null) return offset;
		if (!instance.getBlock(offset).isAir()) return offset;
		
		Point offsetDown = offset.add(0, -1, 0);
		Block block = instance.getBlock(offsetDown);
		
		TagManager tagManager = MinecraftServer.getTagManager();
		var fences = tagManager.getTag(net.minestom.server.gamedata.tags.Tag.BasicType.BLOCKS, "minecraft:fences");
		var walls = tagManager.getTag(net.minestom.server.gamedata.tags.Tag.BasicType.BLOCKS, "minecraft:walls");
		var fenceGates = tagManager.getTag(net.minestom.server.gamedata.tags.Tag.BasicType.BLOCKS, "minecraft:fence_gates");
		
		assert fences != null;
		assert walls != null;
		assert fenceGates != null;
		
		if (fences.contains(block.namespace())
				|| walls.contains(block.namespace())
				|| fenceGates.contains(block.namespace())) {
			return offsetDown;
		}
		
		return offset;
	}
}
