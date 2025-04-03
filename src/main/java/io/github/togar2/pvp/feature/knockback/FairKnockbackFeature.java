package io.github.togar2.pvp.feature.knockback;

import io.github.togar2.pvp.events.EntityKnockbackEvent;
import io.github.togar2.pvp.events.LegacyKnockbackEvent;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.legacy.LegacyKnockbackSettings;
import io.github.togar2.pvp.player.CombatPlayer;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of {@link VanillaKnockbackFeature} which tries to make the playing field more even for players with high latency.
 * <p>
 * Rising knockback is applied usually when a player is on the ground.
 * This feature determines if the player would be on the ground <i>client side</i> instead of just server side.
 * To have just this change, use {@link FairKnockbackFeature#ONLY_RISING}.
 * <p>
 * The other option is {@link FairKnockbackFeature#RISING_AND_FALLING}, which, along with rising knockback,
 * also compensates for latency with falling knockback. It will use the (estimated) velocity of when the packet will arrive at the client,
 * possibly making falling knockback feel more natural.
 * <p>
 * The changes made by this feature only apply to players with more than 25 ms ping.
 */
public class FairKnockbackFeature extends VanillaKnockbackFeature {
	/**
	 * @see FairKnockbackFeature
	 */
	public static final DefinedFeature<FairKnockbackFeature> ONLY_RISING = new DefinedFeature<>(
			FeatureType.KNOCKBACK, configuration -> new FairKnockbackFeature(configuration, false),
			FeatureType.VERSION
	);
	/**
	 * @see FairKnockbackFeature
	 */
	public static final DefinedFeature<FairKnockbackFeature> RISING_AND_FALLING = new DefinedFeature<>(
			FeatureType.KNOCKBACK, configuration -> new FairKnockbackFeature(configuration, true),
			FeatureType.VERSION
	);
	
	private static final int PING_OFFSET = 25;
	
	protected final boolean compensateFallKnockback;
	
	public FairKnockbackFeature(FeatureConfiguration configuration, boolean compensateFallKnockback) {
		super(configuration);
		this.compensateFallKnockback = compensateFallKnockback;
	}
	
	@Override
	protected boolean applyModernKnockback(LivingEntity target, Entity attacker, @Nullable Entity source,
	                                       EntityKnockbackEvent.KnockbackType type, float strength,
	                                       double dx, double dz) {
		if (!(target instanceof Player player) || player.getLatency() < PING_OFFSET)
			return super.applyModernKnockback(target, attacker, source, type, strength, dx, dz);
		
		EntityKnockbackEvent knockbackEvent = new EntityKnockbackEvent(
				target, source == null ? attacker : source,
				type, strength
		);
		EventDispatcher.call(knockbackEvent);
		if (knockbackEvent.isCancelled()) return false;
		strength = knockbackEvent.getStrength();
		
		// Knockback dealing code, copied from Minestom and modified
		strength *= ServerFlag.SERVER_TICKS_PER_SECOND;
		final Vec velocityModifier = new Vec(dx, dz).normalize().mul(strength);
		final double verticalLimit = .4d * ServerFlag.SERVER_TICKS_PER_SECOND;
		
		Vec velocity = target.getVelocity();
		
		int latencyTicks = getLatencyTicks(player.getLatency());
		double vertical;
		if (isOnGroundClientSide(player, latencyTicks)) {
			vertical = Math.min(verticalLimit, velocity.y() / 2d + strength);
		} else if (compensateFallKnockback) {
			vertical = getCompensatedVerticalVelocity(player.getAerodynamics(), velocity.y(), latencyTicks);
		} else {
			vertical = velocity.y();
		}
		
		target.setVelocity(new Vec(
				velocity.x() / 2d - velocityModifier.x(),
				vertical,
				velocity.z() / 2d - velocityModifier.z()
		));
		
		return true;
	}
	
	@Override
	protected boolean applyLegacyKnockback(LivingEntity target, Entity attacker, @Nullable Entity source,
	                                       boolean extra, int knockback, double dx, double dz) {
		if (!(target instanceof Player player) || player.getLatency() < PING_OFFSET)
			return super.applyLegacyKnockback(target, attacker, source, extra, knockback, dx, dz);
		
		LegacyKnockbackEvent legacyKnockbackEvent = new LegacyKnockbackEvent(
				target, source == null ? attacker : source, extra);
		EventDispatcher.call(legacyKnockbackEvent);
		if (legacyKnockbackEvent.isCancelled()) return false;
		
		LegacyKnockbackSettings settings = legacyKnockbackEvent.getSettings();
		
		double kbResistance = target.getAttributeValue(Attribute.KNOCKBACK_RESISTANCE);
		double horizontal = settings.horizontal() * (1 - kbResistance) * knockback;
		double vertical = settings.vertical() * (1 - kbResistance) * knockback;
		Vec horizontalModifier = new Vec(dx, dz).normalize().mul(horizontal);
		
		Vec velocity = target.getVelocity();
		
		int latencyTicks = getLatencyTicks(player.getLatency());
		double yVel;
		if (isOnGroundClientSide(player, latencyTicks)) {
			//TODO divide by 2 at y component or not?
			yVel = Math.min(settings.verticalLimit(), velocity.y() + vertical);
		} else if (compensateFallKnockback) {
			yVel = getCompensatedVerticalVelocity(player.getAerodynamics(), velocity.y(), latencyTicks);
		} else {
			yVel = velocity.y();
		}
		
		target.setVelocity(new Vec(
				velocity.x() / 2d - horizontalModifier.x(),
				yVel,
				velocity.z() / 2d - horizontalModifier.z()
		));
		
		return true;
	}
	
	protected boolean isOnGroundClientSide(Player player, int latencyTicks) {
		if (player.isOnGround() || !(player instanceof CombatPlayer combatPlayer)) return true;
		if (player.getGravityTickCount() > 30) return false; // Very uncertain, default to false
		
		// These are all cases in which isOnGroundAfterTicks() will not be accurate
		Block block = player.getInstance().getBlock(player.getPosition());
		if (player.isFlyingWithElytra()
				|| block.compare(Block.WATER)
				|| block.compare(Block.LAVA)
				|| block.compare(Block.COBWEB)
				|| block.compare(Block.SCAFFOLDING))
			return false;
		
		return combatPlayer.isOnGroundAfterTicks(latencyTicks);
	}
	
	/**
	 * Compensates the given vertical velocity for gravity calculations for a given amount of ticks.
	 * This means for every tick, it will be affected by gravity and vertical air resistance.
	 *
	 * @param aerodynamics the aerodynamics of the player
	 * @param velocity the velocity to compensate
	 * @param ticks the amount of ticks to compensate for
	 * @return the compensated vertical velocity
	 */
	protected static double getCompensatedVerticalVelocity(Aerodynamics aerodynamics, double velocity, int ticks) {
		for (int i = 0; i < ticks; i++) {
			velocity -= aerodynamics.gravity();
			velocity *= aerodynamics.verticalAirResistance();
		}
		
		return velocity;
	}
	
	private static int getLatencyTicks(int latencyMillis) {
		return Math.ceilDiv(latencyMillis * ServerFlag.SERVER_TICKS_PER_SECOND, 1000) + 2;
	}
}
