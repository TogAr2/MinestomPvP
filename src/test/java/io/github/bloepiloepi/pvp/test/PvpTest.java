package io.github.bloepiloepi.pvp.test;

import io.github.bloepiloepi.pvp.PvpExtension;
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
		
		MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event ->
				event.getPlayer().setGameMode(GameMode.CREATIVE));
		
		GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
		eventHandler.addChild(PvpExtension.events());
		
		Commands.init();
		
		OpenToLAN.open();
		
		server.start("localhost", 25565);
	}
}
