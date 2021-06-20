package io.github.bloepiloepi.pvp.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.sound.SoundEvent;

import java.util.Objects;
import java.util.stream.Collectors;

public class SoundManager {
	public static void sendToAround(Entity entity, SoundEvent sound, Sound.Source source, float volume, float pitch) {
		double distance = volume > 1.0F ? (double) (16.0F * volume) : 16.0D;
		
		Audience audience = Audience.audience(Objects.requireNonNull(entity.getInstance())
				.getPlayers().stream().filter((player) -> player.getDistance(entity) < distance)
				.collect(Collectors.toList()));
		
		audience.playSound(Sound.sound(sound, source, volume, pitch), entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getZ());
	}
}
