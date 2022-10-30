package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffect;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffects;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionType;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionTypes;
import net.minestom.server.color.Color;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.arrow.ArrowMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
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
	private PotionMeta potion;
	private boolean fixedColor;
	
	public Arrow(@Nullable Entity shooter, boolean legacy) {
		super(shooter, EntityType.ARROW);
		this.legacy = legacy;
		this.potion = new PotionMeta(new PotionMeta.Builder().potionType(PotionType.EMPTY));
	}
	
	public void inheritEffects(ItemStack stack) {
		if (stack.material() == Material.TIPPED_ARROW) {
			PotionMeta potionMeta = new PotionMeta(stack.meta());
			PotionType potionType = potionMeta.getPotionType();
			List<net.minestom.server.potion.CustomPotionEffect> customEffects = potionMeta.getCustomPotionEffects();
			Color color = potionMeta.getColor();
			
			if (color == null) {
				fixedColor = false;
				if (potionType == PotionType.EMPTY && customEffects.isEmpty()) {
					setColor(-1);
				} else {
					setColor(PotionListener.getPotionColor(
							PotionListener.getAllPotions(potionType, customEffects, legacy)));
				}
			} else {
				fixedColor = true;
				setColor(color.asRGB());
			}
			
			PotionMeta.Builder builder = new PotionMeta.Builder().effects(customEffects);
			builder.potionType(potionType);
			potion = new PotionMeta(builder);
		} else if (stack.material() == Material.ARROW) {
			fixedColor = false;
			setColor(-1);
			
			potion = new PotionMeta(new PotionMeta.Builder().potionType(PotionType.EMPTY));
		}
	}
	
	@Override
	public void update(long time) {
		super.update(time);
		
		if (onGround && stuckTime >= 600 && !potion.getCustomPotionEffects().isEmpty()) {
			triggerStatus((byte) 0);
			
			fixedColor = false;
			setColor(-1);
			
			potion = new PotionMeta(new PotionMeta.Builder().potionType(PotionType.EMPTY));
		}
	}
	
	@Override
	protected void onHurt(LivingEntity entity) {
		CustomPotionType customPotionType = CustomPotionTypes.get(potion.getPotionType());
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
		
		if (potion.getCustomPotionEffects().isEmpty()) return;
		
		potion.getCustomPotionEffects().stream().map(customPotion ->
				new Potion(Objects.requireNonNull(PotionEffect.fromId(customPotion.id())),
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
		if (potion.getPotionType() == PotionType.EMPTY && potion.getCustomPotionEffects().isEmpty()) {
			return DEFAULT_ARROW;
		}
		
		return ItemStack.builder(Material.TIPPED_ARROW).meta(PotionMeta.class, meta -> {
			if (potion.getPotionType() != PotionType.EMPTY) {
				meta.potionType(potion.getPotionType());
			}
			if (!potion.getCustomPotionEffects().isEmpty()) {
				meta.effects(potion.getCustomPotionEffects());
			}
			if (fixedColor) {
				meta.color(new Color(getColor()));
			}
		}).build();
	}
	
	public void addPotion(net.minestom.server.potion.CustomPotionEffect effect) {
		potion.getCustomPotionEffects().add(effect);
		setColor(PotionListener.getPotionColor(
				PotionListener.getAllPotions(potion.getPotionType(), potion.getCustomPotionEffects(), legacy)));
	}
	
	private void setColor(int color) {
		((ArrowMeta) getEntityMeta()).setColor(color);
	}
	
	private int getColor() {
		return ((ArrowMeta) getEntityMeta()).getColor();
	}
}
