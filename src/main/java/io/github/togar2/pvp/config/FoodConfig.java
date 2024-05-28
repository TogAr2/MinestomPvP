package io.github.togar2.pvp.config;

import io.github.togar2.pvp.feature.config.CombatConfiguration;
import io.github.togar2.pvp.feature.config.CombatFeatures;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * Creates an EventNode with food events.
 * This includes eating and exhaustion for movement and block breaking.
 */
public class FoodConfig extends ElementConfig<EntityInstanceEvent> {
	public static final FoodConfig DEFAULT = defaultBuilder().build();
	public static final FoodConfig LEGACY = legacyBuilder().build();
	
	private final boolean
			naturalExhaustionEnabled, naturalRegenerationEnabled,
			foodEnabled, foodSoundsEnabled, blockBreakExhaustionEnabled, moveExhaustionEnabled;
	
	FoodConfig(boolean legacy, boolean naturalExhaustionEnabled, boolean naturalRegenerationEnabled,
	           boolean foodEnabled, boolean foodSoundsEnabled, boolean blockBreakExhaustionEnabled,
	           boolean moveExhaustionEnabled) {
		super(legacy);
		this.naturalExhaustionEnabled = naturalExhaustionEnabled;
		this.naturalRegenerationEnabled = naturalRegenerationEnabled;
		this.foodEnabled = foodEnabled;
		this.foodSoundsEnabled = foodSoundsEnabled;
		this.blockBreakExhaustionEnabled = blockBreakExhaustionEnabled;
		this.moveExhaustionEnabled = moveExhaustionEnabled;
	}
	
	public boolean isNaturalExhaustionEnabled() {
		return naturalExhaustionEnabled;
	}
	
	public boolean isNaturalRegenerationEnabled() {
		return naturalRegenerationEnabled;
	}
	
	public boolean isFoodEnabled() {
		return foodEnabled;
	}
	
	public boolean isFoodSoundsEnabled() {
		return foodSoundsEnabled;
	}
	
	public boolean isBlockBreakExhaustionEnabled() {
		return blockBreakExhaustionEnabled;
	}
	
	public boolean isMoveExhaustionEnabled() {
		return moveExhaustionEnabled;
	}
	
	@Override
	public EventNode<EntityInstanceEvent> createNode() {
		return new CombatConfiguration().legacy(isLegacy())
				.add(CombatFeatures.VANILLA_FOOD)
				.add(CombatFeatures.VANILLA_EXHAUSTION)
				.add(CombatFeatures.VANILLA_REGENERATION)
				.build().createNode();
	}
	
	/**
	 * Creates a builder with the default options.
	 *
	 * @return A builder with default options
	 */
	public static FoodConfigBuilder defaultBuilder() {
		return new FoodConfigBuilder(false).defaultOptions();
	}
	
	/**
	 * Creates a builder with the legacy options.
	 *
	 * @return A builder with legacy options
	 */
	public static FoodConfigBuilder legacyBuilder() {
		return new FoodConfigBuilder(true).defaultOptions();
	}
	
	/**
	 * Creates an empty builder which has everything disabled.
	 *
	 * @return An empty builder
	 */
	public static FoodConfigBuilder emptyBuilder(boolean legacy) {
		return new FoodConfigBuilder(legacy);
	}
}
