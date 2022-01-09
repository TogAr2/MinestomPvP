package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.potion.PotionListener;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

public class SpectralArrow extends AbstractArrow {
	private static final ItemStack PICKUP_ITEM = ItemStack.of(Material.SPECTRAL_ARROW);
	private int duration = 200;
	
	public SpectralArrow(@Nullable Entity shooter) {
		super(shooter, EntityType.SPECTRAL_ARROW);
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	@Override
	protected ItemStack getPickupItem() {
		return PICKUP_ITEM;
	}
	
	@Override
	protected void onHurt(LivingEntity entity) {
		entity.addEffect(new Potion(PotionEffect.GLOWING, (byte) 0, duration, PotionListener.defaultFlags()));
	}
}
