package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.CrossbowMeta;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class ProjectileListener {
	private static final Tag<Byte> START_SOUND_PLAYED = Tag.Byte("StartSoundPlayed");
	private static final Tag<Byte> MID_LOAD_SOUND_PLAYED = Tag.Byte("MidLoadSoundPlayed");
	
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
		
		node.addListener(EventListener.builder(PlayerUseItemEvent.class).handler(event -> {
			ItemStack stack = event.getItemStack();
			if (crossbow(stack).isCharged()) {
				// Make sure the animation event is not called, because this is not an animation
				event.setCancelled(true);
				
				stack = performCrossbowShooting(event.getPlayer(), event.getHand(), stack, getCrossbowPower(stack), 1.0);
				event.getPlayer().setItemInHand(event.getHand(), setCrossbowCharged(stack, false));
			} else {
				if (EntityUtils.getProjectile(event.getPlayer(),
						Arrow.ARROW_OR_FIREWORK_PREDICATE, Arrow.ARROW_PREDICATE).first().isAir()) {
					event.setCancelled(true);
				} else {
					ItemStack newStack = stack
							.withTag(START_SOUND_PLAYED, (byte) 0)
							.withTag(MID_LOAD_SOUND_PLAYED, (byte) 0);
					event.getPlayer().setItemInHand(event.getHand(), newStack);
					
					Tracker.itemUseHand.put(event.getPlayer().getUuid(), event.getHand());
				}
			}
		}).filter(event -> event.getItemStack().getMaterial() == Material.CROSSBOW).build());
		
		node.addListener(PlayerItemAnimationEvent.class, event -> {
			if (event.getArmAnimationType() == PlayerItemAnimationEvent.ItemAnimationType.BOW) {
				if (!event.getPlayer().isCreative()
						&& EntityUtils.getProjectile(event.getPlayer(), Arrow.ARROW_PREDICATE).first().isAir()) {
					event.setCancelled(true);
				}
			}
		});
		
		node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			if (EntityUtils.isChargingCrossbow(player)) {
				Player.Hand hand = EntityUtils.getActiveHand(player);
				ItemStack stack = player.getItemInHand(hand);
				
				int quickCharge = EnchantmentUtils.getLevel(Enchantment.QUICK_CHARGE, stack);
				
				long useDuration = System.currentTimeMillis() - Tracker.itemUseStartTime.get(player.getUuid());
				long useTicks = useDuration / MinecraftServer.TICK_MS;
				double progress = (getCrossbowUseDuration(stack) - useTicks) / (double) getCrossbowChargeDuration(stack);
				
				Byte startSoundPlayed = stack.getTag(START_SOUND_PLAYED);
				Byte midLoadSoundPlayed = stack.getTag(MID_LOAD_SOUND_PLAYED);
				if (startSoundPlayed == null) startSoundPlayed = (byte) 0;
				if (midLoadSoundPlayed == null) midLoadSoundPlayed = (byte) 0;
				
				if (progress >= 0.2 && startSoundPlayed == (byte) 0) {
					System.out.println("Playing start sound");
					SoundEvent startSound = getCrossbowStartSound(quickCharge);
					SoundManager.sendToAround(player, startSound, Sound.Source.PLAYER, 0.5F, 1.0F);
					
					stack = stack.withTag(START_SOUND_PLAYED, (byte) 1);
					player.setItemInHand(hand, stack);
				}
				
				SoundEvent midLoadSound = quickCharge == 0 ? SoundEvent.CROSSBOW_LOADING_MIDDLE : null;
				if (progress >= 0.5F && midLoadSound != null && midLoadSoundPlayed == (byte) 0) {
					System.out.println("Playing mid load sound");
					SoundManager.sendToAround(player, midLoadSound, Sound.Source.PLAYER, 0.5F, 1.0F);
					
					stack = stack.withTag(MID_LOAD_SOUND_PLAYED, (byte) 1);
					player.setItemInHand(hand, stack);
				}
			}
		});
		
		node.addListener(EventListener.builder(ItemUpdateStateEvent.class).handler(event -> {
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
		}).filter(event -> event.getItemStack().getMaterial() == Material.BOW).build());
		
		node.addListener(EventListener.builder(ItemUpdateStateEvent.class).handler(event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			
			int quickCharge = EnchantmentUtils.getLevel(Enchantment.QUICK_CHARGE, stack);
			
			if (quickCharge < 6) {
				long useDuration = System.currentTimeMillis() - Tracker.itemUseStartTime.get(player.getUuid());
				double power = getCrossbowPowerForTime(useDuration, stack);
				if (!(power >= 1.0F) || crossbow(stack).isCharged())
					return;
			}
			
			stack = loadCrossbowProjectiles(player, stack);
			if (stack == null) return;
			stack = setCrossbowCharged(stack, true);
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			SoundManager.sendToAround(player, SoundEvent.CROSSBOW_LOADING_END, Sound.Source.PLAYER,
					1.0F, 1.0F / (random.nextFloat() * 0.5F + 1.0F) + 0.2F);
			
			player.setItemInHand(event.getHand(), stack);
		}).filter(event -> event.getItemStack().getMaterial() == Material.CROSSBOW).build());
		
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
	
	public static double getCrossbowPower(ItemStack stack) {
		return crossbowContainsProjectile(stack, Material.FIREWORK_ROCKET) ? 1.6 : 3.15;
	}
	
	public static double getCrossbowPowerForTime(long useDurationMillis, ItemStack stack) {
		long ticks = useDurationMillis / MinecraftServer.TICK_MS;
		double power = ticks / (double) getCrossbowChargeDuration(stack);
		if (power > 1) {
			power = 1;
		}
		
		return power;
	}
	
	public static ItemStack setCrossbowCharged(ItemStack stack, boolean charged) {
		return stack.withMeta(meta -> ((CrossbowMeta.Builder) meta).charged(charged));
	}
	
	public static ItemStack setCrossbowProjectile(ItemStack stack, ItemStack projectile) {
		return stack.withMeta(meta -> ((CrossbowMeta.Builder) meta).projectile(projectile));
	}
	
	public static ItemStack setCrossbowProjectiles(ItemStack stack, ItemStack projectile1,
	                                               ItemStack projectile2, ItemStack projectile3) {
		return stack.withMeta(meta -> ((CrossbowMeta.Builder) meta)
				.projectiles(projectile1, projectile2, projectile3));
	}
	
	public static boolean crossbowContainsProjectile(ItemStack stack, Material projectile) {
		CrossbowMeta meta = crossbow(stack);
		if (meta.getProjectile1().getMaterial() == projectile) return true;
		if (meta.getProjectile2().getMaterial() == projectile) return true;
		return meta.getProjectile3().getMaterial() == projectile;
	}
	
	public static int getCrossbowUseDuration(ItemStack stack) {
		return getCrossbowChargeDuration(stack) + 3;
	}
	
	public static int getCrossbowChargeDuration(ItemStack stack) {
		int quickCharge = EnchantmentUtils.getLevel(Enchantment.QUICK_CHARGE, stack);
		return quickCharge == 0 ? 25 : 25 - 5 * quickCharge;
	}
	
	public static SoundEvent getCrossbowStartSound(int quickCharge) {
		switch (quickCharge) {
			case 1:
				return SoundEvent.CROSSBOW_QUICK_CHARGE_1;
			case 2:
				return SoundEvent.CROSSBOW_QUICK_CHARGE_2;
			case 3:
				return SoundEvent.CROSSBOW_QUICK_CHARGE_3;
			default:
				return SoundEvent.CROSSBOW_LOADING_START;
		}
	}
	
	public static ItemStack loadCrossbowProjectiles(Player player, ItemStack stack) {
		boolean multiShot = EnchantmentUtils.getLevel(Enchantment.MULTISHOT, stack) > 0;
		
		Pair<ItemStack, Integer> pair = EntityUtils.getProjectile(player,
				Arrow.ARROW_OR_FIREWORK_PREDICATE, Arrow.ARROW_PREDICATE);
		ItemStack projectile = pair.first();
		int projectileSlot = pair.second();
		
		if (projectile.isAir() && player.isCreative()) {
			projectile = Arrow.DEFAULT_ARROW;
			projectileSlot = -1;
		}
		
		if (multiShot) {
			stack = setCrossbowProjectiles(stack, projectile, projectile, projectile);
		} else {
			stack = setCrossbowProjectile(stack, projectile);
		}
		
		if (!player.isCreative() && projectileSlot >= 0) {
			player.getInventory().setItemStack(projectileSlot, projectile.withAmount(projectile.getAmount() - 1));
		}
		
		return stack;
	}
	
	public static CrossbowMeta crossbow(ItemStack stack) {
		return (CrossbowMeta) stack.getMeta();
	}
	
	public static ItemStack performCrossbowShooting(Player player, Player.Hand hand, ItemStack stack,
	                                           double power, double spread) {
		CrossbowMeta meta = crossbow(stack);
		ItemStack projectile = meta.getProjectile1();
		if (!projectile.isAir()) {
			shootCrossbowProjectile(player, hand, stack, projectile, 1.0F, power, spread, 0.0F);
		}
		
		if (meta.isTriple()) {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			boolean firstHighPitch = random.nextBoolean();
			float firstPitch = getRandomShotPitch(firstHighPitch, random);
			float secondPitch = getRandomShotPitch(!firstHighPitch, random);
			
			projectile = meta.getProjectile2();
			if (!projectile.isAir()) {
				shootCrossbowProjectile(player, hand, stack, projectile, firstPitch, power, spread, -10.0F);
			}
			projectile = meta.getProjectile3();
			if (!projectile.isAir()) {
				shootCrossbowProjectile(player, hand, stack, projectile, secondPitch, power, spread, 10.0F);
			}
		}
		
		return setCrossbowProjectile(stack, ItemStack.AIR);
	}
	
	public static void shootCrossbowProjectile(Player player, Player.Hand hand, ItemStack crossbowStack,
	                                           ItemStack projectile, float soundPitch,
	                                           double power, double spread, float yaw) {
		boolean firework = projectile.getMaterial() == Material.FIREWORK_ROCKET;
		if (firework) return; //TODO firework
		
		AbstractArrow arrow = getCrossbowArrow(player, crossbowStack, projectile);
		if (player.isCreative() || yaw != 0.0) {
			arrow.pickupMode = AbstractArrow.PickupMode.CREATIVE_ONLY;
		}
		
		Position position = player.getPosition().clone().add(0D, player.getEyeHeight(), 0D);
		arrow.setInstance(Objects.requireNonNull(player.getInstance()),
				position.clone().subtract(0, 0.10000000149011612D, 0)); // Yeah wait what
		
		Position toPosition = position.clone();
		toPosition.setYaw(toPosition.getYaw() + yaw);
		Vector direction = toPosition.getDirection();
		toPosition = toPosition.clone().add(direction.getX(), direction.getY(), direction.getZ())
				.subtract(0, 0.2, 0); //????????
		
		arrow.shoot(toPosition, power, spread);
		
		//TODO damage crossbow
		
		SoundManager.sendToAround(player, SoundEvent.CROSSBOW_SHOOT, Sound.Source.PLAYER, 1.0F, soundPitch);
	}
	
	public static AbstractArrow getCrossbowArrow(Player player, ItemStack crossbowStack, ItemStack projectile) {
		AbstractArrow arrow = createArrow(projectile, player);
		arrow.setCritical(true); // Player shooter is always critical
		arrow.setSound(SoundEvent.CROSSBOW_HIT);
		
		int piercing = EnchantmentUtils.getLevel(Enchantment.PIERCING, crossbowStack);
		if (piercing > 0) {
			arrow.setPiercingLevel((byte) piercing);
		}
		
		return arrow;
	}
	
	public static float getRandomShotPitch(boolean high, ThreadLocalRandom random) {
		float base = high ? 0.63F : 0.43F;
		return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + base;
	}
}
