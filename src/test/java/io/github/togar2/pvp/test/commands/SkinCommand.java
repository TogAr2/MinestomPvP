package io.github.togar2.pvp.test.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.PlayerSkin;

public class SkinCommand extends Command {
	
	public SkinCommand() {
		super("skin");
		
		ArgumentWord name = ArgumentType.Word("username");
		
		addSyntax((sender, args) -> MinecraftServer.getSchedulerManager().buildTask(() -> {
			String username = args.get(name);
			sender.asPlayer().setSkin(PlayerSkin.fromUsername(username));
		}).schedule(), name);
	}
}
