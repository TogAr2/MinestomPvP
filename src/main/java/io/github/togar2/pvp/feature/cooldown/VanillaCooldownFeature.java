package io.github.togar2.pvp.feature.cooldown;

import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.feature.RegistrableFeature;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.tag.Tag;
import net.minestom.server.utils.MathUtils;

public class VanillaCooldownFeature implements CooldownFeature, RegistrableFeature, CombatFeature {
	public static final Tag<Long> LAST_ATTACKED_TICKS = Tag.Long("lastAttackedTicks");
	
	@Override
	public void init(EventNode<Event> node) {
		node.addListener(EventListener.builder(PlayerHandAnimationEvent.class).handler(event ->
				resetCooldownProgress(event.getPlayer())).build());
		
		node.addListener(EventListener.builder(PlayerChangeHeldSlotEvent.class).handler(event -> {
			if (!event.getPlayer().getItemInMainHand()
					.isSimilar(event.getPlayer().getInventory().getItemStack(event.getSlot()))) {
				resetCooldownProgress(event.getPlayer());
			}
		}).build());
	}
	
	@Override
	public void resetCooldownProgress(Player player) {
		player.setTag(LAST_ATTACKED_TICKS, player.getAliveTicks());
	}
	
	@Override
	public double getAttackCooldownProgress(Player player) {
		Long lastAttacked = player.getTag(LAST_ATTACKED_TICKS);
		if (lastAttacked == null) return 1.0;
		
		long timeSinceLastAttacked = player.getAliveTicks() - lastAttacked;
		return MathUtils.clamp(
				(timeSinceLastAttacked + 0.5) / getAttackCooldownProgressPerTick(player),
				0, 1
		);
	}
	
	protected double getAttackCooldownProgressPerTick(Player player) {
		return (1 / player.getAttributeValue(Attribute.ATTACK_SPEED)) * 20;
	}
}
