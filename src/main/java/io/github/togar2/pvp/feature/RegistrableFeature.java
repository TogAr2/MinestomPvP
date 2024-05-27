package io.github.togar2.pvp.feature;

import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

public interface RegistrableFeature extends CombatFeature {
	default int getPriority() {
		return 0;
	}
	
	default void initPlayer(Player player, boolean firstInit) {}
	
	void init(EventNode<EntityInstanceEvent> node);
	
	default EventNode<EntityInstanceEvent> createNode() {
		return CombatFeatureRegistry.createNode(this);
	}
}
