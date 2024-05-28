package io.github.togar2.pvp.feature.projectile;

import io.github.togar2.pvp.enchantment.EnchantmentUtils;
import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.entity.Tracker;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.config.FeatureType;
import io.github.togar2.pvp.feature.item.ItemDamageFeature;
import io.github.togar2.pvp.projectile.AbstractArrow;
import io.github.togar2.pvp.projectile.Arrow;
import io.github.togar2.pvp.projectile.SpectralArrow;
import io.github.togar2.pvp.utils.CombatVersion;
import io.github.togar2.pvp.utils.ViewUtil;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.Enchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.CrossbowMeta;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class VanillaCrossbowFeature implements CrossbowFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaCrossbowFeature> DEFINED = new DefinedFeature<>(
			FeatureType.CROSSBOW, VanillaCrossbowFeature::new,
			FeatureType.ITEM_DAMAGE, FeatureType.VERSION
	);
	
	private static final Tag<Boolean> START_SOUND_PLAYED = Tag.Transient("StartSoundPlayed");
	private static final Tag<Boolean> MID_LOAD_SOUND_PLAYED = Tag.Transient("MidLoadSoundPlayed");
	
	private final ItemDamageFeature itemDamageFeature;
	private final CombatVersion version;
	
	public VanillaCrossbowFeature(FeatureConfiguration configuration) {
		this.itemDamageFeature = configuration.get(FeatureType.ITEM_DAMAGE);
		this.version = configuration.get(FeatureType.VERSION);
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(PlayerUseItemEvent.class, event -> {
			ItemStack stack = event.getItemStack();
			if (stack.material() != Material.CROSSBOW) return;
			Player player = event.getPlayer();
			
			if (stack.meta(CrossbowMeta.class).isCharged()) {
				// Make sure the animation event is not called, because this is not an animation
				event.setCancelled(true);
				
				stack = performCrossbowShooting(player, event.getHand(), stack, getCrossbowPower(stack), 1.0);
				player.setItemInHand(event.getHand(), setCrossbowCharged(stack, false));
			} else {
				if (EntityUtils.getProjectile(player,
						Arrow.ARROW_OR_FIREWORK_PREDICATE, Arrow.ARROW_PREDICATE).first().isAir()) {
					event.setCancelled(true);
				} else {
					player.setTag(START_SOUND_PLAYED, false);
					player.setTag(MID_LOAD_SOUND_PLAYED, false);
				}
			}
		});
		
		node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			
			// If not charging crossbow, return
			LivingEntityMeta meta = (LivingEntityMeta) player.getEntityMeta();
			if (!meta.isHandActive() || player.getItemInHand(meta.getActiveHand()).material() != Material.CROSSBOW)
				return;
			
			Player.Hand hand = player.getPlayerMeta().getActiveHand();
			ItemStack stack = player.getItemInHand(hand);
			
			int quickCharge = EnchantmentUtils.getLevel(Enchantment.QUICK_CHARGE, stack);
			
			long useDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
			long useTicks = useDuration / MinecraftServer.TICK_MS;
			double progress = (getCrossbowUseDuration(stack) - useTicks) / (double) getCrossbowChargeDuration(stack);
			
			Boolean startSoundPlayed = player.getTag(START_SOUND_PLAYED);
			Boolean midLoadSoundPlayed = player.getTag(MID_LOAD_SOUND_PLAYED);
			if (startSoundPlayed == null) startSoundPlayed = false;
			if (midLoadSoundPlayed == null) midLoadSoundPlayed = false;
			
			if (progress >= 0.2 && !startSoundPlayed) {
				SoundEvent startSound = getCrossbowStartSound(quickCharge);
				ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
						startSound, Sound.Source.PLAYER,
						0.5f, 1.0f
				), player);
				
				player.setTag(START_SOUND_PLAYED, true);
				player.setItemInHand(hand, stack);
			}
			
			SoundEvent midLoadSound = quickCharge == 0 ? SoundEvent.ITEM_CROSSBOW_LOADING_MIDDLE : null;
			if (progress >= 0.5F && midLoadSound != null && !midLoadSoundPlayed) {
				ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
						midLoadSound, Sound.Source.PLAYER,
						0.5f, 1.0f
				), player);
				
				player.setTag(MID_LOAD_SOUND_PLAYED, true);
				player.setItemInHand(hand, stack);
			}
		});
		
		node.addListener(ItemUpdateStateEvent.class, event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			if (stack.material() != Material.CROSSBOW) return;
			
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
			ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
					SoundEvent.ITEM_CROSSBOW_LOADING_END, Sound.Source.PLAYER,
					1.0f, 1.0f / (random.nextFloat() * 0.5f + 1.0f) + 0.2f
			), player);
			
			player.setItemInHand(event.getHand(), stack);
		});
	}
	
	protected AbstractArrow createArrow(ItemStack stack, @Nullable Entity shooter) {
		if (stack.material() == Material.SPECTRAL_ARROW) {
			return new SpectralArrow(shooter);
		} else {
			Arrow arrow = new Arrow(shooter, version.legacy());
			arrow.inheritEffects(stack);
			return arrow;
		}
	}
	
	protected double getCrossbowPower(ItemStack stack) {
		return crossbowContainsProjectile(stack, Material.FIREWORK_ROCKET) ? 1.6 : 3.15;
	}
	
	protected double getCrossbowPowerForTime(long useDurationMillis, ItemStack stack) {
		long ticks = useDurationMillis / MinecraftServer.TICK_MS;
		double power = ticks / (double) getCrossbowChargeDuration(stack);
		if (power > 1) {
			power = 1;
		}
		
		return power;
	}
	
	protected ItemStack setCrossbowCharged(ItemStack stack, boolean charged) {
		return stack.withMeta(CrossbowMeta.class, meta -> meta.charged(charged));
	}
	
	protected ItemStack setCrossbowProjectile(ItemStack stack, ItemStack projectile) {
		return stack.withMeta(CrossbowMeta.class, meta -> meta.projectile(projectile));
	}
	
	protected ItemStack setCrossbowProjectiles(ItemStack stack, ItemStack projectile1,
	                                               ItemStack projectile2, ItemStack projectile3) {
		return stack.withMeta(CrossbowMeta.class, meta -> meta.projectiles(projectile1, projectile2, projectile3));
	}
	
	protected boolean crossbowContainsProjectile(ItemStack stack, Material projectile) {
		CrossbowMeta meta = stack.meta(CrossbowMeta.class);
		if (meta.getProjectiles().get(0).material() == projectile) return true;
		if (meta.getProjectiles().size() < 2) return false;
		if (meta.getProjectiles().get(1).material() == projectile) return true;
		return meta.getProjectiles().get(2).material() == projectile;
	}
	
	protected int getCrossbowUseDuration(ItemStack stack) {
		return getCrossbowChargeDuration(stack) + 3;
	}
	
	protected int getCrossbowChargeDuration(ItemStack stack) {
		int quickCharge = EnchantmentUtils.getLevel(Enchantment.QUICK_CHARGE, stack);
		return quickCharge == 0 ? 25 : 25 - 5 * quickCharge;
	}
	
	protected SoundEvent getCrossbowStartSound(int quickCharge) {
		return switch (quickCharge) {
			case 1 -> SoundEvent.ITEM_CROSSBOW_QUICK_CHARGE_1;
			case 2 -> SoundEvent.ITEM_CROSSBOW_QUICK_CHARGE_2;
			case 3 -> SoundEvent.ITEM_CROSSBOW_QUICK_CHARGE_3;
			default -> SoundEvent.ITEM_CROSSBOW_LOADING_START;
		};
	}
	
	protected ItemStack loadCrossbowProjectiles(Player player, ItemStack stack) {
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
	
	protected ItemStack performCrossbowShooting(Player player, Player.Hand hand, ItemStack stack,
	                                            double power, double spread) {
		CrossbowMeta meta = stack.meta(CrossbowMeta.class);
		ItemStack projectile = meta.getProjectiles().get(0);
		if (!projectile.isAir()) {
			shootCrossbowProjectile(player, hand, stack, projectile, 1.0F, power, spread, 0.0F);
		}
		
		if (meta.getProjectiles().size() > 2) {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			boolean firstHighPitch = random.nextBoolean();
			float firstPitch = getRandomShotPitch(firstHighPitch, random);
			float secondPitch = getRandomShotPitch(!firstHighPitch, random);
			
			projectile = meta.getProjectiles().get(1);
			if (!projectile.isAir()) {
				shootCrossbowProjectile(player, hand, stack, projectile, firstPitch, power, spread, -10.0F);
			}
			projectile = meta.getProjectiles().get(2);
			if (!projectile.isAir()) {
				shootCrossbowProjectile(player, hand, stack, projectile, secondPitch, power, spread, 10.0F);
			}
		}
		
		return setCrossbowProjectile(stack, ItemStack.AIR);
	}
	
	protected void shootCrossbowProjectile(Player player, Player.Hand hand, ItemStack crossbowStack,
	                                       ItemStack projectile, float soundPitch,
	                                       double power, double spread, float yaw) {
		boolean firework = projectile.material() == Material.FIREWORK_ROCKET;
		if (firework) return; //TODO firework
		
		AbstractArrow arrow = getCrossbowArrow(player, crossbowStack, projectile);
		if (player.isCreative() || yaw != 0.0) {
			arrow.setPickupMode(AbstractArrow.PickupMode.CREATIVE_ONLY);
		}
		
		//TODO fix velocity
		Pos position = player.getPosition().add(0, player.getEyeHeight() - 0.1, 0);
		arrow.setInstance(Objects.requireNonNull(player.getInstance()), position);
		
		position = position.withYaw(position.yaw() + yaw);
		//Vec direction = position.direction();
		//position = position.add(direction).sub(0, 0.2, 0); //????????
		
		arrow.shootFrom(position, power, spread);
		
		itemDamageFeature.damageEquipment(player, hand == Player.Hand.MAIN ?
				EquipmentSlot.MAIN_HAND : EquipmentSlot.OFF_HAND, firework ? 3 : 1);
		
		ViewUtil.viewersAndSelf(player).playSound(Sound.sound(
				SoundEvent.ITEM_CROSSBOW_SHOOT, Sound.Source.PLAYER,
				1.0f, soundPitch
		), player);
	}
	
	protected AbstractArrow getCrossbowArrow(Player player, ItemStack crossbowStack, ItemStack projectile) {
		AbstractArrow arrow = createArrow(projectile, player);
		arrow.setCritical(true); // Player shooter is always critical
		arrow.setSound(SoundEvent.ITEM_CROSSBOW_HIT);
		
		int piercing = EnchantmentUtils.getLevel(Enchantment.PIERCING, crossbowStack);
		if (piercing > 0) {
			arrow.setPiercingLevel((byte) piercing);
		}
		
		return arrow;
	}
	
	protected float getRandomShotPitch(boolean high, ThreadLocalRandom random) {
		float base = high ? 0.63F : 0.43F;
		return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + base;
	}
}
