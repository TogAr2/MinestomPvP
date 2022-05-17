package io.github.bloepiloepi.pvp.entities;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class CrystalEntity extends LivingEntity {
	private final boolean fire;
	
	public CrystalEntity(boolean fire) {
		super(EntityType.END_CRYSTAL);
		this.fire = fire;
	}
	
	public CrystalEntity() {
		this(false);
	}
	
	@Override
	public void update(long time) {
		if (fire && !instance.getBlock(position).compare(Block.FIRE))
			instance.setBlock(position, Block.FIRE);
	}
	
	@Override
	public boolean damage(@NotNull DamageType type, float value) {
		if (isDead())
			return false;
		if (isInvulnerable() || isImmune(type)) {
			return false;
		}
		
		EntityDamageEvent entityDamageEvent = new EntityDamageEvent(this, type, value, type.getSound(this));
		EventDispatcher.callCancellable(entityDamageEvent, () -> {
			// Set the last damage type since the event is not cancelled
			this.lastDamageSource = entityDamageEvent.getDamageType();
			
			if (!(type instanceof CustomDamageType damageType) || !damageType.isExplosive()) {
				instance.explode((float) position.x(), (float) position.y(), (float) position.z(), 6.0f);
			}
		});
		
		return !entityDamageEvent.isCancelled();
	}
}
