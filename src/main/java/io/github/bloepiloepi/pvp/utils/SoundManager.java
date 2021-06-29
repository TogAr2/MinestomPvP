package io.github.bloepiloepi.pvp.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.sound.SoundEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SoundManager {
	public static final Map<EntityType, SoundEvent> ENTITY_HURT_SOUND = new HashMap<>();
	
	public static void sendToAround(Entity entity, SoundEvent sound, Sound.Source source, float volume, float pitch) {
		double distance = volume > 1.0F ? (double) (16.0F * volume) : 16.0D;
		
		Audience audience = Audience.audience(Objects.requireNonNull(entity.getInstance())
				.getPlayers().stream().filter((player) -> player.getDistance(entity) < distance)
				.collect(Collectors.toList()));
		
		audience.playSound(Sound.sound(sound, source, volume, pitch), entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getZ());
	}
	
	//public static SoundEvent getHurtSound(LivingEntity entity) {
	//	ENTITY_HURT_SOUND.getOrDefault(entity.getEntityType(), SoundEvent.GENERIC_HURT);
	//}
	
	//static {
	//	ENTITY_HURT_SOUND.put(EntityType.PLAYER, );
	//}
}
