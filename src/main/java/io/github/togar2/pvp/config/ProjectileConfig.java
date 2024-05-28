package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.config.CombatConfiguration;
import io.github.togar2.pvp.feature.config.CombatFeatures;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * Creates an EventNode with projectile events.
 * This includes fishing rods, snowballs, eggs,
 * ender pearls, bows and crossbows.
 */
public class ProjectileConfig extends ElementConfig<EntityInstanceEvent> {
	public static final ProjectileConfig DEFAULT = defaultBuilder().build();
	public static final ProjectileConfig LEGACY = legacyBuilder().build();
	
	private final boolean
			fishingRodEnabled, snowballEnabled, eggEnabled,
			enderPearlEnabled, crossbowEnabled, bowEnabled,
			tridentEnabled;
	
	ProjectileConfig(boolean legacy, boolean fishingRodEnabled, boolean snowballEnabled,
	                        boolean eggEnabled, boolean enderPearlEnabled, boolean crossbowEnabled,
	                        boolean bowEnabled, boolean tridentEnabled) {
		super(legacy);
		this.fishingRodEnabled = fishingRodEnabled;
		this.snowballEnabled = snowballEnabled;
		this.eggEnabled = eggEnabled;
		this.enderPearlEnabled = enderPearlEnabled;
		this.crossbowEnabled = crossbowEnabled;
		this.bowEnabled = bowEnabled;
		this.tridentEnabled = tridentEnabled;
	}
	
	public boolean isFishingRodEnabled() {
		return fishingRodEnabled;
	}
	
	public boolean isSnowballEnabled() {
		return snowballEnabled;
	}
	
	public boolean isEggEnabled() {
		return eggEnabled;
	}
	
	public boolean isEnderPearlEnabled() {
		return enderPearlEnabled;
	}
	
	public boolean isCrossbowEnabled() {
		return crossbowEnabled;
	}
	
	public boolean isBowEnabled() {
		return bowEnabled;
	}
	
	public boolean isTridentEnabled() {
		return tridentEnabled;
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		//todo trident
		return new CombatConfiguration().legacy(isLegacy())
				.add(CombatFeatures.VANILLA_BOW)
				.add(CombatFeatures.VANILLA_CROSSBOW)
				.add(CombatFeatures.VANILLA_FISHING_ROD)
				.add(CombatFeatures.VANILLA_ITEM_PROJECTILE)
				.build().createNode();
	}
	
	/**
	 * Creates a builder with the default options.
	 *
	 * @return A builder with default options
	 */
	public static ProjectileConfigBuilder defaultBuilder() {
		return new ProjectileConfigBuilder(false).defaultOptions();
	}
	
	/**
	 * Creates a builder with the legacy options.
	 *
	 * @return A builder with legacy options
	 */
	public static ProjectileConfigBuilder legacyBuilder() {
		return new ProjectileConfigBuilder(true).defaultOptions();
	}
	
	/**
	 * Creates an empty builder which has everything disabled.
	 *
	 * @return An empty builder
	 */
	public static ProjectileConfigBuilder emptyBuilder(boolean legacy) {
		return new ProjectileConfigBuilder(legacy);
	}
}
