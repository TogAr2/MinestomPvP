package io.github.bloepiloepi.pvp.utils;

import net.minestom.server.effects.Effects;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.PacketUtils;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public class EffectManager {
	
	public static void sendNearby(@NotNull Instance instance, @NotNull Effects effect,
	                              int x, int y, int z, int data, double distance, boolean global) {
		EffectPacket packet = new EffectPacket();
		packet.effectId = effect.getId();
		packet.position = new BlockPosition(x, y, z);
		packet.data = data;
		packet.disableRelativeVolume = global;
		
		double distanceSquared = distance * distance;
		PacketUtils.sendGroupedPacket(instance.getPlayers(), packet, player -> {
			Position position = player.getPosition();
			double dx = x - position.getX();
			double dy = y - position.getY();
			double dz = z - position.getZ();
			
			return dx * dx + dy * dy + dz * dz < distanceSquared;
		});
	}
}
