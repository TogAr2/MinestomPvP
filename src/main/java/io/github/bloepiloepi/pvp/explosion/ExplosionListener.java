package io.github.bloepiloepi.pvp.explosion;

import io.github.bloepiloepi.pvp.entities.CrystalEntity;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class ExplosionListener {
	
	public static EventNode<EntityEvent> events() {
		EventNode<EntityEvent> node = EventNode.type("explosion-events", EventFilter.ENTITY);
		
		node.addListener(PlayerUseItemOnBlockEvent.class, event -> {
			Instance instance = event.getInstance();
			Block block = instance.getBlock(event.getPosition());
			if (!block.compare(Block.OBSIDIAN) && !block.compare(Block.BEDROCK)) return;
			
			Point above = event.getPosition().add(0, 1, 0);
			if (!instance.getBlock(above).isAir()) return;
			
			BoundingBox checkIntersect = new BoundingBox(1, 2, 1);
			for (Entity entity : instance.getNearbyEntities(above, 3)) {
				if (entity.getBoundingBox().intersectBox(above.sub(entity.getPosition()), checkIntersect)) return;
			}
			
			CrystalEntity entity = new CrystalEntity();
			entity.setInstance(instance, above.add(0.5, 0, 0.5));
			
			event.getPlayer().setItemInHand(event.getHand(), event.getItemStack().consume(1));
		});
		
		return node;
	}
}
