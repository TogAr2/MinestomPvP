package io.github.togar2.pvp.feature;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

public class CombatConfiguration {
	private final Set<ConstructableFeature> features = new HashSet<>();
	
	public final CombatConfiguration addAll(Collection<Class<? extends CombatFeature>> classes) {
		for (Class<? extends CombatFeature> clazz : classes) {
			add(clazz);
		}
		return this;
	}
	
	@SafeVarargs
	public final CombatConfiguration addAll(Class<? extends CombatFeature>... classes) {
		for (Class<? extends CombatFeature> clazz : classes) {
			add(clazz);
		}
		return this;
	}
	
	public CombatConfiguration add(ConstructableFeature feature) {
		features.add(feature);
		return this;
	}
	
	public CombatConfiguration add(CombatFeature feature) {
		return add(wrap(feature));
	}
	
	public CombatConfiguration add(Class<? extends CombatFeature> clazz, CombatFeature... override) {
		return add(wrapIndependentOverride(clazz, override));
	}
	
	public static ConstructableFeature wrap(CombatFeature feature) {
		return new ConstructedFeature(feature);
	}
	
	public static ConstructableFeature wrapIndependentOverride(Class<? extends CombatFeature> clazz, CombatFeature... override) {
		ConstructableFeature[] overrideArray = new ConstructableFeature[override.length];
		for (int i = 0; i < override.length; i++) {
			overrideArray[i] = wrap(override[i]);
		}
		return wrap(clazz, overrideArray);
	}
	
	@SafeVarargs
	public static ConstructableFeature wrapLazyOverride(Class<? extends CombatFeature> clazz, Class<? extends CombatFeature>... override) {
		ConstructableFeature[] overrideArray = new ConstructableFeature[override.length];
		for (int i = 0; i < override.length; i++) {
			overrideArray[i] = wrap(override[i]);
		}
		return wrap(clazz, overrideArray);
	}
	
	public static ConstructableFeature wrap(Class<? extends CombatFeature> clazz, ConstructableFeature... override) {
		if (clazz.getDeclaredConstructors().length != 1)
			throw new RuntimeException("Cannot determine how to construct " + clazz.getTypeName());
		
		//noinspection unchecked
		return wrap((Constructor<CombatFeature>) clazz.getDeclaredConstructors()[0], override);
	}
	
	public static ConstructableFeature wrap(Constructor<CombatFeature> constructor, ConstructableFeature... override) {
		Class<?>[] parameterTypes = constructor.getParameterTypes();
		
		for (Class<?> type : parameterTypes) {
			if (!CombatFeature.class.isAssignableFrom(type)) {
				throw new RuntimeException(type.getTypeName() + " cannot be assigned to " + CombatFeature.class.getTypeName()
						+ " (constructor of " + constructor.getDeclaringClass().getTypeName() + ")");
			}
		}
		
		for (ConstructableFeature overrideFeature : override) {
			boolean found = false;
			for (Class<?> parameterType : parameterTypes) {
				if (parameterType.isAssignableFrom(overrideFeature.featureClass)) {
					found = true;
					break;
				}
			}
			
			if (!found) throw new RuntimeException("Constructor of " + constructor.getDeclaringClass().getTypeName()
					+ " does not take argument of type " + overrideFeature.featureClass.getTypeName());
		}
		
		return new LazyFeatureInit(constructor, parameterTypes, override);
	}
	
	public CombatFeatureSet build() {
		List<CombatFeature> result = new ArrayList<>(features.size());
		
		List<ConstructableFeature> buildOrder = getBuildOrder();
		for (ConstructableFeature feature : buildOrder) {
			try {
				result.add(feature.construct(this::getFeatureOf));
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
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
				ConstructableFeature dependFeature = lazy.getOverrideOf(dependClass);
				if (dependFeature == null) dependFeature = getFeatureOf(dependClass);
				
				if (dependFeature == null) throw new RuntimeException("A feature of type " + dependClass.getTypeName()
						+ " is required for " + current.featureClass.getTypeName());
				
				visit(order, visiting, dependFeature);
			}
			
			visiting.remove(current);
		}
		
		order.add(current);
	}
	
	private @Nullable ConstructableFeature getFeatureOf(Class<?> clazz) {
		for (ConstructableFeature feature : features) {
			if (clazz.isAssignableFrom(feature.featureClass))
				return feature;
		}
		
		return null;
	}
	
	public sealed abstract static class ConstructableFeature {
		private final Class<?> featureClass;
		
		public ConstructableFeature(Class<?> featureClass) {
			this.featureClass = featureClass;
		}
		
		abstract CombatFeature construct(Function<Class<?>, @Nullable ConstructableFeature> getter) throws
				InvocationTargetException, InstantiationException, IllegalAccessException;
	}
	
	private static final class ConstructedFeature extends ConstructableFeature {
		private final CombatFeature feature;
		
		private ConstructedFeature(CombatFeature feature) {
			super(feature.getClass());
			this.feature = feature;
		}
		
		@Override
		CombatFeature construct(Function<Class<?>, @Nullable ConstructableFeature> getter) {
			return feature;
		}
	}
	
	private static final class LazyFeatureInit extends ConstructableFeature {
		private final Constructor<CombatFeature> constructor;
		private final Class<?>[] dependsOn;
		private final ConstructableFeature[] override;
		
		public LazyFeatureInit(Constructor<CombatFeature> constructor, Class<?>[] dependsOn,
		                       ConstructableFeature[] override) {
			super(constructor.getDeclaringClass());
			this.constructor = constructor;
			this.dependsOn = dependsOn;
			this.override = override;
		}
		
		public @Nullable ConstructableFeature getOverrideOf(Class<?> clazz) {
			for (ConstructableFeature feature : override) {
				if (clazz.isAssignableFrom(feature.featureClass))
					return feature;
			}
			
			return null;
		}
		
		@Override
		CombatFeature construct(Function<Class<?>, @Nullable ConstructableFeature> getter) throws
				InvocationTargetException, InstantiationException, IllegalAccessException {
			Object[] initArgs = new Object[dependsOn.length];
			
			for (int i = 0; i < dependsOn.length; i++) {
				Class<?> dependClass = dependsOn[i];
				ConstructableFeature dependFeature = getOverrideOf(dependClass);
				if (dependFeature == null) dependFeature = getter.apply(dependClass);
				assert dependFeature != null;
				initArgs[i] = dependFeature.construct(getter);
			}
			
			return constructor.newInstance(initArgs);
		}
	}
}
