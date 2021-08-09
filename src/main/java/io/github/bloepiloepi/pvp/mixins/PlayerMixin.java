package io.github.bloepiloepi.pvp.mixins;

import io.github.bloepiloepi.pvp.entities.Tracker;
import io.github.bloepiloepi.pvp.potion.PotionListener;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.*;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
	
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
	
	@Inject(method = "refreshOnGround", at = @At(value = "HEAD"))
	private void onRefreshOnGround(boolean onGround, CallbackInfo ci) {
		if (onGround) {
			Tracker.lastClimbedBlock.remove(getUuid());
		}
	}
	
	@Inject(method = "kill", at = @At(value = "HEAD"))
	private void onKill(CallbackInfo ci) {
		if (!isDead()) {
			Tracker.combatManager.get(getUuid()).recheckStatus();
		}
	}
	
	@Inject(method = "update", at = @At(value = "HEAD"))
	private void onUpdate(long time, CallbackInfo ci) {
		if (Tracker.lastDamagedBy.containsKey(getUuid())) {
			LivingEntity lastDamagedBy = Tracker.lastDamagedBy.get(getUuid());
			if (lastDamagedBy.isDead()) {
				Tracker.lastDamagedBy.remove(getUuid());
			} else if (System.currentTimeMillis() - Tracker.lastDamageTime.get(getUuid()) > 5000) {
				// After 5 seconds of no attack the last damaged by does not count anymore
				Tracker.lastDamagedBy.remove(getUuid());
			}
		}
		
		if (getAliveTicks() % 20 == 0) {
			Tracker.combatManager.get(getUuid()).recheckStatus();
		}
	}
}
