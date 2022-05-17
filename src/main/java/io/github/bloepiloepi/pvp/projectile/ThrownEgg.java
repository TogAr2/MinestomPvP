package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.item.ThrownEggMeta;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ThrownEgg extends CustomEntityProjectile implements ItemHoldingProjectile {
	
	public ThrownEgg(@Nullable Entity shooter) {
		super(shooter, EntityType.EGG, true);
	}
	
	@Override
	public void onHit(Entity entity) {
		triggerStatus((byte) 3); // Egg particles
		
		EntityUtils.damage(entity, CustomDamageType.thrown(this, getShooter()), 0.0F);
		
		remove();
	}
	
	@Override
	public void onStuck() {
		triggerStatus((byte) 3); // Egg particles
		
		remove();
	}
	
	@Override
	public void setItem(@NotNull ItemStack item) {
		((ThrownEggMeta) getEntityMeta()).setItem(item);
	}
}
