package io.github.bloepiloepi.pvp.utils;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.effects.Effects;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket;
import net.minestom.server.network.packet.server.play.EffectPacket;
import net.minestom.server.utils.PacketUtils;
import org.jetbrains.annotations.NotNull;

public class EffectManager {
	
	public static void sendNearby(@NotNull Instance instance, @NotNull Effects effect,
	                              int x, int y, int z, int data, double distance, boolean global) {
		EffectPacket packet = new EffectPacket(effect.getId(), new Pos(x, y, z), data, global);
		
		double distanceSquared = distance * distance;
		PacketUtils.sendGroupedPacket(instance.getPlayers(), packet, player -> {
			Pos position = player.getPosition();
			double dx = x - position.x();
			double dy = y - position.y();
			double dz = z - position.z();
			
			return dx * dx + dy * dy + dz * dz < distanceSquared;
		});
	}
	
	public static void sendGameState(@NotNull Player player,
	                                 @NotNull ChangeGameStatePacket.Reason reason, float value) {
		ChangeGameStatePacket packet = new ChangeGameStatePacket(reason, value);
		player.getPlayerConnection().sendPacket(packet);
	}
}
