package io.github.togar2.pvp.feature;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerRespawnEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;

import java.util.ArrayList;
import java.util.List;

public class CombatFeatureRegistry {
	private static final EventFilter<EntityInstanceEvent, Entity> ENTITY_INSTANCE_FILTER = EventFilter
			.from(EntityInstanceEvent.class, Entity.class, EntityEvent::getEntity);
	
	private static final EventNode<Event> initNode = EventNode.all("combat-feature-init");
	private static final List<RegistrableFeature> features = new ArrayList<>();
	
	public static EventNode<EntityInstanceEvent> createNode(RegistrableFeature feature) {
		if (!features.contains(feature)) {
			features.add(feature);
			initNode.addListener(AsyncPlayerConfigurationEvent.class, event -> feature.initPlayer(event.getPlayer(), true));
			initNode.addListener(PlayerSpawnEvent.class, event -> feature.initPlayer(event.getPlayer(), false));
			initNode.addListener(PlayerRespawnEvent.class, event -> feature.initPlayer(event.getPlayer(), false));
		}
		
		var node = EventNode.type(feature.getClass().getTypeName(), ENTITY_INSTANCE_FILTER);
		node.setPriority(feature.getPriority());
		feature.init(node);
		return node;
	}
	
	public static void init() {
		MinecraftServer.getGlobalEventHandler().addChild(initNode);
	}
}
