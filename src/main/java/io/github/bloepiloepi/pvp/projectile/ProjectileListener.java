package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.config.AttackConfig;
import io.github.bloepiloepi.pvp.config.ProjectileConfig;
import io.github.bloepiloepi.pvp.config.PvPConfig;
import io.github.bloepiloepi.pvp.enchantment.EnchantmentUtils;
import io.github.bloepiloepi.pvp.entity.EntityUtils;
import io.github.bloepiloepi.pvp.entity.PvpPlayer;
import io.github.bloepiloepi.pvp.entity.Tracker;
import io.github.bloepiloepi.pvp.listeners.AttackManager;
import io.github.bloepiloepi.pvp.utils.FluidUtils;
import io.github.bloepiloepi.pvp.utils.ItemUtils;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityShootEvent;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.CrossbowMeta;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProjectileListener {
	private static final Tag<Byte> START_SOUND_PLAYED = Tag.Byte("StartSoundPlayed");
	private static final Tag<Byte> MID_LOAD_SOUND_PLAYED = Tag.Byte("MidLoadSoundPlayed");
	public static final Tag<Long> RIPTIDE_START = Tag.Long("riptideStart");
	
	// Please, don't look at the random hardcoded numbers in this class, even I am confused
	public static EventNode<PlayerInstanceEvent> events(ProjectileConfig config) {
		EventNode<PlayerInstanceEvent> node = EventNode.type("projectile-events", PvPConfig.PLAYER_INSTANCE_FILTER);
		
		if (config.isFishingRodEnabled()) node.addListener(EventListener.builder(PlayerUseItemEvent.class).handler(event -> {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			Player player = event.getPlayer();
			
			if (FishingBobber.fishingBobbers.containsKey(player.getUuid())) {
				int durability = FishingBobber.fishingBobbers.get(player.getUuid()).retrieve();
				if (!player.isCreative())
					ItemUtils.damageEquipment(player, event.getHand() == Player.Hand.MAIN ?
							EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, durability);
				
				SoundManager.sendToAround(player, SoundEvent.ENTITY_FISHING_BOBBER_RETRIEVE, Sound.Source.NEUTRAL,
						1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
			} else {
				SoundManager.sendToAround(player, SoundEvent.ENTITY_FISHING_BOBBER_THROW, Sound.Source.NEUTRAL,
						0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
				
				FishingBobber bobber = new FishingBobber(player, config.isLegacy());
				FishingBobber.fishingBobbers.put(player.getUuid(), bobber);
				
				EntityShootEvent shootEvent = new EntityShootEvent(player, bobber,
						player.getPosition(), 0, 1.0);
				EventDispatcher.call(shootEvent);
				if (shootEvent.isCancelled()) {
					bobber.remove();
					return;
				}
				double spread = shootEvent.getSpread() * (config.isLegacy() ? 0.0075 : 0.0045);
				
				Pos playerPos = player.getPosition();
				float playerPitch = playerPos.pitch();
				float playerYaw = playerPos.yaw();
				
				float zDir = (float) Math.cos(Math.toRadians(-playerYaw) - Math.PI);
				float xDir = (float) Math.sin(Math.toRadians(-playerYaw) - Math.PI);
				double x = playerPos.x() - (double) xDir * 0.3D;
				double y = playerPos.y() + player.getEyeHeight();
				double z = playerPos.z() - (double) zDir * 0.3D;
				bobber.setInstance(Objects.requireNonNull(player.getInstance()), new Pos(x, y, z));
				
				Vec velocity;
				
				if (!config.isLegacy()) {
					velocity = new Vec(
							-xDir,
							MathUtils.clamp(-(
									(float) Math.sin(Math.toRadians(-playerPitch)) /
									(float) -Math.cos(Math.toRadians(-playerPitch))
							), -5.0F, 5.0F),
							-zDir
					);
					double length = velocity.length();
					velocity = velocity.mul(
							0.6D / length + 0.5D + random.nextGaussian() * spread,
							0.6D / length + 0.5D + random.nextGaussian() * spread,
							0.6D / length + 0.5D + random.nextGaussian() * spread
					);
				} else {
					double maxVelocity = 0.4F;
					velocity = new Vec(
							-Math.sin(playerYaw / 180.0F * (float) Math.PI)
									* Math.cos(playerPitch / 180.0F * (float) Math.PI) * maxVelocity,
							-Math.sin(playerPitch / 180.0F * (float) Math.PI) * maxVelocity,
							Math.cos(playerYaw / 180.0F * (float) Math.PI)
									* Math.cos(playerPitch / 180.0F * (float) Math.PI) * maxVelocity
					);
					double length = velocity.length();
					velocity = velocity
							.div(length)
							.add(
									random.nextGaussian() * spread,
									random.nextGaussian() * spread,
									random.nextGaussian() * spread
							)
							.mul(1.5);
				}
				
				bobber.setVelocity(velocity.mul(MinecraftServer.TICK_PER_SECOND * 0.75));
			}
		}).filter(event -> event.getItemStack().material() == Material.FISHING_ROD).build());
		
		node.addListener(EventListener.builder(PlayerUseItemEvent.class).handler(event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			
			if (Tracker.hasCooldown(player, stack.material())) {
				event.setCancelled(true);
				return;
			}
			
			boolean snowball = stack.material() == Material.SNOWBALL;
			boolean enderpearl = stack.material() == Material.ENDER_PEARL;
			
			SoundEvent soundEvent;
			CustomEntityProjectile projectile;
			if (snowball) {
				if (!config.isSnowballEnabled()) return;
				soundEvent = SoundEvent.ENTITY_SNOWBALL_THROW;
				projectile = new Snowball(player);
			} else if (enderpearl) {
				if (!config.isEnderPearlEnabled()) return;
				soundEvent = SoundEvent.ENTITY_ENDER_PEARL_THROW;
				projectile = new ThrownEnderpearl(player);
			} else {
				if (!config.isEggEnabled()) return;
				soundEvent = SoundEvent.ENTITY_EGG_THROW;
				projectile = new ThrownEgg(player);
			}
			
			((ItemHoldingProjectile) projectile).setItem(stack);
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			SoundManager.sendToAround(player, soundEvent,
					snowball || enderpearl ? Sound.Source.NEUTRAL : Sound.Source.PLAYER,
					0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
			
			if (enderpearl) {
				Tracker.setCooldown(player, Material.ENDER_PEARL, 20);
			}
			
			Pos position = player.getPosition().add(0D, player.getEyeHeight(), 0D);
			projectile.setInstance(Objects.requireNonNull(player.getInstance()), position);
			
			Vec direction = position.direction();
			position = position.add(direction).sub(0, 0.2, 0); //????????
			
			projectile.shoot(position, 1.5, 1.0);
			
			Vec playerVel = player.getVelocity();
			projectile.setVelocity(projectile.getVelocity().add(playerVel.x(),
					player.isOnGround() ? 0.0D : playerVel.y(), playerVel.z()));
			
			if (!player.isCreative()) {
				player.setItemInHand(event.getHand(), stack.withAmount(stack.amount() - 1));
			}
		}).filter(event -> event.getItemStack().material() == Material.SNOWBALL
				|| event.getItemStack().material() == Material.EGG
				|| event.getItemStack().material() == Material.ENDER_PEARL)
				.build());
		
		if (config.isCrossbowEnabled()) node.addListener(EventListener.builder(PlayerUseItemEvent.class).handler(event -> {
			ItemStack stack = event.getItemStack();
			if (stack.meta(CrossbowMeta.class).isCharged()) {
				// Make sure the animation event is not called, because this is not an animation
				event.setCancelled(true);
				
				stack = performCrossbowShooting(event.getPlayer(), event.getHand(), stack,
						getCrossbowPower(stack), 1.0, config.isLegacy());
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
				}
			}
		}).filter(event -> event.getItemStack().material() == Material.CROSSBOW).build());
		
		node.addListener(PlayerItemAnimationEvent.class, event -> {
			if (event.getItemAnimationType() == PlayerItemAnimationEvent.ItemAnimationType.BOW) {
				if (!event.getPlayer().isCreative()
						&& EntityUtils.getProjectile(event.getPlayer(), Arrow.ARROW_PREDICATE).first().isAir()) {
					event.setCancelled(true);
				}
			}
		});
		
		if (config.isCrossbowEnabled()) node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			if (EntityUtils.isChargingCrossbow(player)) {
				Player.Hand hand = EntityUtils.getActiveHand(player);
				ItemStack stack = player.getItemInHand(hand);
				
				int quickCharge = EnchantmentUtils.getLevel(Enchantment.QUICK_CHARGE, stack);
				
				long useDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
				long useTicks = useDuration / MinecraftServer.TICK_MS;
				double progress = (getCrossbowUseDuration(stack) - useTicks) / (double) getCrossbowChargeDuration(stack);
				
				Byte startSoundPlayed = stack.getTag(START_SOUND_PLAYED);
				Byte midLoadSoundPlayed = stack.getTag(MID_LOAD_SOUND_PLAYED);
				if (startSoundPlayed == null) startSoundPlayed = (byte) 0;
				if (midLoadSoundPlayed == null) midLoadSoundPlayed = (byte) 0;
				
				if (progress >= 0.2 && startSoundPlayed == (byte) 0) {
					SoundEvent startSound = getCrossbowStartSound(quickCharge);
					SoundManager.sendToAround(player, startSound, Sound.Source.PLAYER, 0.5F, 1.0F);
					
					stack = stack.withTag(START_SOUND_PLAYED, (byte) 1);
					player.setItemInHand(hand, stack);
				}
				
				SoundEvent midLoadSound = quickCharge == 0 ? SoundEvent.ITEM_CROSSBOW_LOADING_MIDDLE : null;
				if (progress >= 0.5F && midLoadSound != null && midLoadSoundPlayed == (byte) 0) {
					SoundManager.sendToAround(player, midLoadSound, Sound.Source.PLAYER, 0.5F, 1.0F);
					
					stack = stack.withTag(MID_LOAD_SOUND_PLAYED, (byte) 1);
					player.setItemInHand(hand, stack);
				}
			}
		});
		
		if (config.isBowEnabled()) node.addListener(EventListener.builder(ItemUpdateStateEvent.class).handler(event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			boolean infinite = player.isCreative() || EnchantmentUtils.getLevel(Enchantment.INFINITY, stack) > 0;
			
			Pair<ItemStack, Integer> projectilePair = EntityUtils.getProjectile(player, Arrow.ARROW_PREDICATE);
			ItemStack projectile = projectilePair.first();
			int projectileSlot = projectilePair.second();
			
			if (!infinite && projectile.isAir()) return;
			if (projectile.isAir()) {
				projectile = Arrow.DEFAULT_ARROW;
				projectileSlot = -1;
			}
			
			long useDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
			double power = getBowPower(useDuration);
			if (power < 0.1) return;
			
			// Arrow creation
			AbstractArrow arrow = createArrow(projectile, player, config.isLegacy());
			
			if (power >= 1) {
				arrow.setCritical(true);
			}
			
			int powerEnchantment = EnchantmentUtils.getLevel(Enchantment.POWER, stack);
			if (powerEnchantment > 0) {
				arrow.setBaseDamage(arrow.getBaseDamage() + (double) powerEnchantment * 0.5 + 0.5);
			}
			
			int punchEnchantment = EnchantmentUtils.getLevel(Enchantment.PUNCH, stack);
			if (punchEnchantment > 0) {
				arrow.setKnockback(punchEnchantment);
			}
			
			if (EnchantmentUtils.getLevel(Enchantment.FLAME, stack) > 0) {
				EntityUtils.setOnFireForSeconds(arrow, 100);
			}
			
			ItemUtils.damageEquipment(player, event.getHand() == Player.Hand.MAIN ?
					EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, 1);
			
			boolean reallyInfinite = infinite && projectile.material() == Material.ARROW;
			if (reallyInfinite || player.isCreative()
					&& (projectile.material() == Material.SPECTRAL_ARROW
					|| projectile.material() == Material.TIPPED_ARROW)) {
				arrow.pickupMode = AbstractArrow.PickupMode.CREATIVE_ONLY;
			}
			
			// Arrow shooting
			Pos position = player.getPosition().add(0D, player.getEyeHeight(), 0D);
			arrow.setInstance(Objects.requireNonNull(player.getInstance()),
					position.sub(0, 0.10000000149011612D, 0)); // Yeah wait what
			
			Vec direction = position.direction();
			position = position.add(direction).sub(0, 0.2, 0); //????????
			
			arrow.shoot(position, power * 3, 1.0);
			
			Vec playerVel = player.getVelocity();
			arrow.setVelocity(arrow.getVelocity().add(playerVel.x(),
					player.isOnGround() ? 0.0D : playerVel.y(), playerVel.z()));
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			SoundManager.sendToAround(player, SoundEvent.ENTITY_ARROW_SHOOT, Sound.Source.PLAYER,
					1.0f, 1.0f / (random.nextFloat() * 0.4f + 1.2f) + (float) power * 0.5f);
			
			if (!reallyInfinite && !player.isCreative() && projectileSlot >= 0) {
				player.getInventory().setItemStack(projectileSlot,
						projectile.withAmount(projectile.amount() - 1));
			}
		}).filter(event -> event.getItemStack().material() == Material.BOW).build());
		
		if (config.isCrossbowEnabled()) node.addListener(EventListener.builder(ItemUpdateStateEvent.class).handler(event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			
			int quickCharge = EnchantmentUtils.getLevel(Enchantment.QUICK_CHARGE, stack);
			
			if (quickCharge < 6) {
				long useDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
				double power = getCrossbowPowerForTime(useDuration, stack);
				if (!(power >= 1.0F) || stack.meta(CrossbowMeta.class).isCharged())
					return;
			}
			
			stack = loadCrossbowProjectiles(player, stack);
			if (stack == null) return;
			stack = setCrossbowCharged(stack, true);
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			SoundManager.sendToAround(player, SoundEvent.ITEM_CROSSBOW_LOADING_END, Sound.Source.PLAYER,
					1.0F, 1.0F / (random.nextFloat() * 0.5F + 1.0F) + 0.2F);
			
			player.setItemInHand(event.getHand(), stack);
		}).filter(event -> event.getItemStack().material() == Material.CROSSBOW).build());
		
		if (config.isTridentEnabled()) node.addListener(EventListener.builder(ItemUpdateStateEvent.class).handler(event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			
			long useDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
			int ticks = (int) ((useDuration / 1000.0) * 20);
			if (ticks < 10) return;
			
			int riptide = EnchantmentUtils.getLevel(Enchantment.RIPTIDE, stack);
			if (riptide > 0 && !FluidUtils.isTouchingWater(player)) return;
			
			ItemUtils.damageEquipment(player, event.getHand() == Player.Hand.MAIN ?
					EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, 1);
			if (riptide > 0) {
				float yaw = player.getPosition().yaw();
				float pitch = player.getPosition().pitch();
				double h = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
				double k = -Math.sin(Math.toRadians(pitch));
				double l = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
				double length = Math.sqrt(h * h + k * k + l * l);
				double n = 3.0 * ((1.0 + riptide) / 4.0);
				
				player.setTag(RIPTIDE_START, player.getAliveTicks());
				player.setVelocity(player.getVelocity().add(new Vec(
						h * (n / length),
						k * (n / length),
						l * (n / length)
				).mul(MinecraftServer.TICK_PER_SECOND)));
				
				SoundEvent soundEvent = riptide >= 3 ? SoundEvent.ITEM_TRIDENT_RIPTIDE_3 :
						(riptide == 2 ? SoundEvent.ITEM_TRIDENT_RIPTIDE_2 : SoundEvent.ITEM_TRIDENT_RIPTIDE_1);
				if (player.getChunk() != null) player.getChunk().getViewersAsAudience().playSound(Sound.sound(
						soundEvent, Sound.Source.PLAYER,
						1.0f, 1.0f
				), player);
				
				player.scheduleNextTick(entity -> player.refreshActiveHand(false, false, true));
			} else {
				ThrownTrident trident = new ThrownTrident(player, config.isLegacy(), stack);
				Pos position = player.getPosition().add(0D, player.getEyeHeight(), 0D);
				trident.setInstance(Objects.requireNonNull(player.getInstance()),
						position.sub(0, 0.10000000149011612D, 0));
				
				Vec direction = position.direction();
				position = position.add(direction).sub(0, 0.2, 0); //????????
				
				trident.shoot(position, 2.5, 1.0);
				
				Vec playerVel = player.getVelocity();
				trident.setVelocity(trident.getVelocity().add(playerVel.x(),
						player.isOnGround() ? 0.0 : playerVel.y(), playerVel.z()));
				
				if (player.getChunk() != null) player.getChunk().getViewersAsAudience().playSound(Sound.sound(
						SoundEvent.ITEM_TRIDENT_THROW, Sound.Source.PLAYER,
						1.0f, 1.0f
				), trident);
				if (!player.isCreative()) player.setItemInHand(event.getHand(), stack.consume(1));
			}
		}).filter(event -> event.getItemStack().material() == Material.TRIDENT).build());
		
		if (config.isTridentEnabled()) node.addListener(PlayerTickEvent.class, event -> {
			if (event.getPlayer().getEntityMeta().isInRiptideSpinAttack()) {
				Player player = event.getPlayer();
				long ticks = player.getAliveTicks() - player.getTag(RIPTIDE_START);
				AtomicBoolean stopRiptide = new AtomicBoolean(ticks >= 20);
				
				assert player.getInstance() != null;
				player.getInstance().getEntityTracker().nearbyEntities(player.getPosition(), 5,
						EntityTracker.Target.ENTITIES, entity -> {
					if (entity != player && !stopRiptide.get() && entity instanceof LivingEntity
							&& entity.getBoundingBox().intersectEntity(entity.getPosition(), player)) {
						stopRiptide.set(true);
						AttackManager.performAttack(player, entity, config.isLegacy() ?
								AttackConfig.LEGACY : AttackConfig.DEFAULT);
						if (player instanceof PvpPlayer pvpPlayer)
							pvpPlayer.mulVelocity(-0.2);
					}
				});
				
				//TODO detect player bouncing against wall
				
				if (stopRiptide.get())
					event.getPlayer().refreshActiveHand(false, false, false);
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
	
	public static AbstractArrow createArrow(ItemStack stack, @Nullable Entity shooter, boolean legacy) {
		if (stack.material() == Material.SPECTRAL_ARROW) {
			return new SpectralArrow(shooter);
		} else {
			Arrow arrow = new Arrow(shooter, legacy);
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
		return stack.withMeta(CrossbowMeta.class, meta -> meta.charged(charged));
	}
	
	public static ItemStack setCrossbowProjectile(ItemStack stack, ItemStack projectile) {
		return stack.withMeta(CrossbowMeta.class, meta -> meta.projectile(projectile));
	}
	
	public static ItemStack setCrossbowProjectiles(ItemStack stack, ItemStack projectile1,
	                                               ItemStack projectile2, ItemStack projectile3) {
		return stack.withMeta(CrossbowMeta.class, meta -> meta.projectiles(projectile1, projectile2, projectile3));
	}
	
	public static boolean crossbowContainsProjectile(ItemStack stack, Material projectile) {
		CrossbowMeta meta = stack.meta(CrossbowMeta.class);
		if (meta.getProjectiles().get(0).material() == projectile) return true;
		if (meta.getProjectiles().size() < 2) return false;
		if (meta.getProjectiles().get(1).material() == projectile) return true;
		return meta.getProjectiles().get(2).material() == projectile;
	}
	
	public static int getCrossbowUseDuration(ItemStack stack) {
		return getCrossbowChargeDuration(stack) + 3;
	}
	
	public static int getCrossbowChargeDuration(ItemStack stack) {
		int quickCharge = EnchantmentUtils.getLevel(Enchantment.QUICK_CHARGE, stack);
		return quickCharge == 0 ? 25 : 25 - 5 * quickCharge;
	}
	
	public static SoundEvent getCrossbowStartSound(int quickCharge) {
		return switch (quickCharge) {
			case 1 -> SoundEvent.ITEM_CROSSBOW_QUICK_CHARGE_1;
			case 2 -> SoundEvent.ITEM_CROSSBOW_QUICK_CHARGE_2;
			case 3 -> SoundEvent.ITEM_CROSSBOW_QUICK_CHARGE_3;
			default -> SoundEvent.ITEM_CROSSBOW_LOADING_START;
		};
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
			player.getInventory().setItemStack(projectileSlot, projectile.withAmount(projectile.amount() - 1));
		}
		
		return stack;
	}
	
	public static ItemStack performCrossbowShooting(Player player, Player.Hand hand, ItemStack stack,
	                                           double power, double spread, boolean legacy) {
		CrossbowMeta meta = stack.meta(CrossbowMeta.class);
		ItemStack projectile = meta.getProjectiles().get(0);
		if (!projectile.isAir()) {
			shootCrossbowProjectile(player, hand, stack, projectile, 1.0F, power, spread, 0.0F, legacy);
		}
		
		if (meta.getProjectiles().size() > 2) {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			boolean firstHighPitch = random.nextBoolean();
			float firstPitch = getRandomShotPitch(firstHighPitch, random);
			float secondPitch = getRandomShotPitch(!firstHighPitch, random);
			
			projectile = meta.getProjectiles().get(1);
			if (!projectile.isAir()) {
				shootCrossbowProjectile(player, hand, stack, projectile, firstPitch, power, spread, -10.0F, legacy);
			}
			projectile = meta.getProjectiles().get(2);
			if (!projectile.isAir()) {
				shootCrossbowProjectile(player, hand, stack, projectile, secondPitch, power, spread, 10.0F, legacy);
			}
		}
		
		return setCrossbowProjectile(stack, ItemStack.AIR);
	}
	
	public static void shootCrossbowProjectile(Player player, Player.Hand hand, ItemStack crossbowStack,
	                                           ItemStack projectile, float soundPitch,
	                                           double power, double spread, float yaw, boolean legacy) {
		boolean firework = projectile.material() == Material.FIREWORK_ROCKET;
		if (firework) return; //TODO firework
		
		AbstractArrow arrow = getCrossbowArrow(player, crossbowStack, projectile, legacy);
		if (player.isCreative() || yaw != 0.0) {
			arrow.pickupMode = AbstractArrow.PickupMode.CREATIVE_ONLY;
		}
		
		Pos position = player.getPosition().add(0D, player.getEyeHeight(), 0D);
		arrow.setInstance(Objects.requireNonNull(player.getInstance()),
				position.sub(0, 0.10000000149011612D, 0)); // Yeah wait what
		
		position = position.withYaw(position.yaw() + yaw);
		Vec direction = position.direction();
		position = position.add(direction).sub(0, 0.2, 0); //????????
		
		arrow.shoot(position, power, spread);
		
		ItemUtils.damageEquipment(player, hand == Player.Hand.MAIN ?
				EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, firework ? 3 : 1);
		
		SoundManager.sendToAround(player, SoundEvent.ITEM_CROSSBOW_SHOOT, Sound.Source.PLAYER, 1.0F, soundPitch);
	}
	
	public static AbstractArrow getCrossbowArrow(Player player, ItemStack crossbowStack, ItemStack projectile, boolean legacy) {
		AbstractArrow arrow = createArrow(projectile, player, legacy);
		arrow.setCritical(true); // Player shooter is always critical
		arrow.setSound(SoundEvent.ITEM_CROSSBOW_HIT);
		
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
