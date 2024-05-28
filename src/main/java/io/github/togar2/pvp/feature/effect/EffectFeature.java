package io.github.togar2.pvp.feature.effect;

import io.github.togar2.pvp.feature.CombatFeature;

public interface EffectFeature extends CombatFeature {
	EffectFeature NO_OP = new EffectFeature() {};
}
