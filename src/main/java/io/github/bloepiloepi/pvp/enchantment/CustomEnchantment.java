package io.github.bloepiloepi.pvp.enchantment;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entity.EntityGroup;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CustomEnchantment {
	private final Enchantment enchantment;
	private final EquipmentSlot[] slotTypes;
	
	public CustomEnchantment(Enchantment enchantment, EquipmentSlot... slotTypes) {
		this.enchantment = enchantment;
		this.slotTypes = slotTypes;
	}
	
	public Enchantment getEnchantment() {
		return enchantment;
	}
	
	public Map<EquipmentSlot, ItemStack> getEquipment(LivingEntity entity) {
		Map<EquipmentSlot, ItemStack> map = new HashMap<>();
		
		for (EquipmentSlot slot : this.slotTypes) {
			ItemStack itemStack = entity.getEquipment(slot);
			if (!itemStack.isAir()) {
				map.put(slot, itemStack);
			}
		}
		
		return map;
	}
	
	public int getProtectionAmount(short level, CustomDamageType damageType) {
		return 0;
	}
	
	public float getAttackDamage(short level, EntityGroup group, boolean legacy) {
		return 0.0F;
	}
	
	public void onTargetDamaged(LivingEntity user, Entity target, int level) {}
	public void onUserDamaged(LivingEntity user, LivingEntity attacker, int level) {}
}
