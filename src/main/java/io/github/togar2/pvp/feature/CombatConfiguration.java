package io.github.togar2.pvp.feature;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CombatConfiguration {
	private final Set<CombatFeature> features = new HashSet<>();
	private final Set<LazyFeatureInit> lazyFeatures = new HashSet<>();
	
	public CombatConfiguration add(IndependentFeature feature) {
		features.add(feature);
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
		
		lazyFeatures.add(new LazyFeatureInit(constructor.getDeclaringClass(), constructor, parameterTypes));
		return this;
	}
	
	public CombatFeatureSet build() {
		List<CombatFeature> result = new ArrayList<>(features);
		System.out.println("build start");
		
		List<LazyFeatureInit> buildOrder = getBuildOrder();
		for (LazyFeatureInit feature : buildOrder) {
			try {
				result.add(construct(feature));
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		System.out.println("build done");
		return new CombatFeatureSet(result.toArray(CombatFeature[]::new));
	}
	
	private CombatFeature construct(LazyFeatureInit feature) throws InvocationTargetException,
																	InstantiationException, IllegalAccessException {
		Object[] initArgs = new Object[feature.dependsOn.length];
		
		for (int i = 0; i < feature.dependsOn.length; i++) {
			Class<?> dependClass = feature.dependsOn[i];
			CombatFeature dependFeature = getFeatureOf(dependClass);
			if (dependFeature != null) {
				initArgs[i] = dependFeature;
			} else {
				LazyFeatureInit dependLazyInit = getLazyFeatureOf(dependClass);
				assert dependLazyInit != null;
				initArgs[i] = construct(dependLazyInit);
			}
		}
		
		return feature.constructor.newInstance(initArgs);
	}
	
	/**
	 * Performs a (recursive) topological sort to make sure all the features
	 * that are depended on by other features are first in the list
	 *
	 * @return the list with ordering
	 */
	private List<LazyFeatureInit> getBuildOrder() {
		List<LazyFeatureInit> order = new ArrayList<>(lazyFeatures.size());
		Set<LazyFeatureInit> visiting = new HashSet<>();
		
		while (true) {
			// Find unprocessed feature
			LazyFeatureInit unprocessed = null;
			for (LazyFeatureInit lazyFeature : lazyFeatures) {
				if (order.contains(lazyFeature)) continue;
				if (visiting.contains(lazyFeature)) continue;
				unprocessed = lazyFeature;
			}
			
			if (unprocessed == null) break;
			visit(order, visiting, unprocessed);
		}
		
		return order;
	}
	
	private void visit(List<LazyFeatureInit> order, Set<LazyFeatureInit> visiting, LazyFeatureInit current) {
		if (order.contains(current)) return; // Feature has already been added
		if (visiting.contains(current)) throw new RuntimeException("Configuration has a recursive dependency");
		
		visiting.add(current);
		
		for (Class<?> dependClass : current.dependsOn) {
			if (getFeatureOf(dependClass) != null) continue;
			LazyFeatureInit lazyDepend = getLazyFeatureOf(dependClass);
			if (lazyDepend == null) throw new RuntimeException("A feature of type " + dependClass.getTypeName()
					+ " is required for " + current.featureClass.getTypeName());
			
			visit(order, visiting, lazyDepend);
		}
		
		visiting.remove(current);
		order.add(current);
	}
	
	private @Nullable CombatFeature getFeatureOf(Class<?> clazz) {
		for (CombatFeature feature : features) {
			if (clazz.isAssignableFrom(feature.getClass()))
				return feature;
		}
		
		return null;
	}
	
	private @Nullable LazyFeatureInit getLazyFeatureOf(Class<?> clazz) {
		for (LazyFeatureInit feature : lazyFeatures) {
			if (clazz.isAssignableFrom(feature.featureClass))
				return feature;
		}
		
		return null;
	}
	
	private record LazyFeatureInit(Class<?> featureClass, Constructor<CombatFeature> constructor, Class<?>[] dependsOn) {}
}
