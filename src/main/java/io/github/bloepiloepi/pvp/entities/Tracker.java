package io.github.bloepiloepi.pvp.entities;

import io.github.bloepiloepi.pvp.food.HungerManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.entity.EntityTickEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SetCooldownPacket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

//TODO move to Data class
public class Tracker {
	public static final Map<UUID, Integer> lastAttackedTicks = new HashMap<>();
	public static final Map<UUID, Integer> timeUntilRegen = new HashMap<>();
	public static final Map<UUID, Float> lastDamageTaken = new HashMap<>();
	public static final Map<UUID, HungerManager> hungerManager = new HashMap<>();
	public static final Map<UUID, Map<Material, Long>> cooldownEnd = new HashMap<>();
	public static final Map<UUID, Boolean> falling = new HashMap<>();
	
	public static <K> void increaseInt(Map<K, Integer> map, K key, int amount) {
		map.put(key, map.getOrDefault(key, 0) + amount);
	}
	
	public static <K> void decreaseInt(Map<K, Integer> map, K key, int amount) {
		map.put(key, map.getOrDefault(key, 0) - amount);
	}
	
	public static <K> void increaseFloat(Map<K, Float> map, K key, float amount) {
		map.put(key, map.getOrDefault(key, 0F) + amount);
	}
	
	public static <K> void decreaseFloat(Map<K, Float> map, K key, float amount) {
		map.put(key, map.getOrDefault(key, 0F) - amount);
	}
	
	public static boolean hasCooldown(Player player, Material material) {
		Map<Material, Long> cooldownMap = cooldownEnd.get(player.getUuid());
		
		return cooldownMap.containsKey(material) && cooldownMap.get(material) > System.currentTimeMillis();
	}
	
	public static void setCooldown(Player player, Material material, int durationTicks) {
		cooldownEnd.get(player.getUuid()).put(material, System.currentTimeMillis() + (long) durationTicks * MinecraftServer.TICK_MS);
		onCooldownUpdate(player, material, durationTicks);
	}
	
	public static void updateCooldown(long time) {
		if (cooldownEnd.isEmpty()) return;
		
		cooldownEnd.forEach((uuid, cooldownMap) -> {
			if (cooldownMap.isEmpty()) return;
			
			Iterator<Map.Entry<Material, Long>> iterator = cooldownMap.entrySet().iterator();
			Player player = MinecraftServer.getConnectionManager().getPlayer(uuid);
			assert player != null;
			
			while (iterator.hasNext()) {
				Map.Entry<Material, Long> entry = iterator.next();
				if (entry.getValue() <= time) {
					iterator.remove();
					onCooldownUpdate(player, entry.getKey(), 0);
				}
			}
		});
	}
	
	public static void onCooldownUpdate(Player player, Material material, int duration) {
		SetCooldownPacket packet = new SetCooldownPacket();
		packet.itemId = material.getId();
		packet.cooldownTicks = duration;
		
		player.getPlayerConnection().sendPacket(packet);
	}
	
	public static void register(GlobalEventHandler eventHandler) {
		//TODO new event api
		eventHandler.addEventCallback(PlayerLoginEvent.class, event -> {
			UUID uuid = event.getPlayer().getUuid();
			
			Tracker.lastAttackedTicks.put(uuid, 0);
			Tracker.timeUntilRegen.put(uuid, 0);
			Tracker.lastDamageTaken.put(uuid, 0F);
			Tracker.hungerManager.put(uuid, new HungerManager(event.getPlayer()));
			Tracker.cooldownEnd.put(uuid, new HashMap<>());
			Tracker.falling.put(uuid, false);
		});
		
		eventHandler.addEventCallback(PlayerDisconnectEvent.class, event -> {
			UUID uuid = event.getPlayer().getUuid();
			
			Tracker.lastAttackedTicks.remove(uuid);
			Tracker.timeUntilRegen.remove(uuid);
			Tracker.lastDamageTaken.remove(uuid);
			Tracker.hungerManager.remove(uuid);
			Tracker.cooldownEnd.remove(uuid);
			Tracker.falling.remove(uuid);
		});
		
		eventHandler.addEventCallback(PlayerTickEvent.class, event -> {
			if (event.getPlayer().isOnline()) {
				Tracker.increaseInt(Tracker.lastAttackedTicks, event.getPlayer().getUuid(), 1);
				Tracker.hungerManager.get(event.getPlayer().getUuid()).update();
			}
		});
		
		eventHandler.addEventCallback(EntityTickEvent.class, event -> {
			if (Tracker.timeUntilRegen.getOrDefault(event.getEntity().getUuid(), 0) > 0) {
				Tracker.decreaseInt(Tracker.timeUntilRegen, event.getEntity().getUuid(), 1);
			}
		});
		
		MinecraftServer.getUpdateManager().addTickEndCallback(Tracker::updateCooldown);
	}
}
