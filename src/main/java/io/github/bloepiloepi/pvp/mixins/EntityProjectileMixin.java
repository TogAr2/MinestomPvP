package io.github.bloepiloepi.pvp.mixins;

import io.github.bloepiloepi.pvp.projectile.EntityHittableProjectile;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Mixin(EntityProjectile.class)
public abstract class EntityProjectileMixin extends Entity {
	private EntityProjectileMixin() {
		super(EntityType.PIG);
	}
	
	@Shadow @Final private Entity shooter;
	@Shadow public abstract void onStuck();
	@Shadow public abstract void onUnstuck();
	
	/**
	 * @author me
	 */
	@SuppressWarnings("ConstantConditions")
	@Overwrite
	public void tick(long time) {
		Position posBefore = getPosition().clone();
		
		if ((Object) this instanceof EntityHittableProjectile &&
				((EntityHittableProjectile) (Object) this).shouldCallHit()
				&& willBeStuck(posBefore)) {
			((EntityHittableProjectile) (Object) this).onHit(null);
			((EntityHittableProjectile) (Object) this).setHitCalled(true);
		}
		
		super.tick(time);
		Position posNow = getPosition().clone();
		if (hackIsStuck(posBefore, posNow)) {
			if (super.onGround) {
				return;
			}
			super.onGround = true;
			getVelocity().zero();
			sendPacketToViewersAndSelf(getVelocityPacket());
			setNoGravity(true);
			onStuck();
		} else {
			if ((Object) this instanceof EntityHittableProjectile &&
					((EntityHittableProjectile) (Object) this).shouldCallHit()
					&& willBeStuck(posBefore)) {
				((EntityHittableProjectile) (Object) this).onHit(null);
				((EntityHittableProjectile) (Object) this).setHitCalled(true);
			}
			
			if (isRemoved() || !super.onGround) {
				return;
			}
			super.onGround = false;
			setNoGravity(false);
			onUnstuck();
		}
	}
	
	private boolean willBeStuck(Position pos) {
		return hackIsStuck(pos, pos.clone().add(getVelocity().clone().multiply(0.06).toPosition()));
	}
	
	private boolean hackIsStuck(Position pos, Position posNow) {
		if (pos.isSimilar(posNow)) {
			return true;
		}
		
		Instance instance = getInstance();
		Chunk chunk = null;
		Collection<Entity> entities = null;
		assert instance != null;
		
        /*
          What we're about to do is to discretely jump from the previous position to the new one.
          For each point we will be checking blocks and entities we're in.
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
			
			Chunk currentChunk = instance.getChunkAt(pos);
			if (currentChunk != chunk) {
				chunk = currentChunk;
				entities = instance.getChunkEntities(chunk)
						.stream()
						.filter(entity -> entity instanceof LivingEntity)
						.collect(Collectors.toSet());
			}
            /*
              We won't check collisions with entities for first ticks of arrow's life, because it spawns in the
              shooter and will immediately damage him.
             */
			boolean shouldDamageShooter = getAliveTicks() < 6;
			assert entities != null;
			Optional<Entity> victimOptional = entities.stream()
					.filter(entity -> {
						if (shouldDamageShooter && entity == shooter) return false;
						return entity.getBoundingBox().intersect(pos.getX(), pos.getY(), pos.getZ());
					})
					.findAny();
			if (victimOptional.isPresent()) {
				LivingEntity victim = (LivingEntity) victimOptional.get();
				
				EventDispatcher.call(new EntityAttackEvent(this, victim));
				
				remove();
				return super.onGround;
			}
		}
		
		return false;
	}
}
