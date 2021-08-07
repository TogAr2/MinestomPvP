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
			stack.getMaterial() == Material.ARROW
			|| stack.getMaterial() == Material.SPECTRAL_ARROW
			|| stack.getMaterial() == Material.TIPPED_ARROW;
	public static final Predicate<ItemStack> ARROW_OR_FIREWORK_PREDICATE = ARROW_PREDICATE.or(stack ->
			stack.getMaterial() == Material.FIREWORK_ROCKET);
	
	private PotionMeta potion;
	private boolean fixedColor;
	
	public Arrow(@Nullable Entity shooter) {
		super(shooter, EntityType.ARROW);
	}
	
	public void inheritEffects(ItemStack stack) {
		if (stack.getMaterial() == Material.TIPPED_ARROW) {
			PotionType potionType = ((PotionMeta) stack.getMeta()).getPotionType();
			List<net.minestom.server.potion.CustomPotionEffect> customEffects =
					((PotionMeta) stack.getMeta()).getCustomPotionEffects();
			
			Color color = ((PotionMeta) stack.getMeta()).getColor();
			if (color == null) {
				fixedColor = false;
				if (potionType == PotionType.EMPTY && customEffects.isEmpty()) {
					setColor(-1);
				} else {
					setColor(PotionListener.getPotionColor(
							PotionListener.getAllPotions(potionType, customEffects)));
				}
			} else {
				fixedColor = true;
				setColor(color.asRGB());
			}
			
			PotionMeta.Builder builder = new PotionMeta.Builder().effects(customEffects);
			if (potionType != null) builder.potionType(potionType);
			potion = builder.build();
		} else if (stack.getMaterial() == Material.ARROW) {
			fixedColor = false;
			setColor(-1);
			
			potion = new PotionMeta.Builder().potionType(PotionType.EMPTY).build();
		}
	}
	
	@Override
	public void update(long time) {
		super.update(time);
		
		if (onGround && stuckTime >= 600 && !potion.getCustomPotionEffects().isEmpty()) {
			triggerStatus((byte) 0);
			
			fixedColor = false;
			setColor(-1);
			
			potion = new PotionMeta.Builder().potionType(PotionType.EMPTY).build();
		}
	}
	
	@Override
	protected void onHurt(LivingEntity entity) {
		CustomPotionType customPotionType = CustomPotionTypes.get(potion.getPotionType());
		if (customPotionType != null) {
			for (Potion potion : customPotionType.getEffects()) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.getEffect());
				if (customPotionEffect.isInstant()) {
					customPotionEffect.applyInstantEffect(this, null,
							entity, potion.getAmplifier(), 1.0D);
				} else {
					int duration = Math.max(potion.getDuration() / 8, 1);
					byte flags = potion.getFlags();
					entity.addEffect(new Potion(potion.getEffect(), potion.getAmplifier(), duration,
							PotionListener.hasParticles(flags), PotionListener.hasIcon(flags),
							PotionListener.isAmbient(flags)));
				}
			}
		}
		
		if (potion.getCustomPotionEffects().isEmpty()) return;
		
		potion.getCustomPotionEffects().stream().map(customPotion ->
				new Potion(Objects.requireNonNull(PotionEffect.fromId(customPotion.getId())),
						customPotion.getAmplifier(), customPotion.getDuration(),
						customPotion.showParticles(), customPotion.showIcon(),
						customPotion.isAmbient()))
				.forEach(potion -> {
					CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.getEffect());
					if (customPotionEffect.isInstant()) {
						customPotionEffect.applyInstantEffect(this, null,
								entity, potion.getAmplifier(), 1.0D);
					} else {
						byte flags = potion.getFlags();
						entity.addEffect(new Potion(potion.getEffect(), potion.getAmplifier(),
								potion.getDuration(), PotionListener.hasParticles(flags),
								PotionListener.hasIcon(flags), PotionListener.isAmbient(flags)));
					}
				});
	}
	
	@Override
	protected ItemStack getPickupItem() {
		if (potion.getPotionType() == PotionType.EMPTY && potion.getCustomPotionEffects().isEmpty()) {
			return DEFAULT_ARROW;
		}
		
		return ItemStack.builder(Material.TIPPED_ARROW).meta(meta0 -> {
			PotionMeta.Builder meta = (PotionMeta.Builder) meta0;
			
			if (potion.getPotionType() != null && potion.getPotionType() != PotionType.EMPTY) {
				meta.potionType(potion.getPotionType());
			}
			if (!potion.getCustomPotionEffects().isEmpty()) {
				meta.effects(potion.getCustomPotionEffects());
			}
			if (fixedColor) {
				meta.color(new Color(getColor()));
			}
			
			return meta;
		}).build();
	}
	
	public void addPotion(net.minestom.server.potion.CustomPotionEffect effect) {
		potion.getCustomPotionEffects().add(effect);
		setColor(PotionListener.getPotionColor(
				PotionListener.getAllPotions(potion.getPotionType(), potion.getCustomPotionEffects())));
	}
	
	private void setColor(int color) {
		((ArrowMeta) getEntityMeta()).setColor(color);
	}
	
	private int getColor() {
		return ((ArrowMeta) getEntityMeta()).getColor();
	}
}
