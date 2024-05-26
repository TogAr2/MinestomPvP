package io.github.togar2.pvp.feature;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;

public class CombatFeatureSet implements RegistrableFeature<Event> {
	private final CombatFeature[] features;
	
	public CombatFeatureSet(CombatFeature... features) {
		this.features = features;
	}
	
	@Override
	public void init(EventNode<Event> node) {
		for (CombatFeature feature : features) {
			if (!(feature instanceof RegistrableFeature<?> registrable)) continue;
			node.addChild(registrable.createNode());
		}
	}
	
	@Override
	public EventFilter<Event, ?> getEventFilter() {
		return EventFilter.ALL;
	}
}
