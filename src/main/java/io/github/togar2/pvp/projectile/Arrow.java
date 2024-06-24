package io.github.togar2.pvp.projectile;

import io.github.togar2.pvp.feature.effect.EffectFeature;
import net.minestom.server.color.Color;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.projectile.ArrowMeta;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
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
	
	private final EffectFeature effectFeature;
	
	private ItemStack itemStack = DEFAULT_ARROW;
	private boolean fixedColor;
	
	public Arrow(@Nullable Entity shooter, EffectFeature effectFeature) {
		super(shooter, EntityType.ARROW);
		this.effectFeature = effectFeature;
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
					setColor(effectFeature.getPotionColor(potion));
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
		
		if (onGround && stuckTime >= 600 && (!itemStack.has(ItemComponent.POTION_CONTENTS)
				|| !Objects.equals(itemStack.get(ItemComponent.POTION_CONTENTS), PotionContents.EMPTY))) {
			triggerStatus((byte) 0);
			itemStack = DEFAULT_ARROW;
		}
	}
	
	@Override
	protected void onHurt(LivingEntity entity) {
		effectFeature.addArrowEffects(entity, this);
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
		setColor(effectFeature.getPotionColor(potion));
	}
	
	private void updateColor() {
		PotionContents potionContents = itemStack.get(ItemComponent.POTION_CONTENTS);
		if (potionContents == null || potionContents.equals(PotionContents.EMPTY)) {
			setColor(-1);
			return;
		}
		//todo color from potion contents
		
	}
	
	private void setColor(int color) {
		((ArrowMeta) getEntityMeta()).setColor(color);
	}
	
	private int getColor() {
		return ((ArrowMeta) getEntityMeta()).getColor();
	}
	
	public PotionMeta getPotion() {
		return potion;
	}
}
