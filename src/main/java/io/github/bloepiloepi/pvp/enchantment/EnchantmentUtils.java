package io.github.bloepiloepi.pvp.enchantment;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.entity.EntityGroup;
import io.github.bloepiloepi.pvp.enums.ArmorMaterial;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class EnchantmentUtils {
	
	public static short getLevel(Enchantment enchantment, ItemStack stack) {
		if (stack.isAir()) return 0;
		Map<Enchantment, Short> enchantmentMap = stack.meta().getEnchantmentMap();
		if (enchantmentMap.containsKey(enchantment)) return (short) MathUtils.clamp(enchantmentMap.get(enchantment), 0, 255);
		return 0;
	}
	
	public static short getEquipmentLevel(CustomEnchantment customEnchantment, LivingEntity entity) {
		if (customEnchantment == null) return 0;
		
		Iterator<ItemStack> iterator = customEnchantment.getEquipment(entity).values().iterator();
		
		short highest = 0;
		while (iterator.hasNext()) {
			ItemStack itemStack = iterator.next();
			short level = getLevel(customEnchantment.getEnchantment(), itemStack);
			if (level > highest) {
				highest = level;
			}
		}
		
		return highest;
	}
	
	@Nullable
	public static Map.Entry<EquipmentSlot, ItemStack> chooseEquipmentWith(CustomEnchantment enchantment, LivingEntity entity,
	                                                                      Predicate<ItemStack> condition) {
		Map<EquipmentSlot, ItemStack> equipmentMap = enchantment.getEquipment(entity);
		if (equipmentMap.isEmpty()) {
			return null;
		}
		
		List<Map.Entry<EquipmentSlot, ItemStack>> possibleStacks = new ArrayList<>();
		
		for (Map.Entry<EquipmentSlot, ItemStack> entry : equipmentMap.entrySet()) {
			ItemStack itemStack = entry.getValue();
			
			if (!itemStack.isAir() && getLevel(enchantment.getEnchantment(), itemStack) > 0 && condition.test(itemStack)) {
				possibleStacks.add(entry);
			}
		}
		
		return possibleStacks.isEmpty() ? null :
				possibleStacks.get(ThreadLocalRandom.current().nextInt(possibleStacks.size()));
	}
	
	private static void forEachEnchantment(BiConsumer<CustomEnchantment, Short> consumer, ItemStack stack) {
		if (!stack.isAir()) {
			Set<Enchantment> enchantments = stack.meta().getEnchantmentMap().keySet();
			
			for(Enchantment enchantment : enchantments) {
				CustomEnchantment customEnchantment = CustomEnchantments.get(enchantment);
				consumer.accept(customEnchantment, stack.meta().getEnchantmentMap().get(enchantment));
			}
		}
	}
	
	public static void forEachEnchantment(BiConsumer<CustomEnchantment, Short> consumer, Iterable<ItemStack> stacks) {
		for (ItemStack itemStack : stacks) {
			forEachEnchantment(consumer, itemStack);
		}
	}
	
	public static int getProtectionAmount(Iterable<ItemStack> equipment, CustomDamageType type) {
		AtomicInteger result = new AtomicInteger();
		forEachEnchantment((enchantment, level) -> result.addAndGet(enchantment.getProtectionAmount(level, type)), equipment);
		return result.get();
	}
	
	public static float getAttackDamage(ItemStack stack, EntityGroup group, boolean legacy) {
		AtomicReference<Float> result = new AtomicReference<>((float) 0);
		forEachEnchantment((enchantment, level) -> result.updateAndGet(v -> v + enchantment.getAttackDamage(level, group, legacy)), stack);
		return result.get();
	}
	
	public static boolean shouldPreventStackWithUnbreakingDamage(ItemStack item, int unbreakingLevel) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		if (ArmorMaterial.fromMaterial(item.material()) != null && random.nextFloat() < 0.6F) {
			return false;
		} else {
			return random.nextInt(unbreakingLevel + 1) > 0;
		}
	}
	
	public static double getExplosionKnockback(LivingEntity entity, double strength) {
		short level = getEquipmentLevel(CustomEnchantments.get(Enchantment.BLAST_PROTECTION), entity);
		if (level > 0) {
			strength -= Math.floor((strength * (double) (level * 0.15F)));
		}
		
		return strength;
	}
	
	public static void onUserDamaged(LivingEntity user, LivingEntity attacker) {
		if (user != null) {
			forEachEnchantment((enchantment, level) -> enchantment.onUserDamaged(user, attacker, level),
					Arrays.asList(user.getBoots(), user.getLeggings(),
							user.getChestplate(), user.getHelmet(),
							user.getItemInMainHand(), user.getItemInOffHand()));
		}
	}
	
	public static void onTargetDamaged(LivingEntity user, Entity target) {
		if (user != null) {
			forEachEnchantment((enchantment, level) -> enchantment.onTargetDamaged(user, target, level),
					Arrays.asList(user.getBoots(), user.getLeggings(),
							user.getChestplate(), user.getHelmet(),
							user.getItemInMainHand(), user.getItemInOffHand()));
		}
	}
	
	public static short getKnockback(LivingEntity entity) {
		return getEquipmentLevel(CustomEnchantments.get(Enchantment.KNOCKBACK), entity);
	}
	
	public static short getSweeping(LivingEntity entity) {
		return getEquipmentLevel(CustomEnchantments.get(Enchantment.SWEEPING), entity);
	}
	
	public static short getFireAspect(LivingEntity entity) {
		return getEquipmentLevel(CustomEnchantments.get(Enchantment.FIRE_ASPECT), entity);
	}
	
	public static short getBlockEfficiency(LivingEntity entity) {
		return getEquipmentLevel(CustomEnchantments.get(Enchantment.EFFICIENCY), entity);
	}
}
