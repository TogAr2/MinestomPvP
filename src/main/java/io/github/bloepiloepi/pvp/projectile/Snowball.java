package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.item.SnowballMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Snowball extends EntityHittableProjectile {
	
	public Snowball(@Nullable Entity shooter) {
		super(shooter, EntityType.SNOWBALL);
	}
	
	@Override
	public boolean onHit(@Nullable Entity entity) {
		triggerStatus((byte) 3); // Snowball particles
		
		if (entity != null) {
			int damage = entity.getEntityType() == EntityType.BLAZE ? 3 : 0;
			EntityUtils.damage(entity, CustomDamageType.thrown(this, getShooter()), damage);
		}
		
		return true;
	}
	
	@Override
	public void setItem(@NotNull ItemStack item) {
		((SnowballMeta) getEntityMeta()).setItem(item);
	}
}
