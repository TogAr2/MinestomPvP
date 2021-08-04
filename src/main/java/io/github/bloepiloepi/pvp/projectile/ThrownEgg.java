package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.item.ThrownEggMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThrownEgg extends EntityHittableProjectile {
	
	public ThrownEgg(@Nullable Entity shooter) {
		super(shooter, EntityType.EGG);
	}
	
	@Override
	public void onHit(@Nullable Entity entity) {
		triggerStatus((byte) 3); // Egg particles
		
		if (entity != null) {
			EntityUtils.damage(entity, CustomDamageType.thrown(this, getShooter()), 0.0F);
		}
		
		remove();
	}
	
	@Override
	public void setItem(@NotNull ItemStack item) {
		((ThrownEggMeta) getEntityMeta()).setItem(item);
	}
}
