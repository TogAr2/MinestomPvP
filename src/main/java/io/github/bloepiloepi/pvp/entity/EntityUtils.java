package io.github.bloepiloepi.pvp.entity;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.enchantment.enchantments.ProtectionEnchantment;
import io.github.bloepiloepi.pvp.food.HungerManager;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.projectile.Arrow;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.entity.metadata.arrow.AbstractArrowMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class EntityUtils {
	public static final Tag<Long> FIRE_EXTINGUISH_TIME = Tag.Long("fireExtinguishTime");
	
	public static boolean hasEffect(Entity entity, PotionEffect type) {
		return entity.getActiveEffects().stream().anyMatch((effect) -> effect.getPotion().effect() == type);
	}
	
	public static Potion getEffect(Entity entity, PotionEffect type) {
		for (TimedPotion potion : entity.getActiveEffects()) {
			if (potion.getPotion().effect() == type) {
				return potion.getPotion();
			}
		}
		
		return new Potion(type, (byte) 0, 0, PotionListener.defaultFlags());
	}
	
	public static void setFireForDuration(Entity entity, int duration, TemporalUnit temporalUnit) {
		setFireForDuration(entity, Duration.of(duration, temporalUnit));
	}
	
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
		
		int ticks = seconds * MinecraftServer.TICK_PER_SECOND;
		if (living) {
			ticks = ProtectionEnchantment.transformFireDuration(livingEntity, ticks);
		}
		int millis = ticks * MinecraftServer.TICK_MS;
		
		long fireExtinguishTime = entity.hasTag(FIRE_EXTINGUISH_TIME) ? entity.getTag(FIRE_EXTINGUISH_TIME) : 0;
		if (System.currentTimeMillis() + millis > fireExtinguishTime) {
			setFireForDuration(entity, millis, TimeUnit.MILLISECOND);
		}
	}
	
	public static boolean damage(Entity entity, DamageType type, float amount) {
		if (entity instanceof LivingEntity) {
			return ((LivingEntity) entity).damage(type, amount);
		}
		
		return false;
	}
	
	public static boolean blockedByShield(LivingEntity entity, CustomDamageType type, boolean legacy) {
		Entity damager = type.getDirectEntity();
		boolean piercing = false;
		if (damager != null && damager.getEntityMeta() instanceof AbstractArrowMeta) {
			if (((AbstractArrowMeta) damager.getEntityMeta()).getPiercingLevel() > 0) {
				piercing = true;
			}
		}
		
		if (!type.bypassesArmor() && !piercing && isBlocking(entity)) {
			if (legacy) return true;
			
			Pos attackerPos = type.getPosition();
			if (attackerPos != null) {
				Pos entityPos = entity.getPosition();
				
				Vec attackerPosVector = attackerPos.asVec();
				Vec entityRotation = entityPos.direction();
				Vec attackerDirection = entityPos.asVec().sub(attackerPosVector).normalize();
				attackerDirection = attackerDirection.withY(0);
				
				return attackerDirection.dot(entityRotation) < 0.0D;
			}
		}
		
		return false;
	}
	
	public static boolean isBlocking(LivingEntity entity) {
		if (entity.getEntityMeta() instanceof LivingEntityMeta meta) {
			if (meta.isHandActive()) {
				return entity.getItemInHand(meta.getActiveHand()).material() == Material.SHIELD;
			}
		}
		
		return false;
	}
	
	public static boolean isChargingCrossbow(LivingEntity entity) {
		LivingEntityMeta meta = (LivingEntityMeta) entity.getEntityMeta();
		
		if (meta.isHandActive()) {
			return entity.getItemInHand(meta.getActiveHand()).material() == Material.CROSSBOW;
		}
		
		return false;
	}
	
	public static Player.Hand getActiveHand(LivingEntity entity) {
		return ((LivingEntityMeta) entity.getEntityMeta()).getActiveHand();
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
	
	public static void addExhaustion(Player player, float exhaustion) {
		if (!player.isInvulnerable() && player.getGameMode().canTakeDamage() && player.isOnline()) {
			HungerManager.addExhaustion(player, exhaustion);
		}
	}
	
	//TODO needs improving
	public static boolean isClimbing(Entity entity) {
		if (entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) return false;
		
		Block block = Objects.requireNonNull(entity.getInstance()).getBlock(entity.getPosition());
		return block.compare(Block.LADDER) || block.compare(Block.VINE) || block.compare(Block.TWISTING_VINES)
				|| block.compare(Block.TWISTING_VINES_PLANT) || block.compare(Block.WEEPING_VINES)
				|| block.compare(Block.WEEPING_VINES_PLANT) || block.compare(Block.ACACIA_TRAPDOOR)
				|| block.compare(Block.BIRCH_TRAPDOOR) || block.compare(Block.CRIMSON_TRAPDOOR)
				|| block.compare(Block.DARK_OAK_TRAPDOOR) || block.compare(Block.IRON_TRAPDOOR)
				|| block.compare(Block.JUNGLE_TRAPDOOR) || block.compare(Block.OAK_TRAPDOOR)
				|| block.compare(Block.SPRUCE_TRAPDOOR) || block.compare(Block.WARPED_TRAPDOOR);
	}
	
	public static double getBodyY(Entity entity, double heightScale) {
		return entity.getPosition().y() + entity.getBoundingBox().height() * heightScale;
	}
	
	public static boolean hasPotionEffect(LivingEntity entity, PotionEffect effect) {
		return entity.getActiveEffects().stream()
				.map((potion) -> potion.getPotion().effect())
				.anyMatch((potionEffect) -> potionEffect == effect);
	}
	
	public static @Nullable ItemEntity spawnItemAtLocation(Entity entity, ItemStack itemStack, double up) {
		if (itemStack.isAir()) return null;
		
		ItemEntity item = new ItemEntity(itemStack);
		item.setPickupDelay(10, TimeUnit.SERVER_TICK); // Default 0.5 seconds
		item.setInstance(Objects.requireNonNull(entity.getInstance()), entity.getPosition().add(0, up, 0));
		
		return item;
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
			String name = entity.getEntityType().name();
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
}
