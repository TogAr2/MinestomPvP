package io.github.bloepiloepi.pvp.test.commands;

import io.github.bloepiloepi.pvp.damage.CustomDamageType;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;

public class DamageCommand extends Command {
	
	public DamageCommand() {
		super("damage");
		
		ArgumentEntity entityArg = ArgumentType.Entity("entity").singleEntity(true);
		ArgumentFloat amountArg = ArgumentType.Float("amount");
		
		addSyntax((sender, args) -> {
			Entity entity = args.get(entityArg).findFirstEntity(sender);
			if (entity == null) {
				sender.sendMessage("Could not find an entity");
				return;
			}
			if (!(entity instanceof LivingEntity)) {
				sender.sendMessage("Invalid entity");
				return;
			}
			
			((LivingEntity) entity).damage(CustomDamageType.GENERIC, args.get(amountArg));
		}, entityArg, amountArg);
	}
}
