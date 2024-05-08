package io.github.togar2.pvp.feature;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

public class FeatureSet implements CombatFeature {
	private final CombatFeature[] features;
	
	public FeatureSet(CombatFeature... features) {
		this.features = features;
	}
	
	@Override
	public void init(EventNode<Event> node) {
		for (CombatFeature feature : features) {
			EventNode<Event> currentNode = EventNode.all(feature.getClass().getTypeName());
			feature.init(currentNode);
			node.addChild(currentNode);
		}
	}
}
