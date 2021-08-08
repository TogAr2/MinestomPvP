package io.github.bloepiloepi.pvp.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.Position;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SoundManager {
	public static void sendToAround(Instance instance, Position position, SoundEvent sound, Sound.Source source, float volume, float pitch, Predicate<Player> predicate) {
		double distance = volume > 1.0F ? (double) (16.0F * volume) : 16.0D;
		
		Predicate<Player> positionPredicate = (player) -> player.getPosition().getDistance(position) < distance;
		if (predicate != null) {
			predicate = predicate.and(positionPredicate);
		} else {
			predicate = positionPredicate;
		}
		
		Audience audience = Audience.audience(instance
				.getPlayers().stream().filter(predicate)
				.collect(Collectors.toList()));
		
		audience.playSound(Sound.sound(sound, source, volume, pitch), position.getX(), position.getY(), position.getZ());
	}
	
	public static void sendToAround(Instance instance, Position position, SoundEvent sound, Sound.Source source, float volume, float pitch) {
		sendToAround(instance, position, sound, source, volume, pitch, (player) -> true);
	}
	
	public static void sendToAround(Entity entity, SoundEvent sound, Sound.Source source, float volume, float pitch) {
		sendToAround(Objects.requireNonNull(entity.getInstance()), entity.getPosition(), sound, source, volume, pitch);
	}
	
	public static void sendToAround(Player notSend, Entity entity, SoundEvent sound, Sound.Source source, float volume, float pitch) {
		sendToAround(Objects.requireNonNull(entity.getInstance()), entity.getPosition(), sound, source, volume, pitch, (player) -> player != notSend);
	}
}
