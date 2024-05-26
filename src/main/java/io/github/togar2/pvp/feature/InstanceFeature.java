package io.github.togar2.pvp.feature;

import net.minestom.server.event.EventFilter;
import net.minestom.server.event.trait.InstanceEvent;

public interface InstanceFeature extends RegistrableFeature<InstanceEvent> {
	@Override
	default EventFilter<InstanceEvent, ?> getEventFilter() {
		return EventFilter.INSTANCE;
	}
}
