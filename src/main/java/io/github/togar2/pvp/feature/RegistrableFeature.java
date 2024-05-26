package io.github.togar2.pvp.feature;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;

public interface RegistrableFeature<T extends Event> extends CombatFeature {
	void init(EventNode<T> node);
	
	EventFilter<T, ?> getEventFilter();
	
	default EventNode<T> createNode() {
		EventNode<T> node = EventNode.type(getClass().getTypeName(), getEventFilter());
		init(node);
		return node;
	}
}
