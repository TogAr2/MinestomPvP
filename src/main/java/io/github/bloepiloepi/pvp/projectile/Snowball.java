package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.item.SnowballMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Snowball extends CustomEntityProjectile implements ItemHoldingProjectile {
	
	public Snowball(@Nullable Entity shooter) {
		super(shooter, EntityType.SNOWBALL, true);
	}
	
	@Override
	public void onHit(Entity entity) {
		triggerStatus((byte) 3); // Snowball particles
		
		int damage = entity.getEntityType() == EntityType.BLAZE ? 3 : 0;
		EntityUtils.damage(entity, CustomDamageType.thrown(this, getShooter()), damage);
		
		remove();
	}
	
	@Override
	public void onStuck() {
		triggerStatus((byte) 3); // Snowball particles
		
		remove();
	}
	
	@Override
	public void tick(long time) {
		super.tick(time);
	}
	
	@Override
	public void setItem(@NotNull ItemStack item) {
		((SnowballMeta) getEntityMeta()).setItem(item);
	}
}
