package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.food.FoodListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.PlayerEvent;

public class FoodConfig extends PvPConfig<PlayerEvent> {
	public static final FoodConfig DEFAULT = new FoodConfig(
			false, true, true,
			true, true, true
	);
	public static final FoodConfig LEGACY = new FoodConfig(
			true, true, true,
			true, true, true
	);
	
	private final boolean naturalExhaustionEnabled;
	private final boolean naturalRegenerationEnabled;
	private final boolean foodEnabled;
	private final boolean blockBreakExhaustionEnabled;
	private final boolean moveExhaustionEnabled;
	
	public FoodConfig(boolean legacy, boolean naturalExhaustionEnabled, boolean naturalRegenerationEnabled, boolean foodEnabled, boolean blockBreakExhaustionEnabled, boolean moveExhaustionEnabled) {
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
}
