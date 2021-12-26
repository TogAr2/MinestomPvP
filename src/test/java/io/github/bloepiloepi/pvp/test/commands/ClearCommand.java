package io.github.bloepiloepi.pvp.test.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class ClearCommand extends Command {
	
	public ClearCommand() {
		super("clear");
		
		setDefaultExecutor((sender, args) -> {
			if (sender instanceof Player player) {
				player.getInventory().clear();
			}
		});
	}
}
