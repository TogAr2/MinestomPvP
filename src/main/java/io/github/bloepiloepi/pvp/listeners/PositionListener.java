package io.github.bloepiloepi.pvp.listeners;

import io.github.bloepiloepi.pvp.entities.Tracker;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerMoveEvent;

public class PositionListener {
	
	public static void register(GlobalEventHandler eventHandler) {
		eventHandler.addEventCallback(PlayerMoveEvent.class, event -> {
			Tracker.falling.put(event.getPlayer().getUuid(),
					event.getNewPosition().getY() - event.getPlayer().getPosition().getY() < 0);
		});
	}
}
