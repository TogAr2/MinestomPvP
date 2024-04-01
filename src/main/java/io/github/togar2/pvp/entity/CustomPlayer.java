package io.github.togar2.pvp.entity;

import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.PhysicsUtils;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityVelocityEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.player.PlayerUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class CustomPlayer extends Player implements PvpPlayer {
	private boolean velocityUpdate = false;
	private PhysicsResult previousPhysicsResult = null;
	
	private double jumpVelocity = 0.42;
	
	public CustomPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
		super(uuid, username, playerConnection);
	}
	
	public void setJumpVelocity(double jumpVelocity) {
		this.jumpVelocity = jumpVelocity;
	}
	
	protected double getJumpVelocity() {
		return jumpVelocity;
	}
	
	public double getJumpBoostVelocityModifier() {
		TimedPotion effect = getEffect(PotionEffect.JUMP_BOOST);
		return effect != null ?
				(0.1 * (effect.potion().amplifier() + 1)) : 0.0;
	}
	
	@Override
	public void jump() {
		int tps = ServerFlag.SERVER_TICKS_PER_SECOND;
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
	
	@Override
	public void addVelocityNoUpdate(Vec add) {
		velocity = velocity.add(add);
	}
	
	@Override
	public void mulVelocityNoUpdate(double factor) {
		velocity = velocity.mul(factor);
	}
	
	@Override
	public void setVelocity(@NotNull Vec velocity) {
		EntityVelocityEvent entityVelocityEvent = new EntityVelocityEvent(this, velocity);
		EventDispatcher.callCancellable(entityVelocityEvent, () -> {
			this.velocity = entityVelocityEvent.getVelocity();
			velocityUpdate = true;
		});
	}
	
	public void sendImmediateVelocityUpdate() {
		if (velocityUpdate) {
			velocityUpdate = false;
			sendPacketToViewersAndSelf(getVelocityPacket());
		}
	}
	
	@Override
	protected void movementTick() {
		this.gravityTickCount = onGround ? 0 : gravityTickCount + 1;
		if (vehicle != null) return;
		
		final double tps = ServerFlag.SERVER_TICKS_PER_SECOND;
		
		// Slow falling effect
		Aerodynamics aerodynamics = getAerodynamics();
		if (velocity.y() < 0 && hasEffect(PotionEffect.SLOW_FALLING))
			aerodynamics = aerodynamics.withGravity(0.01);
		
		PhysicsResult physicsResult = PhysicsUtils.simulateMovement(position, velocity.div(ServerFlag.SERVER_TICKS_PER_SECOND), boundingBox,
				instance.getWorldBorder(), instance, aerodynamics, hasNoGravity(), hasPhysics, onGround, isFlying(), previousPhysicsResult);
		this.previousPhysicsResult = physicsResult;
		
		Chunk finalChunk = ChunkUtils.retrieve(instance, currentChunk, physicsResult.newPosition());
		if (!ChunkUtils.isLoaded(finalChunk)) return;
		
		velocity = physicsResult.newVelocity().mul(tps);
		onGround = physicsResult.isOnGround();
		
		// Levitation effect
		TimedPotion levitation = getEffect(PotionEffect.LEVITATION);
		if (levitation != null) {
			velocity = velocity.withY(
					((0.05 * (double)
							(levitation.potion().amplifier() + 1)
							- (velocity.y() / tps)) * 0.2) * tps
			);
		}
		
		if (!PlayerUtils.isSocketClient(this)) {
			refreshPosition(physicsResult.newPosition(), true, true);
		}
		sendImmediateVelocityUpdate();
	}
	
//	@SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
//	@Override
//	protected void movementTick(boolean wasOnGround, boolean flying, Pos positionBeforeMove, Vec newVelocity) {
//		final double tps = MinecraftServer.TICK_PER_SECOND;
//		final double drag;
//		if (wasOnGround) {
//			final Chunk chunk = ChunkUtils.retrieve(instance, currentChunk, position);
//			synchronized (chunk) {
//				drag = chunk.getBlock(positionBeforeMove.sub(0, 0.5000001, 0)).registry().friction() * 0.91;
//			}
//		} else drag = 0.91;
//
//		boolean slowFall = velocity.y() < 0 && hasEffect(PotionEffect.SLOW_FALLING);
//		double gravity = hasNoGravity() || flying ? 0 : slowFall ? 0.01 : gravityAcceleration;
//		double gravityDrag = hasNoGravity() ? 0 : flying ? 0.6 : (1 - gravityDragPerTick);
//
//		this.velocity = newVelocity
//				// Apply drag
//				.apply((x, y, z) -> new Vec(
//						x * drag,
//						(y - gravity) * gravityDrag,
//						z * drag
//				))
//				// Convert from block/tick to block/sec
//				.mul(tps)
//				// Prevent infinitely decreasing velocity
//				.apply(Vec.Operator.EPSILON);
//
//		if (EntityUtils.hasEffect(this, PotionEffect.LEVITATION)) {
//			velocity = velocity.withY(
//					((0.05 * (double)
//							(EntityUtils.getEffect(this, PotionEffect.LEVITATION).amplifier() + 1)
//							- (velocity.y() / tps)) * 0.2) * tps
//			);
//		}
//
//		sendImmediateVelocityUpdate();
//	}
}
