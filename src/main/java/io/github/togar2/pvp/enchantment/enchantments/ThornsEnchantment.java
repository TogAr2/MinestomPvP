package io.github.togar2.pvp.enchantment.enchantments;

import io.github.togar2.pvp.enchantment.CustomEnchantment;
import io.github.togar2.pvp.enchantment.EnchantmentUtils;
import io.github.togar2.pvp.utils.ItemUtils;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.enchant.Enchantment;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ThornsEnchantment extends CustomEnchantment {
	public ThornsEnchantment(EquipmentSlot... slotTypes) {
		super(Enchantment.THORNS, slotTypes);
	}
	
	@Override
	public void onUserDamaged(LivingEntity user, LivingEntity attacker, int level) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		if (!shouldDamageAttacker(level, random)) return;
		
		Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentUtils.pickRandom(user, this);
		
		if (attacker != null) {
			attacker.damage(new Damage(DamageType.THORNS, user, user, null, getDamageAmount(level, random)));
		}
		
		if (entry != null) {
			ItemUtils.damageEquipment(user, entry.getKey(), 2);
		}
	}
	
	private static boolean shouldDamageAttacker(int level, ThreadLocalRandom random) {
		if (level <= 0) return false;
		return random.nextFloat() < 0.15f * level;
	}
	
	private static int getDamageAmount(int level, ThreadLocalRandom random) {
		return level > 10 ? level - 10 : 1 + random.nextInt(4);
	}
}
