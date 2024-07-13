package io.github.togar2.pvp.feature.config;

import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.feature.FeatureType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * A (mutable) configuration for a feature.
 * It contains dependencies for the feature, accessible by their feature type.
 * See {@link FeatureConfiguration#get(FeatureType)}
 */
public class FeatureConfiguration {
	protected final Map<FeatureType<?>, CombatFeature> combatFeatures = new HashMap<>();
	
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
		return (T) combatFeatures.getOrDefault(type, type.defaultFeature());
	}
	
	@SuppressWarnings("unchecked")
	<T extends CombatFeature> @Nullable T getRaw(FeatureType<T> type) {
		return (T) combatFeatures.get(type);
	}
	
	public Collection<CombatFeature> listFeatures() {
		return combatFeatures.values();
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
	
	FeatureConfiguration overlay() {
		return new Overlay(this);
	}
	
	private static class Overlay extends FeatureConfiguration {
		private final FeatureConfiguration backing;
		
		public Overlay(FeatureConfiguration backing) {
			this.backing = backing;
		}
		
		@Override
		public <T extends CombatFeature> @NotNull T get(FeatureType<T> type) {
			if (super.combatFeatures.containsKey(type)) {
				return super.get(type);
			} else {
				return backing.get(type);
			}
		}
	}
}
