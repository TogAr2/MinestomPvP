package io.github.bloepiloepi.pvp.entities;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class CustomPlayer extends Player {
	private PhysicsResult lastPhysicsResult = null;
	
	public CustomPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
		super(uuid, username, playerConnection);
	}
	
	@Override
	public void tick(long time) {
		if (instance == null || isRemoved() || !ChunkUtils.isLoaded(currentChunk))
			return;
		
		customVelocityTick();
		super.tick(time);
	}
	
	private void customVelocityTick() {
		//if (PlayerUtils.isSocketClient(this)) return;
		if (vehicle != null) return;
		
		final boolean noGravity = hasNoGravity();
		final boolean hasVelocity = hasVelocity();
		if (!hasVelocity && noGravity) {
			return;
		}
		final float tps = MinecraftServer.TICK_PER_SECOND;
		final Vec currentVelocity = getVelocity();
		final Vec deltaPos = new Vec(
				currentVelocity.x() / tps,
				currentVelocity.y() / tps - (noGravity ? 0 : gravityAcceleration),
				currentVelocity.z() / tps
		);
		
		final Pos newPosition;
		final Vec newVelocity;
		if (this.hasPhysics) {
			final var physicsResult = CollisionUtils.handlePhysics(this, deltaPos, lastPhysicsResult);
			this.lastPhysicsResult = physicsResult;
			this.onGround = physicsResult.isOnGround();
			newPosition = physicsResult.newPosition();
			newVelocity = physicsResult.newVelocity();
		} else {
			newVelocity = deltaPos;
			newPosition = position.add(currentVelocity.div(20));
		}
		
		// World border collision
		final Pos finalVelocityPosition = CollisionUtils.applyWorldBorder(instance, position, newPosition);
		final boolean positionChanged = !finalVelocityPosition.samePoint(position);
		if (!positionChanged) {
			if (!hasVelocity && newVelocity.isZero()) {
				return;
			}
			if (hasVelocity) {
				this.velocity = Vec.ZERO;
				sendPacketToViewers(getVelocityPacket());
				return;
			}
		}
		final Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, finalVelocityPosition);
		if (!ChunkUtils.isLoaded(finalChunk)) {
			// Entity shouldn't be updated when moving in an unloaded chunk
			return;
		}
		
//		if (positionChanged) {
//			if (entityType == EntityType.ITEM || entityType == EntityType.FALLING_BLOCK) {
//				// TODO find other exceptions
//				this.previousPosition = this.position;
//				this.position = finalVelocityPosition;
//				refreshCoordinate(finalVelocityPosition);
//			} else {
//				refreshPosition(finalVelocityPosition, true);
//			}
//		}
		
		// Update velocity
		if (hasVelocity || !newVelocity.isZero()) {
			@SuppressWarnings("ConstantConditions")
			final double airDrag = this instanceof LivingEntity ? 0.91 : 0.98;
			final double drag = this.onGround ?
					finalChunk.getBlock(position).registry().friction() : airDrag;
			this.velocity = newVelocity
					// Convert from block/tick to block/sec
					.mul(tps)
					// Apply drag
					.apply((x, y, z) -> new Vec(
							x * drag,
							!noGravity ? y * (1 - gravityDragPerTick) : y,
							z * drag
					))
					// Prevent infinitely decreasing velocity
					.apply(Vec.Operator.EPSILON);
		}
		// Verify if velocity packet has to be sent
		if (hasVelocity || gravityTickCount > 0) {
			sendPacketToViewers(getVelocityPacket());
		}
	}
}
