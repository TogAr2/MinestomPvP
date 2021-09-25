package io.github.bloepiloepi.pvp.test;

import io.github.bloepiloepi.pvp.PvpExtension;
import io.github.bloepiloepi.pvp.test.commands.Commands;
import net.minestom.server.MinecraftServer;
import net.minestom.server.attribute.Attribute;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.*;
import net.minestom.server.extras.lan.OpenToLAN;
import net.minestom.server.instance.Instance;

public class PvpTest {
	public static void main(String[] args) {
		MinecraftServer server = MinecraftServer.init();
		
		Instance instance = MinecraftServer.getInstanceManager().createInstanceContainer();
		instance.setChunkGenerator(new DemoGenerator());
		instance.enableAutoChunkLoad(true);
		
		Pos spawn = new Pos(0, 60, 0);
		MinecraftServer.getGlobalEventHandler().addListener(PlayerLoginEvent.class, event -> {
			event.setSpawningInstance(instance);
			event.getPlayer().setRespawnPoint(spawn);
			event.getPlayer().setPermissionLevel(4);
			event.getPlayer().getAttribute(Attribute.MAX_HEALTH).setBaseValue(1000);
			event.getPlayer().heal();
			
			LivingEntity entity = new LivingEntity(EntityType.ZOMBIE);
			entity.setInstance(instance, spawn);
			entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(500);
			entity.heal();
		});
		
		MinecraftServer.getGlobalEventHandler().addListener(PlayerSpawnEvent.class, event -> {
			event.getPlayer().setGameMode(GameMode.CREATIVE);
			PvpExtension.setLegacyAttack(event.getPlayer(), true);
		});
		
		MinecraftServer.getGlobalEventHandler().addListener(PlayerStartFlyingEvent.class,
				event -> event.getPlayer().setNoGravity(true));
		MinecraftServer.getGlobalEventHandler().addListener(PlayerStopFlyingEvent.class,
				event -> event.getPlayer().setNoGravity(false));
		
		GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();
		eventHandler.addChild(PvpExtension.legacyEvents());
		
		Commands.init();
		
		OpenToLAN.open();
		
		server.start("localhost", 25565);
	}
}
