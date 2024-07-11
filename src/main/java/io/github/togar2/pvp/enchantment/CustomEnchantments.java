package io.github.togar2.pvp.enchantment;

import io.github.togar2.pvp.enchantment.enchantments.DamageEnchantment;
import io.github.togar2.pvp.enchantment.enchantments.ImpalingEnchantment;
import io.github.togar2.pvp.enchantment.enchantments.ProtectionEnchantment;
import io.github.togar2.pvp.enchantment.enchantments.ThornsEnchantment;
import io.github.togar2.pvp.feature.FeatureType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomEnchantments {
	private static final Map<DynamicRegistry.Key<Enchantment>, PvPEnchantment> ENCHANTMENTS = new HashMap<>();
	
	public static PvPEnchantment get(DynamicRegistry.Key<Enchantment> enchantment) {
		return ENCHANTMENTS.get(enchantment);
	}
	
	public static void register(PvPEnchantment... enchantments) {
		for (PvPEnchantment enchantment : enchantments) {
			ENCHANTMENTS.put(enchantment.getEnchantment(), enchantment);
		}
	}
	
	public static FeatureType<?>[] getAllFeatureDependencies() {
		Set<FeatureType<?>> features = new HashSet<>();
		
		for (PvPEnchantment enchantment : ENCHANTMENTS.values()) {
			features.addAll(enchantment.getDependencies());
		}
		
		return features.toArray(FeatureType[]::new);
	}
	
	private static boolean registered = false;
	
	static {
		registerAll();
	}
	
	public static void registerAll() {
		if (registered) return;
		registered = true;
		
		EquipmentSlot[] ALL_ARMOR_SLOTS = EquipmentSlot.armors().toArray(EquipmentSlot[]::new);
		
		register(
				new ProtectionEnchantment(Enchantment.PROTECTION, ProtectionEnchantment.Type.ALL, ALL_ARMOR_SLOTS),
				new ProtectionEnchantment(Enchantment.FIRE_PROTECTION, ProtectionEnchantment.Type.FIRE, ALL_ARMOR_SLOTS),
				new ProtectionEnchantment(Enchantment.FEATHER_FALLING, ProtectionEnchantment.Type.FALL, ALL_ARMOR_SLOTS),
				new ProtectionEnchantment(Enchantment.BLAST_PROTECTION, ProtectionEnchantment.Type.EXPLOSION, ALL_ARMOR_SLOTS),
				new ProtectionEnchantment(Enchantment.PROJECTILE_PROTECTION, ProtectionEnchantment.Type.PROJECTILE, ALL_ARMOR_SLOTS),
				new PvPEnchantment(Enchantment.RESPIRATION, ALL_ARMOR_SLOTS),
				new PvPEnchantment(Enchantment.AQUA_AFFINITY, ALL_ARMOR_SLOTS),
				new ThornsEnchantment(ALL_ARMOR_SLOTS),
				new PvPEnchantment(Enchantment.DEPTH_STRIDER, ALL_ARMOR_SLOTS),
				new PvPEnchantment(Enchantment.FROST_WALKER, EquipmentSlot.BOOTS),
				new PvPEnchantment(Enchantment.BINDING_CURSE, ALL_ARMOR_SLOTS),
				new PvPEnchantment(Enchantment.SOUL_SPEED, EquipmentSlot.BOOTS),
				new DamageEnchantment(Enchantment.SHARPNESS, DamageEnchantment.Type.ALL, EquipmentSlot.MAIN_HAND),
				new DamageEnchantment(Enchantment.SMITE, DamageEnchantment.Type.UNDEAD, EquipmentSlot.MAIN_HAND),
				new DamageEnchantment(Enchantment.BANE_OF_ARTHROPODS, DamageEnchantment.Type.ARTHROPODS, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.KNOCKBACK, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.FIRE_ASPECT, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.LOOTING, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.SWEEPING_EDGE, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.EFFICIENCY, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.SILK_TOUCH, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.UNBREAKING, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.FORTUNE, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.POWER, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.PUNCH, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.FLAME, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.INFINITY, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.LUCK_OF_THE_SEA, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.LURE, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.LOYALTY, EquipmentSlot.MAIN_HAND),
				new ImpalingEnchantment(EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.RIPTIDE, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.CHANNELING, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.MULTISHOT, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.QUICK_CHARGE, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.PIERCING, EquipmentSlot.MAIN_HAND),
				new PvPEnchantment(Enchantment.MENDING, EquipmentSlot.values()),
				new PvPEnchantment(Enchantment.VANISHING_CURSE, EquipmentSlot.values())
		);
	}
}
