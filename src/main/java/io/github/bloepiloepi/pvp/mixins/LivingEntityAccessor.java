package io.github.bloepiloepi.pvp.mixins;

import net.minestom.server.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	
	@Accessor(value = "fireExtinguishTime")
	long fireExtinguishTime();
}
