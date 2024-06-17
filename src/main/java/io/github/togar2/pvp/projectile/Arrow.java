package io.github.togar2.pvp.projectile;

import io.github.togar2.pvp.potion.PotionListener;
import io.github.togar2.pvp.potion.effect.CustomPotionEffect;
import io.github.togar2.pvp.potion.effect.CustomPotionEffects;
import io.github.togar2.pvp.potion.item.CustomPotionType;
import io.github.togar2.pvp.potion.item.CustomPotionTypes;
import net.kyori.adventure.util.RGBLike;
import net.minestom.server.color.Color;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.projectile.ArrowMeta;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class Arrow extends AbstractArrow {
	public static final ItemStack DEFAULT_ARROW = ItemStack.of(Material.ARROW);
	public static final Predicate<ItemStack> ARROW_PREDICATE = stack ->
			stack.material() == Material.ARROW
			|| stack.material() == Material.SPECTRAL_ARROW
			|| stack.material() == Material.TIPPED_ARROW;
	public static final Predicate<ItemStack> ARROW_OR_FIREWORK_PREDICATE = ARROW_PREDICATE.or(stack ->
			stack.material() == Material.FIREWORK_ROCKET);
	
	private final boolean legacy;
	private PotionContents potion;
	private boolean fixedColor;
	
	public Arrow(@Nullable Entity shooter, boolean legacy) {
		super(shooter, EntityType.ARROW);
		this.legacy = legacy;
		this.potion = new PotionContents(PotionType.MUNDANE);
	}
	
	public void inheritEffects(ItemStack stack) {
		if (stack.material() == Material.TIPPED_ARROW && stack.has(ItemComponent.POTION_CONTENTS)) {
			PotionContents potionContents = new PotionContents(stack.get(ItemComponent.POTION_CONTENTS).potion());
			PotionType potionType = potionContents.potion();
			List<net.minestom.server.potion.CustomPotionEffect> customEffects = potionContents.customEffects();
			RGBLike color = potionContents.customColor();
			
			if (color == null) {
				fixedColor = false;
				if (potionType == PotionType.MUNDANE && customEffects.isEmpty()) {
					setColor(-1);
				} else {
					setColor(PotionListener.getPotionColor(
							PotionListener.getAllPotions(potionType, customEffects, legacy)));
				}
			} else {
				fixedColor = true;
				
				int red = color.red();
				int green = color.green();
				int blue = color.blue();
				
				setColor((red << 16) | (green << 8) | blue);
			}
			
			potion = new PotionContents(potionType, null, customEffects);
		} else if (stack.material() == Material.ARROW) {
			fixedColor = false;
			setColor(-1);
			
			potion = new PotionContents(PotionType.MUNDANE);
		}
	}
	
	@Override
	public void update(long time) {
		super.update(time);
		
		if (onGround && stuckTime >= 600 && !potion.customEffects().isEmpty()) {
			triggerStatus((byte) 0);
			
			fixedColor = false;
			setColor(-1);
			
			potion = new PotionContents(PotionType.MUNDANE);
		}
	}
	
	@Override
	protected void onHurt(LivingEntity entity) {
		CustomPotionType customPotionType = CustomPotionTypes.get(potion.potion());
		if (customPotionType != null) {
			for (Potion potion : legacy ? customPotionType.getLegacyEffects() : customPotionType.getEffects()) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.effect());
				if (customPotionEffect.isInstant()) {
					customPotionEffect.applyInstantEffect(this, null,
							entity, potion.amplifier(), 1.0D, legacy);
				} else {
					int duration = Math.max(potion.duration() / 8, 1);
					entity.addEffect(new Potion(potion.effect(), potion.amplifier(), duration, potion.flags()));
				}
			}
		}
		
		if (potion.customEffects().isEmpty()) return;
		
		potion.customEffects().stream().map(customPotion ->
				new Potion(Objects.requireNonNull(customPotion.id()),
						customPotion.amplifier(), customPotion.duration(),
						PotionListener.createFlags(
								customPotion.isAmbient(),
								customPotion.showParticles(),
								customPotion.showIcon()
						)))
				.forEach(potion -> {
					CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.effect());
					if (customPotionEffect.isInstant()) {
						customPotionEffect.applyInstantEffect(this, null,
								entity, potion.amplifier(), 1.0D, legacy);
					} else {
						entity.addEffect(new Potion(potion.effect(), potion.amplifier(),
								potion.duration(), potion.flags()));
					}
				});
	}
	
	@Override
	protected ItemStack getPickupItem() {
		if (potion.potion() == PotionType.MUNDANE && potion.customEffects().isEmpty()) {
			return DEFAULT_ARROW;
		}
		
		return ItemStack.of(Material.TIPPED_ARROW).with(ItemComponent.POTION_CONTENTS, new PotionContents(
				potion.potion(), fixedColor ? new Color(getColor()) : null, potion.customEffects()));
	}
	
	public void addPotion(net.minestom.server.potion.CustomPotionEffect effect) {
		potion.customEffects().add(effect);
		setColor(PotionListener.getPotionColor(
				PotionListener.getAllPotions(potion.potion(), potion.customEffects(), legacy)));
	}
	
	private void setColor(int color) {
		((ArrowMeta) getEntityMeta()).setColor(color);
	}
	
	private int getColor() {
		return ((ArrowMeta) getEntityMeta()).getColor();
	}
}
