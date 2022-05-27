package io.github.bloepiloepi.pvp.enchantment.enchantments;

import io.github.bloepiloepi.pvp.enchantment.CustomEnchantment;
import io.github.bloepiloepi.pvp.entity.EntityGroup;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.item.Enchantment;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;

import java.util.concurrent.ThreadLocalRandom;

public class DamageEnchantment extends CustomEnchantment {
	private final Type type;
	
	public DamageEnchantment(Enchantment enchantment, Type type, EquipmentSlot... slotTypes) {
		super(enchantment, slotTypes);
		this.type = type;
	}
	
	@Override
	public float getAttackDamage(short level, EntityGroup group, boolean legacy) {
		if (type == Type.ALL) {
			if (legacy) return level * 1.25F;
			return 1.0F + (float) Math.max(0, level - 1) * 0.5F;
		} else if (type == Type.UNDEAD && group == EntityGroup.UNDEAD) {
			return (float) level * 2.5F;
		} else {
			return type == Type.ARTHROPODS && group == EntityGroup.ARTHROPOD ? (float) level * 2.5F : 0.0F;
		}
	}
	
	@Override
	public void onTargetDamaged(LivingEntity user, Entity target, int level) {
		if (target instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) target;
			if (type == Type.ARTHROPODS && EntityGroup.ofEntity(livingEntity) == EntityGroup.ARTHROPOD) {
				int i = 20 + ThreadLocalRandom.current().nextInt(10 * level);
				livingEntity.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 3, i, PotionListener.defaultFlags()));
			}
		}
	}
	
	public enum Type {
		ALL, UNDEAD, ARTHROPODS
	}
}
