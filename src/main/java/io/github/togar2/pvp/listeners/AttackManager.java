package io.github.togar2.pvp.listeners;

import io.github.togar2.pvp.config.AttackConfig;
import io.github.togar2.pvp.config.PvPConfig;
import io.github.togar2.pvp.events.PlayerSpectateEvent;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;

public class AttackManager {
	public static EventNode<EntityInstanceEvent> events(AttackConfig config) {
		EventNode<EntityInstanceEvent> node = EventNode.type("attack-events", PvPConfig.ENTITY_INSTANCE_FILTER);
		
		node.addListener(EntityAttackEvent.class, event -> {
			if (event.getEntity() instanceof Player player
					&& player.getGameMode() == GameMode.SPECTATOR && config.isSpectatingEnabled()) {
				makeSpectate(player, event.getTarget());
				return;
			}
			
			config.getHandler().performAttack(event.getEntity(), event.getTarget(), config);
		});
		
		if (!config.isLegacy()) {
			node.addListener(EventListener.builder(PlayerHandAnimationEvent.class).handler(event ->
					config.getHandler().resetCooldownProgress(event.getPlayer())).build());
			
			node.addListener(EventListener.builder(PlayerChangeHeldSlotEvent.class).handler(event -> {
				if (!event.getPlayer().getItemInMainHand()
						.isSimilar(event.getPlayer().getInventory().getItemStack(event.getSlot()))) {
					config.getHandler().resetCooldownProgress(event.getPlayer());
				}
			}).build());
		}
		
		return node;
	}
	
	public static void makeSpectate(Player player, Entity target) {
		PlayerSpectateEvent playerSpectateEvent = new PlayerSpectateEvent(player, target);
		EventDispatcher.callCancellable(playerSpectateEvent, () -> {
			player.spectate(target);
			player.setTag(AttackHandler.SPECTATING, target.getEntityId());
		});
	}
	
	public static void spectateTick(Player player) {
		Integer spectatingId = player.getTag(AttackHandler.SPECTATING);
		if (spectatingId == null) return;
		Entity spectating = Entity.getEntity(spectatingId);
		if (spectating == null || spectating == player) return;
		
		// This is to make sure other players don't see the player standing still while spectating
		// And when the player stops spectating,
		// they are at the entities position instead of their position before spectating
		player.teleport(spectating.getPosition());
		
		if (player.getEntityMeta().isSneaking() || spectating.isRemoved()
				|| (spectating instanceof LivingEntity livingSpectating && livingSpectating.isDead())) {
			player.stopSpectating();
			player.removeTag(AttackHandler.SPECTATING);
		}
	}
}
