package io.github.togar2.pvp.entity;

import io.github.togar2.pvp.damage.combat.CombatManager;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Tracker {
	public static final Tag<Long> ITEM_USE_START_TIME = Tag.Long("itemUseStartTime");
	public static final Tag<Block> LAST_CLIMBED_BLOCK = Tag.Integer("lastClimbedBlock").map(Block::fromStateId, Block::stateId);
	public static final Tag<Double> FALL_DISTANCE = Tag.Double("fallDistance");
	
	public static final Map<UUID, CombatManager> combatManager = new HashMap<>();
	
	public static void register(EventNode<? super EntityEvent> eventNode) {
		EventNode<EntityEvent> node = EventNode.type("tracker-events", EventFilter.ENTITY);
		eventNode.addChild(node);
		
		node.addListener(AsyncPlayerConfigurationEvent.class, event -> {
			UUID uuid = event.getPlayer().getUuid();
			
			Tracker.combatManager.put(uuid, new CombatManager(event.getPlayer()));
		});
		
		node.addListener(PlayerDisconnectEvent.class, event -> {
			Tracker.combatManager.remove(event.getPlayer().getUuid());
		});
		
		node.addListener(PlayerSpawnEvent.class, event -> {
			CombatManager combatManager = Tracker.combatManager.get(event.getPlayer().getUuid());
			if (combatManager != null) combatManager.reset();
		});
		
		node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			UUID uuid = player.getUuid();
			
			if (player.isOnGround()) {
				player.removeTag(LAST_CLIMBED_BLOCK);
			}
			
			Tracker.combatManager.get(uuid).tick();
		});
		
		node.addListener(PlayerItemAnimationEvent.class, event ->
				event.getPlayer().setTag(ITEM_USE_START_TIME, System.currentTimeMillis()));
		
		node.addListener(PlayerMoveEvent.class, event -> {
			Player player = event.getPlayer();
			if (EntityUtils.isClimbing(player)) {
				//TODO this is duplicate code (VanillaFallFeature)
				player.setTag(LAST_CLIMBED_BLOCK, Objects.requireNonNull(player.getInstance())
						.getBlock(player.getPosition()));
				player.setTag(FALL_DISTANCE, 0.0);
			}
		});
		
		node.addListener(PlayerSpawnEvent.class, event -> event.getPlayer().setTag(FALL_DISTANCE, 0.0));
		node.addListener(PlayerRespawnEvent.class, event -> event.getPlayer().setTag(FALL_DISTANCE, 0.0));
	}
}
