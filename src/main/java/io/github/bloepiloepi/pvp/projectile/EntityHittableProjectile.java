package io.github.bloepiloepi.pvp.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EntityHittableProjectile extends EntityProjectile {
	private boolean beforeHitCalled = false;
	
	public EntityHittableProjectile(@Nullable Entity shooter, @NotNull EntityType entityType) {
		super(shooter, entityType);
	}
	
	public boolean isBeforeHitCalled() {
		return beforeHitCalled;
	}
	
	public void setBeforeHitCalled(boolean beforeHitCalled) {
		this.beforeHitCalled = beforeHitCalled;
	}
	
	public void beforeHitBlock() {
	}
	
	public void onHit(@Nullable Entity entity) {
		//TODO fix block side collision
	}
	
	@Override
	public void onStuck() {
		onHit(null);
	}
	
	public abstract void setItem(@NotNull ItemStack item);
}
