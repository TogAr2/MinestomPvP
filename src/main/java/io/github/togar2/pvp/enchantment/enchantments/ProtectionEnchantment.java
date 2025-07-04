package io.github.togar2.pvp.enchantment.enchantments;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.enchantment.CombatEnchantment;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.enchantment.EnchantmentFeature;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;

public class ProtectionEnchantment extends CombatEnchantment {
	private final Type type;
	
	public ProtectionEnchantment(RegistryKey<Enchantment> enchantment, Type type, EquipmentSlot... slotTypes) {
		super(enchantment, slotTypes);
		this.type = type;
	}
	
	@Override
	public int getProtectionAmount(int level, DamageType damageType,
	                               EnchantmentFeature feature, FeatureConfiguration configuration) {
		DamageTypeInfo damageTypeInfo = DamageTypeInfo.of(MinecraftServer.getDamageTypeRegistry().getKey(damageType));
		if (damageTypeInfo.outOfWorld()) {
			return 0;
		} else if (type == Type.ALL) {
			return level;
		} else if (type == Type.FIRE && damageTypeInfo.fire()) {
			return level * 2;
		} else if (type == Type.FALL && damageTypeInfo.fall()) {
			return level * 3;
		} else if (type == Type.EXPLOSION && damageTypeInfo.explosive()) {
			return level * 2;
		} else {
			return type == Type.PROJECTILE && damageTypeInfo.projectile() ? level * 2 : 0;
		}
	}
	
	public enum Type {
		ALL, FIRE, FALL, EXPLOSION, PROJECTILE
	}
}
