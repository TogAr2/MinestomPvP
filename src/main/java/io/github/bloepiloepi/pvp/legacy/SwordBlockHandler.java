package io.github.bloepiloepi.pvp.legacy;

import io.github.bloepiloepi.pvp.entities.Tracker;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemUpdateStateEvent;
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
		
		return node;
	}
	
	private static void handleUseItem(PlayerUseItemEvent event) {
		Player player = event.getPlayer();
		
		if (isSword(event.getItemStack()) && !Tracker.blockingSword.get(player.getUuid())) {
			Player.Hand oppositeHand = event.getHand() == Player.Hand.MAIN ? Player.Hand.OFF : Player.Hand.MAIN;
			Tracker.blockReplacementItem.put(player.getUuid(), player.getItemInHand(oppositeHand));
			Tracker.blockingSword.put(player.getUuid(), true);
			
			player.setItemInHand(oppositeHand, SHIELD);
			player.refreshActiveHand(true, oppositeHand == Player.Hand.OFF, false);
			player.sendPacketToViewersAndSelf(player.getMetadataPacket());
		}
	}
	
	private static void handleUpdateState(ItemUpdateStateEvent event) {
		if (event.getItemStack().getMaterial() == Material.SHIELD) {
			Player player = event.getPlayer();
			
			if (Tracker.blockReplacementItem.containsKey(player.getUuid())) {
				Tracker.blockingSword.put(player.getUuid(), false);
				player.setItemInHand(event.getHand(), Tracker.blockReplacementItem.get(player.getUuid()));
			}
		}
	}
	
	private static boolean isSword(ItemStack stack) {
		return stack.getMaterial().registry().translationKey().contains("sword");
	}
}
