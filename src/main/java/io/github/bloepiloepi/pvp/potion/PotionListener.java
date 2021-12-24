package io.github.bloepiloepi.pvp.potion;

import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.food.FoodListener;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffect;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffects;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionType;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionTypes;
import io.github.bloepiloepi.pvp.projectile.ThrownPotion;
import io.github.bloepiloepi.pvp.utils.SoundManager;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
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
import net.minestom.server.utils.time.TimeUnit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class PotionListener {
	private static final ItemStack GLASS_BOTTLE = ItemStack.of(Material.GLASS_BOTTLE);
	
	private static final Map<TimedPotion, Integer> durationLeftMap = new ConcurrentHashMap<>();
	
	public static EventNode<EntityEvent> events(boolean legacy) {
		EventNode<EntityEvent> node = EventNode.type("potion-events", EventFilter.ENTITY);
		
		node.addListener(EntityTickEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity)) return;
			
			Entity entity = event.getEntity();
			
			for (TimedPotion potion : entity.getActiveEffects()) {
				durationLeftMap.putIfAbsent(potion, potion.getPotion().duration() - 1);
				int durationLeft = durationLeftMap.get(potion);
				
				if (durationLeft > 0) {
					CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.getPotion().effect());
					byte amplifier = potion.getPotion().amplifier();
					
					if (customPotionEffect.canApplyUpdateEffect(durationLeft, amplifier)) {
						customPotionEffect.applyUpdateEffect((LivingEntity) entity, amplifier, legacy);
					}
					
					durationLeftMap.put(potion, durationLeft - 1);
				}
			}
			
			//TODO keep track of underlying potions with longer duration
			durationLeftMap.keySet().removeIf(potion -> !entity.getActiveEffects().contains(potion));
		});
		
		node.addListener(EntityPotionAddEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity)) return;
			
			CustomPotionEffect customPotionEffect = CustomPotionEffects.get(event.getPotion().effect());
			customPotionEffect.onApplied((LivingEntity) event.getEntity(), event.getPotion().amplifier(), legacy);
			
			updatePotionVisibility((LivingEntity) event.getEntity());
		});
		
		node.addListener(EntityPotionRemoveEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity)) return;
			
			CustomPotionEffect customPotionEffect = CustomPotionEffects.get(event.getPotion().effect());
			customPotionEffect.onRemoved((LivingEntity) event.getEntity(), event.getPotion().amplifier(), legacy);
			
			//Delay update 1 tick because we need to have the removing effect removed
			MinecraftServer.getSchedulerManager().buildTask(() ->
					updatePotionVisibility((LivingEntity) event.getEntity())
			).delay(1, TimeUnit.SERVER_TICK).schedule();
		});
		
		node.addListener(EntityDeathEvent.class, event ->
				event.getEntity().clearEffects());
		
		node.addListener(EventListener.builder(PlayerPreEatEvent.class).handler(event -> {
			event.setEatingTime(32L * MinecraftServer.TICK_MS); //Potion use time is always 32 ticks
		}).filter(event -> event.getFoodItem().getMaterial() == Material.POTION).build());
		
		node.addListener(EventListener.builder(PlayerEatEvent.class).handler(event -> {
			Player player = event.getPlayer();
			ItemStack stack = event.getFoodItem();
			
			FoodListener.triggerEatSounds(player, null);
			
			List<Potion> potions = getAllPotions((PotionMeta) stack.getMeta(), legacy);
			
			//Apply the potions
			for (Potion potion : potions) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.effect());
				
				if (customPotionEffect.isInstant()) {
					customPotionEffect.applyInstantEffect(player, player, player, potion.amplifier(), 1.0D, legacy);
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
			ThreadLocalRandom random = ThreadLocalRandom.current();
			SoundManager.sendToAround(event.getPlayer(), SoundEvent.ENTITY_SPLASH_POTION_THROW, Sound.Source.PLAYER,
					0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
			
			throwPotion(event.getPlayer(), event.getItemStack(), event.getHand(), legacy);
		}).filter(event -> event.getItemStack().getMaterial() == Material.SPLASH_POTION).build());
		
		node.addListener(EventListener.builder(PlayerUseItemEvent.class).handler(event -> {
			ThreadLocalRandom random = ThreadLocalRandom.current();
			SoundManager.sendToAround(event.getPlayer(), SoundEvent.ENTITY_LINGERING_POTION_THROW, Sound.Source.NEUTRAL,
					0.5f, 0.4f / (random.nextFloat() * 0.4f + 0.8f));
			
			throwPotion(event.getPlayer(), event.getItemStack(), event.getHand(), legacy);
		}).filter(event -> event.getItemStack().getMaterial() == Material.LINGERING_POTION).build());
		
		return node;
	}
	
	private static void throwPotion(Player player, ItemStack stack, Player.Hand hand, boolean legacy) {
		ThrownPotion thrownPotion = new ThrownPotion(player, legacy);
		thrownPotion.setItem(stack);
		
		Pos position = player.getPosition().add(0D, player.getEyeHeight() - 0.1, 0D);
		thrownPotion.setInstance(Objects.requireNonNull(player.getInstance()), position);
		
		Vec direction = position.direction();
		position = position.add(direction.x(), direction.y() + 0.2, direction.z());
		
		thrownPotion.shoot(position, 0.5, 1.0);
		
		Vec playerVel = player.getVelocity();
		thrownPotion.setVelocity(thrownPotion.getVelocity().add(playerVel.x(),
				player.isOnGround() ? 0.0D : playerVel.y(), playerVel.z()));
		
		if (!player.isCreative()) {
			player.setItemInHand(hand, stack.withAmount(stack.getAmount() - 1));
		}
	}
	
	public static void updatePotionVisibility(LivingEntity entity) {
		LivingEntityMeta meta = (LivingEntityMeta) entity.getEntityMeta();
		
		if (entity instanceof Player) {
			if (((Player) entity).getGameMode() == GameMode.SPECTATOR) {
				meta.setPotionEffectAmbient(false);
				meta.setPotionEffectColor(0);
				meta.setInvisible(true);
				
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
			if (!potion.getPotion().isAmbient()) {
				return false;
			}
		}
		
		return true;
	}
	
	public static int getColor(ItemStack stack, boolean legacy) {
		PotionMeta meta = (PotionMeta) stack.getMeta();
		if (meta.getColor() != null) {
			return meta.getColor().asRGB();
		} else {
			return meta.getPotionType() == PotionType.EMPTY ? 16253176 : getPotionColor(getAllPotions(meta, legacy));
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
			if (potion.hasParticles()) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.effect());
				int color = customPotionEffect.getColor();
				int amplifier = potion.amplifier() + 1;
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
	
	public static List<Potion> getAllPotions(PotionMeta meta, boolean legacy) {
		return getAllPotions(meta.getPotionType(), meta.getCustomPotionEffects(), legacy);
	}
	
	public static List<Potion> getAllPotions(PotionType potionType,
	                                         Collection<net.minestom.server.potion.CustomPotionEffect> customEffects,
	                                         boolean legacy) {
		//PotionType effects plus custom effects
		List<Potion> potions = new ArrayList<>();
		
		CustomPotionType customPotionType = CustomPotionTypes.get(potionType);
		if (customPotionType != null) {
			potions.addAll(legacy ? customPotionType.getLegacyEffects() : customPotionType.getEffects());
		}
		
		potions.addAll(customEffects.stream().map((customPotion) ->
				new Potion(Objects.requireNonNull(PotionEffect.fromId(customPotion.id())),
						customPotion.amplifier(), customPotion.duration(),
						createFlags(
								customPotion.isAmbient(),
								customPotion.showParticles(),
								customPotion.showIcon()
						)))
				.collect(Collectors.toList()));
		
		return potions;
	}
	
	public static byte createFlags(boolean ambient, boolean particles, boolean icon) {
		byte flags = 0;
		if (ambient) {
			flags = (byte) (flags | 0x01);
		}
		if (particles) {
			flags = (byte) (flags | 0x02);
		}
		if (icon) {
			flags = (byte) (flags | 0x04);
		}
		return flags;
	}
	
	private static final byte DEFAULT_FLAGS = createFlags(false, true, true);
	public static byte defaultFlags() {
		return DEFAULT_FLAGS;
	}
}
