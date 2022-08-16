package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.listeners.ArmorToolListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;

/**
 * Creates an EventNode with armor and tool related events.
 * This changes attributes like attack damage and armor when
 * an entity equips items.
 */
public class ArmorToolConfig extends ElementConfig<EntityEvent> {
	public static final ArmorToolConfig DEFAULT = new ArmorToolConfig(
			false, true, true
	);
	public static final ArmorToolConfig LEGACY = new ArmorToolConfig(
			true, true, true
	);
	
	private final boolean armorModifiersEnabled;
	private final boolean toolModifiersEnabled;
	
	public ArmorToolConfig(boolean legacy, boolean armorModifiersEnabled, boolean toolModifiersEnabled) {
		super(legacy);
		this.armorModifiersEnabled = armorModifiersEnabled;
		this.toolModifiersEnabled = toolModifiersEnabled;
	}
	
	public boolean isArmorModifiersEnabled() {
		return armorModifiersEnabled;
	}
	
	public boolean isToolModifiersEnabled() {
		return toolModifiersEnabled;
	}
	
	@Override
	public EventNode<EntityEvent> createNode() {
		return ArmorToolListener.events(this);
	}
}
