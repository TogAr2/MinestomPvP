package io.github.togar2.pvp;

import io.github.togar2.pvp.enchantment.CombatEnchantments;
import io.github.togar2.pvp.feature.CombatFeatures;
import io.github.togar2.pvp.feature.config.CombatFeatureRegistry;
import io.github.togar2.pvp.player.CombatPlayer;
import io.github.togar2.pvp.player.CombatPlayerImpl;
import io.github.togar2.pvp.potion.effect.CustomPotionEffects;
import io.github.togar2.pvp.potion.item.CombatPotionTypes;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

public class MinestomPvP {
	public static EventNode<EntityInstanceEvent> events() {
		return CombatFeatures.modernVanilla().createNode();
	}
	
	public static EventNode<EntityInstanceEvent> legacyEvents() {
		return CombatFeatures.legacyVanilla().createNode();
	}
	
	/**
	 * Disables or enables legacy attack for a player.
	 * With legacy attack, the player has no attack speed and 1.0 attack damage instead of 2.0.
	 *
	 * @param player the player
	 * @param legacyAttack {@code true} if legacy attack should be enabled
	 */
	public static void setLegacyAttack(Player player, boolean legacyAttack) {
		AttributeInstance speed = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		AttributeInstance damage = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
		if (legacyAttack) {
			speed.setBaseValue(100);
			damage.setBaseValue(1.0F);
		} else {
			speed.setBaseValue(speed.attribute().defaultValue());
			damage.setBaseValue(damage.attribute().defaultValue());
		}
	}
	
	/**
	 * Initialize the PvP extension.
	 */
	public static void init() {
		CombatEnchantments.registerAll();
		CustomPotionEffects.registerAll();
		CombatPotionTypes.registerAll();
		
		CombatFeatureRegistry.init();
		MinecraftServer.getConnectionManager().setPlayerProvider(CombatPlayerImpl::new);
		CombatPlayer.init(MinecraftServer.getGlobalEventHandler());
	}
}
