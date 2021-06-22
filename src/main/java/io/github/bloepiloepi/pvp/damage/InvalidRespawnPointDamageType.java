package io.github.bloepiloepi.pvp.damage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InvalidRespawnPointDamageType extends CustomDamageType {
	
	public InvalidRespawnPointDamageType() {
		super("badRespawnPoint");
		
		this.setScaledWithDifficulty();
		this.setExplosive();
	}
	
	@Override
	public @Nullable Component buildDeathMessage(@NotNull Player killed) {
		Component component = Component.text("[")
				.append(Component.translatable("death.attack.badRespawnPoint.link")
						.clickEvent(ClickEvent.openUrl("https://bugs.mojang.com/browse/MCPE-28723"))
						.hoverEvent(HoverEvent.showText(Component.text("MCPE-28723"))))
				.append(Component.text("]"));
		
		return Component.translatable("death.attack.badRespawnPoint.message", killed.getName(), component);
	}
}
