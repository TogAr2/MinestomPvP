package io.github.togar2.pvp.explosion;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.other.EndCrystalMeta;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class CrystalEntity extends LivingEntity {
	private final boolean fire;
	
	public CrystalEntity(boolean fire, boolean showingBottom) {
		super(EntityType.END_CRYSTAL);
		this.fire = fire;
		setNoGravity(true);
		hasPhysics = false;
		((EndCrystalMeta) getEntityMeta()).setShowingBottom(showingBottom);
	}
	
	public CrystalEntity() {
		this(false, false);
	}
	
	@Override
	public void update(long time) {
		if (fire && !instance.getBlock(position).compare(Block.FIRE))
			instance.setBlock(position, Block.FIRE);
	}
	
	@Override
	public boolean damage(@NotNull Damage damage) {
		if (isDead() || isRemoved())
			return false;
		if (isInvulnerable() || isImmune(damage.getType())) {
			return false;
		}
		
		// Set the last damage type since the event is not cancelled
		this.lastDamage = damage;
		
		remove();
		if (instance.getExplosionSupplier() != null
				&& !DamageTypeInfo.of(MinecraftServer.getDamageTypeRegistry().get(damage.getType())).explosive()) {
			instance.explode((float) position.x(), (float) position.y(), (float) position.z(), 6.0f);
		}
		
		return true;
	}
}
