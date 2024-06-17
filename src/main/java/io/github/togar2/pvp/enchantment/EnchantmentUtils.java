package io.github.togar2.pvp.enchantment;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.entity.EntityGroup;
import io.github.togar2.pvp.enums.ArmorMaterial;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.ItemComponent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.utils.MathUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class EnchantmentUtils {
	public static int getLevel(DynamicRegistry.Key<Enchantment> enchantment, ItemStack stack) {
		Map<DynamicRegistry.Key<Enchantment>, Integer> enchantmentMap = stack.get(ItemComponent.ENCHANTMENTS).enchantments();
		return enchantmentMap.containsKey(enchantment)
				? MathUtils.clamp(enchantmentMap.get(enchantment), 0, 255)
				: 0;
	}
	
	public static int getEquipmentLevel(CustomEnchantment customEnchantment, LivingEntity entity) {
		if (customEnchantment == null) return 0;
		
		Iterator<ItemStack> iterator = customEnchantment.getEquipment(entity).values().iterator();
		
		int highest = 0;
		while (iterator.hasNext()) {
			ItemStack itemStack = iterator.next();
			int level = getLevel(customEnchantment.getEnchantment(), itemStack);
			if (level > highest) {
				highest = level;
			}
		}
		
		return highest;
	}
	
	public static Map.Entry<EquipmentSlot, ItemStack> pickRandom(LivingEntity entity, CustomEnchantment enchantment) {
		Map<EquipmentSlot, ItemStack> equipmentMap = enchantment.getEquipment(entity);
		if (equipmentMap.isEmpty()) return null;
		
		List<Map.Entry<EquipmentSlot, ItemStack>> possibleStacks = new ArrayList<>();
		
		for (Map.Entry<EquipmentSlot, ItemStack> entry : equipmentMap.entrySet()) {
			ItemStack itemStack = entry.getValue();
			
			if (!itemStack.isAir() && getLevel(enchantment.getEnchantment(), itemStack) > 0) {
				possibleStacks.add(entry);
			}
		}
		
		return possibleStacks.isEmpty() ? null :
				possibleStacks.get(ThreadLocalRandom.current().nextInt(possibleStacks.size()));
	}
	
	public static void forEachEnchantment(Iterable<ItemStack> stacks, BiConsumer<CustomEnchantment, Integer> consumer) {
		for (ItemStack itemStack : stacks) {
			Set<DynamicRegistry.Key<Enchantment>> enchantments = itemStack.get(ItemComponent.ENCHANTMENTS).enchantments().keySet();
			
			for (DynamicRegistry.Key<Enchantment> enchantment : enchantments) {
				CustomEnchantment customEnchantment = CustomEnchantments.get(enchantment);
				consumer.accept(customEnchantment, itemStack.get(ItemComponent.ENCHANTMENTS).level(enchantment));
			}
		}
	}
	
	public static int getProtectionAmount(Iterable<ItemStack> equipment, DamageTypeInfo typeInfo) {
		AtomicInteger result = new AtomicInteger();
		forEachEnchantment(equipment, (enchantment, level) -> result.addAndGet(enchantment.getProtectionAmount(level, typeInfo)));
		return result.get();
	}
	
	public static float getAttackDamage(ItemStack stack, EntityGroup group, boolean legacy) {
		AtomicReference<Float> result = new AtomicReference<>((float) 0);
		stack.get(ItemComponent.ENCHANTMENTS).enchantments().forEach((enchantment, level) -> {
			CustomEnchantment customEnchantment = CustomEnchantments.get(enchantment);
			result.updateAndGet(v -> v + customEnchantment.getAttackDamage(level, group, legacy));
		});
		
		return result.get();
	}
	
	public static boolean shouldNotBreak(ItemStack item, int unbreakingLevel) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		if (ArmorMaterial.fromMaterial(item.material()) != null && random.nextFloat() < 0.6f) {
			return false;
		} else {
			return random.nextInt(unbreakingLevel + 1) > 0;
		}
	}
	
	public static double getExplosionKnockback(LivingEntity entity, double strength) {
		int level = getEquipmentLevel(CustomEnchantments.get(Enchantment.BLAST_PROTECTION), entity);
		if (level > 0) {
			strength -= Math.floor((strength * (double) (level * 0.15f)));
		}
		
		return strength;
	}
	
	public static void onUserDamaged(LivingEntity user, LivingEntity attacker) {
		if (user != null) {
			forEachEnchantment(Arrays.asList(
					user.getBoots(), user.getLeggings(),
					user.getChestplate(), user.getHelmet(),
					user.getItemInMainHand(), user.getItemInOffHand()
			), (enchantment, level) -> enchantment.onUserDamaged(user, attacker, level));
		}
	}
	
	public static void onTargetDamaged(LivingEntity user, Entity target) {
		if (user != null) {
			forEachEnchantment(Arrays.asList(
					user.getBoots(), user.getLeggings(),
					user.getChestplate(), user.getHelmet(),
					user.getItemInMainHand(), user.getItemInOffHand()
			), (enchantment, level) -> enchantment.onTargetDamaged(user, target, level));
		}
	}
	
	public static int getKnockback(LivingEntity entity) {
		return getEquipmentLevel(CustomEnchantments.get(Enchantment.KNOCKBACK), entity);
	}
	
	public static int getSweeping(LivingEntity entity) {
		return getEquipmentLevel(CustomEnchantments.get(Enchantment.SWEEPING_EDGE), entity);
	}
	
	public static int getFireAspect(LivingEntity entity) {
		return getEquipmentLevel(CustomEnchantments.get(Enchantment.FIRE_ASPECT), entity);
	}
	
	public static int getBlockEfficiency(LivingEntity entity) {
		return getEquipmentLevel(CustomEnchantments.get(Enchantment.EFFICIENCY), entity);
	}
}
