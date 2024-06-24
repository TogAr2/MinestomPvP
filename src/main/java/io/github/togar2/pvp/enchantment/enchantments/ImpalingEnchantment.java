package io.github.togar2.pvp.enchantment.enchantments;

import io.github.togar2.pvp.enchantment.CustomEnchantment;
import io.github.togar2.pvp.entity.EntityGroup;
import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.enchant.Enchantment;

public class ImpalingEnchantment extends CustomEnchantment {
	public ImpalingEnchantment(EquipmentSlot... slotTypes) {
		super(Enchantment.IMPALING, slotTypes);
	}
	
	@Override
	public float getAttackDamage(int level, EntityGroup group, CombatVersion version) {
		return group == EntityGroup.AQUATIC ? (float) level * 2.5F : 0.0F;
	}
}
