package io.github.togar2.pvp.feature;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;

public interface EntityInstanceFeature extends RegistrableFeature<EntityInstanceEvent> {
	EventFilter<EntityInstanceEvent, Entity> ENTITY_INSTANCE_FILTER = EventFilter
			.from(EntityInstanceEvent.class, Entity.class, EntityEvent::getEntity);
	
	@Override
	default EventFilter<EntityInstanceEvent, ?> getEventFilter() {
		return ENTITY_INSTANCE_FILTER;
	}
}
