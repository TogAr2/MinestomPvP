package io.github.togar2.pvp.feature;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombatConfiguration {
	private final Set<ConstructableFeature> features = new HashSet<>();
	
	public CombatConfiguration add(IndependentFeature feature) {
		features.add(new ConstructedFeature(feature));
		return this;
	}
	
	public CombatConfiguration add(Class<? extends CombatFeature> clazz) {
		if (clazz.getDeclaredConstructors().length != 1)
			throw new RuntimeException("Cannot determine how to construct " + clazz.getTypeName());
		
		//noinspection unchecked
		add((Constructor<CombatFeature>) clazz.getDeclaredConstructors()[0]);
		return this;
	}
	
	public CombatConfiguration add(Constructor<CombatFeature> constructor) {
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		
		for (Class<?> type : parameterTypes) {
			if (!CombatFeature.class.isAssignableFrom(type)) {
				throw new RuntimeException(type.getTypeName() + " cannot be assigned to " + CombatFeature.class.getTypeName()
						+ " (constructor of " + constructor.getDeclaringClass().getTypeName() + ")");
			}
		}
		
		features.add(new LazyFeatureInit(constructor, parameterTypes));
		return this;
	}
	
	public CombatFeatureSet build() {
		List<CombatFeature> result = new ArrayList<>(features.size());
		System.out.println("build start");
		
		List<ConstructableFeature> buildOrder = getBuildOrder();
		for (ConstructableFeature feature : buildOrder) {
			try {
				result.add(feature.construct());
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		System.out.println("build done");
		return new CombatFeatureSet(result.toArray(CombatFeature[]::new));
	}
	
	/**
	 * Performs a (recursive) topological sort to make sure all the features
	 * that are depended on by other features are first in the list
	 *
	 * @return the list with ordering
	 */
	private List<ConstructableFeature> getBuildOrder() {
		List<ConstructableFeature> order = new ArrayList<>(features.size());
		Set<ConstructableFeature> visiting = new HashSet<>();
		
		while (true) {
			// Find unprocessed feature
			ConstructableFeature unprocessed = null;
			for (ConstructableFeature feature : features) {
				if (order.contains(feature)) continue;
				if (visiting.contains(feature)) continue;
				unprocessed = feature;
			}
			
			if (unprocessed == null) break;
			visit(order, visiting, unprocessed);
		}
		
		return order;
	}
	
	private void visit(List<ConstructableFeature> order, Set<ConstructableFeature> visiting, ConstructableFeature current) {
		if (order.contains(current)) return; // Feature has already been added
		if (visiting.contains(current)) throw new RuntimeException("Configuration has a recursive dependency");
		
		if (current instanceof LazyFeatureInit lazy) {
			visiting.add(current);
			
			for (Class<?> dependClass : lazy.dependsOn) {
				ConstructableFeature dependFeature = getFeatureOf(dependClass);
				if (dependFeature == null) throw new RuntimeException("A feature of type " + dependClass.getTypeName()
						+ " is required for " + current.featureClass.getTypeName());
				
				visit(order, visiting, lazy);
			}
			
			visiting.remove(current);
		}
		
		order.add(current);
	}
	
	private @Nullable ConstructableFeature getFeatureOf(Class<?> clazz) {
		for (ConstructableFeature feature : features) {
			if (clazz.isAssignableFrom(feature.getClass()))
				return feature;
		}
		
		return null;
	}
	
	private sealed abstract static class ConstructableFeature {
		private final Class<?> featureClass;
		
		public ConstructableFeature(Class<?> featureClass) {
			this.featureClass = featureClass;
		}
		
		abstract CombatFeature construct() throws InvocationTargetException, InstantiationException, IllegalAccessException;
	}
	
	static final class ConstructedFeature extends ConstructableFeature {
		private final CombatFeature feature;
		
		private ConstructedFeature(CombatFeature feature) {
			super(feature.getClass());
			this.feature = feature;
		}
		
		@Override
		CombatFeature construct() {
			return feature;
		}
	}
	
	final class LazyFeatureInit extends ConstructableFeature {
		private final Constructor<CombatFeature> constructor;
		private final Class<?>[] dependsOn;
		
		public LazyFeatureInit(Constructor<CombatFeature> constructor, Class<?>[] dependsOn) {
			super(constructor.getDeclaringClass());
			this.constructor = constructor;
			this.dependsOn = dependsOn;
		}
		
		@Override
		CombatFeature construct() throws InvocationTargetException, InstantiationException, IllegalAccessException {
			Object[] initArgs = new Object[dependsOn.length];
			
			for (int i = 0; i < dependsOn.length; i++) {
				Class<?> dependClass = dependsOn[i];
				ConstructableFeature dependFeature = getFeatureOf(dependClass);
				assert dependFeature != null;
				initArgs[i] = dependFeature.construct();
			}
			
			return constructor.newInstance(initArgs);
		}
	}
}
