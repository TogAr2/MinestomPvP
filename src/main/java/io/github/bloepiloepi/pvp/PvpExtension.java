package io.github.bloepiloepi.pvp;

import io.github.bloepiloepi.pvp.enchantment.CustomEnchantments;
import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.food.FoodListener;
import io.github.bloepiloepi.pvp.listeners.ArmorToolListener;
import io.github.bloepiloepi.pvp.listeners.AttackManager;
import io.github.bloepiloepi.pvp.listeners.DamageListener;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffects;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionTypes;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.extensions.Extension;

public class PvpExtension extends Extension {
	
	public static EventNode<EntityEvent> events() {
		EventNode<EntityEvent> node = EventNode.type("pvp-events", EventFilter.ENTITY);
		
		node.addChild(attackEvents());
		node.addChild(damageEvents());
		node.addChild(armorToolEvents());
		node.addChild(foodEvents());
		node.addChild(potionEvents());
		
		return node;
	}
	
	/**
	 * Creates an EventNode with attack events.
	 * This includes entity hitting, attack cooldown
	 * and spectating entities as a spectator.
	 *
	 * @return The EventNode with attack events
	 */
	public static EventNode<EntityEvent> attackEvents() {
		return AttackManager.events();
	}
	
	/**
	 * Creates an EventNode with damage events.
	 * This includes armor, shields and damage invulnerability.
	 * (This only reduces damage based on armor attribute,
	 * to change that attribute for different types of armor you need #armorToolEvents().
	 *
	 * @return The EventNode with damage events
	 */
	public static EventNode<EntityEvent> damageEvents() {
		return DamageListener.events();
	}
	
	/**
	 * Creates an EventNode with armor and tool related events.
	 * This changes attributes like attack damage and armor when
	 * an entity equips items.
	 *
	 * @return The EventNode with armor and tool events
	 */
	public static EventNode<EntityEvent> armorToolEvents() {
		return ArmorToolListener.events();
	}
	
	/**
	 * Creates an EventNode with food events.
	 * This includes eating and exhaustion for movement and block breaking.
	 *
	 * @return The EventNode with food events
	 */
	public static EventNode<PlayerEvent> foodEvents() {
		return FoodListener.events();
	}
	
	/**
	 * Creates an EventNode with potion events.
	 * This includes potion drinking, potion splashing and effects
	 * for potion add and remove (like glowing and invisibility).
	 *
	 * @return The EventNode with potion events
	 */
	public static EventNode<EntityEvent> potionEvents() {
		return PotionListener.events();
	}
	
	@Override
	public void initialize() {
		CustomEnchantments.registerAll();
		CustomPotionEffects.registerAll();
		CustomPotionTypes.registerAll();
		
		Tracker.register(getEventNode());
	}
	
	@Override
	public void terminate() {
	
	}
}
