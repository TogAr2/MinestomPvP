package io.github.togar2.pvp.feature.attributes;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.entity.LivingEntity;

public interface DataFeature<T> extends CombatFeature {
	DataFeature<Attribute> NO_OP = (entity, attribute) -> 0;
	
	float getValue(LivingEntity entity, T attribute);
}
