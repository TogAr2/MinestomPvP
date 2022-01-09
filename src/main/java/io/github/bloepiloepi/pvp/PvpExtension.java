package io.github.bloepiloepi.pvp;

import io.github.bloepiloepi.pvp.enchantment.CustomEnchantments;
import io.github.bloepiloepi.pvp.entities.ArrowPickup;
import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.food.FoodListener;
import io.github.bloepiloepi.pvp.legacy.SwordBlockHandler;
import io.github.bloepiloepi.pvp.listeners.ArmorToolListener;
import io.github.bloepiloepi.pvp.listeners.AttackManager;
import io.github.bloepiloepi.pvp.listeners.DamageListener;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.potion.effect.CustomPotionEffects;
import io.github.bloepiloepi.pvp.potion.item.CustomPotionTypes;
import io.github.bloepiloepi.pvp.projectile.ProjectileListener;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.AttributeInstance;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.extensions.Extension;
import net.minestom.server.item.Material;
import net.minestom.server.registry.Registry;

import java.lang.reflect.Field;

public class PvpExtension extends Extension {
	
	public static EventNode<EntityEvent> events() {
		EventNode<EntityEvent> node = EventNode.type("pvp-events", EventFilter.ENTITY);
		
		node.addChild(attackEvents());
		node.addChild(damageEvents());
		node.addChild(armorToolEvents());
		node.addChild(foodEvents());
		node.addChild(potionEvents());
		node.addChild(projectileEvents());
		
		return node;
	}
	
	public static EventNode<EntityEvent> legacyEvents() {
		EventNode<EntityEvent> node = EventNode.type("legacy-pvp-events", EventFilter.ENTITY);
		
		node.addChild(AttackManager.events(true));
		node.addChild(DamageListener.events(true));
		node.addChild(ArmorToolListener.events(true));
		node.addChild(FoodListener.events(true));
		node.addChild(PotionListener.events(true));
		node.addChild(ProjectileListener.events(true));
		node.addChild(SwordBlockHandler.legacyEvents());
		
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
		return AttackManager.events(false);
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
		return DamageListener.events(false);
	}
	
	/**
	 * Creates an EventNode with armor and tool related events.
	 * This changes attributes like attack damage and armor when
	 * an entity equips items.
	 *
	 * @return The EventNode with armor and tool events
	 */
	public static EventNode<EntityEvent> armorToolEvents() {
		return ArmorToolListener.events(false);
	}
	
	/**
	 * Creates an EventNode with food events.
	 * This includes eating and exhaustion for movement and block breaking.
	 *
	 * @return The EventNode with food events
	 */
	public static EventNode<PlayerEvent> foodEvents() {
		return FoodListener.events(false);
	}
	
	/**
	 * Creates an EventNode with potion events.
	 * This includes potion drinking, potion splashing and effects
	 * for potion add and remove (like glowing and invisibility).
	 *
	 * @return The EventNode with potion events
	 */
	public static EventNode<EntityEvent> potionEvents() {
		return PotionListener.events(false);
	}
	
	/**
	 * Creates an EventNode with projectile events.
	 * This includes fishing rods, snowballs, eggs,
	 * ender pearls, bows and crossbows.
	 *
	 * @return The EventNode with projectile events
	 */
	public static EventNode<PlayerEvent> projectileEvents() {
		return ProjectileListener.events(false);
	}
	
	/**
	 * Disables or enables legacy attack for a player.
	 * With legacy attack, the player has no attack speed and 1.0 attack damage instead of 2.0.
	 *
	 * @param player the player
	 * @param legacyAttack {@code true} if legacy attack should be enabled
	 */
	public static void setLegacyAttack(Player player, boolean legacyAttack) {
		AttributeInstance speed = player.getAttribute(Attribute.ATTACK_SPEED);
		AttributeInstance damage = player.getAttribute(Attribute.ATTACK_DAMAGE);
		if (legacyAttack) {
			speed.setBaseValue(100);
			damage.setBaseValue(1.0F);
		} else {
			speed.setBaseValue(speed.getAttribute().defaultValue());
			damage.setBaseValue(damage.getAttribute().defaultValue());
		}
	}
	
	@Override
	public void initialize() {
		init();
		
		getEventNode().addChild(events());
	}
	
	@Override
	public void terminate() {
		ArrowPickup.stop();
	}
	
	/**
	 * Initialize the PvP extension.
	 */
	public static void init() {
		CustomEnchantments.registerAll();
		CustomPotionEffects.registerAll();
		CustomPotionTypes.registerAll();
		
		Tracker.register(MinecraftServer.getGlobalEventHandler());
		
		ArrowPickup.init();
		
		try {
			Field isFood = Registry.MaterialEntry.class.getDeclaredField("isFood");
			isFood.setAccessible(true);
			isFood.set(Material.POTION.registry(), true);
			isFood.set(Material.MILK_BUCKET.registry(), true);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
