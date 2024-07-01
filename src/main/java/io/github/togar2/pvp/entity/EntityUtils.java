package io.github.togar2.pvp.entity;

import io.github.togar2.pvp.projectile.Arrow;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.time.TimeUnit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class EntityUtils {
	public static Iterable<ItemStack> getArmorItems(LivingEntity entity) {
		List<ItemStack> list = new ArrayList<>();
		for (EquipmentSlot slot : EquipmentSlot.armors()) {
			if (slot.isArmor() && !entity.getEquipment(slot).isAir()) {
				list.add(entity.getEquipment(slot));
			}
		}
		
		return list;
	}
	
	public static void spawnItemAtLocation(Entity entity, ItemStack itemStack, double up) {
		if (itemStack.isAir()) return;
		
		ItemEntity item = new ItemEntity(itemStack);
		item.setPickupDelay(10, TimeUnit.SERVER_TICK); // Default 0.5 seconds
		item.setInstance(Objects.requireNonNull(entity.getInstance()), entity.getPosition().add(0, up, 0));
	}
	
	public static Pair<ItemStack, Integer> getProjectile(Player player, Predicate<ItemStack> predicate) {
		return getProjectile(player, predicate, predicate);
	}
	
	public static Pair<ItemStack, Integer> getProjectile(Player player, Predicate<ItemStack> heldSupportedPredicate,
	                                      Predicate<ItemStack> allSupportedPredicate) {
		Pair<ItemStack, Integer> held = getHeldItem(player, heldSupportedPredicate);
		if (!held.first().isAir()) return held;
		
		ItemStack[] itemStacks = player.getInventory().getItemStacks();
		for (int i = 0; i < itemStacks.length; i++) {
			ItemStack stack = itemStacks[i];
			if (stack == null || stack.isAir()) continue;
			if (allSupportedPredicate.test(stack)) return Pair.of(stack, i);
		}
		
		return player.getGameMode() == GameMode.CREATIVE ? Pair.of(Arrow.DEFAULT_ARROW, -1) : Pair.of(ItemStack.AIR, -1);
	}
	
	private static Pair<ItemStack, Integer> getHeldItem(Player player, Predicate<ItemStack> predicate) {
		ItemStack stack = player.getItemInHand(Player.Hand.OFF);
		if (predicate.test(stack)) return Pair.of(stack, PlayerInventoryUtils.OFFHAND_SLOT);
		
		stack = player.getItemInHand(Player.Hand.MAIN);
		if (predicate.test(stack)) return Pair.of(stack, (int) player.getHeldSlot());
		
		return Pair.of(ItemStack.AIR, -1);
	}
	
	public static Component getName(Entity entity) {
		HoverEvent<HoverEvent.ShowEntity> hoverEvent = HoverEvent.showEntity(entity.getEntityType().key(), entity.getUuid());
		if (entity instanceof Player) {
			return ((Player) entity).getName().hoverEvent(hoverEvent);
		} else {
			// Use entity type without underscores and starting with capital letter
			String name = entity.getEntityType().namespace().value().replace('_', ' ');
			name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
			return Component.text(name).hoverEvent(hoverEvent);
		}
	}
	
	public static void setLastDamage(LivingEntity livingEntity, Damage lastDamage) {
		// Use reflection to set lastDamage field
		try {
			Field field = LivingEntity.class.getDeclaredField("lastDamage");
			field.setAccessible(true);
			field.set(livingEntity, lastDamage);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
