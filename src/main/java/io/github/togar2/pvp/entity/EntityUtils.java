package io.github.togar2.pvp.entity;

import io.github.togar2.pvp.damage.DamageTypeInfo;
import io.github.togar2.pvp.enchantment.enchantments.ProtectionEnchantment;
import io.github.togar2.pvp.projectile.Arrow;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerFlag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.entity.metadata.projectile.AbstractArrowMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.time.TimeUnit;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class EntityUtils {
	public static final Tag<Long> FIRE_EXTINGUISH_TIME = Tag.Long("fireExtinguishTime");
	
	public static void setFireForDuration(Entity entity, Duration duration) {
		if (entity instanceof LivingEntity) {
			((LivingEntity) entity).setFireForDuration(duration);
			return;
		}
		
		EntityFireEvent entityFireEvent = new EntityFireEvent(entity, duration);
		
		// Do not start fire event if the fire needs to be removed (< 0 duration)
		if (duration.toMillis() > 0) {
			EventDispatcher.callCancellable(entityFireEvent, () -> entity.setOnFire(true));
		}
		// FIRE_EXTINGUISH_TIME is updated by event listener
	}
	
	public static void setOnFireForSeconds(Entity entity, int seconds) {
		boolean living = entity instanceof LivingEntity;
		LivingEntity livingEntity = living ? (LivingEntity) entity : null;
		
		int ticks = seconds * ServerFlag.SERVER_TICKS_PER_SECOND;
		if (living) {
			ticks = ProtectionEnchantment.transformFireDuration(livingEntity, ticks);
		}
		int millis = ticks * MinecraftServer.TICK_MS;
		
		long fireExtinguishTime = entity.hasTag(FIRE_EXTINGUISH_TIME) ? entity.getTag(FIRE_EXTINGUISH_TIME) : 0;
		if (System.currentTimeMillis() + millis > fireExtinguishTime) {
			setFireForDuration(entity, Duration.ofMillis(millis));
		}
	}
	
	public static boolean blockedByShield(LivingEntity entity, Damage damage, DamageTypeInfo typeInfo, boolean legacy) {
		Entity source = damage.getSource();
		boolean piercing = false;
		if (source != null && source.getEntityMeta() instanceof AbstractArrowMeta) {
			if (((AbstractArrowMeta) source.getEntityMeta()).getPiercingLevel() > 0) {
				piercing = true;
			}
		}
		
		// If damage doesn't bypass armor, no piercing, and a shield is active
		if (!typeInfo.bypassesArmor() && !piercing
				&& entity.getEntityMeta() instanceof LivingEntityMeta meta
				&& meta.isHandActive() && entity.getItemInHand(meta.getActiveHand()).material() == Material.SHIELD) {
			if (legacy) return true;
			
			if (source != null) {
				Pos attackerPos = source.getPosition();
				Pos entityPos = entity.getPosition();
				
				Vec attackerPosVector = attackerPos.asVec();
				Vec entityRotation = entityPos.direction();
				Vec attackerDirection = entityPos.asVec().sub(attackerPosVector).normalize();
				attackerDirection = attackerDirection.withY(0);
				
				return attackerDirection.dot(entityRotation) < 0.0;
			}
		}
		
		return false;
	}
	
	public static Iterable<ItemStack> getArmorItems(LivingEntity entity) {
		List<ItemStack> list = new ArrayList<>();
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.isArmor() && !entity.getEquipment(slot).isAir()) {
				list.add(entity.getEquipment(slot));
			}
		}
		
		return list;
	}
	
	//TODO needs improving
	public static boolean isClimbing(Entity entity) {
		if (entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) return false;
		
		var tag = MinecraftServer.getTagManager().getTag(net.minestom.server.gamedata.tags.Tag.BasicType.BLOCKS, "minecraft:climbable");
		assert tag != null;
		
		Block block = Objects.requireNonNull(entity.getInstance()).getBlock(entity.getPosition());
		return tag.contains(block.namespace());
	}
	
	public static double getBodyY(Entity entity, double heightScale) {
		return entity.getPosition().y() + entity.getBoundingBox().height() * heightScale;
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
		
		return player.isCreative() ? Pair.of(Arrow.DEFAULT_ARROW, -1) : Pair.of(ItemStack.AIR, -1);
	}
	
	private static Pair<ItemStack, Integer> getHeldItem(Player player, Predicate<ItemStack> predicate) {
		ItemStack stack = player.getItemInHand(Player.Hand.OFF);
		if (predicate.test(stack)) return Pair.of(stack, PlayerInventoryUtils.OFFHAND_SLOT);
		
		stack = player.getItemInHand(Player.Hand.MAIN);
		if (predicate.test(stack)) return Pair.of(stack, (int) player.getHeldSlot());
		
		return Pair.of(ItemStack.AIR, -1);
	}
	
	public static boolean randomTeleport(Entity entity, Pos to, boolean status) {
		Instance instance = entity.getInstance();
		assert instance != null;
		
		boolean success = false;
		int lowestY = to.blockY();
		if (lowestY == 0) lowestY++;
		while (lowestY > instance.getDimensionType().getMinY()) {
			Block block = instance.getBlock(to.blockX(), lowestY - 1, to.blockZ());
			if (!block.isAir() && !block.isLiquid()) {
				Block above = instance.getBlock(to.blockX(), lowestY, to.blockZ());
				Block above2 = instance.getBlock(to.blockX(), lowestY + 1, to.blockZ());
				if (above.isAir() && above2.isAir()) {
					success = true;
					break;
				} else {
					lowestY--;
				}
			} else {
				lowestY--;
			}
		}
		
		if (!success) return false;
		
		entity.teleport(to.withY(lowestY));
		
		if (status) {
			entity.triggerStatus((byte) 46);
		}
		
		return true;
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

	public static Pos getPreviousPosition(Entity entity) {
		// Use reflection to get previousPosition field
		try {
			Field field = Entity.class.getDeclaredField("previousPosition");
			field.setAccessible(true);
			return (Pos) field.get(entity);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
			return null;
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
