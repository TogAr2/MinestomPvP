package io.github.bloepiloepi.pvp.projectile;

import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.trait.EntityEvent;

public class ProjectileListener {
	
	public static EventNode<EntityEvent> events() {
		EventNode<EntityEvent> node = EventNode.type("projectile-events", EventFilter.ENTITY);
		
		node.addListener(EventListener.builder(EntityAttackEvent.class).handler(event -> {
			if (!(event.getEntity() instanceof EntityHittableProjectile)) return;
			((EntityHittableProjectile) event.getEntity()).onHit(event.getTarget());
		}).ignoreCancelled(false).build());
		
		return node;
	}
}
