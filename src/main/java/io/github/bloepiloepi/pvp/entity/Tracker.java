package io.github.bloepiloepi.pvp.entity;

import io.github.bloepiloepi.pvp.damage.combat.CombatManager;
import io.github.bloepiloepi.pvp.food.HungerManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SetCooldownPacket;
import net.minestom.server.utils.time.TimeUnit;

import java.util.*;

public class Tracker {
	public static final Map<UUID, Integer> lastAttackedTicks = new HashMap<>();
	public static final Map<UUID, Integer> invulnerableTime = new HashMap<>();
	public static final Map<UUID, Float> lastDamageTaken = new HashMap<>();
	public static final Map<UUID, HungerManager> hungerManager = new HashMap<>();
	public static final Map<UUID, Map<Material, Long>> cooldownEnd = new HashMap<>();
	public static final Map<UUID, Entity> spectating = new HashMap<>();
	public static final Map<UUID, Long> itemUseStartTime = new HashMap<>();
	public static final Map<UUID, Player.Hand> itemUseHand = new HashMap<>();
	public static final Map<UUID, Block> lastClimbedBlock = new HashMap<>();
	public static final Map<UUID, CombatManager> combatManager = new HashMap<>();
	public static final Map<UUID, LivingEntity> lastDamagedBy = new HashMap<>();
	public static final Map<UUID, Long> lastDamageTime = new HashMap<>();
	public static final Map<UUID, Long> fireExtinguishTime = new HashMap<>();
	public static final Map<UUID, ItemStack> blockReplacementItem = new HashMap<>();
	public static final Map<UUID, Boolean> blockingSword = new HashMap<>();
	public static final Map<UUID, Long> lastSwingTime = new HashMap<>();
	public static final Map<UUID, Double> fallDistance = new HashMap<>();
	
	public static <K> void increaseInt(Map<K, Integer> map, K key, int amount) {
		map.put(key, map.getOrDefault(key, 0) + amount);
	}
	
	public static <K> void decreaseInt(Map<K, Integer> map, K key, int amount) {
		map.put(key, map.getOrDefault(key, 0) - amount);
	}
	
	public static boolean hasCooldown(Player player, Material material) {
		Map<Material, Long> cooldownMap = cooldownEnd.get(player.getUuid());
		
		return cooldownMap.containsKey(material) && cooldownMap.get(material) > System.currentTimeMillis();
	}
	
	public static void setCooldown(Player player, Material material, int durationTicks) {
		cooldownEnd.get(player.getUuid()).put(material, System.currentTimeMillis() + (long) durationTicks * MinecraftServer.TICK_MS);
		onCooldown(player, material, durationTicks);
	}
	
	public static void updateCooldown() {
		if (cooldownEnd.isEmpty()) return;
		long time = System.currentTimeMillis();
		
		cooldownEnd.forEach((uuid, cooldownMap) -> {
			if (cooldownMap.isEmpty()) return;
			
			Iterator<Map.Entry<Material, Long>> iterator = cooldownMap.entrySet().iterator();
			Player player = MinecraftServer.getConnectionManager().getPlayer(uuid);
			assert player != null;
			
			while (iterator.hasNext()) {
				Map.Entry<Material, Long> entry = iterator.next();
				if (entry.getValue() <= time) {
					iterator.remove();
					onCooldown(player, entry.getKey(), 0);
				}
			}
		});
	}
	
	@SuppressWarnings("UnstableApiUsage")
	public static void onCooldown(Player player, Material material, int duration) {
		player.getPlayerConnection().sendPacket(new SetCooldownPacket(material.id(), duration));
	}
	
	public static void register(EventNode<? super EntityEvent> eventNode) {
		EventNode<EntityEvent> node = EventNode.type("tracker-events", EventFilter.ENTITY);
		eventNode.addChild(node);
		
		node.addListener(PlayerLoginEvent.class, event -> {
			UUID uuid = event.getPlayer().getUuid();
			
			Tracker.lastAttackedTicks.put(uuid, 0);
			Tracker.invulnerableTime.put(uuid, 0);
			Tracker.lastDamageTaken.put(uuid, 0F);
			Tracker.hungerManager.put(uuid, new HungerManager(event.getPlayer()));
			Tracker.cooldownEnd.put(uuid, new HashMap<>());
			Tracker.spectating.put(uuid, event.getPlayer());
			Tracker.combatManager.put(uuid, new CombatManager(event.getPlayer()));
			Tracker.blockingSword.put(uuid, false);
			Tracker.lastSwingTime.put(uuid, 0L);
			Tracker.fallDistance.put(uuid, 0.0);
		});
		
		node.addListener(PlayerDisconnectEvent.class, event -> {
			UUID uuid = event.getPlayer().getUuid();
			
			Tracker.lastAttackedTicks.remove(uuid);
			Tracker.invulnerableTime.remove(uuid);
			Tracker.lastDamageTaken.remove(uuid);
			Tracker.hungerManager.remove(uuid);
			Tracker.cooldownEnd.remove(uuid);
			Tracker.spectating.remove(uuid);
			Tracker.itemUseStartTime.remove(uuid);
			Tracker.itemUseHand.remove(uuid);
			Tracker.lastClimbedBlock.remove(uuid);
			Tracker.combatManager.remove(uuid);
			Tracker.lastDamagedBy.remove(uuid);
			Tracker.lastDamageTime.remove(uuid);
			Tracker.fireExtinguishTime.remove(uuid);
			Tracker.blockReplacementItem.remove(uuid);
			Tracker.blockingSword.remove(uuid);
			Tracker.lastSwingTime.remove(uuid);
			Tracker.fallDistance.remove(uuid);
		});
		
		node.addListener(PlayerSpawnEvent.class, event -> {
			CombatManager combatManager = Tracker.combatManager.get(event.getPlayer().getUuid());
			if (combatManager != null) combatManager.reset();
		});
		
		node.addListener(PlayerTickEvent.class, event -> {
			Player player = event.getPlayer();
			UUID uuid = player.getUuid();
			Tracker.increaseInt(Tracker.lastAttackedTicks, uuid, 1);
			
			if (player.isOnGround()) {
				Tracker.lastClimbedBlock.remove(uuid);
			}
			
			if (player.isDead()) {
				Tracker.combatManager.get(uuid).recheckStatus();
			}
			if (player.getAliveTicks() % 20 == 0 && player.isOnline()) {
				Tracker.combatManager.get(uuid).recheckStatus();
			}
			
			if (Tracker.lastDamagedBy.containsKey(uuid)) {
				LivingEntity lastDamagedBy = Tracker.lastDamagedBy.get(uuid);
				if (lastDamagedBy.isDead()) {
					Tracker.lastDamagedBy.remove(uuid);
				} else if (System.currentTimeMillis() - Tracker.lastDamageTime.get(uuid) > 5000) {
					// After 5 seconds of no attack the last damaged by does not count anymore
					Tracker.lastDamagedBy.remove(uuid);
				}
			}
		});
		
		node.addListener(EntityTickEvent.class, event -> {
			if (Tracker.invulnerableTime.getOrDefault(event.getEntity().getUuid(), 0) > 0) {
				Tracker.decreaseInt(Tracker.invulnerableTime, event.getEntity().getUuid(), 1);
			}
		});
		
		node.addListener(PlayerUseItemEvent.class, event -> {
			if (Tracker.hasCooldown(event.getPlayer(), event.getItemStack().material())) {
				event.setCancelled(true);
			}
		});
		
		node.addListener(PlayerPreEatEvent.class, event -> {
			if (Tracker.hasCooldown(event.getPlayer(), event.getFoodItem().material())) {
				event.setCancelled(true);
			}
		});
		
		node.addListener(PlayerItemAnimationEvent.class, event ->
				itemUseStartTime.put(event.getPlayer().getUuid(), System.currentTimeMillis()));
		
		node.addListener(PlayerMoveEvent.class, event -> {
			Player player = event.getPlayer();
			if (EntityUtils.isClimbing(player)) {
				lastClimbedBlock.put(player.getUuid(), Objects.requireNonNull(player.getInstance())
						.getBlock(player.getPosition()));
				fallDistance.put(player.getUuid(), 0.0);
			}
		});
		
		node.addListener(PlayerSpawnEvent.class, event -> fallDistance.put(event.getPlayer().getUuid(), 0.0));
		
		node.addListener(EntityFireEvent.class, event ->
				Tracker.fireExtinguishTime.put(event.getEntity().getUuid(),
						System.currentTimeMillis() + event.getFireTime(TimeUnit.MILLISECOND)));
		
		node.addListener(RemoveEntityFromInstanceEvent.class, event ->
				Tracker.fireExtinguishTime.remove(event.getEntity().getUuid()));
		
		MinecraftServer.getSchedulerManager()
				.buildTask(Tracker::updateCooldown)
				.repeat(1, TimeUnit.SERVER_TICK).schedule();
	}
}
