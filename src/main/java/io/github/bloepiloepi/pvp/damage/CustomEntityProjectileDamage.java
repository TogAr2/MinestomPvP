package io.github.bloepiloepi.pvp.damage;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomEntityProjectileDamage extends CustomDamageType {
	private final Entity shooter;
	private final Entity projectile;
	
	public CustomEntityProjectileDamage(@Nullable Entity shooter, @NotNull Entity projectile) {
		super("projectile_source");
		this.shooter = shooter;
		this.projectile = projectile;
		
		this.setProjectile();
	}
	
	@NotNull
	public Entity getProjectile() {
		return projectile;
	}
	
	@Nullable
	public Entity getShooter() {
		return shooter;
	}
	
	@Override
	public @Nullable Entity getAttacker() {
		return projectile;
	}
}
