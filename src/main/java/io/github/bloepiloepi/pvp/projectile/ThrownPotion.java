package io.github.bloepiloepi.pvp.projectile;

import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffect;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffects;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionType;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionTypes;
import io.github.bloepiloepi.pvp.utils.EffectManager;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.effects.Effects;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.item.ThrownPotionMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.metadata.PotionMeta;
import net.minestom.server.potion.Potion;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ThrownPotion extends EntityHittableProjectile {
	
	public ThrownPotion(@Nullable Entity shooter) {
		super(shooter, EntityType.POTION);
	}
	
	@Override
	public void onHit(@Nullable Entity entity) {
		ItemStack item = getItem();
		
		PotionMeta meta = (PotionMeta) item.getMeta();
		CustomPotionType baseType = CustomPotionTypes.get(meta.getPotionType());
		List<Potion> potions = PotionListener.getAllPotions(meta);
		
		if (!potions.isEmpty()) {
			if (item.getMaterial() == Material.LINGERING_POTION) {
				//TODO lingering
			} else {
				applySplash(potions, entity);
			}
		}
		
		BlockPosition blockPosition = getPosition().toBlockPosition();
		Effects effect = baseType.hasInstantEffect() ? Effects.INSTANT_SPLASH : Effects.SPLASH_POTION;
		EffectManager.sendNearby(Objects.requireNonNull(getInstance()), effect, blockPosition.getX(),
				blockPosition.getY(), blockPosition.getZ(), PotionListener.getColor(item), 64.0D, false);
		
		remove();
	}
	
	private void applySplash(List<Potion> potions, @Nullable Entity hitEntity) {
		BoundingBox boundingBox = getBoundingBox().expand(8.0D, 4.0D, 8.0D);
		List<LivingEntity> entities = Objects.requireNonNull(getInstance()).getEntities().stream()
				.filter(boundingBox::intersect).filter(entity -> entity instanceof LivingEntity)
				.map(entity -> (LivingEntity) entity).collect(Collectors.toList());
		
		if (entities.isEmpty()) return;
		
		Entity source = getShooter() == null ? this : getShooter();
		
		for (LivingEntity entity : entities) {
			if (entity.getEntityType() == EntityType.ARMOR_STAND) continue;
			
			double distanceSquared = getDistanceSquared(entity);
			if (distanceSquared >= 16.0D) continue;
			
			double proximity = entity == hitEntity ? 1.0D : (1.0D - Math.sqrt(distanceSquared) / 4.0D);
			
			for (Potion potion : potions) {
				CustomPotionEffect customPotionEffect = CustomPotionEffects.get(potion.getEffect());
				if (customPotionEffect.isInstant()) {
					customPotionEffect.applyInstantEffect(this, getShooter(), entity,potion.getAmplifier(), proximity);
				} else {
					int duration = (int) (proximity * (double) potion.getDuration() + 0.5D);
					if (duration > 20) {
						entity.addEffect(new Potion(potion.getEffect(), potion.getAmplifier(), duration,
								(potion.getFlags() & 0x02) > 0, (potion.getFlags() & 0x04) > 0,
								(potion.getFlags() & 0x01) > 0));
					}
				}
			}
		}
	}
	
	@NotNull
	public ItemStack getItem() {
		return ((ThrownPotionMeta) getEntityMeta()).getItem();
	}
}
