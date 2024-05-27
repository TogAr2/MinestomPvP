package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.CombatConfiguration;
import io.github.togar2.pvp.feature.attack.VanillaAttackFeature;
import io.github.togar2.pvp.feature.attack.VanillaCriticalFeature;
import io.github.togar2.pvp.feature.attack.VanillaSweepingFeature;
import io.github.togar2.pvp.feature.cooldown.VanillaCooldownFeature;
import io.github.togar2.pvp.feature.food.VanillaExhaustionFeature;
import io.github.togar2.pvp.feature.item.VanillaItemDamageFeature;
import io.github.togar2.pvp.feature.knockback.VanillaKnockbackFeature;
import io.github.togar2.pvp.feature.provider.DifficultyProvider;
import io.github.togar2.pvp.feature.spectate.VanillaSpectateFeature;
import io.github.togar2.pvp.listeners.AttackHandler;
import io.github.togar2.pvp.utils.CombatVersion;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * Creates an EventNode with attack events.
 * This includes entity hitting, attack cooldown
 * and spectating entities as a spectator.
 */
public class AttackConfig extends ElementConfig<EntityInstanceEvent> {
	public static final AttackConfig DEFAULT = defaultBuilder().build();
	public static final AttackConfig LEGACY = legacyBuilder().build();
	
	private final AttackHandler handler;
	private final boolean
			spectatingEnabled, attackCooldownEnabled, legacyKnockback,
			soundsEnabled, toolDamageEnabled, damageIndicatorParticlesEnabled,
			exhaustionEnabled;
	
	AttackConfig(boolean legacy, AttackHandler handler, boolean spectatingEnabled, boolean attackCooldownEnabled,
	             boolean legacyKnockback, boolean soundsEnabled, boolean toolDamageEnabled,
	             boolean damageIndicatorParticlesEnabled, boolean exhaustionEnabled) {
		super(legacy);
		this.handler = handler;
		this.spectatingEnabled = spectatingEnabled;
		this.attackCooldownEnabled = attackCooldownEnabled;
		this.legacyKnockback = legacyKnockback;
		this.soundsEnabled = soundsEnabled;
		this.toolDamageEnabled = toolDamageEnabled;
		this.damageIndicatorParticlesEnabled = damageIndicatorParticlesEnabled;
		this.exhaustionEnabled = exhaustionEnabled;
	}
	
	public AttackHandler getHandler() {
		return handler;
	}
	
	public boolean isSpectatingEnabled() {
		return spectatingEnabled;
	}
	
	public boolean isAttackCooldownEnabled() {
		return attackCooldownEnabled;
	}
	
	public boolean isLegacyKnockback() {
		return legacyKnockback;
	}
	
	public boolean isSoundsEnabled() {
		return soundsEnabled;
	}
	
	public boolean isToolDamageEnabled() {
		return toolDamageEnabled;
	}
	
	public boolean isDamageIndicatorParticlesEnabled() {
		return damageIndicatorParticlesEnabled;
	}
	
	public boolean isExhaustionEnabled() {
		return exhaustionEnabled;
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		return new CombatConfiguration()
				.add(VanillaSpectateFeature.class)
				.add(VanillaAttackFeature.class)
				.add(VanillaCooldownFeature.class)
				.add(VanillaExhaustionFeature.class)
				.add(VanillaItemDamageFeature.class)
				.add(VanillaCriticalFeature.class)
				.add(VanillaSweepingFeature.class)
				.add(VanillaKnockbackFeature.class)
				.add(DifficultyProvider.DEFAULT)
				.add(CombatVersion.fromLegacy(isLegacy()))
				.build().createNode();
	}
	
	/**
	 * Creates a builder with the default options.
	 *
	 * @return A builder with default options
	 */
	public static AttackConfigBuilder defaultBuilder() {
		return new AttackConfigBuilder(false).defaultOptions();
	}
	
	/**
	 * Creates a builder with the legacy options.
	 *
	 * @return A builder with legacy options
	 */
	public static AttackConfigBuilder legacyBuilder() {
		return new AttackConfigBuilder(true).legacyOptions();
	}
	
	/**
	 * Creates an empty builder which has everything disabled.
	 *
	 * @return An empty builder
	 */
	public static AttackConfigBuilder emptyBuilder(boolean legacy) {
		return new AttackConfigBuilder(legacy);
	}
}
