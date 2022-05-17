package io.github.bloepiloepi.pvp.entities;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class CustomPlayer extends Player implements PvpPlayer {
	
	public CustomPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
		super(uuid, username, playerConnection);
	}

	protected double getJumpVelocity() {
		return 0.42;
	}

	public double getJumpBoostVelocityModifier() {
		return EntityUtils.hasEffect(this, PotionEffect.JUMP_BOOST) ?
				(0.1 * (EntityUtils.getEffect(this, PotionEffect.JUMP_BOOST).amplifier() + 1)) : 0.0;
	}

	@Override
	public void jump() {
		int tps = MinecraftServer.TICK_PER_SECOND;
		double yVel = getJumpVelocity() + getJumpBoostVelocityModifier();
		velocity = velocity.withY(yVel * tps);
		if (isSprinting()) {
			double angle = position.yaw() * (Math.PI / 180);
			velocity = velocity.add(-Math.sin(angle) * 0.2 * tps, 0, Math.cos(angle) * 0.2 * tps);
		}
	}

	@Override
	public void afterSprintAttack() {
		velocity = velocity.mul(0.6, 1, 0.6);
	}
	
	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
	@Override
	protected void updateVelocity(boolean wasOnGround, Pos positionBeforeMove, Vec newVelocity) {
		final double tps = MinecraftServer.TICK_PER_SECOND;
		final double drag;
		if (wasOnGround) {
			final Chunk chunk = ChunkUtils.retrieve(instance, currentChunk, position);
			synchronized (chunk) {
				drag = chunk.getBlock(positionBeforeMove.sub(0, 0.5000001, 0)).registry().friction() * 0.91;
			}
		} else drag = 0.91;
		
		boolean slowFall = velocity.y() < 0 && EntityUtils.hasEffect(this, PotionEffect.SLOW_FALLING);
		double gravity = hasNoGravity() || isFlying() ? 0 : slowFall ? 0.01 : gravityAcceleration;
		double gravityDrag = hasNoGravity() ? 0 : isFlying() ? 0.6 : (1 - gravityDragPerTick);
		
		this.velocity = newVelocity
				// Apply drag
				.apply((x, y, z) -> new Vec(
						x * drag,
						(y - gravity) * gravityDrag,
						z * drag
				))
				// Convert from block/tick to block/sec
				.mul(tps)
				// Prevent infinitely decreasing velocity
				.apply(Vec.Operator.EPSILON);
		
		if (EntityUtils.hasEffect(this, PotionEffect.LEVITATION)) {
			velocity = velocity.withY(
					((0.05 * (double)
							(EntityUtils.getEffect(this, PotionEffect.LEVITATION).amplifier() + 1)
							- (velocity.y() / tps)) * 0.2) * tps
			);
		}
	}
}
