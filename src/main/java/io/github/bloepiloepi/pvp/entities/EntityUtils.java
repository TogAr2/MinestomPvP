package io.github.bloepiloepi.pvp.entities;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.enchantment.enchantments.ProtectionEnchantment;
import io.github.bloepiloepi.pvp.enums.Tool;
import io.github.bloepiloepi.pvp.projectile.Arrow;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.entity.metadata.arrow.AbstractArrowMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.chunk.ChunkUtils;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class EntityUtils {
	
	public static boolean hasEffect(Entity entity, PotionEffect type) {
		return entity.getActiveEffects().stream().anyMatch((effect) -> effect.getPotion().getEffect() == type);
	}
	
	public static Potion getEffect(Entity entity, PotionEffect type) {
		for (TimedPotion potion : entity.getActiveEffects()) {
			if (potion.getPotion().getEffect() == type) {
				return potion.getPotion();
			}
		}
		
		return new Potion(type, (byte) 0, 0);
	}
	
	public static void setOnFireForSeconds(Entity entity, int seconds) {
		if (!(entity instanceof LivingEntity)) return;
		LivingEntity living = (LivingEntity) entity;
		
		int ticks = seconds * 20;
		ticks = ProtectionEnchantment.transformFireDuration(living, ticks);
		
		//FIXME this makes fire duration lower if it was higher than current seconds
		living.setFireForDuration(ticks);
	}
	
	public static boolean damage(Entity entity, DamageType type, float amount) {
		if (entity instanceof LivingEntity) {
			return ((LivingEntity) entity).damage(type, amount);
		}
		
		return false;
	}
	
	public static boolean blockedByShield(LivingEntity entity, CustomDamageType type) {
		Entity damager = type.getDirectEntity();
		boolean piercing = false;
		if (damager != null && damager.getEntityMeta() instanceof AbstractArrowMeta) {
			if (((AbstractArrowMeta) damager.getEntityMeta()).getPiercingLevel() > 0) {
				piercing = true;
			}
		}
		
		if (!type.bypassesArmor() && !piercing && isBlocking(entity)) {
			Position attackerPos = type.getPosition();
			if (attackerPos != null) {
				Position entityPos = entity.getPosition();
				
				Vector attackerPosVector = attackerPos.toVector();
				Vector entityRotation = entityPos.getDirection();
				Vector attackerDirection = entityPos.toVector().subtract(attackerPosVector).normalize();
				attackerDirection.setY(0);
				
				return attackerDirection.dot(entityRotation) < 0.0D;
			}
		}
		
		return false;
	}
	
	public static boolean isBlocking(LivingEntity entity) {
		LivingEntityMeta meta = (LivingEntityMeta) entity.getEntityMeta();
		
		if (meta.isHandActive()) {
			return entity.getItemInHand(meta.getActiveHand()).getMaterial() == Material.SHIELD;
		}
		
		return false;
	}
	
	public static boolean isChargingCrossbow(LivingEntity entity) {
		LivingEntityMeta meta = (LivingEntityMeta) entity.getEntityMeta();
		
		if (meta.isHandActive()) {
			return entity.getItemInHand(meta.getActiveHand()).getMaterial() == Material.CROSSBOW;
		}
		
		return false;
	}
	
	public static Player.Hand getActiveHand(LivingEntity entity) {
		return ((LivingEntityMeta) entity.getEntityMeta()).getActiveHand();
	}
	
	public static void takeShieldHit(LivingEntity entity, LivingEntity attacker, boolean applyKnockback) {
		if (applyKnockback) {
			Position entityPos = entity.getPosition();
			Position attackerPos = attacker.getPosition();
			attacker.takeKnockback(0.5F, attackerPos.getX() - entityPos.getX(), attackerPos.getZ() - entityPos.getZ());
		}
		
		if (!(entity instanceof Player)) return;
		
		Tool tool = Tool.fromMaterial(attacker.getItemInMainHand().getMaterial());
		if (tool != null && tool.isAxe()) {
			disableShield((Player) entity, true); //For some reason the vanilla server always passes true
		}
	}
	
	public static void disableShield(Player player, boolean sprinting) {
		float chance = 0.25F + (float) EnchantmentUtils.getBlockEfficiency(player) * 0.05F;
		if (sprinting) {
			chance += 0.75F;
		}
		
		if (ThreadLocalRandom.current().nextFloat() < chance) {
			Tracker.setCooldown(player, Material.SHIELD, 100);
			
			//Shield disable status
			player.triggerStatus((byte) 30);
			player.triggerStatus((byte) 9);
			
			Player.Hand hand = player.getEntityMeta().getActiveHand();
			player.refreshActiveHand(false, hand == Player.Hand.OFF, false);
		}
	}
	
	public static Iterable<ItemStack> getArmorItems(LivingEntity entity) {
		List<ItemStack> list = new ArrayList<>();
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.isArmor()) {
				list.add(entity.getEquipment(slot));
			}
		}
		
		return list;
	}
	
	public static void addExhaustion(Player player, float exhaustion) {
		if (!player.isInvulnerable() && player.getGameMode().canTakeDamage() && player.isOnline()) {
			Tracker.hungerManager.get(player.getUuid()).addExhaustion(exhaustion);
		}
	}
	
	//TODO needs improving
	public static boolean isClimbing(Player player) {
		if (player.getGameMode() == GameMode.SPECTATOR) return false;
		
		switch (Objects.requireNonNull(player.getInstance()).getBlock(player.getPosition().toBlockPosition())) {
			case LADDER:
			case VINE:
			case TWISTING_VINES:
			case TWISTING_VINES_PLANT:
			case WEEPING_VINES:
			case WEEPING_VINES_PLANT:
			case ACACIA_TRAPDOOR:
			case BIRCH_TRAPDOOR:
			case CRIMSON_TRAPDOOR:
			case DARK_OAK_TRAPDOOR:
			case IRON_TRAPDOOR:
			case JUNGLE_TRAPDOOR:
			case OAK_TRAPDOOR:
			case SPRUCE_TRAPDOOR:
			case WARPED_TRAPDOOR:
				return true;
			default:
				return false;
		}
	}
	
	public static double getBodyY(Entity entity, double heightScale) {
		return entity.getPosition().getY() + entity.getBoundingBox().getHeight() * heightScale;
	}
	
	public static boolean hasPotionEffect(LivingEntity entity, PotionEffect effect) {
		return entity.getActiveEffects().stream()
				.map((potion) -> potion.getPotion().getEffect())
				.anyMatch((potionEffect) -> potionEffect == effect);
	}
	
	public static @Nullable ItemEntity spawnItemAtLocation(Entity entity, ItemStack itemStack, double up) {
		if (itemStack.isAir()) return null;
		
		ItemEntity item = new ItemEntity(itemStack, entity.getPosition().clone().add(0, up, 0));
		item.setPickupDelay(10, TimeUnit.TICK); // Default 0.5 seconds
		item.setInstance(Objects.requireNonNull(entity.getInstance()));
		
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
	
	public static boolean randomTeleport(Entity entity, Position to, boolean status) {
		BlockPosition blockPosition = to.toBlockPosition();
		Instance instance = entity.getInstance();
		assert instance != null;
		int chunkX = ChunkUtils.getChunkCoordinate(blockPosition.getX());
		int chunkZ = ChunkUtils.getChunkCoordinate(blockPosition.getZ());
		if (!instance.isChunkLoaded(chunkX, chunkZ)) {
			return false;
		}
		
		boolean success = false;
		int lowestY = blockPosition.getY();
		while (lowestY > instance.getDimensionType().getMinY()) {
			Block block = instance.getBlock(blockPosition.getX(), lowestY - 1, blockPosition.getZ());
			if (!block.isAir() && !block.isLiquid()) {
				Block above = instance.getBlock(blockPosition.getX(), lowestY, blockPosition.getZ());
				Block above2 = instance.getBlock(blockPosition.getX(), lowestY + 1, blockPosition.getZ());
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
		
		Position finalPos = to.clone();
		finalPos.setY(lowestY);
		entity.teleport(finalPos);
		
		if (status) {
			entity.triggerStatus((byte) 46);
		}
		
		return true;
	}
	
	public static void updateProjectileRotation(EntityProjectile projectile) {
		Vector velocity = projectile.getVelocity();
		double xz = Math.sqrt(velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
		projectile.getPosition().setYaw((float) Math.toDegrees(Math.atan2(velocity.getY(), xz)));
		projectile.getPosition().setPitch((float) Math.toDegrees(Math.atan2(velocity.getX(), velocity.getZ())));
	}
	
	public static Vector getActualVelocity(Entity entity) {
		if (!(entity instanceof Player)) {
			return entity.getVelocity();
		} else {
			return Tracker.playerVelocity.get(entity.getUuid());
		}
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
}
