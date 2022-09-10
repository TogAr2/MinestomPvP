package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.food.FoodListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerInstanceEvent;

/**
 * Creates an EventNode with food events.
 * This includes eating and exhaustion for movement and block breaking.
 */
public class FoodConfig extends ElementConfig<PlayerInstanceEvent> {
	public static final FoodConfig DEFAULT = defaultBuilder().build();
	public static final FoodConfig LEGACY = legacyBuilder().build();
	
	private final boolean
			naturalExhaustionEnabled, naturalRegenerationEnabled,
			foodEnabled, blockBreakExhaustionEnabled, moveExhaustionEnabled;
	
	FoodConfig(boolean legacy, boolean naturalExhaustionEnabled, boolean naturalRegenerationEnabled,
	           boolean foodEnabled, boolean blockBreakExhaustionEnabled, boolean moveExhaustionEnabled) {
		super(legacy);
		this.naturalExhaustionEnabled = naturalExhaustionEnabled;
		this.naturalRegenerationEnabled = naturalRegenerationEnabled;
		this.foodEnabled = foodEnabled;
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
	
	public boolean isBlockBreakExhaustionEnabled() {
		return blockBreakExhaustionEnabled;
	}
	
	public boolean isMoveExhaustionEnabled() {
		return moveExhaustionEnabled;
	}
	
	@Override
	public EventNode<PlayerInstanceEvent> createNode() {
		return FoodListener.events(this);
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
