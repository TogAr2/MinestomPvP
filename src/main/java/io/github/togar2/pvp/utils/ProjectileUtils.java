package io.github.togar2.pvp.utils;

import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// Completely copied from Minestom except singleCollision parameter
public class ProjectileUtils {
	public static @NotNull PhysicsResult simulateMovement(@NotNull Pos entityPosition, @NotNull Vec entityVelocityPerTick,
	                                                      @NotNull BoundingBox entityBoundingBox, @NotNull WorldBorder worldBorder,
	                                                      @NotNull Block.Getter blockGetter, @NotNull Aerodynamics aerodynamics,
	                                                      boolean entityNoGravity, boolean entityHasPhysics, boolean entityOnGround,
	                                                      boolean entityFlying, @Nullable PhysicsResult previousPhysicsResult,
	                                                      boolean singleCollision) {
		final PhysicsResult physicsResult = entityHasPhysics ?
				CollisionUtils.handlePhysics(blockGetter, entityBoundingBox, entityPosition, entityVelocityPerTick, previousPhysicsResult, singleCollision) :
				CollisionUtils.blocklessCollision(entityPosition, entityVelocityPerTick);
		
		Pos newPosition = physicsResult.newPosition();
		Vec newVelocity = physicsResult.newVelocity();
		
		Pos positionWithinBorder = CollisionUtils.applyWorldBorder(worldBorder, entityPosition, newPosition);
		//newVelocity = updateVelocity(entityPosition, newVelocity, blockGetter, aerodynamics, !positionWithinBorder.samePoint(entityPosition), entityFlying, entityOnGround, entityNoGravity);
		return new PhysicsResult(positionWithinBorder, newVelocity, physicsResult.isOnGround(), physicsResult.collisionX(), physicsResult.collisionY(), physicsResult.collisionZ(),
				physicsResult.originalDelta(), physicsResult.collisionPoints(), physicsResult.collisionShapes(), physicsResult.hasCollision(), physicsResult.res());
	}
	
	private static @NotNull Vec updateVelocity(@NotNull Pos entityPosition, @NotNull Vec currentVelocity, @NotNull Block.Getter blockGetter, @NotNull Aerodynamics aerodynamics,
	                                           boolean positionChanged, boolean entityFlying, boolean entityOnGround, boolean entityNoGravity) {
		if (!positionChanged) {
			if (entityFlying) return Vec.ZERO;
			return new Vec(0, entityNoGravity ? 0 : -aerodynamics.gravity() * aerodynamics.verticalAirResistance(), 0);
		}
		
		double drag = entityOnGround ? blockGetter.getBlock(entityPosition.sub(0, 0.5000001, 0)).registry().friction() * aerodynamics.horizontalAirResistance() :
				aerodynamics.horizontalAirResistance();
		double gravity = entityFlying ? 0 : aerodynamics.gravity();
		double gravityDrag = entityFlying ? 0.6 : aerodynamics.verticalAirResistance();
		
		double x = currentVelocity.x() * drag;
		double y = entityNoGravity ? currentVelocity.y() : (currentVelocity.y() - gravity) * gravityDrag;
		double z = currentVelocity.z() * drag;
		return new Vec(Math.abs(x) < Vec.EPSILON ? 0 : x, Math.abs(y) < Vec.EPSILON ? 0 : y, Math.abs(z) < Vec.EPSILON ? 0 : z);
	}
}
