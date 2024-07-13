package io.github.togar2.pvp.feature.explosion;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

/**
 * Combat feature which handles explosions. Contains a method to prime an explosive at a certain place.
 * <p>
 * Important to note is that implementations of this feature might provide an {@link ExplosionSupplier}.
 * This explosion supplier should be registered to every (Minestom) instance which should allow explosions.
 * See {@link ExplosionFeature#getExplosionSupplier()}.
 */
public interface ExplosionFeature extends CombatFeature {
	ExplosionFeature NO_OP = new ExplosionFeature() {
		@Override
		public @Nullable ExplosionSupplier getExplosionSupplier() {
			return null;
		}
		
		@Override
		public void primeExplosive(Instance instance, Point blockPosition, @Nullable LivingEntity cause, int fuse) {}
	};
	
	@Nullable ExplosionSupplier getExplosionSupplier();
	
	void primeExplosive(Instance instance, Point blockPosition, @Nullable LivingEntity cause, int fuse);
}
