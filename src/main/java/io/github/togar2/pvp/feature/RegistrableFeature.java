package io.github.togar2.pvp.feature;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

public interface RegistrableFeature extends CombatFeature {
	void init(EventNode<Event> node);
	
	default EventNode<Event> createNode() {
		EventNode<Event> node = EventNode.all(getClass().getTypeName());
		init(node);
		return node;
	}
}
