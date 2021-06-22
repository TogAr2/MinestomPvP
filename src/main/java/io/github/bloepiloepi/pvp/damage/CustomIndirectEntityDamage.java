package io.github.bloepiloepi.pvp.damage;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomIndirectEntityDamage extends CustomEntityDamage {
	private final Entity owner;
	
	public CustomIndirectEntityDamage(String name, @NotNull Entity projectile, @Nullable Entity owner) {
		super(name, projectile);
		this.owner = owner;
	}
	
	@Override
	@Nullable
	public Entity getDirectEntity() {
		return this.entity;
	}
	
	@Override
	@Nullable
	public Entity getEntity() {
		return owner;
	}
	
	@Nullable
	public Entity getOwner() {
		return owner;
	}
}
