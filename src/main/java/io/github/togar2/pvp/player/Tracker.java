package io.github.togar2.pvp.player;

import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerItemAnimationEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.tag.Tag;

public class Tracker {
	//TODO move elsewhere
	public static final Tag<Long> ITEM_USE_START_TIME = Tag.Transient("itemUseStartTime");
	
	public static void register(EventNode<? super EntityEvent> eventNode) {
		EventNode<EntityEvent> node = EventNode.type("tracker-events", EventFilter.ENTITY);
		eventNode.addChild(node);
		
		node.addListener(PlayerItemAnimationEvent.class, event ->
				event.getPlayer().setTag(ITEM_USE_START_TIME, System.currentTimeMillis()));
	}
}
