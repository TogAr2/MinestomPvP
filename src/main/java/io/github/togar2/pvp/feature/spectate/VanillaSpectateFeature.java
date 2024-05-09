package io.github.togar2.pvp.feature.spectate;

import io.github.togar2.pvp.events.PlayerSpectateEvent;
import io.github.togar2.pvp.feature.CombatFeature;
import io.github.togar2.pvp.feature.RegistrableFeature;
import io.github.togar2.pvp.listeners.AttackHandler;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.tag.Tag;

public class VanillaSpectateFeature implements SpectateFeature, RegistrableFeature, CombatFeature {
	public static final Tag<Integer> SPECTATING = Tag.Integer("spectating");
	
	@Override
	public void init(EventNode<Event> node) {
		node.addListener(EntityAttackEvent.class, event -> {
			if (event.getEntity() instanceof Player player && player.getGameMode() == GameMode.SPECTATOR)
				makeSpectate(player, event.getTarget());
		});
		
		node.addListener(PlayerTickEvent.class, event -> spectateTick(event.getPlayer()));
	}
	
	protected void spectateTick(Player player) {
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
			stopSpectating(player);
		}
	}
	
	@Override
	public void makeSpectate(Player player, Entity target) {
		PlayerSpectateEvent playerSpectateEvent = new PlayerSpectateEvent(player, target);
		EventDispatcher.callCancellable(playerSpectateEvent, () -> {
			player.spectate(target);
			player.setTag(SPECTATING, target.getEntityId());
		});
	}
	
	@Override
	public void stopSpectating(Player player) {
		player.stopSpectating();
		player.removeTag(SPECTATING);
	}
}
