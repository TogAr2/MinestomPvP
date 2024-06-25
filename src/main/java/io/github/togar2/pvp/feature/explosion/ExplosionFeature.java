package io.github.togar2.pvp.feature.explosion;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.ExplosionSupplier;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.Nullable;

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
