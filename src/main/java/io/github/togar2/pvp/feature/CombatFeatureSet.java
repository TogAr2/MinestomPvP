package io.github.togar2.pvp.feature;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

public class CombatFeatureSet implements RegistrableFeature {
	private final CombatFeature[] features;
	
	public CombatFeatureSet(CombatFeature... features) {
		this.features = features;
	}
	
	@Override
	public void init(EventNode<Event> node) {
		for (CombatFeature feature : features) {
			if (!(feature instanceof RegistrableFeature registrable)) continue;
			EventNode<Event> currentNode = EventNode.all(feature.getClass().getTypeName());
			registrable.init(currentNode);
			node.addChild(currentNode);
		}
	}
}
