package io.github.bloepiloepi.pvp.enchantment.enchantments;

import io.github.bloepiloepi.pvp.enchantment.CustomEnchantment;
import io.github.bloepiloepi.pvp.entity.EntityGroup;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.Enchantment;

public class ImpalingEnchantment extends CustomEnchantment {
	public ImpalingEnchantment(EquipmentSlot... slotTypes) {
		super(Enchantment.IMPALING, slotTypes);
	}
	
	@Override
	public float getAttackDamage(short level, EntityGroup group, boolean legacy) {
		return group == EntityGroup.AQUATIC ? (float) level * 2.5F : 0.0F;
	}
}
