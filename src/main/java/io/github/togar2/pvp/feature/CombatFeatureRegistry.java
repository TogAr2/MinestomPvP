package io.github.togar2.pvp.feature;

import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

public class CombatFeatureRegistry {
	private static final Collection<Class<? extends CombatFeature>> features = new HashSet<>();
	
	private static EventNode<Event> global;
	
	public static void register(Class<? extends CombatFeature> clazz) {
		if (features.add(clazz)) {
			if (global != null) trySetup(clazz);
		}
	}
	
	public static void init() {
		global = EventNode.all("combat-global");
		MinecraftServer.getGlobalEventHandler().addChild(global);
		features.forEach(CombatFeatureRegistry::trySetup);
	}
	
	private static void trySetup(Class<? extends CombatFeature> clazz) {
		for (Method method : clazz.getDeclaredMethods()) {
			for (Annotation annotation : method.getAnnotations()) {
				if (annotation.annotationType() == CombatSetup.class) {
					method.setAccessible(true);
					try {
						method.invoke(null, global);
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}
}
