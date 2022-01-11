package io.github.bloepiloepi.pvp.enchantment.enchantments;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.enchantment.CustomEnchantment;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.utils.ItemUtils;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;

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
		
		Map.Entry<EquipmentSlot, ItemStack> entry =
				EnchantmentUtils.chooseEquipmentWith(this, user, (stack) -> true);
		
		if (attacker != null) {
			attacker.damage(CustomDamageType.thorns(user), getDamageAmount(level, random));
		}
		
		if (entry != null) {
			ItemUtils.damageEquipment(user, entry.getKey(), 2);
		}
	}
	
	private static boolean shouldDamageAttacker(int level, ThreadLocalRandom random) {
		if (level <= 0) return false;
		return random.nextFloat() < 0.15F * level;
	}
	
	private static int getDamageAmount(int level, ThreadLocalRandom random) {
		return level > 10 ? level - 10 : 1 + random.nextInt(4);
	}
}
