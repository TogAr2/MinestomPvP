package io.github.togar2.pvp.feature.attributes;

import io.github.togar2.pvp.feature.CombatFeature;

/**
 * Combat feature which handles equipment changes (applies weapon and armor attributes).
 */
public interface EquipmentFeature extends CombatFeature {
	EquipmentFeature NO_OP = new EquipmentFeature() {};
}
