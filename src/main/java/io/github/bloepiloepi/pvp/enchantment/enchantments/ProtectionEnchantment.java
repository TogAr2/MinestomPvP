package io.github.bloepiloepi.pvp.enchantment.enchantments;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.enchantment.CustomEnchantment;
import io.github.bloepiloepi.pvp.enchantment.CustomEnchantments;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.Enchantment;

public class ProtectionEnchantment extends CustomEnchantment {
	private final Type type;
	
	public ProtectionEnchantment(Enchantment enchantment, Type type, EquipmentSlot... slotTypes) {
		super(enchantment, slotTypes);
		this.type = type;
	}
	
	@Override
	public int getProtectionAmount(short level, CustomDamageType damageType) {
		if (damageType.isOutOfWorld()) {
			return 0;
		} else if (type == Type.ALL) {
			return level;
		} else if (type == Type.FIRE && damageType.isFire()) {
			return level * 2;
		} else if (type == Type.FALL && damageType.isFall()) {
			return level * 3;
		} else if (type == Type.EXPLOSION && damageType.isExplosive()) {
			return level * 2;
		} else {
			return type == Type.PROJECTILE && damageType.isProjectile() ? level * 2 : 0;
		}
	}
	
	public static int transformFireDuration(LivingEntity entity, int duration) {
		short level = EnchantmentUtils.getEquipmentLevel(CustomEnchantments.get(Enchantment.FIRE_PROTECTION), entity);
		if (level > 0) {
			duration -= Math.floor((float) duration * (float) level * 0.15F);
		}
		
		return duration;
	}
	
	public enum Type {
		ALL, FIRE, FALL, EXPLOSION, PROJECTILE
	}
}
