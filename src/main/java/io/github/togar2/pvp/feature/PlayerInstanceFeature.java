package io.github.togar2.pvp.feature;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;

public interface PlayerInstanceFeature extends RegistrableFeature<PlayerInstanceEvent> {
	EventFilter<PlayerInstanceEvent, Player> PLAYER_INSTANCE_FILTER = EventFilter
			.from(PlayerInstanceEvent.class, Player.class, PlayerEvent::getEntity);
	
	@Override
	default EventFilter<PlayerInstanceEvent, ?> getEventFilter() {
		return PLAYER_INSTANCE_FILTER;
	}
}
