package io.github.bloepiloepi.pvp.entity;

import io.github.bloepiloepi.pvp.damage.combat.CombatManager;
import io.github.bloepiloepi.pvp.food.HungerManager;
import io.github.bloepiloepi.pvp.legacy.SwordBlockHandler;
import io.github.bloepiloepi.pvp.listeners.AttackManager;
import io.github.bloepiloepi.pvp.listeners.DamageListener;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityFireEvent;
import net.minestom.server.event.instance.AddEntityToInstanceEvent;
import net.minestom.server.event.instance.RemoveEntityFromInstanceEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.SetCooldownPacket;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.time.TimeUnit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Tracker {
	public static final Tag<Long> ITEM_USE_START_TIME = Tag.Long("itemUseStartTime");
	public static final Tag<Block> LAST_CLIMBED_BLOCK = Tag.Short("lastClimbedBlock").map(Block::fromStateId, Block::stateId);
	public static final Tag<Double> FALL_DISTANCE = Tag.Double("fallDistance");
	
	public static final Map<UUID, Map<Material, Long>> cooldownEnd = new HashMap<>();
	public static final Map<UUID, CombatManager> combatManager = new HashMap<>();
	
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
			
			event.getPlayer().setTag(AttackManager.LAST_ATTACKED_TICKS, 0L);
			event.getPlayer().setTag(SwordBlockHandler.LAST_SWING_TIME, 0L);
			event.getPlayer().setTag(SwordBlockHandler.BLOCKING_SWORD, false);
			event.getPlayer().setTag(HungerManager.EXHAUSTION, 0F);
			event.getPlayer().setTag(HungerManager.STARVATION_TICKS, 0);
			Tracker.cooldownEnd.put(uuid, new HashMap<>());
			Tracker.combatManager.put(uuid, new CombatManager(event.getPlayer()));
		});
		
		node.addListener(PlayerDisconnectEvent.class, event -> {
			Tracker.cooldownEnd.remove(event.getPlayer().getUuid());
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
			
			if (player.isDead()) {
				Tracker.combatManager.get(uuid).recheckStatus();
			}
			if (player.getAliveTicks() % 20 == 0 && player.isOnline()) {
				Tracker.combatManager.get(uuid).recheckStatus();
			}
			
			if (player.hasTag(DamageListener.LAST_DAMAGED_BY)) {
				Integer lastDamagedById = player.getTag(DamageListener.LAST_DAMAGED_BY);
				if (lastDamagedById == null) {
					player.removeTag(DamageListener.LAST_DAMAGED_BY);
				} else {
					Entity lastDamagedBy = Entity.getEntity(lastDamagedById);
					if (lastDamagedBy instanceof LivingEntity living && living.isDead()) {
						player.removeTag(DamageListener.LAST_DAMAGED_BY);
					} else if (System.currentTimeMillis() - player.getTag(DamageListener.LAST_DAMAGE_TIME) > 5000) {
						// After 5 seconds of no attack the last damaged by does not count anymore
						player.removeTag(DamageListener.LAST_DAMAGED_BY);
					}
				}
			}
			
			AttackManager.spectateTick(player);
		});
		
		node.addListener(PlayerUseItemEvent.class, event -> {
			if (Tracker.hasCooldown(event.getPlayer(), event.getItemStack().material())) {
				event.setCancelled(true);
			}
		});
		
		node.addListener(PlayerPreEatEvent.class, event -> {
			if (Tracker.hasCooldown(event.getPlayer(), event.getItemStack().material())) {
				event.setCancelled(true);
			}
		});
		
		node.addListener(PlayerItemAnimationEvent.class, event ->
				event.getPlayer().setTag(ITEM_USE_START_TIME, System.currentTimeMillis()));
		
		node.addListener(PlayerMoveEvent.class, event -> {
			Player player = event.getPlayer();
			if (EntityUtils.isClimbing(player)) {
				player.setTag(LAST_CLIMBED_BLOCK, Objects.requireNonNull(player.getInstance())
						.getBlock(player.getPosition()));
				player.setTag(FALL_DISTANCE, 0.0);
			}
		});
		
		node.addListener(PlayerSpawnEvent.class, event -> event.getPlayer().setTag(FALL_DISTANCE, 0.0));
		node.addListener(PlayerRespawnEvent.class, event -> event.getPlayer().setTag(FALL_DISTANCE, 0.0));
		
		node.addListener(EntityFireEvent.class, event ->
				event.getEntity().setTag(EntityUtils.FIRE_EXTINGUISH_TIME,
						System.currentTimeMillis() + event.getFireTime(TimeUnit.MILLISECOND)));
		
		node.addListener(AddEntityToInstanceEvent.class, event -> {
			if (event.getEntity() instanceof LivingEntity)
				PotionListener.durationLeftMap.put(event.getEntity().getUuid(), new ConcurrentHashMap<>());
		});
		
		node.addListener(RemoveEntityFromInstanceEvent.class, event ->
				PotionListener.durationLeftMap.remove(event.getEntity().getUuid()));
		
		MinecraftServer.getSchedulerManager()
				.buildTask(Tracker::updateCooldown)
				.repeat(1, TimeUnit.SERVER_TICK).schedule();
	}
}
