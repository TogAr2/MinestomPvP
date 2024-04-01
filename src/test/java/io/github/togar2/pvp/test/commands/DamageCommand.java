package io.github.togar2.pvp.test.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;

public class DamageCommand extends Command {
	
	public DamageCommand() {
		super("damage");
		
		ArgumentLiteral nonGenArg = ArgumentType.Literal("nongen");
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
			
			((LivingEntity) entity).damage(DamageType.GENERIC, args.get(amountArg));
		}, entityArg, amountArg);
		
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
			
			((LivingEntity) entity).damage(new Damage(
					DamageType.PLAYER_ATTACK,
					((Player) sender), ((Player) sender),
					null, args.get(amountArg)
			));
		}, nonGenArg, entityArg, amountArg);
	}
}
