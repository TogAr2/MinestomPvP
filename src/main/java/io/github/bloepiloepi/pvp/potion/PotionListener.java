package io.github.bloepiloepi.pvp.potion;

import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffect;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffects;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionType;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionTypes;
import io.github.bloepiloepi.pvp.projectile.ProjectileListener;
import io.github.bloepiloepi.pvp.projectile.ThrownPotion;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.*;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityPotionAddEvent;
import net.minestom.server.event.entity.EntityPotionRemoveEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.player.PlayerEatEvent;
import net.minestom.server.event.player.PlayerPreEatEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;
import net.minestom.server.utils.time.TimeUnit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PotionListener {
	private static final ItemStack GLASS_BOTTLE = ItemStack.of(Material.GLASS_BOTTLE);
	
	private static final Map<TimedPotion, Integer> durationLeftMap = new ConcurrentHashMap<>();
	
	public static EventNode<EntityEvent> events() {
		EventNode<EntityEvent> node = EventNode.type("potion-events", EventFilter.ENTITY);
		
		node.addListener(EntityTickEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity)) return;
			
			Entity entity = event.getEntity();
			
			for (TimedPotion potion : entity.getActiveEffects()) {
				durationLeftMap.putIfAbsent(potion, potion.getPotion().getDuration() - 1);
				int durationLeft = durationLeftMap.get(potion);
				
				if (durationLeft > 0) {
					CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.getPotion().getEffect());
					byte amplifier = potion.getPotion().getAmplifier();
					
					if (customPotionEffect.canApplyUpdateEffect(durationLeft, amplifier)) {
						customPotionEffect.applyUpdateEffect((LivingEntity) entity, amplifier);
					}
					
					durationLeftMap.put(potion, durationLeft - 1);
				}
			}
			
			//TODO keep track of underlying potions with longer duration
			durationLeftMap.keySet().removeIf(potion -> !entity.getActiveEffects().contains(potion));
		});
		
		node.addListener(EntityPotionAddEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity)) return;
			
			CustomPotionEffect customPotionEffect = CustomPotionEffects.get(event.getPotion().getEffect());
			customPotionEffect.onApplied((LivingEntity) event.getEntity(), event.getPotion().getAmplifier());
			
			updatePotionVisibility((LivingEntity) event.getEntity());
		});
		
		node.addListener(EntityPotionRemoveEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity)) return;
			
			CustomPotionEffect customPotionEffect = CustomPotionEffects.get(event.getPotion().getEffect());
			customPotionEffect.onRemoved((LivingEntity) event.getEntity(), event.getPotion().getAmplifier());
			
			//Delay update 1 tick because we need to have the removing effect removed
			MinecraftServer.getSchedulerManager().buildTask(() ->
					updatePotionVisibility((LivingEntity) event.getEntity())
			).delay(1, TimeUnit.TICK).schedule();
		});
		
		node.addListener(EntityDeathEvent.class, event ->
				event.getEntity().clearEffects());
		
		node.addListener(EventListener.builder(PlayerPreEatEvent.class).handler(event -> {
			event.setEatingTime(32L * MinecraftServer.TICK_MS); //Potion use time is always 32 ticks
		}).filter(event -> event.getFoodItem().getMaterial() == Material.POTION).ignoreCancelled(false).build());
		
		node.addListener(EventListener.builder(PlayerEatEvent.class).handler(event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getFoodItem();
			
			List<Potion> potions = getAllPotions((PotionMeta) stack.getMeta());
			
			//Apply the potions
			for (Potion potion : potions) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.getEffect());
				
				if (customPotionEffect.isInstant()) {
					customPotionEffect.applyInstantEffect(player, player, player, potion.getAmplifier(), 1.0D);
				} else {
					player.addEffect(potion);
				}
			}
			
			if (!player.isCreative()) {
				if (stack.getAmount() == 1) {
					player.setItemInHand(event.getHand(), GLASS_BOTTLE);
				} else {
					player.setItemInHand(event.getHand(), stack.withAmount(stack.getAmount() - 1));
					player.getInventory().addItemStack(GLASS_BOTTLE);
				}
			}
		}).filter(event -> event.getFoodItem().getMaterial() == Material.POTION).build());
		
		node.addListener(EventListener.builder(PlayerUseItemEvent.class).handler(event -> {
			ProjectileListener.onShoot(event.getPlayer(), event.getItemStack(), () -> {
				ThreadLocalRandom random = ThreadLocalRandom.current();
				SoundManager.sendToAround(event.getPlayer(), SoundEvent.SPLASH_POTION_THROW, Sound.Source.PLAYER,
						0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
				
				throwPotion(event.getPlayer(), event.getItemStack(), event.getHand());
			});
		}).filter(event -> event.getItemStack().getMaterial() == Material.SPLASH_POTION).ignoreCancelled(false).build());
		
		node.addListener(EventListener.builder(PlayerUseItemEvent.class).handler(event -> {
			ProjectileListener.onShoot(event.getPlayer(), event.getItemStack(), () -> {
				ThreadLocalRandom random = ThreadLocalRandom.current();
				SoundManager.sendToAround(event.getPlayer(), SoundEvent.LINGERING_POTION_THROW, Sound.Source.NEUTRAL,
						0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
				
				throwPotion(event.getPlayer(), event.getItemStack(), event.getHand());
			});
		}).filter(event -> event.getItemStack().getMaterial() == Material.LINGERING_POTION).ignoreCancelled(false).build());
		
		return node;
	}
	
	private static void throwPotion(Player player, ItemStack stack, Player.Hand hand) {
		ThrownPotion thrownPotion = new ThrownPotion(player);
		thrownPotion.setItem(stack);
		
		Position position = player.getPosition().clone().add(0D, player.getEyeHeight(), 0D);
		thrownPotion.setInstance(Objects.requireNonNull(player.getInstance()), position);
		
		Vector direction = position.getDirection();
		position = position.clone().add(direction.getX(), direction.getY(), direction.getZ());
		
		thrownPotion.shoot(position, 0.5, 1.0);
		
		Vector playerVel = player.getVelocity();
		thrownPotion.setVelocity(thrownPotion.getVelocity().add(playerVel.getX(),
				player.isOnGround() ? 0.0D : playerVel.getY(), playerVel.getZ()));
		
		if (!player.isCreative()) {
			player.setItemInHand(hand, stack.withAmount(stack.getAmount() - 1));
		}
	}
	
	public static void updatePotionVisibility(LivingEntity entity) {
		LivingEntityMeta meta = (LivingEntityMeta) entity.getEntityMeta();
		meta.setNotifyAboutChanges(false);
		
		if (entity instanceof Player) {
			if (((Player) entity).getGameMode() == GameMode.SPECTATOR) {
				meta.setPotionEffectAmbient(false);
				meta.setPotionEffectColor(0);
				meta.setInvisible(true);
				meta.setNotifyAboutChanges(true);
				
				return;
			}
		}
		
		Collection<TimedPotion> effects = entity.getActiveEffects();
		if (effects.isEmpty()) {
			meta.setPotionEffectAmbient(false);
			meta.setPotionEffectColor(0);
			meta.setInvisible(false);
		} else {
			meta.setPotionEffectAmbient(containsOnlyAmbientEffects(effects));
			meta.setPotionEffectColor(getPotionColor(effects.stream().map(TimedPotion::getPotion).collect(Collectors.toList())));
			meta.setInvisible(EntityUtils.hasPotionEffect(entity, PotionEffect.INVISIBILITY));
		}
	}
	
	private static boolean containsOnlyAmbientEffects(Collection<TimedPotion> effects) {
		if (effects.isEmpty()) return true;
		
		for (TimedPotion potion : effects) {
			if (!isAmbient(potion.getPotion().getFlags())) {
				return false;
			}
		}
		
		return true;
	}
	
	public static int getColor(ItemStack stack) {
		PotionMeta meta = (PotionMeta) stack.getMeta();
		if (meta.getColor() != null) {
			return meta.getColor().asRGB();
		} else {
			return meta.getPotionType() == PotionType.EMPTY ? 16253176 : getPotionColor(getAllPotions(meta));
		}
	}
	
	public static int getPotionColor(Collection<Potion> effects) {
		if (effects.isEmpty()) {
			return 3694022;
		}
		
		float r = 0.0F;
		float g = 0.0F;
		float b = 0.0F;
		int totalAmplifier = 0;
		
		for (Potion potion : effects) {
			if (PotionListener.hasParticles(potion.getFlags())) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.getEffect());
				int color = customPotionEffect.getColor();
				int amplifier = potion.getAmplifier() + 1;
				r += (float) (amplifier * (color >> 16 & 255)) / 255.0F;
				g += (float) (amplifier * (color >> 8 & 255)) / 255.0F;
				b += (float) (amplifier * (color & 255)) / 255.0F;
				totalAmplifier += amplifier;
			}
		}
		
		if (totalAmplifier == 0) {
			return 0;
		} else {
			r = r / (float) totalAmplifier * 255.0F;
			g = g / (float) totalAmplifier * 255.0F;
			b = b / (float) totalAmplifier * 255.0F;
			return (int) r << 16 | (int) g << 8 | (int) b;
		}
	}
	
	public static List<Potion> getAllPotions(PotionMeta meta) {
		return getAllPotions(meta.getPotionType(), meta.getCustomPotionEffects());
	}
	
	public static List<Potion> getAllPotions(PotionType potionType,
	                                         Collection<net.minestom.server.potion.CustomPotionEffect> customEffects) {
		//PotionType effects plus custom effects
		List<Potion> potions = new ArrayList<>();
		
		CustomPotionType customPotionType = CustomPotionTypes.get(potionType);
		if (customPotionType != null) {
			potions.addAll(customPotionType.getEffects());
		}
		
		potions.addAll(customEffects.stream().map((customPotion) ->
				new Potion(Objects.requireNonNull(PotionEffect.fromId(customPotion.getId())),
						customPotion.getAmplifier(), customPotion.getDuration(),
						customPotion.showParticles(), customPotion.showIcon(),
						customPotion.isAmbient()))
				.collect(Collectors.toList()));
		
		return potions;
	}
	
	public static boolean isAmbient(byte flags) {
		return (flags & 0x01) > 0;
	}
	
	public static boolean hasParticles(byte flags) {
		return (flags & 0x02) > 0;
	}
	
	public static boolean hasIcon(byte flags) {
		return (flags & 0x04) > 0;
	}
}
