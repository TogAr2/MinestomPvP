package io.github.togar2.pvp.feature.attributes;

import io.github.togar2.pvp.feature.CombatFeature;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;

public interface DataFeature<T> extends CombatFeature {
	DataFeature<Attribute> NO_OP = (entity, attribute) -> 0;
	
	double getValue(LivingEntity entity, T attribute);
}
