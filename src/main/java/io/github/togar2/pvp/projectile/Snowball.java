package io.github.togar2.pvp.projectile;

import io.github.togar2.pvp.entity.EntityUtils;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
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
		EntityUtils.damage(entity, new Damage(DamageType.THROWN, this, getShooter(), null, damage));
		
		remove();
	}
	
	@Override
	public void onStuck() {
		triggerStatus((byte) 3); // Snowball particles
		
		remove();
	}
	
	@Override
	public void setItem(@NotNull ItemStack item) {
		((SnowballMeta) getEntityMeta()).setItem(item);
	}
}
