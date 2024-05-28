package io.github.togar2.pvp.feature.attack;

import io.github.togar2.pvp.enchantment.EnchantmentUtils;
import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.enums.Tool;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.config.FeatureType;
import io.github.togar2.pvp.feature.knockback.KnockbackFeature;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

public class VanillaSweepingFeature implements SweepingFeature {
	private final KnockbackFeature knockbackFeature;
	
	public VanillaSweepingFeature(FeatureConfiguration configuration) {
		this.knockbackFeature = configuration.get(FeatureType.KNOCKBACK);
	}
	
	@Override
	public boolean shouldSweep(LivingEntity attacker, AttackValues.PreSweeping values) {
		if (!values.strong() || values.critical() || values.sprint() || !attacker.isOnGround()) return false;
		
		double lastMoveDistance = attacker.getPreviousPosition().distance(attacker.getPosition()) * 0.6;
		if (lastMoveDistance >= attacker.getAttributeValue(Attribute.MOVEMENT_SPEED)) return false;
		
		Tool tool = Tool.fromMaterial(attacker.getItemInMainHand().material());
		return tool != null && tool.isSword();
	}
	
	@Override
	public float getSweepingDamage(LivingEntity attacker, float damage) {
		float sweepingMultiplier = 0;
		int sweepingLevel = EnchantmentUtils.getSweeping(attacker);
		if (sweepingLevel > 0) sweepingMultiplier = 1.0f - (1.0f / (float) (sweepingLevel + 1));
		return 1.0f + sweepingMultiplier * damage;
	}
	
	@Override
	public void applySweeping(LivingEntity attacker, LivingEntity target, float damage) {
		float sweepingDamage = getSweepingDamage(attacker, damage);
		
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
				knockbackFeature.applySweepingKnockback(attacker, target);
				
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
