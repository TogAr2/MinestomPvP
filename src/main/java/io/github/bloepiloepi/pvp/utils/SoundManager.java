package io.github.bloepiloepi.pvp.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SoundManager {
	public static void sendToAround(Instance instance, Position position, SoundEvent sound, Sound.Source source, float volume, float pitch) {
		double distance = volume > 1.0F ? (double) (16.0F * volume) : 16.0D;
		
		Audience audience = Audience.audience(instance
				.getPlayers().stream().filter((player) -> player.getPosition().getDistance(position) < distance)
				.collect(Collectors.toList()));
		
		audience.playSound(Sound.sound(sound, source, volume, pitch), position.getX(), position.getY(), position.getZ());
	}
	
	public static void sendToAround(Entity entity, SoundEvent sound, Sound.Source source, float volume, float pitch) {
		sendToAround(Objects.requireNonNull(entity.getInstance()), entity.getPosition(), sound, source, volume, pitch);
	}
}
