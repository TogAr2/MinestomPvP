package io.github.togar2.pvp.utils;

import net.kyori.adventure.audience.Audience;
import net.minestom.server.adventure.audience.PacketGroupingAudience;
import net.minestom.server.entity.Entity;

import java.util.Collections;

public class ViewUtil {
	public static Audience viewersAndSelf(Entity origin) {
		if (origin.getChunk() == null) return Audience.empty();
		return origin.getChunk().getViewersAsAudience();
	}
	
	public static PacketGroupingAudience packetGroup(Entity origin) {
		if (origin.getChunk() == null) return PacketGroupingAudience.of(Collections.emptyList());
		return ((PacketGroupingAudience) origin.getChunk().getViewersAsAudience());
	}
}
