package io.github.togar2.pvp.feature;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

public interface CombatFeature {
	default void init(EventNode<Event> node) {
	}
}
