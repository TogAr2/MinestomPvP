package io.github.bloepiloepi.pvp.legacy;

import io.github.bloepiloepi.pvp.config.PvPConfig;
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
import net.minestom.server.tag.Tag;

public class SwordBlockHandler {
	public static final Tag<Long> LAST_SWING_TIME = Tag.Long("lastSwingTime");
	public static final Tag<Boolean> BLOCKING_SWORD = Tag.Boolean("blockingSword");
	public static final Tag<ItemStack> BLOCK_REPLACEMENT_ITEM = Tag.ItemStack("blockReplacementItem");
	
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
			if (event.getHand() == Player.Hand.MAIN)
				event.getPlayer().setTag(LAST_SWING_TIME, System.currentTimeMillis());
		});
		
		return node;
	}
	
	private static void handleUseItem(PlayerUseItemEvent event) {
		Player player = event.getPlayer();
		
		if (event.getHand() == Player.Hand.MAIN && isSword(event.getItemStack())
				&& !player.getTag(BLOCKING_SWORD)) {
			long elapsedSwingTime = System.currentTimeMillis() - player.getTag(LAST_SWING_TIME);
			if (elapsedSwingTime < 50) {
				return;
			}
			
			player.setTag(BLOCK_REPLACEMENT_ITEM, player.getItemInOffHand());
			player.setTag(BLOCKING_SWORD, true);
			
			player.setItemInOffHand(SHIELD);
			player.refreshActiveHand(true, true, false);
			player.sendPacketToViewersAndSelf(player.getMetadataPacket());
		}
	}
	
	private static void unblock(Player player) {
		if (player.hasTag(BLOCK_REPLACEMENT_ITEM)) {
			player.setTag(BLOCKING_SWORD, false);
			player.setItemInOffHand(player.getTag(BLOCK_REPLACEMENT_ITEM));
			player.removeTag(BLOCK_REPLACEMENT_ITEM);
		}
	}
	
	private static void handleUpdateState(ItemUpdateStateEvent event) {
		if (event.getHand() == Player.Hand.OFF && event.getItemStack().material() == Material.SHIELD) {
			unblock(event.getPlayer());
		}
	}
	
	private static void handleSwapItem(PlayerSwapItemEvent event) {
		Player player = event.getPlayer();
		if (player.getItemInOffHand().material() == Material.SHIELD && player.getTag(BLOCKING_SWORD))
			event.setCancelled(true);
	}
	
	private static void handleChangeSlot(PlayerChangeHeldSlotEvent event) {
		Player player = event.getPlayer();
		if (player.getItemInOffHand().material() == Material.SHIELD && player.getTag(BLOCKING_SWORD))
			unblock(player);
	}
	
	private static boolean isSword(ItemStack stack) {
		return stack.material().registry().translationKey().contains("sword");
	}
}
