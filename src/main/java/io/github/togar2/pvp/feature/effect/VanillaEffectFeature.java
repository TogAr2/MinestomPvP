package io.github.togar2.pvp.feature.effect;

import io.github.togar2.pvp.events.PotionVisibilityEvent;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.config.FeatureType;
import io.github.togar2.pvp.potion.effect.CustomPotionEffect;
import io.github.togar2.pvp.potion.effect.CustomPotionEffects;
import io.github.togar2.pvp.utils.CombatVersion;
import io.github.togar2.pvp.utils.PotionUtils;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDeathEvent;
import net.minestom.server.event.entity.EntityPotionAddEvent;
import net.minestom.server.event.entity.EntityPotionRemoveEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.time.TimeUnit;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VanillaEffectFeature implements EffectFeature, RegistrableFeature {
	public static final Tag<Map<PotionEffect, Integer>> DURATION_LEFT = Tag.Transient("effectDurationLeft");
	
	private final CombatVersion version;
	
	public VanillaEffectFeature(FeatureConfiguration configuration) {
		this.version = configuration.get(FeatureType.VERSION);
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		node.addListener(EntityDeathEvent.class, event ->
				event.getEntity().clearEffects());
		
		node.addListener(EntityTickEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity entity)) return;
			Map<PotionEffect, Integer> potionMap = getDurationLeftMap(entity);
			
			for (TimedPotion potion : entity.getActiveEffects()) {
				potionMap.putIfAbsent(potion.potion().effect(), potion.potion().duration() - 1);
				int durationLeft = potionMap.get(potion.potion().effect());
				
				if (durationLeft > 0) {
					CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.potion().effect());
					byte amplifier = potion.potion().amplifier();
					
					if (customPotionEffect.canApplyUpdateEffect(durationLeft, amplifier)) {
						customPotionEffect.applyUpdateEffect(entity, amplifier, version);
					}
					
					potionMap.put(potion.potion().effect(), durationLeft - 1);
				}
			}
			
			//TODO keep track of underlying potions with longer duration
			if (potionMap.size() != entity.getActiveEffects().size()) {
				potionMap.keySet().removeIf(effect -> !entity.hasEffect(effect));
			}
		});
		
		node.addListener(EntityPotionAddEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity entity)) return;
			Map<PotionEffect, Integer> potionMap = getDurationLeftMap(entity);
			potionMap.put(event.getPotion().effect(), event.getPotion().duration());
			
			CustomPotionEffect customPotionEffect = CustomPotionEffects.get(event.getPotion().effect());
			customPotionEffect.onApplied(entity, event.getPotion().amplifier(), version);
			
			updatePotionVisibility(entity);
		});
		
		node.addListener(EntityPotionRemoveEvent.class, event -> {
			if (!(event.getEntity() instanceof LivingEntity entity)) return;
			
			CustomPotionEffect customPotionEffect = CustomPotionEffects.get(event.getPotion().effect());
			customPotionEffect.onRemoved(entity, event.getPotion().amplifier(), version);
			
			//Delay update 1 tick because we need to have the removing effect removed
			MinecraftServer.getSchedulerManager()
					.buildTask(() -> updatePotionVisibility(entity))
					.delay(1, TimeUnit.SERVER_TICK)
					.schedule();
		});
	}
	
	private Map<PotionEffect, Integer> getDurationLeftMap(Entity entity) {
		Map<PotionEffect, Integer> potionMap = entity.getTag(DURATION_LEFT);
		if (potionMap == null) {
			potionMap = new ConcurrentHashMap<>();
			entity.setTag(DURATION_LEFT, potionMap);
		}
		return potionMap;
	}
	
	protected static void updatePotionVisibility(LivingEntity entity) {
		boolean ambient;
		int color;
		boolean invisible;
		
		if (entity instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) {
			ambient = false;
			color = 0;
			invisible = true;
		} else {
			Collection<TimedPotion> effects = entity.getActiveEffects();
			if (effects.isEmpty()) {
				ambient = false;
				color = 0;
				invisible = false;
			} else {
				ambient = containsOnlyAmbientEffects(effects);
				color = PotionUtils.getPotionColor(effects.stream().map(TimedPotion::potion).collect(Collectors.toList()));
				invisible = entity.hasEffect(PotionEffect.INVISIBILITY);
			}
		}
		
		PotionVisibilityEvent potionVisibilityEvent = new PotionVisibilityEvent(entity, ambient, color, invisible);
		EventDispatcher.callCancellable(potionVisibilityEvent, () -> {
			LivingEntityMeta meta = (LivingEntityMeta) entity.getEntityMeta();
			
			meta.setPotionEffectAmbient(potionVisibilityEvent.isAmbient());
			meta.setPotionEffectColor(potionVisibilityEvent.getColor());
			meta.setInvisible(potionVisibilityEvent.isInvisible());
		});
	}
	
	private static boolean containsOnlyAmbientEffects(Collection<TimedPotion> effects) {
		if (effects.isEmpty()) return true;
		
		for (TimedPotion potion : effects) {
			if (!potion.potion().isAmbient()) {
				return false;
			}
		}
		
		return true;
	}
}
