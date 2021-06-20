package io.github.bloepiloepi.pvp;

import io.github.bloepiloepi.pvp.enchantment.CustomEnchantments;
import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.food.FoodListener;
import io.github.bloepiloepi.pvp.listeners.ArmorToolListener;
import io.github.bloepiloepi.pvp.listeners.AttackManager;
import io.github.bloepiloepi.pvp.listeners.DamageListener;
import io.github.bloepiloepi.pvp.listeners.PositionListener;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffects;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionTypes;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.extensions.Extension;

public class PvpExtension extends Extension {
	
	public static EventNode<EntityEvent> newEventNode() {
		EventNode<EntityEvent> eventNode = EventNode.type("pvp-events", EventFilter.ENTITY);
		
		Tracker.register(eventNode);
		AttackManager.register(eventNode);
		DamageListener.register(eventNode);
		ArmorToolListener.register(eventNode);
		FoodListener.register(eventNode);
		PositionListener.register(eventNode);
		PotionListener.register(eventNode);
		
		return eventNode;
	}
	
	@Override
	public void initialize() {
		CustomEnchantments.registerAll();
		CustomPotionEffects.registerAll();
		CustomPotionTypes.registerAll();
	}
	
	@Override
	public void terminate() {
	
	}
}
