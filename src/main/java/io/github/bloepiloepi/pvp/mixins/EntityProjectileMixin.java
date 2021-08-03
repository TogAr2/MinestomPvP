package io.github.bloepiloepi.pvp.mixins;

import io.github.bloepiloepi.pvp.projectile.EntityHittableProjectile;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(EntityProjectile.class)
public abstract class EntityProjectileMixin extends Entity {
	private EntityProjectileMixin() {
		super(EntityType.PIG);
	}
	
	@Shadow protected abstract boolean isStuck(Position pos, Position posNow);
	
	@ModifyConstant(method = "isStuck", constant = @Constant(longValue = 3))
	private long minAliveTicks(long original) {
		return 4;
	}
	
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "tick", at = @At("HEAD"))
	private void onTick(long time, CallbackInfo ci) {
		Position originalPos = getPosition().clone();
		
		// Hacky way of checking if the projectile will be stuck in the next few ticks
		if ((Object) this instanceof EntityHittableProjectile
				&& !((EntityHittableProjectile) (Object) this).isBeforeHitCalled()
				&& hackIsStuck(originalPos, originalPos.clone().add(getVelocity().clone().multiply(0.1).toPosition()))) {
			((EntityHittableProjectile) (Object) this).beforeHitBlock();
			((EntityHittableProjectile) (Object) this).setBeforeHitCalled(true);
		}
	}
	
	private boolean hackIsStuck(Position pos, Position posNow) {
		if (pos.isSimilar(posNow)) {
			return true;
		}
		
		Instance instance = getInstance();
		assert instance != null;
		
        /*
          What we're about to do is to discretely jump from the previous position to the new one.
          For each point we will be checking blocks we're in.
         */
		double part = .25D; // half of the bounding box
		Vector dir = posNow.toVector().subtract(pos.toVector());
		int parts = (int) Math.ceil(dir.length() / part);
		Position direction = dir.normalize().multiply(part).toPosition();
		for (int i = 0; i < parts; ++i) {
			// If we're at last part, we can't just add another direction-vector, because we can exceed end point.
			if (i == parts - 1) {
				pos.setX(posNow.getX());
				pos.setY(posNow.getY());
				pos.setZ(posNow.getZ());
			} else {
				pos.add(direction);
			}
			BlockPosition bpos = pos.toBlockPosition();
			Block block = instance.getBlock(bpos.getX(), bpos.getY() - 1, bpos.getZ());
			if (!block.isAir() && !block.isLiquid()) {
				return true;
			}
		}
		
		return false;
	}
}
