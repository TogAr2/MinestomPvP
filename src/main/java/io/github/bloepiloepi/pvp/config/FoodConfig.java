package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.food.FoodListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerEvent;

/**
 * Creates an EventNode with food events.
 * This includes eating and exhaustion for movement and block breaking.
 */
public class FoodConfig extends ElementConfig<PlayerEvent> {
	public static final FoodConfig DEFAULT = new FoodConfigBuilder(false).defaultOptions().build();
	public static final FoodConfig LEGACY = new FoodConfigBuilder(true).defaultOptions().build();
	
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
	public EventNode<PlayerEvent> createNode() {
		return FoodListener.events(this);
	}
	
	public static FoodConfigBuilder builder(boolean legacy) {
		return new FoodConfigBuilder(legacy);
	}
}
