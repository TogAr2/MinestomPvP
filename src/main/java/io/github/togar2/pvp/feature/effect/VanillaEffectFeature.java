package io.github.togar2.pvp.feature.effect;

import io.github.togar2.pvp.events.PotionVisibilityEvent;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import io.github.togar2.pvp.feature.potion.PotionFeature;
import io.github.togar2.pvp.potion.effect.CustomPotionEffect;
import io.github.togar2.pvp.potion.effect.CustomPotionEffects;
import io.github.togar2.pvp.potion.item.CustomPotionType;
import io.github.togar2.pvp.potion.item.CustomPotionTypes;
import io.github.togar2.pvp.projectile.Arrow;
import io.github.togar2.pvp.utils.CombatVersion;
import io.github.togar2.pvp.utils.PotionFlags;
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
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.PotionType;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.time.TimeUnit;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VanillaEffectFeature implements EffectFeature, RegistrableFeature {
	public static final DefinedFeature<VanillaEffectFeature> DEFINED = new DefinedFeature<>(
			FeatureType.EFFECT, VanillaEffectFeature::new,
			FeatureType.POTION, FeatureType.VERSION
	);
	
	public static final Tag<Map<PotionEffect, Integer>> DURATION_LEFT = Tag.Transient("effectDurationLeft");
	public static final int DEFAULT_POTION_COLOR = 0xff385dc6;
	
	private final PotionFeature potionFeature;
	private final CombatVersion version;
	
	public VanillaEffectFeature(FeatureConfiguration configuration) {
		this.potionFeature = configuration.get(FeatureType.POTION);
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
	
	protected void updatePotionVisibility(LivingEntity entity) {
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
				color = getPotionColor(effects.stream().map(TimedPotion::potion).collect(Collectors.toList()));
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
	
	private boolean containsOnlyAmbientEffects(Collection<TimedPotion> effects) {
		if (effects.isEmpty()) return true;
		
		for (TimedPotion potion : effects) {
			if (!potion.potion().isAmbient()) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void addArrowEffects(LivingEntity entity, Arrow arrow) {
		PotionMeta potionMeta = arrow.getPotion();
		
		CustomPotionType customPotionType = CustomPotionTypes.get(potionMeta.getPotionType());
		if (customPotionType != null) {
			for (Potion potion : customPotionType.getEffects(version)) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.effect());
				if (customPotionEffect.isInstant()) {
					customPotionEffect.applyInstantEffect(arrow, null,
							entity, potion.amplifier(), 1.0D, version);
				} else {
					int duration = Math.max(potion.duration() / 8, 1);
					entity.addEffect(new Potion(potion.effect(), potion.amplifier(), duration, potion.flags()));
				}
			}
		}
		
		if (potionMeta.getCustomPotionEffects().isEmpty()) return;
		
		potionMeta.getCustomPotionEffects().stream().map(customPotion ->
						new Potion(Objects.requireNonNull(PotionEffect.fromId(customPotion.id())),
								customPotion.amplifier(), customPotion.duration(),
								PotionFlags.create(
										customPotion.isAmbient(),
										customPotion.showParticles(),
										customPotion.showIcon()
								)))
				.forEach(potion -> {
					CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.effect());
					if (customPotionEffect.isInstant()) {
						customPotionEffect.applyInstantEffect(arrow, null,
								entity, potion.amplifier(), 1.0D, version);
					} else {
						entity.addEffect(new Potion(potion.effect(), potion.amplifier(),
								potion.duration(), potion.flags()));
					}
				});
	}
	
	@Override
	public void addSplashPotionEffects(LivingEntity entity, List<Potion> potions, double proximity,
	                                   @Nullable Entity source, @Nullable Entity attacker) {
		for (Potion potion : potions) {
			CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.effect());
			if (customPotionEffect.isInstant()) {
				customPotionEffect.applyInstantEffect(source, attacker,
						entity, potion.amplifier(), proximity, version);
			} else {
				int duration = potion.duration();
				if (version.legacy()) duration = (int) Math.floor(duration * 0.75);
				duration = (int) (proximity * (double) duration + 0.5);
				
				if (duration > 20) {
					entity.addEffect(new Potion(potion.effect(), potion.amplifier(), duration, potion.flags()));
				}
			}
		}
	}
	
	@Override
	public boolean hasInstantEffect(Collection<Potion> effects) {
		if (effects.isEmpty()) return false;
		
		for (Potion potion : effects) {
			if (CustomPotionEffects.get(potion.effect()).isInstant()) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int getPotionColor(PotionContents contents) {
		if (contents.customColor() != null) {
			return contents.customColor();
		}
		
		int r = 0, g = 0, b = 0;
		int total = 0;
		
		if (contents.getColor() != null) {
			return contents.getColor().asRGB();
		} else {
			return contents.getPotionType() == PotionType.EMPTY ? 16253176 : getPotionColor(potionFeature.getAllPotions(contents));
		}
	}
	
	@Override
	public int getPotionColor(Collection<Potion> effects) {
		return PotionColorUtils.getPotionColor(effects);
	}
}
