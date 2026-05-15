package io.github.togar2.pvp.test.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;

public class HealCommand extends Command {

    public HealCommand() {
        super("heal");
        setCondition(Conditions::playerOnly);
        setDefaultExecutor((sender, context) -> {
            Player p = (Player) sender;
            p.setHealth((float) p.getAttributeValue(Attribute.MAX_HEALTH));
        });
    }

}
