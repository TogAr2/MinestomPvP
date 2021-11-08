package io.github.bloepiloepi.pvp.entities;

import io.github.bloepiloepi.pvp.events.PickupArrowEvent;
import io.github.bloepiloepi.pvp.projectile.AbstractArrow;
import net.minestom.server.MinecraftServer;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.timer.Task;
import net.minestom.server.utils.time.TimeUnit;

import java.util.Set;

public class ArrowPickup {
	private static Task task;
	
	public static void init() {
		task = MinecraftServer.getSchedulerManager().buildTask(() -> {
			for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
				for (Entity entity : instance.getEntities()) {
					if (!(entity instanceof Player player) || !player.canPickupItem())
						continue;
					
					Set<Entity> entities = instance.getChunkEntities(player.getChunk());
					
					for (Entity entity2 : entities) {
						if (!(entity2 instanceof AbstractArrow arrow)) continue;
						
						// Do not pickup if not visible
						if (!entity2.isViewer(player))
							continue;
						
						if (arrow.shouldRemove() || arrow.isRemoveScheduled() || !arrow.canBePickedUp(player))
							continue;
						
						BoundingBox arrowBoundingBox = arrow.getBoundingBox();
						if (player.getBoundingBox().expand(1, 0.5f, 1).intersect(arrowBoundingBox)) {
							PickupArrowEvent event = new PickupArrowEvent(player, arrow);
							EventDispatcher.callCancellable(event, () -> {
								if (arrow.pickup(player)) {
									player.sendPacketToViewersAndSelf(new CollectItemPacket(
											arrow.getEntityId(), player.getEntityId(), 1
									));
									
									arrow.remove();
								}
							});
						}
					}
				}
			}
		}).repeat(1, TimeUnit.SERVER_TICK).schedule();
	}
	
	public static void stop() {
		task.cancel();
	}
}
