package io.github.bloepiloepi.pvp.damage;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.Nullable;

public class CustomEntityDamage extends CustomDamageType {
	@Nullable
	protected final Entity source;
	private boolean thorns;
	
	public CustomEntityDamage(@Nullable Entity source) {
		super("entity_source");
		this.source = source;
	}
	
	public CustomEntityDamage setThorns() {
		this.thorns = true;
		return this;
	}
	
	public boolean isThorns() {
		return this.thorns;
	}
	
	@Nullable
	public Entity getAttacker() {
		return this.source;
	}
	
	//TODO death message
	
	public boolean isScaledWithDifficulty() {
		return this.source != null && this.source instanceof LivingEntity && !(this.source instanceof Player);
	}
	
	@Nullable
	public Position getPosition() {
		return this.source != null ? this.source.getPosition() : null;
	}
}
