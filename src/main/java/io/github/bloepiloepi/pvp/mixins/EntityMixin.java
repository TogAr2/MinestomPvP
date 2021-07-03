package io.github.bloepiloepi.pvp.mixins;

import io.github.bloepiloepi.pvp.entities.Tracker;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {
	
	@Shadow public abstract @NotNull UUID getUuid();
	@Shadow @Final protected Position position;
	
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "teleport(Lnet/minestom/server/utils/Position;[JLjava/lang/Runnable;)V", at = @At(value = "HEAD"))
	private void onTeleport(@NotNull Position position, long[] chunks, @Nullable Runnable callback, CallbackInfo ci) {
		if ((Object) this instanceof Player) {
			if (Tracker.spectating.get(getUuid()) != (Object) this) {
				if (this.position.getDistance(position) < 16) return;
				
				((Player) (Object) this).stopSpectating();
			}
		}
	}
}
