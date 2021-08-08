package io.github.bloepiloepi.pvp.mixins;

import net.minestom.server.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public interface PlayerAccessor {
	
	@Accessor(value = "startEatingTime")
	long getStartEatingTime();
}
