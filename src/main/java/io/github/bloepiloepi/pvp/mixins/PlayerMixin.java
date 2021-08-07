package io.github.bloepiloepi.pvp.mixins;

import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends Entity {
	
	public PlayerMixin(@NotNull EntityType entityType) {
		super(entityType);
	}
	
	@Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minestom/server/item/Material;isFood()Z"))
	private boolean onIsFood(Material material) {
		return material.isFood() || material == Material.POTION || material == Material.MILK_BUCKET;
	}
	
	@Inject(method = "setGameMode", at = @At(value = "TAIL"))
	private void onSetGameMode(@NotNull GameMode gameMode, CallbackInfo ci) {
		PotionListener.updatePotionVisibility((Player) (Object) this);
	}
	
	@Inject(method = "spectate", at = @At(value = "TAIL"))
	private void onSpectate(@NotNull Entity entity, CallbackInfo ci) {
		Tracker.spectating.put(getUuid(), entity);
	}
}
