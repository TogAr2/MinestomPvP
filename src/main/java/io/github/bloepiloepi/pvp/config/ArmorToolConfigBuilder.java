package io.github.bloepiloepi.pvp.config;

public class ArmorToolConfigBuilder {
	private final boolean legacy;
	private boolean armorModifiersEnabled, toolModifiersEnabled;
	
	ArmorToolConfigBuilder(boolean legacy) {
		this.legacy = legacy;
	}
	
	public ArmorToolConfigBuilder defaultOptions() {
		armorModifiersEnabled = true;
		toolModifiersEnabled = true;
		return this;
	}
	
	public ArmorToolConfigBuilder armorModifiers(boolean armorModifiersEnabled) {
		this.armorModifiersEnabled = armorModifiersEnabled;
		return this;
	}
	
	public ArmorToolConfigBuilder toolModifiers(boolean toolModifiersEnabled) {
		this.toolModifiersEnabled = toolModifiersEnabled;
		return this;
	}
	
	public ArmorToolConfig build() {
		return new ArmorToolConfig(legacy, armorModifiersEnabled, toolModifiersEnabled);
	}
}
