package io.github.togar2.pvp.feature.potion;

import io.github.togar2.pvp.entity.Tracker;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.potion.effect.CustomPotionEffect;
import io.github.togar2.pvp.potion.effect.CustomPotionEffects;
import io.github.togar2.pvp.potion.item.CustomPotionType;
import io.github.togar2.pvp.potion.item.CustomPotionTypes;
import io.github.togar2.pvp.projectile.ThrownPotion;
import io.github.togar2.pvp.utils.CombatVersion;
import io.github.togar2.pvp.utils.PotionUtils;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerPreEatEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.sound.SoundEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class VanillaPotionFeature implements PotionFeature, RegistrableFeature {
	private static final int USE_TICKS = 32;
	private static final ItemStack GLASS_BOTTLE = ItemStack.of(Material.GLASS_BOTTLE);
	
	private final CombatVersion version;
	
	public VanillaPotionFeature(CombatVersion version) {
		this.version = version;
	}
	
	@Override
	public void init(EventNode<Event> node) {
		node.addListener(PlayerPreEatEvent.class, event -> {
			if (event.getItemStack().material() == Material.POTION) {
				event.setEatingTime((long) USE_TICKS * MinecraftServer.TICK_MS); // Potion use time is always 32 ticks
			}
		});
		
		node.addListener(ItemUpdateStateEvent.class, event -> {
			if (event.getItemStack().material() != Material.POTION) return;
			if (!event.getPlayer().isEating()) return; // Temporary hack, waiting on Minestom PR #2128
			
			Player player = event.getPlayer();
			ItemStack stack = event.getItemStack();
			
			triggerDrinkingSound(player);
			
			List<Potion> potions = getAllPotions(stack.meta(PotionMeta.class));
			
			// Apply the potions
			for (Potion potion : potions) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.effect());
				
				if (customPotionEffect.isInstant()) {
					customPotionEffect.applyInstantEffect(player, player, player, potion.amplifier(), 1.0, version);
				} else {
					player.addEffect(potion);
				}
			}
			
			if (!player.isCreative()) {
				if (stack.amount() == 1) {
					player.setItemInHand(event.getHand(), GLASS_BOTTLE);
				} else {
					player.setItemInHand(event.getHand(), stack.withAmount(stack.amount() - 1));
					player.getInventory().addItemStack(GLASS_BOTTLE);
				}
			}
		});
		
		node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			if (player.isSilent() || !player.isEating()) return;
			
			tickDrinkingSounds(player);
		});
		
		node.addListener(PlayerUseItemEvent.class, event -> {
			if (event.getItemStack().material() != Material.SPLASH_POTION) return;
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			ViewUtil.viewersAndSelf(event.getPlayer()).playSound(Sound.sound(
					SoundEvent.ENTITY_SPLASH_POTION_THROW, Sound.Source.PLAYER,
					0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f)
			), event.getPlayer());
			
			throwPotion(event.getPlayer(), event.getItemStack(), event.getHand());
		});
		
		node.addListener(PlayerUseItemEvent.class, event -> {
			if (event.getItemStack().material() != Material.LINGERING_POTION) return;
			
			ThreadLocalRandom random = ThreadLocalRandom.current();
			ViewUtil.viewersAndSelf(event.getPlayer()).playSound(Sound.sound(
					SoundEvent.ENTITY_LINGERING_POTION_THROW, Sound.Source.NEUTRAL,
					0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f)
			), event.getPlayer());
			
			throwPotion(event.getPlayer(), event.getItemStack(), event.getHand());
		});
	}
	
	protected void throwPotion(Player player, ItemStack stack, Player.Hand hand) {
		ThrownPotion thrownPotion = new ThrownPotion(player, version.legacy());
		thrownPotion.setItem(stack);
		
		Pos position = player.getPosition().add(0D, player.getEyeHeight() - 0.1, 0D);
		thrownPotion.setInstance(Objects.requireNonNull(player.getInstance()), position);
		
		thrownPotion.shootFromRotation(position.pitch(), position.yaw(), -20, 0.5, 1.0);
		
		Vec playerVel = player.getVelocity();
		thrownPotion.setVelocity(thrownPotion.getVelocity().add(playerVel.x(),
				player.isOnGround() ? 0.0D : playerVel.y(), playerVel.z()));
		
		if (!player.isCreative()) {
			player.setItemInHand(hand, stack.withAmount(stack.amount() - 1));
		}
	}
	
	protected List<Potion> getAllPotions(PotionMeta meta) {
		return getAllPotions(meta.getPotionType(), meta.getCustomPotionEffects());
	}
	
	protected List<Potion> getAllPotions(PotionType potionType,
	                                     Collection<net.minestom.server.potion.CustomPotionEffect> customEffects) {
		//PotionType effects plus custom effects
		List<Potion> potions = new ArrayList<>();
		
		CustomPotionType customPotionType = CustomPotionTypes.get(potionType);
		if (customPotionType != null) {
			potions.addAll(version.legacy() ? customPotionType.getLegacyEffects() : customPotionType.getEffects());
		}
		
		potions.addAll(customEffects.stream().map((customPotion) ->
				new Potion(Objects.requireNonNull(PotionEffect.fromId(customPotion.id())),
						customPotion.amplifier(), customPotion.duration(),
						PotionUtils.createFlags(
								customPotion.isAmbient(),
								customPotion.showParticles(),
								customPotion.showIcon()
						))).toList());
		
		return potions;
	}
	
	protected void tickDrinkingSounds(Player player) {
		ItemStack stack = player.getItemInHand(Objects.requireNonNull(player.getEatingHand()));
		if (stack.material() != Material.POTION) return;
		
		long usedDuration = System.currentTimeMillis() - player.getTag(Tracker.ITEM_USE_START_TIME);
		long usedTicks = usedDuration / MinecraftServer.TICK_MS;
		long remainingUseTicks = USE_TICKS - usedTicks;
		
		boolean canTrigger = remainingUseTicks <= USE_TICKS - 7;
		boolean shouldTrigger = canTrigger && remainingUseTicks % 4 == 0;
		if (!shouldTrigger) return;
		
		triggerDrinkingSound(player);
	}
	
	protected void triggerDrinkingSound(Player player) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		
		player.getViewersAsAudience().playSound(Sound.sound(
				SoundEvent.ENTITY_GENERIC_DRINK, Sound.Source.PLAYER,
				0.5f, random.nextFloat() * 0.1f + 0.9f
		));
	}
}
