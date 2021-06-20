package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.entities.Tracker;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.PlayerEvent;

public class PositionListener {
	
	public static void register(EventNode<EntityEvent> eventNode) {
		EventNode<PlayerEvent> node = EventNode.type("position-events", EventFilter.PLAYER);
		eventNode.addChild(node);
		
		node.addListener(PlayerMoveEvent.class, event -> Tracker.falling.put(event.getPlayer().getUuid(),
				event.getNewPosition().getY() - event.getPlayer().getPosition().getY() < 0));
	}
}
