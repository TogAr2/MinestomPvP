package io.github.bloepiloepi.pvp.damage;

import io.github.bloepiloepi.pvp.entity.EntityUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomEntityDamage extends CustomDamageType {
	protected final Entity entity;
	private boolean thorns;
	
	public CustomEntityDamage(String name, Entity entity) {
		super(name);
		this.entity = entity;
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
	
	@Override
	public @Nullable Component getDeathMessage(@NotNull Player killed) {
		ItemStack weapon = entity instanceof LivingEntity ? ((LivingEntity) entity).getItemInMainHand() : ItemStack.AIR;
		String id = "death.attack." + getIdentifier();
		if (!weapon.isAir() && weapon.getDisplayName() != null) {
			return Component.translatable(id + ".item", EntityUtils.getName(killed), EntityUtils.getName(entity), weapon.getDisplayName());
		} else {
			return Component.translatable(id, EntityUtils.getName(killed), EntityUtils.getName(entity));
		}
	}
	
	@Override
	public boolean isScaledWithDifficulty() {
		return this.entity != null && this.entity instanceof LivingEntity && !(this.entity instanceof Player);
	}
	
	@Override
	@Nullable
	public Pos getPosition() {
		return this.entity != null ? this.entity.getPosition() : null;
	}
}
