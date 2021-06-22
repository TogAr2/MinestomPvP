package io.github.bloepiloepi.pvp.damage;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.Nullable;

public class CustomEntityDamage extends CustomDamageType {
	@Nullable
	protected final Entity entity;
	private boolean thorns;
	
	public CustomEntityDamage(String name, @Nullable Entity entity) {
		super(name);
		this.entity = entity;
	}
	
	public CustomEntityDamage(@Nullable Entity entity) {
		this("entity_source", entity);
	}
	
	public CustomEntityDamage setThorns() {
		this.thorns = true;
		return this;
	}
	
	public boolean isThorns() {
		return this.thorns;
	}
	
	@Override
	@Nullable
	public Entity getEntity() {
		return this.entity;
	}
	
	//TODO death message
	
	@Override
	public boolean isScaledWithDifficulty() {
		return this.entity != null && this.entity instanceof LivingEntity && !(this.entity instanceof Player);
	}
	
	@Override
	@Nullable
	public Position getPosition() {
		return this.entity != null ? this.entity.getPosition() : null;
	}
}
