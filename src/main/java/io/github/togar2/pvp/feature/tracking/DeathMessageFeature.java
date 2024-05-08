package io.github.togar2.pvp.feature.tracking;

import io.github.togar2.pvp.feature.CombatFeature;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface DeathMessageFeature extends CombatFeature {
	@Nullable Component getDeathMessage(Player player);
}
