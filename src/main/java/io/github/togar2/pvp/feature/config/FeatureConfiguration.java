package io.github.togar2.pvp.feature.config;

import io.github.togar2.pvp.feature.CombatFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class FeatureConfiguration {
	private final Map<FeatureType<?>, CombatFeature> combatFeatures = new HashMap<>();
	
	public FeatureConfiguration() {}
	
	public FeatureConfiguration(Map<FeatureType<?>, CombatFeature> combatFeatures) {
		this.combatFeatures.putAll(combatFeatures);
	}
	
	public FeatureConfiguration add(FeatureType<?> type, CombatFeature feature) {
		combatFeatures.put(type, feature);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends CombatFeature> @NotNull T get(FeatureType<T> type) {
		return (T) combatFeatures.computeIfAbsent(type, FeatureType::noopFeature);
	}
	
	@SuppressWarnings("unchecked")
	<T extends CombatFeature> @Nullable T getRaw(FeatureType<T> type) {
		return (T) combatFeatures.get(type);
	}
	
	public Set<FeatureType<?>> listTypes() {
		return combatFeatures.keySet();
	}
	
	public int size() {
		return combatFeatures.size();
	}
	
	public void forEach(BiConsumer<FeatureType<?>, CombatFeature> consumer) {
		combatFeatures.forEach(consumer);
	}
	
	public FeatureConfiguration shallowCopy() {
		FeatureConfiguration clone = new FeatureConfiguration();
		clone.combatFeatures.putAll(combatFeatures);
		return clone;
	}
	
	private static final FeatureConfiguration empty = new FeatureConfiguration();
	public static FeatureConfiguration empty() {
		return empty;
	}
}
