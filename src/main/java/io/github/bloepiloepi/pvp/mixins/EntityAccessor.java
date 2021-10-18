package io.github.bloepiloepi.pvp.mixins;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityAccessor {
	
	@Accessor(value = "velocity")
	void velocity(Vec velocity);
	
	@Accessor(value = "previousPosition")
	Pos previousPosition();
}
