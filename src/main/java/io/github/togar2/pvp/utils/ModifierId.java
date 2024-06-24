package io.github.togar2.pvp.utils;

import net.minestom.server.utils.NamespaceID;

public class ModifierId {
	public static final NamespaceID ATTACK_DAMAGE_MODIFIER_ID = NamespaceID.from("minecraft:base_attack_damage");
	public static final NamespaceID ATTACK_SPEED_MODIFIER_ID = NamespaceID.from("minecraft:base_attack_speed");
	
	public static final NamespaceID[] ARMOR_MODIFIERS = new NamespaceID[]{
			NamespaceID.from("minecraft:armor.boots"),
			NamespaceID.from("minecraft:armor.leggings"),
			NamespaceID.from("minecraft:armor.chestplate"),
			NamespaceID.from("minecraft:armor.helmet"),
	};
}
