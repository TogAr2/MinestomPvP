package io.github.togar2.pvp.enchantment;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.entity.EntityGroup;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;

import java.util.HashMap;
import java.util.Map;

public class CustomEnchantment {
	private final DynamicRegistry.Key<Enchantment> enchantment;
	private final EquipmentSlot[] slotTypes;
	
	public CustomEnchantment(DynamicRegistry.Key<Enchantment> enchantment, EquipmentSlot... slotTypes) {
		this.enchantment = enchantment;
		this.slotTypes = slotTypes;
	}
	
	public DynamicRegistry.Key<Enchantment> getEnchantment() {
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
	
	public int getProtectionAmount(int level, DamageTypeInfo typeInfo) {
		return 0;
	}
	
	public float getAttackDamage(int level, EntityGroup group, boolean legacy) {
		return 0.0F;
	}
	
	public void onTargetDamaged(LivingEntity user, Entity target, int level) {}
	public void onUserDamaged(LivingEntity user, LivingEntity attacker, int level) {}
}
