package io.github.bloepiloepi.pvp.potion.effect;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entity.EntityGroup;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.food.HungerManager;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CustomPotionEffect {
	public static final int PERMANENT = 32767;
	
	private final Map<Attribute, AttributeModifier> attributeModifiers = new HashMap<>();
	private Map<Attribute, AttributeModifier> legacyAttributeModifiers;
	private final PotionEffect potionEffect;
	private final int color;
	
	public CustomPotionEffect(PotionEffect potionEffect, int color) {
		this.potionEffect = potionEffect;
		this.color = color;
	}
	
	public PotionEffect getPotionEffect() {
		return potionEffect;
	}
	
	public int getColor() {
		return color;
	}
	
	public CustomPotionEffect addAttributeModifier(Attribute attribute, String uuid, float amount, AttributeOperation operation) {
		attributeModifiers.put(attribute, new AttributeModifier(UUID.fromString(uuid), potionEffect.name(), amount, operation));
		return this;
	}
	
	public CustomPotionEffect addLegacyAttributeModifier(Attribute attribute, String uuid, float amount, AttributeOperation operation) {
		if (legacyAttributeModifiers == null)
			legacyAttributeModifiers = new HashMap<>();
		legacyAttributeModifiers.put(attribute, new AttributeModifier(UUID.fromString(uuid), potionEffect.name(), amount, operation));
		return this;
	}
	
	public void applyUpdateEffect(LivingEntity entity, byte amplifier, boolean legacy) {
		if (potionEffect == PotionEffect.REGENERATION) {
			if (entity.getHealth() < entity.getMaxHealth()) {
				entity.setHealth(entity.getHealth() + 1);
			}
			return;
		} else if (potionEffect == PotionEffect.POISON) {
			if (entity.getHealth() > 1.0F) {
				entity.damage(CustomDamageType.MAGIC, 1.0F);
			}
			return;
		} else if (potionEffect == PotionEffect.WITHER) {
			entity.damage(CustomDamageType.WITHER, 1.0F);
			return;
		}
		
		if (entity instanceof Player player) {
			if (potionEffect == PotionEffect.HUNGER) {
				EntityUtils.addExhaustion(player, legacy ? 0.025F : 0.005F * (float) (amplifier + 1));
				return;
			} else if (potionEffect == PotionEffect.SATURATION) {
				if (player.isOnline()) HungerManager.add(player, amplifier + 1, 1.0F);
				return;
			}
		}
		
		if (potionEffect == PotionEffect.INSTANT_DAMAGE || potionEffect == PotionEffect.INSTANT_HEALTH) {
			EntityGroup entityGroup = EntityGroup.ofEntity(entity);
			
			if (shouldHeal(entityGroup)) {
				entity.setHealth(entity.getHealth() + (float) Math.max(4 << amplifier, 0));
			} else {
				entity.damage(CustomDamageType.MAGIC, (float) (6 << amplifier));
			}
		}
	}
	
	public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, byte amplifier, double proximity, boolean legacy) {
		EntityGroup targetGroup = EntityGroup.ofEntity(target);
		
		if (potionEffect != PotionEffect.INSTANT_DAMAGE && potionEffect != PotionEffect.INSTANT_HEALTH) {
			applyUpdateEffect(target, amplifier, legacy);
			return;
		}
		
		if (shouldHeal(targetGroup)) {
			int amount = (int) (proximity * (double) (4 << amplifier) + 0.5D);
			target.setHealth(target.getHealth() + (float) amount);
		} else {
			int amount = (int) (proximity * (double) (6 << amplifier) + 0.5D);
			if (source == null) {
				target.damage(CustomDamageType.MAGIC, (float) amount);
			} else {
				target.damage(CustomDamageType.indirectMagic(source, attacker), (float) amount);
			}
		}
	}
	
	private boolean shouldHeal(EntityGroup group) {
		return (group.isUndead() && potionEffect == PotionEffect.INSTANT_DAMAGE)
				|| (!group.isUndead() && potionEffect == PotionEffect.INSTANT_HEALTH);
	}
	
	public boolean canApplyUpdateEffect(int duration, byte amplifier) {
		int applyInterval;
		if (potionEffect == PotionEffect.REGENERATION) {
			applyInterval = 50 >> amplifier;
		} else if (potionEffect == PotionEffect.POISON) {
			applyInterval = 25 >> amplifier;
		} else if (potionEffect == PotionEffect.WITHER) {
			applyInterval = 40 >> amplifier;
		} else {
			return potionEffect == PotionEffect.HUNGER;
		}
		
		if (applyInterval > 0) {
			return duration % applyInterval == 0;
		} else {
			return true;
		}
	}
	
	public boolean isInstant() {
		return false;
	}
	
	public void onApplied(LivingEntity entity, byte amplifier, boolean legacy) {
		Map<Attribute, AttributeModifier> modifiers;
		if (legacy && legacyAttributeModifiers != null) {
			modifiers = legacyAttributeModifiers;
		} else {
			modifiers = attributeModifiers;
		}
		
		modifiers.forEach((attribute, modifier) -> {
			AttributeInstance instance = entity.getAttribute(attribute);
			instance.removeModifier(modifier);
			instance.addModifier(new AttributeModifier(modifier.getId(), potionEffect.name() + " " + amplifier, adjustModifierAmount(amplifier, modifier), modifier.getOperation()));
		});
	}
	
	public void onRemoved(LivingEntity entity, byte amplifier, boolean legacy) {
		Map<Attribute, AttributeModifier> modifiers;
		if (legacy && legacyAttributeModifiers != null) {
			modifiers = legacyAttributeModifiers;
		} else {
			modifiers = attributeModifiers;
		}
		
		modifiers.forEach((attribute, modifier) ->
				entity.getAttribute(attribute).removeModifier(modifier));
	}
	
	private double adjustModifierAmount(byte amplifier, AttributeModifier modifier) {
		return modifier.getAmount() * (amplifier + 1);
	}
}
