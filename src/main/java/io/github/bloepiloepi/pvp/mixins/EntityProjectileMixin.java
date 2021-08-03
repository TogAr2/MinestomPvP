package io.github.bloepiloepi.pvp.mixins;

import net.minestom.server.entity.EntityProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EntityProjectile.class)
public class EntityProjectileMixin {
	
	@ModifyConstant(method = "isStuck", constant = @Constant(longValue = 3))
	private long minAliveTicks(long original) {
		return 4;
	}
}
