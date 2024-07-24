package io.github.togar2.pvp.feature.explosion;

import io.github.togar2.pvp.entity.explosion.TntEntity;
import io.github.togar2.pvp.feature.FeatureType;
import io.github.togar2.pvp.feature.config.DefinedFeature;
import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Vanilla implementation of {@link ExplosionFeature}
 * <p>
 * Provides an explosion supplier which can be registered to an instance,
 * see {@link VanillaExplosionFeature#getExplosionSupplier()}.
 */
public class VanillaExplosionFeature implements ExplosionFeature {
	public static final DefinedFeature<VanillaExplosionFeature> DEFINED = new DefinedFeature<>(
			FeatureType.EXPLOSION, VanillaExplosionFeature::new,
			FeatureType.ENCHANTMENT
	);
	
	private final FeatureConfiguration configuration;
	
	private VanillaExplosionSupplier explosionSupplier;
	
	public VanillaExplosionFeature(FeatureConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public void initDependencies() {
		this.explosionSupplier = new VanillaExplosionSupplier(this, configuration.get(FeatureType.ENCHANTMENT));
	}
	
	@Override
	public VanillaExplosionSupplier getExplosionSupplier() {
		return explosionSupplier;
	}
	
	@Override
	public void primeExplosive(Instance instance, Point blockPosition, @Nullable LivingEntity cause, int fuse) {
		TntEntity entity = new TntEntity(cause);
		entity.setFuse(fuse);
		entity.setInstance(instance, blockPosition.add(0.5, 0, 0.5));
		entity.getViewersAsAudience().playSound(Sound.sound(
				SoundEvent.ENTITY_TNT_PRIMED, Sound.Source.BLOCK,
				1.0f, 1.0f
		), entity);
	}
}
