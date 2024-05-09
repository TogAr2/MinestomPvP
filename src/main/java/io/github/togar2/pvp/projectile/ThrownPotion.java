package io.github.togar2.pvp.projectile;

import io.github.togar2.pvp.potion.PotionListener;
import io.github.togar2.pvp.potion.effect.CustomPotionEffect;
import io.github.togar2.pvp.potion.effect.CustomPotionEffects;
import io.github.togar2.pvp.potion.item.CustomPotionType;
import io.github.togar2.pvp.utils.CombatVersion;
import io.github.togar2.pvp.utils.EffectManager;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.effects.Effects;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.item.ThrownPotionMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ThrownPotion extends CustomEntityProjectile implements ItemHoldingProjectile {
	private final boolean legacy;
	
	public ThrownPotion(@Nullable Entity shooter, boolean legacy) {
		super(shooter, EntityType.POTION);
		this.legacy = legacy;
		
		// Why does Minestom have the wrong value 0.03 in its registries?
		setAerodynamics(getAerodynamics().withGravity(0.05));
	}
	
	@Override
	public boolean onHit(Entity entity) {
		splash(entity);
		return true;
	}
	
	@Override
	public boolean onStuck() {
		splash(null);
		return true;
	}
	
	public void splash(@Nullable Entity entity) {
		ItemStack item = getItem();
		
		PotionMeta meta = item.meta(PotionMeta.class);
		List<Potion> potions = PotionListener.getAllPotions(meta, legacy);
		
		if (!potions.isEmpty()) {
			if (item.material() == Material.LINGERING_POTION) {
				//TODO lingering
			} else {
				applySplash(potions, entity);
			}
		}
		
		Pos position = getPosition();
		Effects effect = CustomPotionType.hasInstantEffect(potions) ? Effects.INSTANT_SPLASH : Effects.SPLASH_POTION;
		EffectManager.sendNearby(
				Objects.requireNonNull(getInstance()), effect, position.blockX(),
				position.blockY(), position.blockZ(), PotionListener.getColor(item, legacy),
				64.0D, false
		);
	}
	
	private void applySplash(List<Potion> potions, @Nullable Entity hitEntity) {
		BoundingBox boundingBox = getBoundingBox().expand(8.0, 4.0, 8.0);
		List<LivingEntity> entities = Objects.requireNonNull(getInstance()).getEntities().stream()
				.filter(entity -> boundingBox.intersectEntity(getPosition().add(0, -2, 0), entity))
				.filter(entity -> entity instanceof LivingEntity)
				.map(entity -> (LivingEntity) entity).collect(Collectors.toList());
		
		if (hitEntity instanceof LivingEntity && !entities.contains(hitEntity))
			entities.add((LivingEntity) hitEntity);
		if (entities.isEmpty()) return;
		
		for (LivingEntity entity : entities) {
			if (entity.getEntityType() == EntityType.ARMOR_STAND) continue;
			
			double distanceSquared = getDistanceSquared(entity);
			if (distanceSquared >= 16.0) continue;
			
			double proximity = entity == hitEntity ? 1.0 : (1.0 - Math.sqrt(distanceSquared) / 4.0);
			
			for (Potion potion : potions) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.effect());
				if (customPotionEffect.isInstant()) {
					customPotionEffect.applyInstantEffect(this, getShooter(),
							entity,potion.amplifier(), proximity, CombatVersion.fromLegacy(legacy));
				} else {
					int duration = potion.duration();
					if (legacy) duration = (int) Math.floor(duration * 0.75);
					duration = (int) (proximity * (double) duration + 0.5);
					
					if (duration > 20) {
						entity.addEffect(new Potion(potion.effect(), potion.amplifier(), duration, potion.flags()));
					}
				}
			}
		}
	}
	
	@NotNull
	public ItemStack getItem() {
		return ((ThrownPotionMeta) getEntityMeta()).getItem();
	}
	
	@Override
	public void setItem(@NotNull ItemStack item) {
		((ThrownPotionMeta) getEntityMeta()).setItem(item);
	}
}
