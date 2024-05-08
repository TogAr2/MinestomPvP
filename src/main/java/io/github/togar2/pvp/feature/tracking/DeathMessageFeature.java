package io.github.togar2.pvp.feature.tracking;

import io.github.togar2.pvp.feature.CombatFeature;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

public interface DeathMessageFeature extends CombatFeature {
	Component getDeathMessage(Player player);
}
