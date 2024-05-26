package io.github.togar2.pvp.feature;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;

public interface RegistrableFeature extends CombatFeature {
	EventFilter<EntityInstanceEvent, Entity> ENTITY_INSTANCE_FILTER = EventFilter
			.from(EntityInstanceEvent.class, Entity.class, EntityEvent::getEntity);
	
	default int getPriority() {
		return 0;
	}
	
	void init(EventNode<EntityInstanceEvent> node);
	
	default EventNode<EntityInstanceEvent> createNode() {
		var node = EventNode.type(getClass().getTypeName(), ENTITY_INSTANCE_FILTER);
		node.setPriority(getPriority());
		init(node);
		return node;
	}
}
