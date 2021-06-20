package io.github.bloepiloepi.pvp.test;

import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.food.FoodListener;
import io.github.bloepiloepi.pvp.listeners.ArmorToolListener;
import io.github.bloepiloepi.pvp.listeners.AttackManager;
import io.github.bloepiloepi.pvp.listeners.DamageListener;
import io.github.bloepiloepi.pvp.listeners.PositionListener;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import io.github.bloepiloepi.pvp.test.commands.Commands;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;

public class PvpTest {
	public static void main(String[] args) {
		MinecraftServer server = MinecraftServer.init();
		
		Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
		instance.setChunkGenerator(new DemoGenerator());
		instance.enableAutoChunkLoad(true);
		
		MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
			event.setSpawningInstance(instance);
			event.getPlayer().setRespawnPoint(new Position(0, 60, 0));
		});
		
		MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
			event.getPlayer().setGameMode(GameMode.CREATIVE);
		});
		
		GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
		
		Tracker.register(eventHandler);
		AttackManager.register(eventHandler);
		DamageListener.register(eventHandler);
		ArmorToolListener.register(eventHandler);
		FoodListener.register(eventHandler);
		PositionListener.register(eventHandler);
		PotionListener.register(eventHandler);
		
		Commands.init();
		
		OpenToLAN.open();
		
		server.start("localhost", 25565);
	}
}
