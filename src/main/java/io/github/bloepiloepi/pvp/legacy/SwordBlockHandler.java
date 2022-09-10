package io.github.bloepiloepi.pvp.legacy;

import io.github.bloepiloepi.pvp.config.PvPConfig;
import io.github.bloepiloepi.pvp.entity.Tracker;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class SwordBlockHandler {
	private static final ItemStack SHIELD = ItemStack.of(Material.SHIELD);
	
	public static EventNode<PlayerInstanceEvent> events() {
		EventNode<PlayerInstanceEvent> node = EventNode.type("legacy-sword-block", PvPConfig.PLAYER_INSTANCE_FILTER);
		
		node.addListener(EventListener.builder(PlayerUseItemEvent.class)
				.handler(SwordBlockHandler::handleUseItem)
				.build());
		
		node.addListener(EventListener.builder(ItemUpdateStateEvent.class)
				.handler(SwordBlockHandler::handleUpdateState)
				.build());
		
		node.addListener(EventListener.builder(PlayerSwapItemEvent.class)
				.handler(SwordBlockHandler::handleSwapItem)
				.build());
		
		node.addListener(EventListener.builder(PlayerChangeHeldSlotEvent.class)
				.handler(SwordBlockHandler::handleChangeSlot)
				.build());
		
		node.addListener(PlayerHandAnimationEvent.class, event -> {
			if (event.getHand() == Player.Hand.MAIN) {
				Tracker.lastSwingTime.put(event.getPlayer().getUuid(), System.currentTimeMillis());
			}
		});
		
		return node;
	}
	
	private static void handleUseItem(PlayerUseItemEvent event) {
		Player player = event.getPlayer();
		
		if (event.getHand() == Player.Hand.MAIN && isSword(event.getItemStack())
				&& !Tracker.blockingSword.get(player.getUuid())) {
			long elapsedSwingTime = System.currentTimeMillis() - Tracker.lastSwingTime.get(player.getUuid());
			if (elapsedSwingTime < 50) {
				return;
			}
			
			Tracker.blockReplacementItem.put(player.getUuid(), player.getItemInOffHand());
			Tracker.blockingSword.put(player.getUuid(), true);
			
			player.setItemInOffHand(SHIELD);
			player.refreshActiveHand(true, true, false);
			player.sendPacketToViewersAndSelf(player.getMetadataPacket());
		}
	}
	
	private static void unblock(Player player) {
		if (Tracker.blockReplacementItem.containsKey(player.getUuid())) {
			Tracker.blockingSword.put(player.getUuid(), false);
			player.setItemInOffHand(Tracker.blockReplacementItem.get(player.getUuid()));
		}
	}
	
	private static void handleUpdateState(ItemUpdateStateEvent event) {
		if (event.getHand() == Player.Hand.OFF && event.getItemStack().material() == Material.SHIELD) {
			unblock(event.getPlayer());
		}
	}
	
	private static void handleSwapItem(PlayerSwapItemEvent event) {
		Player player = event.getPlayer();
		if (player.getItemInOffHand().material() == Material.SHIELD
				&& Tracker.blockingSword.get(player.getUuid())) {
			event.setCancelled(true);
		}
	}
	
	private static void handleChangeSlot(PlayerChangeHeldSlotEvent event) {
		Player player = event.getPlayer();
		if (player.getItemInOffHand().material() == Material.SHIELD
				&& Tracker.blockingSword.get(player.getUuid())) {
			unblock(player);
		}
	}
	
	private static boolean isSword(ItemStack stack) {
		return stack.material().registry().translationKey().contains("sword");
	}
}
