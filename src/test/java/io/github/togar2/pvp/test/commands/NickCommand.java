package io.github.togar2.pvp.test.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;

public class NickCommand extends Command {
	
	public NickCommand() {
		super("nick");
		
		ArgumentWord name = ArgumentType.Word("username");
		
		addSyntax((sender, args) -> MinecraftServer.getSchedulerManager().buildTask(() -> {
			String username = args.get(name);
			Player player = sender.asPlayer();
			
			player.setUsernameField(username);
			//Send packets
			player.setSkin(player.getSkin());
		}).schedule(), name);
	}
}
