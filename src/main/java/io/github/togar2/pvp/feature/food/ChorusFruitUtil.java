package io.github.togar2.pvp.feature.food;

import io.github.togar2.pvp.feature.cooldown.ItemCooldownFeature;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.Material;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.world.DimensionType;

import java.util.concurrent.ThreadLocalRandom;

public class ChorusFruitUtil {
	private static boolean randomTeleport(Entity entity, Pos to) {
		Instance instance = entity.getInstance();
		assert instance != null;
		
		boolean success = false;
		int lowestY = to.blockY();
		if (lowestY == 0) lowestY++;
		while (lowestY > MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType()).minY()) {
			Block block = instance.getBlock(to.blockX(), lowestY - 1, to.blockZ());
			if (!block.isAir() && !block.isLiquid()) {
				Block above = instance.getBlock(to.blockX(), lowestY, to.blockZ());
				Block above2 = instance.getBlock(to.blockX(), lowestY + 1, to.blockZ());
				if (above.isAir() && above2.isAir()) {
					success = true;
					break;
				} else {
					lowestY--;
				}
			} else {
				lowestY--;
			}
		}
		
		if (!success) return false;
		
		entity.teleport(to.withY(lowestY));
		entity.triggerStatus((byte) 46);
		
		return true;
	}
	
	public static void tryChorusTeleport(Player player, ItemCooldownFeature cooldownFeature) {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		Instance instance = player.getInstance();
		assert instance != null;
		
		Pos prevPosition = player.getPosition();
		double prevX = prevPosition.x();
		double prevY = prevPosition.y();
		double prevZ = prevPosition.z();
		
		float pitch = prevPosition.pitch();
		float yaw = prevPosition.yaw();
		
		DimensionType dimensionType = MinecraftServer.getDimensionTypeRegistry().get(instance.getDimensionType());
		assert dimensionType != null;
		
		// Max 16 tries
		for (int i = 0; i < 16; i++) {
			double x = prevX + (random.nextDouble() - 0.5) * 16.0;
			double y = MathUtils.clamp(prevY + (random.nextInt(16) - 8),
					dimensionType.minY(), dimensionType.minY()
							+ dimensionType.logicalHeight() - 1);
			double z = prevZ + (random.nextDouble() - 0.5) * 16.0;
			
			if (player.getVehicle() != null) {
				player.getVehicle().removePassenger(player);
			}
			
			if (randomTeleport(player, new Pos(x, y, z, yaw, pitch))) {
				ViewUtil.packetGroup(player).playSound(Sound.sound(
						SoundEvent.ITEM_CHORUS_FRUIT_TELEPORT, Sound.Source.PLAYER,
						1.0f, 1.0f
				), prevPosition);
				
				if (!player.isSilent()) {
					player.getViewersAsAudience().playSound(Sound.sound(
							SoundEvent.ITEM_CHORUS_FRUIT_TELEPORT, Sound.Source.PLAYER,
							1.0f, 1.0f
					), player);
				}
				
				break;
			}
		}
		
		cooldownFeature.setCooldown(player, Material.CHORUS_FRUIT, 20);
	}
}
