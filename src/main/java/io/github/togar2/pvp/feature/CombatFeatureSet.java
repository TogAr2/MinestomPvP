package io.github.togar2.pvp.feature;

import io.github.togar2.pvp.feature.config.FeatureConfiguration;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

public class CombatFeatureSet extends FeatureConfiguration implements RegistrableFeature {
	@Override
	public void init(EventNode<EntityInstanceEvent> node) {
		for (CombatFeature feature : listFeatures()) {
			if (!(feature instanceof RegistrableFeature registrable)) continue;
			node.addChild(registrable.createNode());
		}
	}
}
