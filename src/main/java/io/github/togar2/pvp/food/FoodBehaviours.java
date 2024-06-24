package io.github.togar2.pvp.food;

import io.github.togar2.pvp.entity.EntityUtils;
import io.github.togar2.pvp.utils.ViewUtil;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.sound.SoundEvent;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.world.DimensionType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class FoodBehaviours {
	private static final Map<Material, FoodBehaviour> COMPONENTS = new HashMap<>();
	
	private static final FoodBehaviour CHORUS_FRUIT_BEHAVIOUR = new FoodBehaviour(ItemStack.AIR) {
		@Override
		public void onEat(Player player, ItemStack stack) {
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
				
				if (EntityUtils.randomTeleport(player, new Pos(x, y, z, yaw, pitch), true)) {
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
			
			//TODO feature Tracker.setCooldown(player, Material.CHORUS_FRUIT, 20);
		}
	};
	
	public static void register(Material material, FoodBehaviour component) {
		COMPONENTS.put(material, component);
	}
	
	public static FoodBehaviour fromMaterial(Material material) {
		return COMPONENTS.get(material);
	}
	
	public static void registerAll() {
		register(Material.MILK_BUCKET, new FoodBehaviour(ItemStack.of(Material.BUCKET)) {
			@Override
			public void onEat(Player player, ItemStack stack) {
				player.clearEffects();
			}
		});
		
		register(Material.HONEY_BOTTLE, new FoodBehaviour(ItemStack.of(Material.GLASS_BOTTLE)) {
			@Override
			public void onEat(Player player, ItemStack stack) {
				player.removeEffect(PotionEffect.POISON);
			}
		});
		
		register(Material.CHORUS_FRUIT, CHORUS_FRUIT_BEHAVIOUR);
	}
}
