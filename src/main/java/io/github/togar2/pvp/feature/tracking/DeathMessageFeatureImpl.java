package io.github.togar2.pvp.feature.tracking;

import io.github.togar2.pvp.damage.combat.CombatManager;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.tag.Tag;

public class DeathMessageFeatureImpl implements TrackingFeature, DeathMessageFeature {
	public static final Tag<CombatManager> COMBAT_MANAGER = Tag.Transient("combatManager");
	
	@Override
	public void init(EventNode<Event> node) {
		node.addListener(AsyncPlayerConfigurationEvent.class, event ->
				event.getPlayer().setTag(COMBAT_MANAGER, new CombatManager(event.getPlayer())));
		
		node.addListener(PlayerSpawnEvent.class, event -> event.getPlayer().getTag(COMBAT_MANAGER).reset());
		
		node.addListener(PlayerTickEvent.class, event -> event.getPlayer().getTag(COMBAT_MANAGER).tick());
	}
	
	@Override
	public void recordDamage(Player player, Entity attacker, Damage damage) {
		player.getTag(COMBAT_MANAGER).recordDamage(attacker.getEntityId(), damage);
	}
	
	@Override
	public Component getDeathMessage(Player player) {
		return player.getTag(COMBAT_MANAGER).getDeathMessage();
	}
}
