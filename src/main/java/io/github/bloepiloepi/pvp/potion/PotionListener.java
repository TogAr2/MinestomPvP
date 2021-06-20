package io.github.bloepiloepi.pvp.potion;

import io.github.bloepiloepi.pvp.entities.EntityUtils;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffect;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffects;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionTypes;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityPotionAddEvent;
import net.minestom.server.event.entity.EntityPotionRemoveEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.player.PlayerEatEvent;
import net.minestom.server.event.player.PlayerPreEatEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.utils.time.TimeUnit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PotionListener {
	private static final ItemStack GLASS_BOTTLE = ItemStack.of(Material.GLASS_BOTTLE);
	
	private static final Map<TimedPotion, Integer> durationLeftMap = new ConcurrentHashMap<>();
	
	public static void register(GlobalEventHandler eventHandler) {
		eventHandler.addEventCallback(EntityTickEvent.class, event -> {
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
		
		eventHandler.addEventCallback(EntityPotionAddEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity)) return;
			
			CustomPotionEffect customPotionEffect = CustomPotionEffects.get(event.getPotion().getEffect());
			customPotionEffect.onApplied((LivingEntity) event.getEntity(), event.getPotion().getAmplifier());
			
			updatePotionVisibility((LivingEntity) event.getEntity());
		});
		
		eventHandler.addEventCallback(EntityPotionRemoveEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity)) return;
			
			CustomPotionEffect customPotionEffect = CustomPotionEffects.get(event.getPotion().getEffect());
			customPotionEffect.onRemoved((LivingEntity) event.getEntity(), event.getPotion().getAmplifier());
			
			//Delay update 1 tick because we need to have the removing effect removed
			MinecraftServer.getSchedulerManager().buildTask(() ->
					updatePotionVisibility((LivingEntity) event.getEntity())
			).delay(1, TimeUnit.TICK).schedule();
		});
		
		eventHandler.addEventCallback(EntityDeathEvent.class, event ->
				event.getEntity().clearEffects());
		
		eventHandler.addEventCallback(PlayerPreEatEvent.class, event -> {
			if (event.getFoodItem().getMaterial() != Material.POTION) return;
			event.setEatingTime(32L * MinecraftServer.TICK_MS); //Potion use time is always 32 ticks
		});
		
		eventHandler.addEventCallback(PlayerEatEvent.class, event -> {
			if (event.getFoodItem().getMaterial() != Material.POTION) return;
			
			Player player = event.getPlayer();
			ItemStack stack = event.getFoodItem();
			PotionMeta meta = (PotionMeta) stack.getMeta();
			
			//PotionType effects plus custom effects
			List<Potion> potions = new ArrayList<>();
			potions.addAll(CustomPotionTypes.get(meta.getPotionType()).getEffects());
			potions.addAll(meta.getCustomPotionEffects().stream().map((customPotion) ->
					new Potion(PotionEffect.fromId(customPotion.getId()), customPotion.getAmplifier(),
							customPotion.getDuration(), customPotion.showParticles(),
							customPotion.showIcon(), customPotion.isAmbient()))
					.collect(Collectors.toList()));
			
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
		});
	}
	
	private static void updatePotionVisibility(LivingEntity entity) {
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
			meta.setPotionEffectColor(getPotionColor(effects));
			meta.setInvisible(EntityUtils.hasPotionEffect(entity, PotionEffect.INVISIBILITY));
		}
	}
	
	private static boolean containsOnlyAmbientEffects(Collection<TimedPotion> effects) {
		if (effects.isEmpty()) return true;
		
		for (TimedPotion potion : effects) {
			//If no ambient
			if ((potion.getPotion().getFlags() & 0x01) <= 0) {
				return false;
			}
		}
		
		return true;
	}
	
	private static int getPotionColor(Collection<TimedPotion> effects) {
		if (effects.isEmpty()) {
			return 3694022;
		}
		
		float r = 0.0F;
		float g = 0.0F;
		float b = 0.0F;
		int totalAmplifier = 0;
		
		for (TimedPotion timedPotion : effects) {
			Potion potion = timedPotion.getPotion();
			
			//If should show particles
			if ((potion.getFlags() & 0x02) > 0) {
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
}
