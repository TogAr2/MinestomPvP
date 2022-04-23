package io.github.bloepiloepi.pvp.utils;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.enums.ArmorMaterial;
import io.github.bloepiloepi.pvp.events.EquipmentDamageEvent;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;

import java.util.function.Consumer;

public class ItemUtils {
	
	public static ItemStack damage(ItemStack stack, int amount) {
		if (amount == 0 || stack.material().registry().maxDamage() <= 0)
			return stack;
		
		return stack.withMeta(meta -> {
			int unbreaking = EnchantmentUtils.getLevel(Enchantment.UNBREAKING, stack);
			int preventAmount = 0;
			int newAmount = amount;
			
			if (unbreaking > 0) {
				for (int i = 0; i < newAmount; i++) {
					if (EnchantmentUtils.shouldPreventStackWithUnbreakingDamage(stack, unbreaking)) {
						preventAmount++;
					}
				}
			}
			
			newAmount -= preventAmount;
			if (newAmount <= 0) return;
			
			meta.damage(stack.meta().getDamage() + newAmount);
		});
	}
	
	public static <T extends LivingEntity> ItemStack damage(ItemStack stack, int amount,
	                                                        T entity, Consumer<T> breakCallback) {
		if (amount == 0 || stack.material().registry().maxDamage() <= 0)
			return stack;
		
		ItemStack newStack = damage(stack, amount);
		if (newStack.meta().getDamage() >= stack.material().registry().maxDamage()) {
			breakCallback.accept(entity);
			newStack = newStack.withAmount(i -> i - 1).withMeta(meta -> meta.damage(0));
		}
		
		return newStack;
	}
	
	public static void damageEquipment(LivingEntity entity, EquipmentSlot slot, int amount) {
		EquipmentDamageEvent equipmentDamageEvent = new EquipmentDamageEvent(entity, slot, amount);
		EventDispatcher.callCancellable(equipmentDamageEvent, () ->
				entity.setEquipment(slot, damage(entity.getEquipment(slot), amount, entity,
						e -> triggerEquipmentBreak(e, slot))));
	}
	
	public static void damageArmor(LivingEntity entity, CustomDamageType damageType,
	                               float damage, EquipmentSlot... slots) {
		if (damage <= 0) return;
		
		damage /= 4;
		if (damage < 1) {
			damage = 1;
		}
		
		for (EquipmentSlot slot : slots) {
			ItemStack stack = entity.getEquipment(slot);
			if ((!damageType.isFire()
					|| !stack.material().namespace().value().toLowerCase().contains("netherite"))
					&& ArmorMaterial.fromMaterial(stack.material()) != null) {
				damageEquipment(entity, slot, (int) damage);
			}
		}
	}
	
	public static void triggerEquipmentBreak(LivingEntity entity, EquipmentSlot slot) {
		entity.triggerStatus(getEquipmentBreakStatus(slot));
	}
	
	private static byte getEquipmentBreakStatus(EquipmentSlot slot) {
		return switch (slot) {
			case OFF_HAND -> (byte) 48;
			case HELMET -> (byte) 49;
			case CHESTPLATE -> (byte) 50;
			case LEGGINGS -> (byte) 51;
			case BOOTS -> (byte) 52;
			default -> (byte) 47;
		};
	}
}
