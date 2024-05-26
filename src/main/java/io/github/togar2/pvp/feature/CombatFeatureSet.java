package io.github.togar2.pvp.feature;

import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

public class CombatFeatureSet implements RegistrableFeature {
	private final CombatFeature[] features;
	
	public CombatFeatureSet(CombatFeature... features) {
		this.features = features;
	}
	
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		for (CombatFeature feature : features) {
			if (!(feature instanceof RegistrableFeature registrable)) continue;
			node.addChild(registrable.createNode());
		}
	}
}
