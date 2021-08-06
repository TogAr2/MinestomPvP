package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class ProjectileListener {
	
	// Please, don't look at the random hardcoded numbers in this class, even I am confused
	public static EventNode<EntityEvent> events() {
		EventNode<EntityEvent> node = EventNode.type("projectile-events", EventFilter.ENTITY);
		
		node.addListener(EventListener.builder(PlayerUseItemEvent.class).handler(event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			
			if (Tracker.hasCooldown(player, stack.getMaterial())) {
				event.setCancelled(true);
			}
			
			boolean snowball = stack.getMaterial() == Material.SNOWBALL;
			boolean enderpearl = stack.getMaterial() == Material.ENDER_PEARL;
			
			SoundEvent soundEvent;
			EntityHittableProjectile projectile;
			if (snowball) {
				soundEvent = SoundEvent.SNOWBALL_THROW;
				projectile = new Snowball(player);
			} else if (enderpearl) {
				soundEvent = SoundEvent.ENDER_PEARL_THROW;
				projectile = new ThrownEnderpearl(player);
			} else {
				soundEvent = SoundEvent.EGG_THROW;
				projectile = new ThrownEgg(player);
			}
			
			projectile.setItem(stack);
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			SoundManager.sendToAround(player, soundEvent,
					snowball || enderpearl ? Sound.Source.NEUTRAL : Sound.Source.PLAYER,
					0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
			
			if (enderpearl) {
				Tracker.setCooldown(player, Material.ENDER_PEARL, 20);
			}
			
			Position position = player.getPosition().clone().add(0D, player.getEyeHeight(), 0D);
			projectile.setInstance(Objects.requireNonNull(player.getInstance()), position);
			
			Vector direction = position.getDirection();
			position = position.clone().add(direction.getX(), direction.getY(), direction.getZ())
					.subtract(0, 0.2, 0); //????????
			
			projectile.shoot(position, 1.5, 1.0);
			
			Vector playerVel = Tracker.playerVelocity.get(player.getUuid());
			projectile.setVelocity(projectile.getVelocity().add(playerVel.getX(),
					player.isOnGround() ? 0.0D : playerVel.getY(), playerVel.getZ()));
			
			if (!player.isCreative()) {
				player.setItemInHand(event.getHand(), stack.withAmount(stack.getAmount() - 1));
			}
		}).filter(event -> event.getItemStack().getMaterial() == Material.SNOWBALL
				|| event.getItemStack().getMaterial() == Material.EGG
				|| event.getItemStack().getMaterial() == Material.ENDER_PEARL)
				.ignoreCancelled(false).build());
		
		node.addListener(PlayerItemAnimationEvent.class, event -> {
			if (event.getArmAnimationType() == PlayerItemAnimationEvent.ItemAnimationType.BOW) {
				if (!event.getPlayer().isCreative()
						&& EntityUtils.getProjectile(event.getPlayer(), Arrow.ARROW_PREDICATE).first().isAir()) {
					event.setCancelled(true);
				}
			}
		});
		
		node.addListener(ItemUpdateStateEvent.class, event -> {
			if (event.getItemStack().getMaterial() == Material.BOW) {
				Player player = event.getPlayer();
				ItemStack stack = event.getItemStack();
				boolean infinite = player.isCreative() || EnchantmentUtils.getLevel(Enchantment.INFINITY_ARROWS, stack) > 0;
				
				Pair<ItemStack, Integer> projectilePair = EntityUtils.getProjectile(player, Arrow.ARROW_PREDICATE);
				ItemStack projectile = projectilePair.first();
				int projectileSlot = projectilePair.second();
				
				if (!infinite && projectile.isAir()) return;
				if (projectile.isAir()) {
					projectile = Arrow.DEFAULT_ARROW;
					projectileSlot = -1;
				}
				
				long useDuration = System.currentTimeMillis() - Tracker.itemUseStartTime.get(player.getUuid());
				double power = getBowPower(useDuration);
				if (power < 0.1) return;
				
				// Arrow creation
				AbstractArrow arrow = createArrow(projectile, player);
				
				if (power >= 1) {
					arrow.setCritical(true);
				}
				
				int powerEnchantment = EnchantmentUtils.getLevel(Enchantment.POWER_ARROWS, stack);
				if (powerEnchantment > 0) {
					arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchantment * 0.5 + 0.5);
				}
				
				int punchEnchantment = EnchantmentUtils.getLevel(Enchantment.PUNCH_ARROWS, stack);
				if (punchEnchantment > 0) {
					arrow.setKnockback(punchEnchantment);
				}
				
				if (EnchantmentUtils.getLevel(Enchantment.FLAMING_ARROWS, stack) > 0) {
					EntityUtils.setOnFireForSeconds(arrow, 100);
				}
				
				//TODO damage bow item
				
				boolean reallyInfinite = infinite && projectile.getMaterial() == Material.ARROW;
				if (reallyInfinite || player.isCreative() && (projectile.getMaterial() == Material.SPECTRAL_ARROW
						|| projectile.getMaterial() == Material.TIPPED_ARROW)) {
					arrow.pickupMode = AbstractArrow.PickupMode.CREATIVE_ONLY;
				}
				
				// Arrow shooting
				Position position = player.getPosition().clone().add(0D, player.getEyeHeight(), 0D);
				arrow.setInstance(Objects.requireNonNull(player.getInstance()),
						position.clone().subtract(0, 0.10000000149011612D, 0)); // Yeah wait what
				
				Vector direction = position.getDirection();
				position = position.clone().add(direction.getX(), direction.getY(), direction.getZ())
						.subtract(0, 0.2, 0); //????????
				
				arrow.shoot(position, power * 3, 1.0);
				
				Vector playerVel = Tracker.playerVelocity.get(player.getUuid());
				arrow.setVelocity(arrow.getVelocity().add(playerVel.getX(),
						player.isOnGround() ? 0.0D : playerVel.getY(), playerVel.getZ()));
				
				ThreadLocalRandom random = ThreadLocalRandom.current();
				SoundManager.sendToAround(player, SoundEvent.ARROW_SHOOT, Sound.Source.PLAYER,
						1.0f, 1.0f / (random.nextFloat() * 0.4f + 1.2f) + (float) power * 0.5f);
				
				if (!reallyInfinite && !player.isCreative() && projectileSlot >= 0) {
					player.getInventory().setItemStack(projectileSlot, projectile.withAmount(projectile.getAmount() - 1));
				}
			}
		});
		
		return node;
	}
	
	public static double getBowPower(long useDurationMillis) {
		double seconds = useDurationMillis / 1000.0;
		double power = (seconds * seconds + seconds * 2.0) / 3.0;
		if (power > 1) {
			power = 1;
		}
		
		return power;
	}
	
	public static AbstractArrow createArrow(ItemStack stack, @Nullable Entity shooter) {
		if (stack.getMaterial() == Material.SPECTRAL_ARROW) {
			return new SpectralArrow(shooter);
		} else {
			Arrow arrow = new Arrow(shooter);
			arrow.inheritEffects(stack);
			return arrow;
		}
	}
}
