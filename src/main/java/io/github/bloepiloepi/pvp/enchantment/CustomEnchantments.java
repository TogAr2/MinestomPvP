package io.github.bloepiloepi.pvp.enchantment;

import io.github.bloepiloepi.pvp.enchantment.enchantments.DamageEnchantment;
import io.github.bloepiloepi.pvp.enchantment.enchantments.ImpalingEnchantment;
import io.github.bloepiloepi.pvp.enchantment.enchantments.ProtectionEnchantment;
import io.github.bloepiloepi.pvp.enchantment.enchantments.ThornsEnchantment;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.Enchantment;

import java.util.HashMap;
import java.util.Map;

public class CustomEnchantments {
	private static final Map<Enchantment, CustomEnchantment> ENCHANTMENTS = new HashMap<>();
	
	public static CustomEnchantment get(Enchantment enchantment) {
		return ENCHANTMENTS.get(enchantment);
	}
	
	public static void register(CustomEnchantment... enchantments) {
		for (CustomEnchantment enchantment : enchantments) {
			ENCHANTMENTS.put(enchantment.getEnchantment(), enchantment);
		}
	}
	
	public static void registerAll() {
		EquipmentSlot[] ALL_ARMOR_SLOTS = new EquipmentSlot[] {
				EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE,
				EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS
		};
		
		register(
				new ProtectionEnchantment(Enchantment.PROTECTION, ProtectionEnchantment.Type.ALL, ALL_ARMOR_SLOTS),
				new ProtectionEnchantment(Enchantment.FIRE_PROTECTION, ProtectionEnchantment.Type.FIRE, ALL_ARMOR_SLOTS),
				new ProtectionEnchantment(Enchantment.FEATHER_FALLING, ProtectionEnchantment.Type.FALL, ALL_ARMOR_SLOTS),
				new ProtectionEnchantment(Enchantment.BLAST_PROTECTION, ProtectionEnchantment.Type.EXPLOSION, ALL_ARMOR_SLOTS),
				new ProtectionEnchantment(Enchantment.PROJECTILE_PROTECTION, ProtectionEnchantment.Type.PROJECTILE, ALL_ARMOR_SLOTS),
				new CustomEnchantment(Enchantment.RESPIRATION, ALL_ARMOR_SLOTS),
				new CustomEnchantment(Enchantment.AQUA_AFFINITY, ALL_ARMOR_SLOTS),
				new ThornsEnchantment(ALL_ARMOR_SLOTS),
				new CustomEnchantment(Enchantment.DEPTH_STRIDER, ALL_ARMOR_SLOTS),
				new CustomEnchantment(Enchantment.FROST_WALKER, EquipmentSlot.BOOTS),
				new CustomEnchantment(Enchantment.BINDING_CURSE, ALL_ARMOR_SLOTS),
				new CustomEnchantment(Enchantment.SOUL_SPEED, EquipmentSlot.BOOTS),
				new DamageEnchantment(Enchantment.SHARPNESS, DamageEnchantment.Type.ALL, EquipmentSlot.MAIN_HAND),
				new DamageEnchantment(Enchantment.SMITE, DamageEnchantment.Type.UNDEAD, EquipmentSlot.MAIN_HAND),
				new DamageEnchantment(Enchantment.BANE_OF_ARTHROPODS, DamageEnchantment.Type.ARTHROPODS, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.KNOCKBACK, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.FIRE_ASPECT, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.LOOTING, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.SWEEPING, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.EFFICIENCY, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.SILK_TOUCH, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.UNBREAKING, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.FORTUNE, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.POWER, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.PUNCH, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.FLAME, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.INFINITY, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.LUCK_OF_THE_SEA, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.LURE, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.LOYALTY, EquipmentSlot.MAIN_HAND),
				new ImpalingEnchantment(EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.RIPTIDE, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.CHANNELING, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.MULTISHOT, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.QUICK_CHARGE, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.PIERCING, EquipmentSlot.MAIN_HAND),
				new CustomEnchantment(Enchantment.MENDING, EquipmentSlot.values()),
				new CustomEnchantment(Enchantment.VANISHING_CURSE, EquipmentSlot.values())
		);
	}
}
