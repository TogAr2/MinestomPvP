package io.github.bloepiloepi.pvp.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityHittableProjectile extends EntityProjectile {
	
	public EntityHittableProjectile(@Nullable Entity shooter, @NotNull EntityType entityType) {
		super(shooter, entityType);
	}
	
	public void onHit(@Nullable Entity entity) {
		//TODO fix block side collision
	}
	
	@Override
	public void onStuck() {
		onHit(null);
	}
}
