package io.github.togar2.pvp.feature.config;

import io.github.togar2.pvp.feature.CombatFeature;

import java.util.Set;

public record DefinedFeature<F extends CombatFeature>(
		FeatureType<?> featureType, Set<FeatureType<?>> dependencies,
		Constructor<F> constructor) {
	
	public DefinedFeature(FeatureType<? super F> featureType, Constructor<F> constructor, FeatureType<?>... dependencies) {
		this(featureType, Set.of(dependencies), constructor);
	}
	
	F construct(FeatureConfiguration configuration) {
		return constructor.construct(configuration);
	}
	
	@FunctionalInterface
	public interface Constructor<F> {
		F construct(FeatureConfiguration configuration);
	}
}
