package io.github.bloepiloepi.pvp.config;

import io.github.bloepiloepi.pvp.listeners.ArmorToolListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityInstanceEvent;

/**
 * Creates an EventNode with armor and tool related events.
 * This changes attributes like attack damage and armor when
 * an entity equips items.
 */
public class ArmorToolConfig extends ElementConfig<EntityInstanceEvent> {
	public static final ArmorToolConfig DEFAULT = defaultBuilder().build();
	public static final ArmorToolConfig LEGACY = legacyBuilder().build();
	
	private final boolean armorModifiersEnabled, toolModifiersEnabled;
	
	ArmorToolConfig(boolean legacy, boolean armorModifiersEnabled, boolean toolModifiersEnabled) {
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
	public EventNode<EntityInstanceEvent> createNode() {
		return ArmorToolListener.events(this);
	}
	
	/**
	 * Creates a builder with the default options.
	 *
	 * @return A builder with default options
	 */
	public static ArmorToolConfigBuilder defaultBuilder() {
		return new ArmorToolConfigBuilder(false).defaultOptions();
	}
	
	/**
	 * Creates a builder with the legacy options.
	 *
	 * @return A builder with legacy options
	 */
	public static ArmorToolConfigBuilder legacyBuilder() {
		return new ArmorToolConfigBuilder(true).defaultOptions();
	}
	
	/**
	 * Creates an empty builder which has everything disabled.
	 *
	 * @return An empty builder
	 */
	public static ArmorToolConfigBuilder emptyBuilder(boolean legacy) {
		return new ArmorToolConfigBuilder(legacy);
	}
}
