package io.github.bloepiloepi.pvp.legacy;

import io.github.bloepiloepi.pvp.entities.Tracker;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public class SwordBlockHandler {
	private static final ItemStack SHIELD = ItemStack.of(Material.SHIELD);
	
	public static EventNode<PlayerEvent> legacyEvents() {
		EventNode<PlayerEvent> node = EventNode.type("legacy-sword-block", EventFilter.PLAYER);
		
		node.addListener(EventListener.builder(PlayerUseItemEvent.class)
				.handler(SwordBlockHandler::handleUseItem)
				.ignoreCancelled(false)
				.build());
		
		node.addListener(EventListener.builder(ItemUpdateStateEvent.class)
				.handler(SwordBlockHandler::handleUpdateState)
				.ignoreCancelled(false)
				.build());
		
		node.addListener(EventListener.builder(PlayerSwapItemEvent.class)
				.handler(SwordBlockHandler::handleSwapItem)
				.ignoreCancelled(false)
				.build());
		
		return node;
	}
	
	private static void handleUseItem(PlayerUseItemEvent event) {
		Player player = event.getPlayer();
		
		if (event.getHand() == Player.Hand.MAIN
				&& isSword(event.getItemStack())
				&& !Tracker.blockingSword.get(player.getUuid())) {
			Tracker.blockReplacementItem.put(player.getUuid(), player.getItemInOffHand());
			Tracker.blockingSword.put(player.getUuid(), true);
			
			player.setItemInOffHand(SHIELD);
			player.refreshActiveHand(true, true, false);
			player.sendPacketToViewersAndSelf(player.getMetadataPacket());
		}
	}
	
	private static void handleUpdateState(ItemUpdateStateEvent event) {
		if (event.getHand() == Player.Hand.OFF && event.getItemStack().getMaterial() == Material.SHIELD) {
			Player player = event.getPlayer();
			
			if (Tracker.blockReplacementItem.containsKey(player.getUuid())) {
				Tracker.blockingSword.put(player.getUuid(), false);
				player.setItemInOffHand(Tracker.blockReplacementItem.get(player.getUuid()));
			}
		}
	}
	
	private static void handleSwapItem(PlayerSwapItemEvent event) {
		Player player = event.getPlayer();
		if (player.getItemInOffHand().getMaterial() == Material.SHIELD
				&& Tracker.blockingSword.get(player.getUuid())) {
			event.setCancelled(true);
		}
	}
	
	private static boolean isSword(ItemStack stack) {
		return stack.getMaterial().registry().translationKey().contains("sword");
	}
}
