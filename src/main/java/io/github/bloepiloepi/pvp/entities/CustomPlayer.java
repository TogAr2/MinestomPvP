package io.github.bloepiloepi.pvp.entities;

import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Chunk;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
	
	protected double getJumpVelocity() {
		return 0.42;
	}
	
	public double getJumpBoostVelocityModifier() {
		return EntityUtils.hasEffect(this, PotionEffect.JUMP_BOOST) ?
				(0.1 * (EntityUtils.getEffect(this, PotionEffect.JUMP_BOOST).amplifier() + 1)) : 0.0;
	}
	
	public void jump() {
		int tps = MinecraftServer.TICK_PER_SECOND;
		double yVel = this.getJumpVelocity() + this.getJumpBoostVelocityModifier();
		velocity = velocity.withY(yVel * tps);
		if (this.isSprinting()) {
			double angle = position.yaw() * (Math.PI / 180);
			velocity = velocity.add(-Math.sin(angle) * 0.2 * tps, 0, Math.cos(angle) * 0.2 * tps);
		}
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
		
		double gravity = ((noGravity || isFlying()) ? 0 : gravityAcceleration);
		if (currentVelocity.y() < 0 && EntityUtils.hasEffect(this, PotionEffect.SLOW_FALLING))
			gravity = 0.01;
		
		final Vec deltaPos = new Vec(
				currentVelocity.x() / tps,
				currentVelocity.y() / tps - gravity,
				currentVelocity.z() / tps
		);
		
		final Pos newPosition;
		final Vec newVelocity;
		if (this.hasPhysics) {
			final var physicsResult = CollisionUtils.handlePhysics(this, deltaPos, lastPhysicsResult);
			this.lastPhysicsResult = physicsResult;
			//this.onGround = physicsResult.isOnGround();
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
					finalChunk.getBlock(position).registry().friction() * airDrag : airDrag;
			this.velocity = newVelocity
					// Convert from block/tick to block/sec
					.mul(tps)
					// Apply drag
					.apply((x, y, z) -> new Vec(
							x * drag,
							!noGravity ? y * (isFlying() ? 0.6 : (1 - gravityDragPerTick)) : y,
							z * drag
					))
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
		// Verify if velocity packet has to be sent
		if (hasVelocity || gravityTickCount > 0) {
			sendPacketToViewers(getVelocityPacket());
		}
	}
	
	private static Method refreshCoordinate = null;
	private static Field lastAbsoluteSynchronizationTime = null;
	
	static {
		try {
			refreshCoordinate = Entity.class.getDeclaredMethod("refreshCoordinate", Point.class);
			lastAbsoluteSynchronizationTime = Entity.class.getDeclaredField("lastAbsoluteSynchronizationTime");
			refreshCoordinate.setAccessible(true);
			lastAbsoluteSynchronizationTime.setAccessible(true);
		} catch (NoSuchMethodException | NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	public void refreshPosition(@NotNull final Pos newPosition, boolean ignoreView) {
		final var previousPosition = this.position;
		final Pos position = ignoreView ? previousPosition.withCoord(newPosition) : newPosition;
		if (position.equals(lastSyncedPosition)) return;
		this.position = position;
		this.previousPosition = previousPosition;
		if (!position.samePoint(previousPosition)) {
			try {
				refreshCoordinate.invoke(this, position);
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		// Update viewers
		final boolean viewChange = !position.sameView(lastSyncedPosition);
		final double distanceX = Math.abs(position.x() - lastSyncedPosition.x());
		final double distanceY = Math.abs(position.y() - lastSyncedPosition.y());
		final double distanceZ = Math.abs(position.z() - lastSyncedPosition.z());
		final boolean positionChange = (distanceX + distanceY + distanceZ) > 0;
		
		final Chunk chunk = getChunk();
		assert chunk != null;
		if (distanceX > 8 || distanceY > 8 || distanceZ > 8) {
			PacketUtils.prepareViewablePacket(chunk, new EntityTeleportPacket(getEntityId(), position, isOnGround()), this);
			try {
				lastAbsoluteSynchronizationTime.set(this, System.currentTimeMillis());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} else if (positionChange && viewChange) {
			PacketUtils.prepareViewablePacket(chunk, EntityPositionAndRotationPacket.getPacket(getEntityId(), position,
					lastSyncedPosition, isOnGround()), this);
			// Fix head rotation
			PacketUtils.prepareViewablePacket(chunk, new EntityHeadLookPacket(getEntityId(), position.yaw()), this);
		} else if (positionChange) {
			PacketUtils.prepareViewablePacket(chunk, EntityPositionPacket.getPacket(getEntityId(), position, lastSyncedPosition, onGround), this);
		} else if (viewChange) {
			PacketUtils.prepareViewablePacket(chunk, new EntityHeadLookPacket(getEntityId(), position.yaw()), this);
			PacketUtils.prepareViewablePacket(chunk, new EntityRotationPacket(getEntityId(), position.yaw(), position.pitch(), onGround), this);
		}
		this.lastSyncedPosition = position;
	}
}
