package io.github.togar2.pvp.feature.enchantment;

import io.github.togar2.pvp.entity.EntityGroup;
import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;

import java.util.Map;

public interface EnchantmentFeature extends CombatFeature {
	EnchantmentFeature NO_OP = new EnchantmentFeature() {
		@Override
		public int getEquipmentLevel(LivingEntity entity, DynamicRegistry.Key<Enchantment> enchantment) {
			return 0;
		}
		
		@Override
		public Map.Entry<EquipmentSlot, ItemStack> pickRandom(LivingEntity entity, DynamicRegistry.Key<Enchantment> enchantment) {
			return null;
		}
		
		@Override
		public int getProtectionAmount(LivingEntity entity, DamageType damageType) {
			return 0;
		}
		
		@Override
		public float getAttackDamage(ItemStack stack, EntityGroup group) {
			return 0;
		}
		
		@Override
		public double getExplosionKnockback(LivingEntity entity, double strength) {
			return 0;
		}
		
		@Override
		public int getKnockback(LivingEntity entity) {
			return 0;
		}
		
		@Override
		public int getSweeping(LivingEntity entity) {
			return 0;
		}
		
		@Override
		public int getFireAspect(LivingEntity entity) {
			return 0;
		}
		
		@Override
		public boolean shouldUnbreakingPreventDamage(ItemStack stack) {
			return false;
		}
		
		@Override
		public void onUserDamaged(LivingEntity user, LivingEntity attacker) {}
		
		@Override
		public void onTargetDamaged(LivingEntity user, Entity target) {}
	};
	
	int getEquipmentLevel(LivingEntity entity, DynamicRegistry.Key<Enchantment> enchantment);
	
	Map.Entry<EquipmentSlot, ItemStack> pickRandom(LivingEntity entity, DynamicRegistry.Key<Enchantment> enchantment);
	
	int getProtectionAmount(LivingEntity entity, DamageType damageType);
	
	float getAttackDamage(ItemStack stack, EntityGroup group);
	
	double getExplosionKnockback(LivingEntity entity, double strength);
	
	int getKnockback(LivingEntity entity);
	
	int getSweeping(LivingEntity entity);
	
	int getFireAspect(LivingEntity entity);
	
	boolean shouldUnbreakingPreventDamage(ItemStack stack);
	
	void onUserDamaged(LivingEntity user, LivingEntity attacker);
	
	void onTargetDamaged(LivingEntity user, Entity target);
}
