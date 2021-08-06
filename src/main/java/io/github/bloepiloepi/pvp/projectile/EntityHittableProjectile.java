package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.events.ProjectileHitEvent;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class EntityHittableProjectile extends EntityProjectile {
	public static final Map<UUID, Position> hitPosition = new HashMap<>();
	
	private boolean hitCalled = false;
	
	public EntityHittableProjectile(@Nullable Entity shooter, @NotNull EntityType entityType) {
		super(shooter, entityType);
	}
	
	public boolean shouldCallHit() {
		return !hitCalled || canMultiHit();
	}
	
	public void setHitCalled(boolean beforeHitCalled) {
		this.hitCalled = beforeHitCalled;
	}
	
	public boolean canMultiHit() {
		return false;
	}
	
	public boolean hit(@Nullable Entity entity) {
		if (entity == null) {
			EventDispatcher.call(new ProjectileHitEvent.ProjectileBlockHitEvent(this, hitPosition.get(getUuid())));
			return onHit(null);
		} else {
			AtomicBoolean result = new AtomicBoolean();
			
			CancellableEvent event = new ProjectileHitEvent.ProjectileEntityHitEvent(this, entity);
			EventDispatcher.callCancellable(event, () -> result.set(onHit(entity)));
			
			return result.get();
		}
	}
	
	protected boolean onHit(@Nullable Entity entity) {
		return true;
	}
	
	@Override
	public void onStuck() {
		remove();
	}
	
	public void setItem(@NotNull ItemStack item) {
	}
}
