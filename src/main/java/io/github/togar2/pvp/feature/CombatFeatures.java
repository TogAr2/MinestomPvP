package io.github.togar2.pvp.feature;

import io.github.togar2.pvp.feature.provider.DifficultyProvider;
import io.github.togar2.pvp.utils.CombatVersion;

public class CombatFeatures {
	public static final RegistrableFeature MODERN_VANILLA = new CombatConfiguration()
			.add(CombatVersion.MODERN)
			.add(DifficultyProvider.DEFAULT)
			.addAllVanilla()
			.build();
	
	public static final RegistrableFeature LEGACY_VANILLA = new CombatConfiguration()
			.add(CombatVersion.LEGACY)
			.add(DifficultyProvider.DEFAULT)
			.addAllVanilla()
			.build();
	
	public static RegistrableFeature getVanilla(CombatVersion version, DifficultyProvider difficultyProvider) {
		return new CombatConfiguration()
				.add(version).add(difficultyProvider)
				.addAllVanilla()
				.build();
	}
}
